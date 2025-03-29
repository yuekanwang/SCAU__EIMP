package com.eimp.component;

import com.eimp.SlideWindow;
import com.eimp.controller.ControllerMap;
import com.eimp.controller.WindowMainController;
import com.eimp.util.ImageUtil;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.awt.*;

/**
 * 初始化目录树
 *
 * @author Cyberangel2023
 * @author shanmu
 */

public class ThumbnailPanel extends BorderPane {
    // 图片能展示的最长名字
    private static final int MAX_NAME = 15;
    // 图片
    private ImageView imageView;
    // 图片工具
    private ImageUtil imageUtil;
    // 图片名称
    private final TextField imageName;
    // 是否被选中
    private boolean isSelected;

    private static final WindowMainController MAIN_WINDOWS_CONTROLLER = (WindowMainController) ControllerMap.getController(WindowMainController.class);

    // 主界面搜索框
    private static TextField searchKey;

    public ThumbnailPanel(ImageUtil imageUtil) {
        this.setMaxSize(150, 190);
        this.setMinSize(150, 190);
        setCache(false);
        this.imageUtil = imageUtil;
        this.isSelected = false;
        // 保持图片大小比例
        this.imageView = new ImageView(new Image(imageUtil.getURL(), 90, 90, true, true, true));
        this.imageView.setFitWidth(140);
        this.imageView.setFitHeight(140);
        this.imageView.setPreserveRatio(true);
        int length = imageUtil.getFileName().length();
        //名字长度大于限定就剪切
        if (length > MAX_NAME) {
            this.imageName = new TextField(imageUtil.getFileName().substring(0, MAX_NAME) + "...") {
            };
        } else {
            this.imageName = new TextField(imageUtil.getFileName());
        }
        imageName.setEditable(false);
        imageName.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-text-fill: black; -fx-alignment: CENTER;");

        //searchKey = MAIN_WINDOWS_CONTROLLER.getSearchPath();
        // 绑定主界面搜索框
        //imageName.textProperty().bindBidirectional(searchKey.textProperty());

        setCenter(imageView);
        setBottom(imageName);

        // 设置鼠标点击事件
        this.setOnMouseClicked(event -> {
            // 双击进入图片展示界面
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Platform.runLater(() -> {
                    SlideWindow.main(this.imageUtil,SlideWindow.LaunchMethodEnum.CLICK);
                });
            }
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                PreviewFlowPane parent = (PreviewFlowPane) this.getParent();
                // 按住ctrl多选时
                if (event.isControlDown()) {
                    // 图片已被选中
                    if (getSelected()) {
                        parent.deleteSelectedtoList(this);
                        parent.setIsShift(true);
                        parent.setFrom(parent.getThumbnailPanels().indexOf(this));
                    }
                    // 图片未被选中
                    else {
                        parent.setIsShift(true);
                        parent.setFrom(parent.getThumbnailPanels().indexOf(this));
                        parent.addSelectedtoList(this);
                    }
                }
                // 按住shift多选时
                else if (event.isShiftDown()) {
                    if (!parent.isShift()) {
                        parent.setIsShift(true);
                        parent.setFrom(parent.getThumbnailPanels().indexOf(this));
                    }
                    parent.setTo(parent.getThumbnailPanels().indexOf(this));
                    parent.shiftSelected();
                }
                // 单击选中图片
                else {
                    if (parent.newSelectedSize() > 1) {
                        parent.clearSelected();
                        parent.addSelectedtoList(this);
                        parent.setIsShift(true);
                        parent.setFrom(parent.getThumbnailPanels().indexOf(this));
                    } else if (parent.newSelectedSize() == 1 && isSelected) {
                        parent.clearSelected();
                        parent.setIsShift(false);
                    } else {
                        parent.clearSelected();
                        parent.addSelectedtoList(this);
                        parent.setIsShift(true);
                        parent.setFrom(parent.getThumbnailPanels().indexOf(this));
                    }
                }
            }
        });
    }

    /**
     * 图片选中背景设置
     */
    public void selected() {
        this.setStyle("-fx-background-color: #cce8ff");
        this.isSelected = true;
    }

    public void removeSelected() {
        this.setStyle("-fx-background-color: transparent");
        this.isSelected = false;
    }

    public ImageUtil getImageUtil() {
        return imageUtil;
    }

    public boolean getSelected() {
        return isSelected;
    }
}
