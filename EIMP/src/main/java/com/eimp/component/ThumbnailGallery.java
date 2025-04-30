package com.eimp.component;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 可滚动的缩略图栏组件
 */
public class ThumbnailGallery extends ScrollPane {
    private final HBox thumbnailsContainer;
    private ImageView selectedThumbnail;
    private int thumbnailSize = 100;
    private int containerSize = 110;
    private static final double CORNER_RADIUS = 8;
    private Consumer<Integer> selectionHandler;
    public ThumbnailGallery() {
        thumbnailsContainer = new HBox(10);
        thumbnailsContainer.setAlignment(Pos.CENTER);
        thumbnailsContainer.getStyleClass().add("thumbnails-container");

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
     * 添加图片到缩略图栏
     *
     * @param imageFile 图片文件
     */
    public void addImage(File imageFile) {
        try {
            // 使用Thumbnailator生成缩略图
            BufferedImage thumbnail = Thumbnails.of(imageFile)
                    .size(thumbnailSize, thumbnailSize)
                    .asBufferedImage();

            Image fxImage = SwingFXUtils.toFXImage(thumbnail, null);
            StackPane thumbnailContainer =  createThumbnailView(fxImage);

            thumbnailsContainer.getChildren().add(thumbnailContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if(fireEvent&&selectionHandler != null) {
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
        selectedThumbnail = null;
    }
}
