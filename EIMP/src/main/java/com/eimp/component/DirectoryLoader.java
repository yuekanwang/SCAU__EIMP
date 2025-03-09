package com.eimp.component;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Cyberangel2023
 */
public class DirectoryLoader implements Callable<List<? extends TreeItem<String>>> {
    private final File directory;

    public DirectoryLoader(File directory) {
        this.directory = directory;
    }

    /**
     * 转换为FileTreeItem
     * @param file 待转换的文件
     */
    private TreeItem<String> toFileTreeItem(File file) {
        return file.isDirectory() ? new FileTreeItem(file, new DirectoryLoader(file)) : null;
    }

    @Override
    public List<? extends TreeItem<String>> call() {
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                // 过滤文件
                .filter(File::isDirectory)
                // 转换为FileTreeItem
                .map(this::toFileTreeItem)
                .collect(Collectors.toList());
    }
}
