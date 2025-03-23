package com.eimp.component;

import com.eimp.util.FileUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

/**
 * TreeItem文件夹版
 *
 * @author Cyberangel2023
 */
public class FileTreeItem extends TreeItem<String> {
    // 该节点Item是否初始化
    private boolean isInit = false;

    // 该节点文件夹
    private final File directory;

    // 异步加载文件夹
    private CompletableFuture<?> completableFuture;

    // 回调
    private final Callable<List<? extends TreeItem<String>>> callable;

    // 线程池，用于异步任务
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FileTreeItem(File directory,Callable<List<? extends TreeItem<String>>> callable) {
        super(FileUtil.getFileName(directory), FileUtil.getFileIcon(directory));
        this.directory = directory;
        this.callable = callable;
        super.getChildren().add(new TreeItem<>());
        addExpandedListener();
    }

    /**
     * 展开监听器
     */
    private void addExpandedListener() {
        expandedProperty().addListener((obserVable, oldValue, newValue) -> {
            // 如果该节点被折叠
            if (!newValue) {
                isInit = false;
                if (completableFuture != null) {
                    completableFuture.cancel(true);
                }
                // 清空子节点
                super.getChildren().setAll(new TreeItem<>());
            }
        });
    }

    /**
     * 重写叶子方法
     */
    @Override
    public boolean isLeaf() {
        return !directory.isDirectory();
    }

    /**
     * 展开时加载子文件夹
     */
    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (!isInit) {
            isInit = true;
            // 异步加载
            completableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, executor).whenCompleteAsync(this::handleAsyncLoadComplete, Platform::runLater);
        }
        return super.getChildren();
    }

    /**
     * 异步加载完成的处理
     * @param result 储存加载完成后的item
     * @param th 异常
     */
    private void handleAsyncLoadComplete(List<? extends TreeItem<String>> result, Throwable th) {
        if (th != null) {
            Thread.currentThread().getUncaughtExceptionHandler()
                    .uncaughtException(Thread.currentThread(), th);
        } else {
            super.getChildren().setAll(result);
        }
        completableFuture = null;
    }

    public File getDirectory() {
        return directory;
    }
}
