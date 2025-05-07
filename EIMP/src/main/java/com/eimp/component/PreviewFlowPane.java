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
import java.util.function.UnaryOperator;

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
    // 当前搜索框内容
    private String search_Text;
    // 被选中图片数组
    private final List<ThumbnailPanel> newSelected = new ArrayList<>();
    // 提供回撤
    private final List<ThumbnailPanel> oldSelected = new ArrayList<>();

    private WindowMainController windowMainController;

    private SortOrder sortOrder;

    public boolean isRename() {
        for (ThumbnailPanel pane : thumbnailPanels) {
            if (pane.isRename()) {
                return true;
            }
        }
        return false;
    }

    public File getDirectory() {
        return directory;
    }

    public void setSearch_Text(String search_Text) {
        this.search_Text = search_Text;
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

    public void updateOfSearch(String newValue) {
        if (windowMainController == null) {
            windowMainController = (WindowMainController) ControllerMap.getController(WindowMainController.class);
        }
        update(this.directory);
        List<ThumbnailPanel> list = new ArrayList<>(thumbnailPanels);
        thumbnailPanels.clear();
        // 过滤匹配
        for (ThumbnailPanel pane : list) {
            String fileName = pane.getImageUtil().getFileName();
            if (fileName.contains(newValue))  {
                thumbnailPanels.add(pane);
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
            clearSelected();
            update(directory);
            if (windowMainController == null) {
                windowMainController = (WindowMainController) ControllerMap.getController(WindowMainController.class);
            }
            windowMainController.updateTipsLabelText();
        }
    }

    public void updateOfSearch() {
        if (search_Text != null) {
            clearSelected();
            updateOfSearch(search_Text);
            if (windowMainController == null) {
                windowMainController = (WindowMainController) ControllerMap.getController(WindowMainController.class);
            }
            windowMainController.updateTipsLabelText();
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

    /**
     * @return 当前目录的图片总数
     */
    public int getTotalCount() {
        return thumbnailPanels.size();
    }

    /**
     * @return 所有图片的总大小 单位：B
     */
    public String getTotalSize() {
        long totalSize = 0;
        for (ThumbnailPanel thumbnailPanel : thumbnailPanels) {
            totalSize += thumbnailPanel.getImageUtil().getSizeOfBytes();
        }
        return FileUtil.fileSizeByString(totalSize);
    }

    /**
     * @return 被选中的图片的大小
     */
    public String getSelectedSize() {
        long size = 0;
        for (ThumbnailPanel thumbnailPanel : newSelected) {
            size += thumbnailPanel.getImageUtil().getSizeOfBytes();
        }
        return FileUtil.fileSizeByString(size);
    }
}
