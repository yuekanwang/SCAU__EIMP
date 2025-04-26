package com.eimp.util;


import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;

import java.awt.*;

/**
 * 图片放缩功能工具包
 */
public class ZoomUtil {
    /**
     * 所属窗口stage
     */
    private Stage stage;
    /**
     * 主照片视图
     */
    private ImageView mainImageView;
    /**
     * 图片面板
     */
    private StackPane imagePane;
    /**
     * 图片放大按钮
     */
    private Button zoomIn;
    /**
     * 图片缩小按钮
     */
    private Button zoomOut;
    /**
     * 图片放缩比例
     */
    private Label zoomScale;
    /**
     * 用于控制持续触发事件的时间线
     */
    private Timeline timeline;
    /**
     * 图片放缩比例
     */
    private IntegerProperty scaleInteger = new SimpleIntegerProperty(-1);
    /**
     * 最大比例
     */
    private final int MAX_SCALE = 5000;
    /**
     * 最小比例
     */
    private int MIN_SCALE;
    /**
     * 遮罩,挡住溢出图片面板的部分
     */
    private Rectangle clip;

    /**
     * 绑定图片放缩的相关构件
     * @param stage 所属舞台
     * @param mainImageView 图片的imageview
     * @param imagePane 图片的底层stackpane
     * @param zoomOut 图片缩小按钮
     * @param zoomIn 图片放大按钮
     * @param zoomScale 图片放缩比例标签
     * @param timeline 图片放缩功能的时间线
     */
    public ZoomUtil(Stage stage, ImageView mainImageView, StackPane imagePane, Button zoomOut, Button zoomIn, Label zoomScale, Timeline timeline) {
        this.stage = stage;
        this.mainImageView = mainImageView;
        this.imagePane = imagePane;
        this.zoomOut = zoomOut;
        this.zoomIn = zoomIn;
        this.zoomScale = zoomScale;
        this.timeline = timeline;
        this.clip = new Rectangle();

        this.setUpZoomScaleListener();
        this.setUpMovementConstraints();
        this.addZoomEventHandler();

        this.clip.widthProperty().bind(imagePane.widthProperty());
        this.clip.heightProperty().bind(imagePane.heightProperty());
        imagePane.setClip(clip);
    }

    /**
     * 更新图片放缩相关参数
     * @param scaleInteger 放缩比例变量
     * @param MIN_SCALE 最小放缩比例
     */
    public void updateZoomUtil(IntegerProperty scaleInteger,int MIN_SCALE){
        this.MIN_SCALE = MIN_SCALE;
        this.scaleInteger.set(scaleInteger.get());
    }

    /**
     * 加放缩事件处理
     */
    private void addZoomEventHandler(){
        this.zoomIn.setOnAction(e -> {
            this.zoomIn();
        });
        this.zoomOut.setOnAction(e -> {
            this.zoomOut();
        });
        this.imagePane.setOnScroll(e->{
            this.zoomByScroll(e);
        });
    }

    /**
     * 初始化并监听放缩比例及功能禁用状态
     */
    private void setUpZoomScaleListener(){
        this.scaleInteger.addListener((obs,oldvalue,newvalue)->{
            if(newvalue.intValue()<=MIN_SCALE){
                this.zoomOut.setDisable(true);
//                if(this.MIN_SCALE<100){
//                    this.displayMode = WindowSlideController.DisplayMode.FIT;
//                    this.updateOriginalSacleStyle();
//                }else{
//                    this.displayMode = WindowSlideController.DisplayMode.ORIGINAL;
//                    this.updateOriginalSacleStyle();
//                }
            } else if (newvalue.intValue()>=MAX_SCALE) {
                this.zoomIn.setDisable(true);
            } else {
                this.zoomIn.setDisable(false);
                this.zoomOut.setDisable(false);
            }
            this.zoomScale.setText(newvalue.toString()+"%");
        });
    }

    private double lastX, lastY;    // 拖拽前鼠标位置
    private double translateX = 0 , translateY = 0;  // 当前偏移量

    /**
     * 图片恢复偏置
     * @param ms 动画毫秒数
     */
    private void recoverTranslattion(double ms){
        TranslateTransition transition = new TranslateTransition(Duration.millis(ms), mainImageView);
        transition.setToX(0);
        transition.setToY(0);
        transition.play();
        this.translateX =0;
        this.translateY =0;
    }

