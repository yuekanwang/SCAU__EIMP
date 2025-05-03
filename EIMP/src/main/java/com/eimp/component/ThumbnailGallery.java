package com.eimp.component;

import com.eimp.controller.WindowSlideController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 可滚动的缩略图栏组件
 */
public class ThumbnailGallery extends ScrollPane {
    /**
     * 线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    /**
     * 图片缓存
     */
    private final Map<File, Image> thumbnailCache = new ConcurrentHashMap<>();
    /**
     * 占位列表
     */
    private final List<StackPane> placeholderPanes = new ArrayList<>();
    /**
     * 初始索引
     */
    private int initIndex;
    private final HBox thumbnailsContainer;
    private ImageView selectedThumbnail;
    private int thumbnailSize = 100;
    private int containerSize = 110;
    /**
     * 矩形圆角
     */
    private static final double CORNER_RADIUS = 8;
    /**
     * 缩略图栏选中回调
     */
    private Consumer<Integer> selectionHandler;

    /**
     *
     * @param idx 初始索引
     */
    public ThumbnailGallery(int idx) {
        thumbnailsContainer = new HBox(10);
        thumbnailsContainer.setAlignment(Pos.CENTER);
        thumbnailsContainer.getStyleClass().add("thumbnails-container");
        initIndex = idx;
        this.setContent(thumbnailsContainer);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setFitToHeight(true);
        this.getStyleClass().add("thumbnail-gallery");

        // 添加鼠标滚轮控制水平滚动
        this.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY();
            this.setHvalue(this.getHvalue() - deltaY/this.getWidth());
            event.consume();
        });
    }

    /**
     * 线程池异步加载添加图片到缩略图栏
     *
     * @param imageFile 图片文件
     */
    public void addImage(File imageFile) {
        // 创建占位容器
        StackPane placeholder = createPlaceholder();
        int insertIndex = placeholderPanes.size();
        placeholderPanes.add(placeholder);

        Platform.runLater(() ->
                thumbnailsContainer.getChildren().add(placeholder)
        );

        executorService.execute(() -> {
            try {
                Image thumbnail = thumbnailCache.computeIfAbsent(imageFile, file -> {
                    try {
                        BufferedImage buffered = Thumbnails.of(file)
                                .outputQuality(0.5)
                                .size(thumbnailSize, thumbnailSize)
                                .asBufferedImage();
                        return SwingFXUtils.toFXImage(buffered, null);
                    } catch (IOException e) {
                        return null;
                    }
                });

                Platform.runLater(() -> {
                    replacePlaceholder(insertIndex, thumbnail);
                    if (insertIndex == initIndex) updateSelectedThumbnail(initIndex);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * 替换占位图,
     * @param index 占位索引
     * @param image 加载好的图片
     */
    private void replacePlaceholder(int index, Image image) {
        if (index >= placeholderPanes.size()) return;

        StackPane thumbnailPane = createThumbnailView(image);
        placeholderPanes.set(index, thumbnailPane);
        thumbnailsContainer.getChildren().set(index, thumbnailPane);
    }

    /**
     * 创建占位图
     * @return 占位图
     */
    private StackPane createPlaceholder() {
        Rectangle rect = new Rectangle(thumbnailSize, thumbnailSize);
        rect.setArcWidth(CORNER_RADIUS);
        rect.setArcHeight(CORNER_RADIUS);
        rect.setFill(Color.TRANSPARENT);
        Label label = new Label("加载中");
        label.setStyle("-fx-text-fill: white;");
        StackPane placeholder = new StackPane(rect,label);
        placeholder.getStyleClass().add("thumbnail-placeholder");
        return placeholder;
    }

    /**
     * 创建缩略图视图
     */
    private StackPane createThumbnailView(Image image) {
        // 创建圆角容器
        Rectangle container = new Rectangle(containerSize, containerSize);
        container.setArcWidth(CORNER_RADIUS);
        container.setArcHeight(CORNER_RADIUS);
        container.setFill(Color.TRANSPARENT);
        container.getStyleClass().add("thumbnail-container");
        ImageView thumbnailView = new ImageView(image);
        thumbnailView.setFitWidth(thumbnailSize);
        thumbnailView.setFitHeight(thumbnailSize);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.getStyleClass().add("thumbnail");
        // 使用StackPane确保缩略图居中
        StackPane containerPane = new StackPane();
        containerPane.getChildren().addAll(container, thumbnailView);
        containerPane.getStyleClass().add("thumbnail-pane");
        // 点击事件
        containerPane.setOnMouseClicked(event -> selectThumbnail(containerPane,true));
        return containerPane;
    }

    /**
     * 设置选中事件处理回调函数
     * @param selectionHandler 回调函数
     */
    public void setSelectionHandler(Consumer<Integer> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    /**
     * 选中缩略图
     */
    private void selectThumbnail(StackPane containerPane,boolean fireEvent) {
        // 重置所有容器的选中状态
        thumbnailsContainer.getChildren().forEach(node -> {
            if (node instanceof StackPane) {
                node.getStyleClass().remove("thumbnail-selected");
            }
        });

        // 设置当前选中状态（仅边框颜色）
        containerPane.getStyleClass().add("thumbnail-selected");
        int idx = thumbnailsContainer.getChildren().indexOf(containerPane);
        // 自动跟随
        double nodeMinX = containerPane.getBoundsInParent().getMinX();
        double nodeMaxX = containerPane.getBoundsInParent().getMaxX();
        double X = (nodeMinX+nodeMaxX)/2;
        if(X < this.getWidth()/2) X = 0;
        else if(X > thumbnailsContainer.getWidth()-this.getWidth()/2) X = thumbnailsContainer.getWidth();
        this.setHvalue(X/thumbnailsContainer.getWidth());

        if(fireEvent && selectionHandler != null) {
            selectionHandler.accept(idx);
        }
    }

    /**
     * 更新缩略图栏选中元素
     * @param idx 索引
     */
    public void updateSelectedThumbnail(int idx) {
        if(thumbnailsContainer.getChildren().size()>0&&idx<thumbnailsContainer.getChildren().size()){
            selectThumbnail((StackPane) thumbnailsContainer.getChildren().get(idx),false);
        }
    }

    /**
     * 设置缩略图大小
     */
    public void setThumbnailSize(int size) {
        this.thumbnailSize = size;
        this.containerSize = size +10;
        thumbnailsContainer.getChildren().forEach(node -> {
            if (node instanceof ImageView) {
                ImageView iv = (ImageView) node;
                iv.setFitWidth(size);
                iv.setFitHeight(size);
            }
        });
    }

    /**
     * 清除所有缩略图
     */
    public void clear() {
        thumbnailsContainer.getChildren().clear();
        placeholderPanes.clear();
        selectedThumbnail = null;
    }
}
