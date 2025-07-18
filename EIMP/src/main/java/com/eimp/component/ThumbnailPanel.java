package com.eimp.component;

import com.eimp.SlideWindow;
import com.eimp.controller.ControllerMap;
import com.eimp.controller.WindowMainController;
import com.eimp.util.ImageUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

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
    private TextField imageName;
    // 是否被选中
    private boolean isSelected;
    // 是否正在重命名
    private boolean isRename = false;

    public boolean isRename() {
        return isRename;
    }

    private static final WindowMainController MAIN_WINDOWS_CONTROLLER = (WindowMainController) ControllerMap.getController(WindowMainController.class);

    // 主界面搜索框
    private static TextField searchKey;

    static {
        searchKey = MAIN_WINDOWS_CONTROLLER.getSearch_Path();
    }

    public ThumbnailPanel(ImageUtil imageUtil) {
        // 增加缩略图尺寸
        this.setMaxSize(150, 190);
        this.setMinSize(150, 190);
        setCache(true);//这里丢进缓存里好一些
        this.imageUtil = imageUtil;
        this.isSelected = false;

        // 优化图片加载和显示
        Image image = new Image(
            imageUtil.getURL(),
            140, 140,  // 目标尺寸
            true,      // 保持宽高比
            true,      // 平滑缩放
            true       // 后台加载
        );
        
        // 设置图片加载完成后的处理
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                // 图片加载完成后设置到ImageView
                this.imageView = new ImageView(image);
                this.imageView.setPreserveRatio(true);
                this.imageView.setSmooth(true);
                this.imageView.setCache(true);
                // 设置图片质量
                this.imageView.setFitWidth(140);
                this.imageView.setFitHeight(140);
                setCenter(imageView);
            }
        });

        // 设置图片加载失败的处理
        image.errorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // 加载失败时显示占位图（这里其实啥图片都没有）
                this.imageView = new ImageView(new Image("/images/error.png", 180, 180, true, true, true));
                setCenter(imageView);
            }
        });

        // 裁剪文本
        cutting();
        // 设置文本默认样式
        if (!MAIN_WINDOWS_CONTROLLER.getF()) {
            initBlackText();
        } else {
            initText();
        }
        // 设置不可编辑
        imageName.setEditable(false);
        imageName.setManaged(true);

        imageName.setOnAction(this::confirmRename);
        imageName.setOnContextMenuRequested(Event::consume);
        imageName.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEscape);
        // 双击事件：进入编辑模式
        imageName.setOnMouseClicked(event  -> {
            PreviewFlowPane parent = (PreviewFlowPane) this.getParent();
            if (event.getButton() == MouseButton.SECONDARY) {
                WindowMainController.Menu menu = MAIN_WINDOWS_CONTROLLER.menu;
                MAIN_WINDOWS_CONTROLLER.setFlagMenu(false);
                menu.close();
                menu.show(MAIN_WINDOWS_CONTROLLER.imagePreviewPane, event.getScreenX(), event.getScreenY());
                if (!this.isSelected) {
                    parent.clearSelected();
                    parent.addSelected(this);
                }
            }
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                parent.clearSelected();
                parent.addSelectedtoList(this);
                parent.setIsShift(true);
                parent.setFrom(parent.getThumbnailPanels().indexOf(this));
                MAIN_WINDOWS_CONTROLLER.renameImage();
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 按住ctrl多选时
                if (event.isControlDown() && !isRename) {
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
                else if (event.isShiftDown() && !isRename) {
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
                    } else if (parent.newSelectedSize() == 1 && isSelected && !isRename) {
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
            MAIN_WINDOWS_CONTROLLER.updateTipsLabelText();
        });

        // 失去焦点事件
        imageName.focusedProperty().addListener((observable,  oldValue, newValue) -> {
            if (!newValue && isRename) {
                // 失去焦点时触发
                String newName = imageName.getText().trim();
                // 检测非法字符
                String regex = "[\\\\/:*?\"<>|]";
                if (Pattern.compile(regex).matcher(newName).find())  {
                    resetName();
                    return;
                }
                if (!newName.isEmpty())  {
                    renameFile(newName);
                }
                resetName();
            }
        });

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
            MAIN_WINDOWS_CONTROLLER.updateTipsLabelText();
        });
    }

    // 裁剪文字
    private void cutting() {
        int length = imageUtil.getFileName().length();
        //名字长度大于限定就剪切
        if (length > MAX_NAME) {
            this.imageName = new TextField(imageUtil.getFileName().substring(0, MAX_NAME) + "...") {
            };
        } else {
            this.imageName = new TextField(imageUtil.getFileName());
        }
    }

    // 检查是否在图片及文字范围内
    public boolean isContains(double x, double y) {
        return this.getBoundsInParent().contains(x, y) || imageName.getBoundsInParent().contains(x, y);
    }

    /**
     * 图片选中背景设置
     */
    public void selected() {
        if (MAIN_WINDOWS_CONTROLLER.getF()) {
            this.setStyle("-fx-background-color: rgba(196, 193, 227, 0.8)");
        } else {
            this.setStyle("-fx-background-color: rgba(130, 130, 130, 0.8)");
        }
        this.isSelected = true;
    }

    public void removeSelected() {
        this.setStyle("-fx-background-color: transparent");
        this.isSelected = false;
    }

    public ImageUtil getImageUtil() {
        return imageUtil;
    }

    public void setImageUtil(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    public TextField getImageName() {
        return imageName;
    }

    public boolean getSelected() {
        return isSelected;
    }

    // 日间字体设置
    public void initText() {
        imageName.setStyle(
                "-fx-background-color: transparent; " + // 透明背景
                        "-fx-border-color: transparent; " + // 透明边框
                        "-fx-text-fill: black; " + // 黑色文本
                        "-fx-highlight-fill: transparent; " + // 选中文本的背景颜色为透明
                        "-fx-highlight-text-fill: black; " + // 选中文本的颜色
                        "-fx-cursor: default; " + // 鼠标光标为默认样式
                        "-fx-alignment: CENTER;" // 设置居中;
        );
    }

    public void setText() {
        imageName.setStyle(
                "-fx-background-color: #D7D5E9; " + // 淡蓝紫背景
                        "-fx-border-color: black; " + // 黑色边框
                        "-fx-text-fill: black; " + // 白色文本
                        "-fx-highlight-fill: #6B3DF7; " + // 选中文本的背景颜色为蓝紫色
                        "-fx-highlight-text-fill: white; " + // 选中文本的颜色
                        "-fx-alignment: CENTER;" // 设置居中;
        );
    }

    // 夜间字体设置
    public void initBlackText() {
        imageName.setStyle(
                "-fx-background-color: transparent; " + // 透明背景
                        "-fx-border-color: transparent; " + // 透明边框
                        "-fx-text-fill: white; " + // 白色文本
                        "-fx-highlight-fill: transparent; " + // 选中文本的背景颜色为透明
                        "-fx-highlight-text-fill: white; " + // 选中文本的颜色
                        "-fx-cursor: default; " + // 鼠标光标为默认样式
                        "-fx-alignment: CENTER;" // 设置居中;
        );
    }

    public void setBlackText() {
        imageName.setStyle(
                "-fx-background-color: #242424; " + // 黑色背景
                        "-fx-border-color: white; " + // 白色边框
                        "-fx-text-fill: white; " + // 白色文本
                        "-fx-highlight-fill: #6B3DF7; " + // 选中文本的背景颜色为蓝紫色
                        "-fx-highlight-text-fill: white; " + // 选中文本的颜色
                        "-fx-alignment: CENTER;" // 设置居中;
        );
    }

    public void startReName() {
        MAIN_WINDOWS_CONTROLLER.imagePreviewPane.setFocusTraversable(false);
        imageName.requestFocus();
        isRename = true;
        // 设置图片全称
        imageName.setText(imageUtil.getFileName());
        // 设置可编辑
        imageName.setEditable(true);
        // 设置文本修改样式
        if (!MAIN_WINDOWS_CONTROLLER.getF()) {
            setBlackText();
        } else {
            setText();
        }
        // 获取焦点
        imageName.requestFocus();
        // 选择全部文字
        imageName.selectAll();
    }

    // TextFiled输出
    private void confirmRename(ActionEvent event) {
        String newName = imageName.getText().trim();
        // 检测非法字符
        String regex = "[\\\\/:*?\"<>|]";
        if (Pattern.compile(regex).matcher(newName).find())  {
            showError();
            resetName();
            return;
        }
        if (!newName.isEmpty())  {
            renameFile(newName);
        }
        resetName();
    }

    // 重命名操作
    private void renameFile(String newName) {
        // 获取图片地址
        Path oldPath = Paths.get(imageUtil.getAbsolutePath());
        Path newPath = oldPath.resolveSibling(newName);
        try {
            // 替换文件
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {

        }
    }

    // 设置Esc按键事件
    private void handleEscape(KeyEvent event) {
        if (event.getCode()  == KeyCode.ESCAPE) {
            resetName();
        }
    }

    // 错误显示
    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("重命名失败: 存在非法字符");
        alert.show();
    }

    // 重新设置
    private void resetName() {
        if (!MAIN_WINDOWS_CONTROLLER.getF()) {
            initBlackText();
        } else {
            initText();
        }
        imageName.setEditable(false);
        isRename = false;
        PreviewFlowPane parent = (PreviewFlowPane) this.getParent();
        parent.update();
        MAIN_WINDOWS_CONTROLLER.imagePreviewPane.setFocusTraversable(true);
    }
}