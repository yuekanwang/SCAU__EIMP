package com.eimp.component;

import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
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

    public File getDirectory() {
        return directory;
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
        thumbnailPanels.clear();
        File[] files = this.directory.listFiles(FileUtil::isSupportImageFormat);
        if (files != null) {
            for (File file : files) {
                ImageUtil imageUtil = new ImageUtil(file);
                ThumbnailPanel thumbnailPanel = new ThumbnailPanel(imageUtil);
                this.thumbnailPanels.add(thumbnailPanel);
            }
        }
        getChildren().setAll(thumbnailPanels);
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
}