    /**
     * 设置鼠标拖拽移动事件,限制移动范围
     */
    private void setUpMovementConstraints() {
        imagePane.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY&&this.isMoveable()) {
                lastX = e.getScreenX();
                lastY = e.getScreenY();
                this.imagePane.setCursor(Cursor.CLOSED_HAND);
            }
        });

        imagePane.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY&&this.isMoveable()) {
                this.imagePane.setCursor(Cursor.CLOSED_HAND);
                Bounds currentBounds = this.mainImageView.getBoundsInParent();
                Bounds stackVisibleBounds = imagePane.getLayoutBounds();
                double imgPaneWidth = stackVisibleBounds.getWidth();
                double imgPaneHeight = stackVisibleBounds.getHeight();
                // 计算允许的移动方向
                boolean widthFilled = currentBounds.getWidth() > imgPaneWidth;
                boolean heightFilled = currentBounds.getHeight() > imgPaneHeight;

                if(widthFilled){
                    double deltaX = e.getScreenX() - lastX;
                    // 应用限制后的偏移量
                    translateX += widthFilled ? deltaX : 0;
                    // 限制移动范围
                    translateX = getFixedOffset(translateX,imgPaneWidth /2 - currentBounds.getWidth()/2, currentBounds.getWidth()/2 - imgPaneWidth/2);
                    mainImageView.setTranslateX(translateX);
                    lastX = e.getScreenX();
                }
                if(heightFilled){
                    double deltaY = e.getScreenY() - lastY;
                    translateY += heightFilled ? deltaY : 0;
                    translateY = getFixedOffset(translateY, imgPaneHeight/2 - currentBounds.getHeight()/2, currentBounds.getHeight()/2 - imgPaneHeight/2);
                    mainImageView.setTranslateY(translateY);
                    lastY = e.getScreenY();
                }
            }
        });

        imagePane.setOnMouseReleased(e -> {
            this.updateCursor();
        });
    }


    /**
     * 获得移动限制范围内的移动偏移量
     * @param value 原始偏移值
     * @param min 最小负偏移值
     * @param max 最大正偏移值
     * @return 修正偏移值
     */
    private double getFixedOffset(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 判断图片是否可移动视图
     * @return 可移动值
     */
    private boolean isMoveable(){
        Bounds currentBounds = this.mainImageView.getBoundsInParent();
        Bounds stackVisibleBounds = imagePane.getLayoutBounds();
        int imgPaneWidth = (int)stackVisibleBounds.getWidth();
        int imgPaneHeight = (int)stackVisibleBounds.getHeight();
        return (int)currentBounds.getWidth() > imgPaneWidth || (int)currentBounds.getHeight() > imgPaneHeight;
    }
    /**
     * 判断图片是否已经填满场景
     * @return 填满场景值
     */
    private boolean isOverflow(){
        Bounds imageBounds = this.mainImageView.getBoundsInParent();
        Bounds stackVisibleBounds = imagePane.getLayoutBounds();
        return  (int)imageBounds.getWidth() >= (int)stackVisibleBounds.getWidth() && (int)imageBounds.getHeight() >= (int)stackVisibleBounds.getHeight();
    }
    /**
     * 更新鼠标状态
     */
    private void updateCursor(){
        if(this.isMoveable()){
            imagePane.setCursor(Cursor.OPEN_HAND);
        }else{
            imagePane.setCursor(Cursor.DEFAULT);
        }
    }
    /**
     * 总放缩比例
     */
    private double totalScale = 1.0;
    /**
     * 放大因子
     */
    private final double zoomInFactor = 1.1;
    /**
     * 缩小因子
     */
    private final double zoomOutFactor = 1/1.1;
    /**
     * 放缩原子操作
     * @param zoomFactor 放缩因子
     * @param zoomCenter 放缩中心
     */
    private void zoom(double zoomFactor, Point2D zoomCenter) {
        // 放缩值更新 以及 图片显示偏移值更新
        double newSacle = this.totalScale * zoomFactor;
        this.translateX *=zoomFactor;
        this.translateY *=zoomFactor;
        this.totalScale = newSacle;

        // 先放缩
        this.mainImageView.setScaleX(newSacle);
        this.mainImageView.setScaleY(newSacle);

        // 再偏移 (图片占满imagepane前先中心放缩,占满后偏移放缩)
        if(zoomFactor>1&&this.isOverflow()){
            this.mainImageView.setTranslateX(this.translateX);
            this.mainImageView.setTranslateY(this.translateY);
        }else if(zoomFactor<1&&this.isOverflow()){
            Bounds currentBounds = this.mainImageView.getBoundsInParent();
            double imgPaneWidth = this.imagePane.getLayoutBounds().getWidth();
            double imgPaneHeight = this.imagePane.getLayoutBounds().getHeight();
            // 限制偏移范围
            translateX = getFixedOffset(translateX,imgPaneWidth /2 - currentBounds.getWidth()/2, currentBounds.getWidth()/2 - imgPaneWidth/2);
            mainImageView.setTranslateX(translateX);
            translateY = getFixedOffset(translateY, imgPaneHeight/2 - currentBounds.getHeight()/2, currentBounds.getHeight()/2 - imgPaneHeight/2);
            mainImageView.setTranslateY(translateY);
        }else{
            this.recoverTranslattion(200);
        }


        // 值溢出修正
        int newScaleInterger = Math.min((int) (this.MIN_SCALE * newSacle), this.MAX_SCALE);
        newScaleInterger = Math.max(newScaleInterger, this.MIN_SCALE);
        if(newScaleInterger == this.MIN_SCALE|| newScaleInterger == this.MAX_SCALE){
            this.stopTimerLine();
        }
        // 更新比例值
        this.scaleInteger.set(newScaleInterger);
        // 更新是否可移动鼠标样式
        this.updateCursor();
    }

    /**
     * 中心放大
     */
    private void zoomIn() {
        this.zoom(this.zoomInFactor,null);
    }

    /**
     * 在轴点放大
     * @param zoomCenter 轴点
     */
    private void zoomIn(Point2D zoomCenter){
        this.zoom(this.zoomInFactor,zoomCenter);
    }

    /**
     * 中心缩小
     */
    private void zoomOut() {
        this.zoom(this.zoomOutFactor,null);
    }

    /**
     * 在轴点缩小
     * @param zoomCenter 轴点
     */
    private void zoomOut(Point2D zoomCenter) {
        this.zoom(this.zoomOutFactor,zoomCenter);
    }

    /**
     * 滚轮放缩移动图片
     * @param scrollEvent
     */
    private void zoomByScroll(ScrollEvent scrollEvent){
        if(stage.isFullScreen()) return;                // 全屏屏蔽
        if(scrollEvent.isControlDown()){
            double deltaY = scrollEvent.getDeltaY();
            if(deltaY>0){
                if(this.scaleInteger.get()>=MAX_SCALE){
                    return;
                }
                zoomIn(new Point2D(scrollEvent.getX(),scrollEvent.getY()));
            }else{
                if(this.scaleInteger.get()<=MIN_SCALE){
                    return;
                }
                zoomOut(new Point2D(scrollEvent.getX(),scrollEvent.getY()));
            }
        }else{
            Bounds currentBounds = this.mainImageView.getBoundsInParent();
            Bounds stackVisibleBounds = imagePane.getLayoutBounds();
            double imgPaneHeight = stackVisibleBounds.getHeight();
            double imgPaneWidth = stackVisibleBounds.getWidth();
            // 计算允许的移动方向
            boolean heightFilled = currentBounds.getHeight() > imgPaneHeight;
            boolean widthFilled = currentBounds.getWidth() > imgPaneWidth;
            if(heightFilled){
                double offset = scrollEvent.getDeltaY() > 0 ? 50 : -50;
                translateY += heightFilled ? offset : 0;
                translateY = getFixedOffset(translateY, imgPaneHeight/2 - currentBounds.getHeight()/2, currentBounds.getHeight()/2 - imgPaneHeight/2);
                mainImageView.setTranslateY(translateY);
            }else if(widthFilled){
                double offset = scrollEvent.getDeltaY() > 0 ? 50 : -50;
                translateX += widthFilled ? offset : 0;
                translateX = getFixedOffset(translateX, imgPaneWidth/2 - currentBounds.getWidth()/2, currentBounds.getWidth()/2 - imgPaneWidth/2);
                mainImageView.setTranslateX(translateX);
            }
        }
    }
    /**
     * 停止持续事件的时间线
     */
    private void stopTimerLine() {
        if(this.timeline != null){
            this.timeline.stop();
        }
    }
}
