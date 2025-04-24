package com.eimp.component;

import com.eimp.controller.ControllerMap;
import com.eimp.controller.WindowMainController;
import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
import com.eimp.util.SortOrder;
import com.eimp.util.SortUtil;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片预览面板
 *
 * @author Cyberangel2023
 */
public class PreviewFlowPane extends FlowPane {
    // 图片列表
    private final List<ThumbnailPanel> thumbnailPanels = new ArrayList<>();
    // 当前展示的文件夹
    private File directory;
    // 被选中图片数组
    private final List<ThumbnailPanel> newSelected = new ArrayList<>();
    // 提供回撤
    private final List<ThumbnailPanel> oldSelected = new ArrayList<>();

    private WindowMainController windowMainController;

    private SortOrder sortOrder;

    public File getDirectory() {
        return directory;
    }

    public List<ThumbnailPanel> getNewSelected() {
        return newSelected;
    }

    public List<ThumbnailPanel> getOldSelected() {
        return oldSelected;
    }

    public boolean newSelectedIsEmpty() {
        return newSelected.isEmpty();
    }

    public int newSelectedSize() {
        return newSelected.size();
    }

    public boolean oldSelectedIsEmpty() {
        return oldSelected.isEmpty();
    }

    public int oldSelectedSize() {
        return oldSelected.size();
    }

    public PreviewFlowPane() {
        setCache(true);
        // 设置间隔
        setVgap(5);
        setHgap(5);
    }

    public List<ThumbnailPanel> getThumbnailPanels() {
        return thumbnailPanels;
    }

    public void update(File directory) {
        this.directory = directory;
        if (windowMainController == null) {
            windowMainController = (WindowMainController) ControllerMap.getController(WindowMainController.class);
        }
        // 清空图片数组
        thumbnailPanels.clear();
        // 过滤文件，获取图片格式的文件
        File[] files = this.directory.listFiles(FileUtil::isSupportImageFormat);
        // 将图片加载进数组
        if (files != null) {
            for (File file : files) {
                ImageUtil imageUtil = new ImageUtil(file);
                ThumbnailPanel thumbnailPanel = new ThumbnailPanel(imageUtil);
                this.thumbnailPanels.add(thumbnailPanel);
            }
        }
        sortOrder = windowMainController.getSortOrder();
        SortUtil.sortThumbnailPanel(this.thumbnailPanels, sortOrder.getNowString());
        getChildren().setAll(thumbnailPanels);
    }

    /**
     * 当改变排序方式时刷新缩略图面板
     */
    public void update() {
        if (directory != null) {
            newSelected.clear();
            oldSelected.clear();
            update(directory);
        }
    }

    /**
     * 添加选中
     */
    public void addSelectedtoList(Object obj) {
        ThumbnailPanel img = (ThumbnailPanel) obj;
        oldSelected.clear();
        oldSelected.addAll(newSelected);
        newSelected.add(img);
        img.selected();
    }

    public void addSelected(ThumbnailPanel pane) {
        newSelected.add(pane);
        pane.selected();
    }

    /**
     * 取消选中
     */
    public void deleteSelectedtoList(Object obj) {
        ThumbnailPanel img = (ThumbnailPanel) obj;
        oldSelected.clear();
        oldSelected.addAll(newSelected);
        img.removeSelected();
        newSelected.remove(img);
    }

    /**
     * 清空选中
     */
    public void clearSelected() {
        oldSelected.clear();
        oldSelected.addAll(newSelected);
        for (ThumbnailPanel pane : newSelected) {
            pane.removeSelected();
        }
        newSelected.clear();
    }

    /**
     * shift多选设置
     */
    // shift选中图片的起始和末尾
    private int from;
    private int to;
    // 允许使用shift多选
    private boolean isShift = false;

    public void setFrom(int from) {
        this.from = from;
    }

    public int getFrom() {
        return from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getTo() {
        return to;
    }

    public void setIsShift(boolean isShift) {
        this.isShift = isShift;
    }

    public boolean isShift() {
        return isShift;
    }

    // shift根据起始末尾选中图片
    public void shiftSelected() {
        clearSelected();
        if (from > to) {
            newSelected.addAll(thumbnailPanels.subList(to, from + 1));
        } else {
            newSelected.addAll(thumbnailPanels.subList(from, to + 1));
        }
        for (ThumbnailPanel pane : newSelected) {
            pane.selected();
        }
    }
}
