package com.eimp.component;

import com.eimp.Util.FileUtil;
import com.eimp.Util.ImageUtil;
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
    /**
     * 图片列表
     */
    private final List<ThumbnailPanel> thumbnailPanels = new ArrayList<>();

    /**
     * 当前展示的文件夹
     */
    private File directory;

    public File getDirectory() {
        return directory;
    }

    public PreviewFlowPane() {
        setCache(true);
        // 设置间隔
        setVgap(5);
        setHgap(5);
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
}
