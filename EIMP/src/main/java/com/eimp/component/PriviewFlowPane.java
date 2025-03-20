package com.eimp.component;

import javafx.scene.layout.FlowPane;

import java.io.File;

/**
 * 图片预览面板
 *
 * @author Cyberangel2023
 */
public class PriviewFlowPane extends FlowPane {
    /**
     * 当前展示的文件夹
     */
    private File directory;

    public File getDirectory() {
        return directory;
    }

    public PriviewFlowPane() {
        setCache(true);
        setVgap(5);
        setHgap(5);
    }

    public void update(File directory) {
        this.directory = directory;

    }
}
