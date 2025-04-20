package com.eimp.controller;

import com.eimp.CropWindow;
import com.eimp.SlideWindow;
import com.eimp.component.CropRectMasker;
import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
import com.eimp.util.ZoomUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class WindowCropController implements Initializable {
    /**
     * 所属窗口stage
     */
    private Stage stage;
    /**
     * 主照片视图
     */
    @FXML private ImageView mainImageView;
    /**
     * 图片放大按钮
     */
    @FXML private Button zoomIn;
    /**
     * 图片缩小按钮
     */
    @FXML private Button zoomOut;
    /**
     * 图片放缩比例
     */
    @FXML private Label zoomScale;

    /**
     * 窗口关闭按钮
     */
    @FXML private Button closeBtn;
    /**
     * 窗口最小化按钮
     */
    @FXML private Button minBtn;
    /**
     * 全屏按钮
     */
    @FXML private Button maxBtn;
    /**
     * 裁剪按钮
     */
    @FXML private Button cropBtn;
    /**
     * 当前图片的信息工具
     */
    private ImageUtil imageUtil;
    /**
     * 图片放缩比例
     */
    private IntegerProperty scaleInteger = new SimpleIntegerProperty(-1);

    /**
     * 图片面板
     */
    @FXML
    private StackPane imagePane;
    /**
     * 裁剪矩形
     */
    @FXML
    private AnchorPane cropRect;
    /**
     * 动态图片遮罩蒙版
     */
    private CropRectMasker dynamicMask;
    /**
     *任意拖拽事件起始坐标
     */
    private double startX, startY;
    /**
     * 待裁剪图片
     */
    private Image image;
    /**
     * 原始宽高
     */
    private double originalWidth;
    private double originalHeight;

    /**
     * 图片放缩工具包
     */
    private ZoomUtil zoomUtil;
    /**
     * 矩形选框是否变形状态
     */
    private boolean isResizing;
    /**
     * 矩形选框是否自由拉伸
     */
    private boolean isFreeResizing;
    /**
     * 当前所选比例
     */
    private double currentScale;
    /**
     * 存放裁剪框的面板
     */
    @FXML
    private Pane cropRectPane;
    /**
     * 裁剪框调节区域宽度
     */
    private final double RESIZE_THRESHOLD =5.0;
    /**
     * 最小裁剪尺寸
     */
    private double CROP_MIN_SIZE;
    /**
     * 裁剪框当前坐标
     */
    private double rectX;
    private double rectY;
    /**
     * 裁剪框面板当前宽高
     */
    private double rectPaneW;
    private double rectPaneH;
    /**
     * 裁剪框当前宽高
     */
    private double rectW;
    private double rectH;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stage = CropWindow.getStage();
        this.setUpWindowControls();
        this.setUpScaleHandle();
    }

    /**
     * 导入图片及所在文件夹
     * @param imageUtil 导入的图片信息工具
     */
    public void importImage(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
        this.outPath=imageUtil.getDirectory().getAbsolutePath();
        this.outDirectory.setText(outPath);

        this.image = new Image(imageUtil.getURL());
        this.initMainImageView();

        // 给图片安装放缩工具包
        this.zoomUtil = new ZoomUtil(this.stage,this.mainImageView,this.imagePane,this.zoomOut,this.zoomIn,this.zoomScale,null);
        this.zoomUtil.updateZoomUtil(this.scaleInteger,100);

        this.initCropRect();
    }

    /**
     * 初始化当前窗口显示的图片及相关信息
     */
    private void initMainImageView() {
        this.originalWidth = this.image.getWidth();
        this.originalHeight = this.image.getHeight();

        this.scaleInteger.set(100);
        mainImageView.fitWidthProperty().bind(imagePane.widthProperty());
        mainImageView.fitHeightProperty().bind(imagePane.heightProperty());
        this.mainImageView.setImage(this.image);
    }

    /**
     * 设置裁剪框宽度
     * @param width 宽度
     */
    private void setCropRectWidth(double width) {
        cropRect.setPrefWidth(width);
        cropRect.setMinWidth(width);
        cropRect.setMaxWidth(width);
    }

    /**
     * 设置裁剪框高度
     * @param height 高度
     */
    private void setCropRectHeight(double height) {
        cropRect.setPrefHeight(height);
        cropRect.setMinHeight(height);
        cropRect.setMaxHeight(height);
    }
    /**
     * 初始化裁剪框,设置监听状态,默认裁剪比例以及最小裁剪尺寸
     */
    private void initCropRect(){
        // 添加图片遮罩
        this.dynamicMask = new CropRectMasker(cropRectPane,cropRect);
        // 设置裁剪框面板监听器,更新布局状态
        this.setUpCropRectPaneListeners();
        this.updateLayout();
        // 设置裁剪框拉伸移动监听器
        this.setUpResizeRectListeners();
        this.setUpDragRectListeners();
        // 设置默认选中自由裁剪比例
        this.newSelected = this.oldSelected = this.s_free;
        this.isFreeResizing = true;
        newSelected.getStyleClass().add("scaleTypeSelected");

        // 设置裁剪框初始大小
        Platform.runLater(() -> {
            // TODO 有布局bug javafx布局异步刷新机制导致属性没跟上
            this.setCropRectWidth(mainImageView.getBoundsInParent().getWidth());
            this.setCropRectHeight(mainImageView.getBoundsInParent().getHeight());
            // 图片裁剪的最小尺寸,最短边的0.1
            this.CROP_MIN_SIZE = Math.min(cropRectPane.getWidth(), cropRectPane.getHeight()) * 0.1;
            this.updateOutSize((int)originalWidth, (int)originalHeight);
        });
    }

    /**
     * 设置裁剪框面板监听器
     */
    private void setUpCropRectPaneListeners(){
        cropRectPane.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            return mainImageView.getBoundsInParent().getWidth();
        },mainImageView.boundsInParentProperty()));
        cropRectPane.minWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            return mainImageView.getBoundsInParent().getWidth();
        },mainImageView.boundsInParentProperty()));
        cropRectPane.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            return mainImageView.getBoundsInParent().getWidth();
        },mainImageView.boundsInParentProperty()));
        cropRectPane.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            return mainImageView.getBoundsInParent().getHeight();
        },mainImageView.boundsInParentProperty()));
        cropRectPane.minHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            return mainImageView.getBoundsInParent().getHeight();
        },mainImageView.boundsInParentProperty()));
        cropRectPane.maxHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            return mainImageView.getBoundsInParent().getHeight();
        },mainImageView.boundsInParentProperty()));

        cropRectPane.widthProperty().addListener(obs -> {
            updateLayout(cropRectPane.getWidth() / rectPaneW);
        });
        cropRectPane.heightProperty().addListener(obs -> {
            updateLayout(cropRectPane.getHeight() / rectPaneH);
        });
        mainImageView.translateXProperty().addListener((observable, oldValue, newValue) -> {
            cropRect.setTranslateX(newValue.doubleValue());
        });
        mainImageView.translateYProperty().addListener((observable, oldValue, newValue) -> {
            cropRect.setTranslateY(newValue.doubleValue());
        });
    }

    /**
     * 更新裁剪框布局参数
     */
    private void updateLayout(){
        rectX = cropRect.getLayoutX();
        rectY = cropRect.getLayoutY();
        rectW = cropRect.getWidth();
        rectH = cropRect.getHeight();
        rectPaneW = cropRectPane.getWidth();
        rectPaneH = cropRectPane.getHeight();
    }

    /**
     * 更新裁剪框布局
     * @param scale 放缩比例
     */
    private void updateLayout(double scale) {
        cropRect.setLayoutX(rectX*scale);
        cropRect.setLayoutY(rectY*scale);
        this.setCropRectWidth(rectW*scale);
        this.setCropRectHeight(rectH*scale);
    }

    /**
     * 设置裁剪框拖拽移动监听器
     */
    private void setUpDragRectListeners(){
        cropRect.setOnMousePressed(this::handleMousePressed);
        cropRect.setOnMouseDragged(this::handleMouseDragged);
        cropRect.setOnMouseMoved(e->{
            double x = e.getX();
            double y = e.getY();
            double width = cropRect.getWidth();
            double height = cropRect.getHeight();
            if(x > RESIZE_THRESHOLD && y > RESIZE_THRESHOLD && x < width - RESIZE_THRESHOLD && y < height - RESIZE_THRESHOLD){
                cropRect.setCursor(Cursor.MOVE);
            }
        });
    }

    /**
     * 裁剪功能
     */
    @FXML
    private void crop(){
        if (mainImageView.getImage() == null) return;
        // 计算实际裁剪区域（考虑缩放）
        double scaleX =originalWidth / cropRectPane.getWidth();
        double scaleY =originalHeight / cropRectPane.getHeight();
        double x = cropRect.getLayoutX() * scaleX;
        double y = cropRect.getLayoutY() * scaleY;
        double width = cropRect.getWidth() * scaleX;
        double height = cropRect.getHeight() * scaleY;
        if(width > originalWidth || height > originalHeight || x < 0 || y< 0 || x > rectPaneW || y > rectPaneH || x+rectW>rectPaneW || y+rectH>rectPaneH){
            Notifications.create()
                .text("BUG")
                .hideAfter(Duration.seconds(1))
                .position(Pos.CENTER)
                .owner(this.stage)
                .darkStyle()
                .show();
            return;
        }
        // 创建裁剪后的图像
        PixelReader reader = mainImageView.getImage().getPixelReader();
        WritableImage croppedImage = new WritableImage(reader, (int)x, (int)y, (int)width, (int)height);

        // 保存裁剪后的图像
        boolean success = saveImage(croppedImage, imageUtil.getFileName());
        if(success){
            Notifications.create()
                    .text("裁剪成功")
                    .hideAfter(Duration.seconds(1))
                    .position(Pos.CENTER)
                    .owner(this.stage)
                    .darkStyle()
                    .show();
            // 创建一个Timeline，在3秒后执行操作
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), event -> {
                        this.stage.close();
                    })
            );
            timeline.play();

        }else{
            Notifications.create()
                    .text("BUG")
                    .hideAfter(Duration.seconds(1))
                    .position(Pos.CENTER)
                    .owner(this.stage)
                    .darkStyle()
                    .show();
        }
    }

    /**
     * 保存裁剪后的图像
     * @param croppedImage 裁剪后的图像
     * @param filename 原文件名
     * @return 保存结果
     */
    private boolean saveImage(WritableImage croppedImage,String filename){
        String type = filename.substring(filename.lastIndexOf(".")+1);
        filename = this.outPath +"\\" + filename.substring(0, filename.lastIndexOf(".")) +"-副本."+type;
        File file = new File(filename);
        if(file.exists()){
            filename=filename.substring(0,filename.lastIndexOf("."))+"-副本."+type;
        }
        // 使用ImageWriter保存图像
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(croppedImage, null);
            ImageIO.write(bufferedImage,type,new File(filename));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 输出尺寸标签
     */
    @FXML
    private Label outSize;

    /**
     * 更新输出尺寸
     */
    private void updateOutSize(){
        outSize.setText(String.format("%d×%d",getOutputWidth(),getOutputHeight()));
    }

    /**
     * 更新输出尺寸
     */
    private void updateOutSize(int width,int height){
        outSize.setText(String.format("%d×%d",width,height));
    }
    /**
     * 计算图片输出宽
     * @return 裁剪后的宽
     */
    private int getOutputWidth(){
        return (int) (cropRect.getPrefWidth() * (originalWidth / cropRectPane.getWidth()));

    }

    /**
     * 计算图片输出高
     * @return 裁剪后的高
     */
    private int getOutputHeight(){
        return (int) (cropRect.getPrefHeight() * (originalHeight / cropRectPane.getHeight()));
    }
    /**
     * 裁剪比例
     */
    @FXML private VBox s_free;
    @FXML private VBox s_1_1;
    @FXML private VBox s_2_3;
    @FXML private VBox s_4_3;
    @FXML private VBox s_3_2;
    @FXML private VBox s_3_4;
    @FXML private VBox s_16_9;
    @FXML private VBox s_9_16;
    @FXML private VBox s_origin;
    @FXML private HBox s_pc;
    @FXML private HBox s_license;
    @FXML private HBox s_phone;

    /**
     * 设置比例点击处理事件
     */
    private void setUpScaleHandle(){
        s_free.setOnMouseClicked(e->{
            this.selected(e,-1);
        });
        s_1_1.setOnMouseClicked(e-> {
            this.selected(e,1.0);
        });
        s_2_3.setOnMouseClicked(e-> {
            this.selected(e,2.0/3.0);
        });
        s_4_3.setOnMouseClicked(e-> {
            this.selected(e,4.0/3.0);
        });
        s_3_2.setOnMouseClicked(e-> {
            this.selected(e,3.0/2.0);
        });
        s_3_4.setOnMouseClicked(e-> {
            this.selected(e,3.0/4.0);
        });
        s_16_9.setOnMouseClicked(e-> {
            this.selected(e,16.0/9.0);
        });
        s_9_16.setOnMouseClicked(e-> {
            this.selected(e,9.0/16.0);
        });
        s_origin.setOnMouseClicked(e-> {
            this.selected(e,this.originalWidth/this.originalHeight);
        });
        s_pc.setOnMouseClicked(e-> {
            this.selected(e,1920.0/1080.0);
        });
        s_license.setOnMouseClicked(e-> {
            this.selected(e,344.0/481.0);
        });
        s_phone.setOnMouseClicked(e-> {
            this.selected(e,9.0/19.5);
        });

    }

    /**
     * 更新裁剪比例
     */
    private void updateCropSacle(){
        double originScale= this.originalWidth / this.originalHeight;
        double width=cropRectPane.getWidth();
        double height=cropRectPane.getHeight();
        if(this.currentScale<originScale){
            this.setCropRectHeight(height);
            this.setCropRectWidth(height * currentScale);
            cropRect.setLayoutX((width-cropRect.getPrefWidth())/2);
            cropRect.setLayoutY(0);
        }else{
            this.setCropRectWidth(width);
            this.setCropRectHeight(width / currentScale);
            cropRect.setLayoutX(0);
            cropRect.setLayoutY((height-cropRect.getPrefHeight())/2);
        }
    }
    /**
     * 被选中比例所属结点
     */
    private Node newSelected;
    private Node oldSelected;

    /**
     * 比例按钮互斥选中背景设置,刷新裁剪框和比例标签
     */
    private void selected(MouseEvent event,double scale) {
        newSelected = (Node)event.getSource();
        if(oldSelected != null) {
            oldSelected.getStyleClass().remove("scaleTypeSelected");
        }
        oldSelected = newSelected;
        newSelected.getStyleClass().add("scaleTypeSelected");

        if(scale != -1) {
            this.currentScale = scale;
            this.isFreeResizing = false;
            this.updateCropSacle();
            this.updateOutSize();
        }else{
            this.isFreeResizing = true;
        }
        this.updateLayout();
    }
    /**
     * 输出目录标签
     */
    @FXML
    private Label outDirectory;
    /**
     * 输出路径
     */
    private String outPath;

    /**
     * 选择输出目录
     */
    @FXML
    private void chooseOutputDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择输出目录");

        // 设置初始目录
        directoryChooser.setInitialDirectory(new File(this.outPath));

        // 显示对话框并获取选择的目录
        File selectedDirectory = directoryChooser.showDialog(this.stage);

        if (selectedDirectory != null) {
            outPath = selectedDirectory.getAbsolutePath();
            this.outDirectory.setText(outPath);
        }
    }

    /**
     * 打开输出目录
     */
    @FXML
    private void openDirectory(){
        FileUtil.openContainingFolder(this.imageUtil.getAbsolutePath());
    }
    /**
     *  窗口控制逻辑
     */
    private void setUpWindowControls() {
        this.setUpResizeListeners();
        minBtn.setTooltip(new Tooltip("最小化"));
        maxBtn.setTooltip(new Tooltip("最大化"));
        closeBtn.setTooltip(new Tooltip("关闭"));
        maxBtn.getStyleClass().add("maxBtn-full");
        // 窗口控制按钮事件
        closeBtn.setOnAction(e -> stage.close());
        minBtn.setOnAction(e -> stage.setIconified(true));
        maxBtn.setOnAction(e -> toggleMaximize());
    }
    /**
     * 窗口最大化切换
     */
    private void toggleMaximize() {
        stage.setMaximized(!stage.isMaximized());
        if(stage.isMaximized()) {
            maxBtn.getStyleClass().remove("maxBtn-full");
            maxBtn.getStyleClass().add("maxBtn-recover");
            maxBtn.getTooltip().setText("还原");
        }else {
            maxBtn.getStyleClass().remove("maxBtn-recover");
            maxBtn.getStyleClass().add("maxBtn-full");
            maxBtn.getTooltip().setText("最大化");
        }
    }

    /**
     * 窗口拖拽前宽度
     */
    private double startWidth;
    /**
     * 窗口拖拽前高度
     */
    private double startHeight;

    /**
     * 顶部调节窗口大小指示区域
     */
    @FXML
    private Region topResize;
    /**
     * 底部调节窗口大小指示区域
     */
    @FXML
    private Region bottomResize;
    /**
     * 左边调节窗口大小指示区域
     */
    @FXML
    private Region leftResize;
    /**
     * 右边调节窗口大小指示区域
     */
    @FXML
    private Region rightResize;
    /**
     * 左上角调节窗口大小指示区域
     */
    @FXML
    private Region leftTopResize;
    /**
     * 右上角调节窗口大小指示区域
     */
    @FXML
    private Region rightTopResize;
    /**
     * 左下角调节窗口大小指示区域
     */
    @FXML
    private Region leftBottomResize;
    /**
     * 右下角调节窗口大小指示区域
     */
    @FXML
    private Region rightBottomResize;

    /**
     * 调节方向枚举值
     */
    private enum ResizeDirection {
        LEFT, RIGHT, TOP, BOTTOM,
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }
    /**
     * 设置所有可调节窗口大小指示区域的监听器
     */
    private void setUpResizeListeners() {
        // 左侧调整
        setupResizeHandler(leftResize, ResizeDirection.LEFT);
        // 右侧调整
        setupResizeHandler(rightResize, ResizeDirection.RIGHT);
        // 顶部调整
        setupResizeHandler(topResize, ResizeDirection.TOP);
        // 底部调整
        setupResizeHandler(bottomResize, ResizeDirection.BOTTOM);
        // 左上角调整
        setupResizeHandler(leftTopResize, ResizeDirection.LEFT_TOP);
        // 右上角调整
        setupResizeHandler(rightTopResize, ResizeDirection.RIGHT_TOP);
        // 左下角调整
        setupResizeHandler(leftBottomResize, ResizeDirection.LEFT_BOTTOM);
        // 右下角调整
        setupResizeHandler(rightBottomResize, ResizeDirection.RIGHT_BOTTOM);
    }

    /**
     * 鼠标拖拽事件分发处理逻辑
     * @param region 调节区域
     * @param direction 调节方向
     */
    private void setupResizeHandler(Region region, ResizeDirection direction) {
        region.setOnMousePressed(event -> startResize(event));
        region.setOnMouseDragged(event -> handleResize(event, direction));
    }

    /**
     * 记录鼠标初始坐标,窗口或矩形选框初始大小
     * @param event
     */
    private void startResize(MouseEvent event) {
        startX = event.getScreenX();
        startY = event.getScreenY();

        startWidth = stage.getWidth();
        startHeight = stage.getHeight();

        event.consume();
    }

    /**
     * 主要处理事件分发逻辑
     * @param event
     * @param direction 调节方向
     */
    private void handleResize(MouseEvent event, ResizeDirection direction) {
        double deltaX = event.getScreenX() - startX;
        double deltaY = event.getScreenY() - startY;

        switch (direction) {
            case LEFT:
                handleLeftResize(deltaX);
                break;
            case RIGHT:
                handleRightResize(deltaX);
                break;
            case TOP:
                handleTopResize(deltaY);
                break;
            case BOTTOM:
                handleBottomResize(deltaY);
                break;
            case LEFT_TOP:
                if(isResizing&&isFreeResizing==false){
                    handleLTResize(deltaX);
                    break;
                }
                handleLeftResize(deltaX);
                handleTopResize(deltaY);
                break;
            case RIGHT_TOP:
                if(isResizing&&isFreeResizing==false){
                    handleRTResize(deltaX);
                    break;
                }
                handleRightResize(deltaX);
                handleTopResize(deltaY);
                break;
            case LEFT_BOTTOM:
                if(isResizing&&isFreeResizing==false){
                    handleLBResize(deltaY);
                    break;
                }
                handleLeftResize(deltaX);
                handleBottomResize(deltaY);
                break;
            case RIGHT_BOTTOM:
                if(isResizing&&isFreeResizing==false){
                    handleRBResize(deltaX);
                    break;
                }
                handleRightResize(deltaX);
                handleBottomResize(deltaY);
                break;
        }
        if(isResizing){
            this.updateOutSize();
        }
        event.consume();
    }

    /**
     * 判断裁剪框是否可移动
     * @return
     */
    private boolean isResizable(){
        double x = cropRect.getLayoutX();
        double y = cropRect.getLayoutY();
        double width = cropRect.getPrefWidth();
        double height = cropRect.getPrefHeight();

        if(x < 0 || y < 0 || x+width >rectPaneW || y+height > rectPaneH || width < CROP_MIN_SIZE || height < CROP_MIN_SIZE || x > rectPaneW || y > rectPaneH){
            return false;
        }
        return true;
    }

    /**
     * 原子调节操作,窗口或矩形选框在左边区域放缩
     * @param deltaX x坐标偏移量
     */
    private void handleLeftResize(double deltaX) {
        double newWidth = startWidth - deltaX;
        if(this.isResizing){
            if(this.isFreeResizing) {
                this.setCropRectWidth(getFixedOffset(newWidth, CROP_MIN_SIZE, startWidth + rectX));
                cropRect.setLayoutX(getFixedOffset(rectX + deltaX, 0, startWidth + rectX - CROP_MIN_SIZE));
            } else {
                double newHeight = newWidth / currentScale;
                double centerH = rectY + rectH / 2;
                double mH, mW,ly,lx;
                if ((centerH) > rectPaneH / 2) {
                    mH = (rectPaneH - (centerH)) * 2;
                    ly = getFixedOffset(rectY + (startHeight - newHeight) / 2, rectPaneH - mH, centerH - CROP_MIN_SIZE / 2);
                } else {
                    mH = centerH * 2;
                    ly = getFixedOffset(rectY + (startHeight - newHeight) / 2, 0, centerH - CROP_MIN_SIZE / 2);
                }
                mW = mH * currentScale;
                lx = getFixedOffset(rectX + deltaX, startWidth + rectX - mW, startWidth + rectX - CROP_MIN_SIZE);
                if(lx>=0){
                    this.setCropRectWidth(getFixedOffset(newWidth, CROP_MIN_SIZE, mW));
                    cropRect.setLayoutX(lx);
                    this.setCropRectHeight(getFixedOffset(newHeight, CROP_MIN_SIZE, mH));
                    cropRect.setLayoutY(ly);
                }else{
                    this.setCropRectWidth(rectX+rectW);
                    cropRect.setLayoutX(0);
                }
            }
        }else if (newWidth > stage.getMinWidth()) {
            stage.setWidth(newWidth);
            stage.setX(startX + deltaX);
        }
    }

    /**
     * 原子调节操作,窗口或矩形选框在右边区域放缩
     * @param deltaX x坐标偏移量
     */
    private void handleRightResize(double deltaX) {
        double newWidth = startWidth + deltaX;
        if(this.isResizing){
            if(this.isFreeResizing) {
                this.setCropRectWidth(getFixedOffset(newWidth,CROP_MIN_SIZE,cropRectPane.getWidth()-rectX));
            }else{
                double newHeight = newWidth / currentScale;
                double centerH = rectY + rectH / 2;
                double mH, mW,ly,lx;
                if ((centerH) > rectPaneH / 2) {
                    mH = (rectPaneH - (centerH)) * 2;
                    ly = getFixedOffset(rectY + (startHeight - newHeight) / 2, rectPaneH - mH, centerH - CROP_MIN_SIZE / 2);
                } else {
                    mH = centerH * 2;
                    ly = getFixedOffset(rectY + (startHeight - newHeight) / 2, 0, centerH - CROP_MIN_SIZE / 2);
                }
                mW = mH * currentScale;
                if(rectX+newWidth<=rectPaneW){
                    this.setCropRectWidth(getFixedOffset(newWidth, CROP_MIN_SIZE, mW));
                    this.setCropRectHeight(getFixedOffset(newHeight, CROP_MIN_SIZE, mH));
                    cropRect.setLayoutY(ly);
                }else{
                    this.setCropRectWidth(rectPaneW-rectX);
                }
            }


        }else if (newWidth > stage.getMinWidth()) {
            stage.setWidth(newWidth);
        }
    }

    /**
     * 原子调节操作,窗口或矩形选框在顶部区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleTopResize(double deltaY) {
        double newHeight = startHeight - deltaY;
        if(this.isResizing){
            if(this.isFreeResizing) {
                this.setCropRectHeight(getFixedOffset(newHeight,CROP_MIN_SIZE,rectY+startHeight));
                cropRect.setLayoutY(getFixedOffset(rectY + deltaY,0,rectY+startHeight-CROP_MIN_SIZE));
            }else{
                double newWidth = newHeight * currentScale;
                double centerW = rectX + rectW / 2;
                double mH, mW,ly,lx;
                if ((centerW) > rectPaneW / 2) {
                    mW = (rectPaneW - (centerW)) * 2;
                    lx = getFixedOffset(rectX + (startWidth - newWidth) / 2, rectPaneW - mW, centerW - CROP_MIN_SIZE / 2);
                } else {
                    mW = centerW * 2;
                    lx = getFixedOffset(rectX + (startWidth - newWidth) / 2, 0, centerW - CROP_MIN_SIZE / 2);
                }
                mH = mW / currentScale;
                ly = getFixedOffset(rectY + deltaY, startHeight + rectY - mH, startHeight + rectY - CROP_MIN_SIZE);
                if(ly>=0){
                    this.setCropRectWidth(getFixedOffset(newWidth, CROP_MIN_SIZE, mW));
                    cropRect.setLayoutX(lx);
                    this.setCropRectHeight(getFixedOffset(newHeight, CROP_MIN_SIZE, mH));
                    cropRect.setLayoutY(ly);
                }else{
                    this.setCropRectHeight(rectY+rectH);
                    cropRect.setLayoutY(0);
                }
            }

        }else if (newHeight > stage.getMinHeight()) {
            stage.setHeight(newHeight);
            stage.setY(startY + deltaY);
        }
    }
    /**
     * 原子调节操作,窗口或矩形选框在底部区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleBottomResize(double deltaY) {
        double newHeight = startHeight + deltaY;
        if(this.isResizing){
            if (this.isFreeResizing){
                this.setCropRectHeight(getFixedOffset(newHeight,CROP_MIN_SIZE,cropRectPane.getHeight()-rectY));
            }else{
                double newWidth = newHeight * currentScale;
                double centerW = rectX + rectW / 2;
                double mH, mW,ly,lx;
                if ((centerW) > rectPaneW / 2) {
                    mW = (rectPaneW - (centerW)) * 2;
                    lx = getFixedOffset(rectX + (startWidth - newWidth) / 2, rectPaneW - mW, centerW - CROP_MIN_SIZE / 2);
                } else {
                    mW = centerW * 2;
                    lx = getFixedOffset(rectX + (startWidth - newWidth) / 2, 0, centerW - CROP_MIN_SIZE / 2);
                }
                mH = mW / currentScale;
                if(rectY+newHeight<=rectPaneH){
                    this.setCropRectWidth(getFixedOffset(newWidth, CROP_MIN_SIZE, mW));
                    cropRect.setLayoutX(lx);
                    this.setCropRectHeight(getFixedOffset(newHeight, CROP_MIN_SIZE, mH));
                }else{
                    this.setCropRectHeight(rectPaneH-rectY);
                }
            }

        }else if (newHeight > stage.getMinHeight()) {
            stage.setHeight(newHeight);
        }
    }

    /**
     * 原子调节操作,窗口或矩形选框在左上边区域放缩
     * @param deltaX x坐标偏移量
     */
    private void handleLTResize(double deltaX) {
        double newWidth = startWidth - deltaX;
        double newHeight = newWidth / currentScale;
        double lx,ly;
        lx = getFixedOffset(rectX + deltaX, 0, startWidth + rectX - CROP_MIN_SIZE);
        ly = getFixedOffset(rectY + rectH- newHeight,0,rectY+startHeight-CROP_MIN_SIZE);
        if(lx>0&&ly>0){
            this.setCropRectWidth(Math.max(CROP_MIN_SIZE,newWidth));
            this.setCropRectHeight(Math.max(CROP_MIN_SIZE,newHeight));
            cropRect.setLayoutX(lx);
            cropRect.setLayoutY(ly);
        }else if(ly==0 && lx>0) {
            double H = (rectY+rectH);
            cropRect.setLayoutY(0);
            this.setCropRectHeight(H);
            double W = H*currentScale;
            cropRect.setLayoutX(rectX + rectW -W);
            this.setCropRectWidth(W);
        }else if(lx==0 && ly>0){
            double W = (rectX+rectW);
            cropRect.setLayoutX(0);
            this.setCropRectWidth(W);
            double H = W/currentScale;
            cropRect.setLayoutY(rectY+rectH-H);
            this.setCropRectHeight(H);
        }
    }
    /**
     * 原子调节操作,窗口或矩形选框在左下区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleLBResize(double deltaY) {
        double newHeight = startHeight + deltaY;
        double newWidth = newHeight * currentScale;
        double lx;
        lx = getFixedOffset(rectX + rectW- newWidth,0,rectX+startWidth-CROP_MIN_SIZE);
        if(rectY+newHeight<rectPaneH&&lx>0){
            this.setCropRectWidth(Math.max(CROP_MIN_SIZE,newWidth));
            this.setCropRectHeight(Math.max(CROP_MIN_SIZE,newHeight));
            cropRect.setLayoutX(lx);
        }else if(lx ==0 && rectY +(rectW+rectX)/currentScale <= rectPaneH) {
            double W = rectX+rectW;
            cropRect.setLayoutX(0);
            this.setCropRectHeight(W/currentScale);
            this.setCropRectWidth(W);
        }else if(rectY+newHeight>=rectPaneH && (rectX + rectW)- (rectPaneH-rectY)*currentScale>=0){
            double H = (rectPaneH-rectY);
            double W = H*currentScale;
            cropRect.setLayoutX(rectX+rectW-W);
            this.setCropRectHeight(H);
            this.setCropRectWidth(W);
        }
    }
    /**
     * 原子调节操作,窗口或矩形选框在左下区域放缩
     * @param deltaX y坐标偏移量
     */
    private void handleRTResize(double deltaX) {
        double newWidth = startWidth + deltaX;
        double newHeight = newWidth / currentScale;
        double ly;
        ly = getFixedOffset(rectY + rectH- newHeight,0,rectY+startHeight-CROP_MIN_SIZE);
        if(rectX+newWidth<rectPaneW&&ly>0){
            this.setCropRectWidth(Math.max(CROP_MIN_SIZE,newWidth));
            this.setCropRectHeight(Math.max(CROP_MIN_SIZE,newHeight));
            cropRect.setLayoutY(ly);
        }else if(ly==0 && rectX + (rectH+rectY)*currentScale <= rectPaneW) {
            double H = rectY+rectH;
            cropRect.setLayoutY(0);
            this.setCropRectHeight(H);
            this.setCropRectWidth(H*currentScale);
        }else if(rectX+newWidth>=rectPaneW && (rectY + rectH)- (rectPaneW-rectX)/currentScale>=0){
            double W = (rectPaneW-rectX);
            this.setCropRectWidth(W);
            double H = W/currentScale;
            cropRect.setLayoutY(rectY+rectH-H);
            this.setCropRectHeight(H);
        }
    }

    /**
     * 原子调节操作,窗口或矩形选框在右下区域放缩
     * @param deltaY x坐标偏移量
     */
    private void handleRBResize(double deltaY) {
        double newHeight = startHeight + deltaY;
        double newWidth = newHeight * currentScale;

        if(newHeight <= rectPaneH-rectY && newWidth<= rectPaneW-rectX){
            this.setCropRectWidth(Math.max(CROP_MIN_SIZE,newWidth));
            this.setCropRectHeight(Math.max(CROP_MIN_SIZE,newHeight));
        }else if(newHeight > rectPaneH-rectY && rectX + (rectPaneH-rectY)*currentScale <= rectPaneW) {
            double H = rectPaneH-rectY;
            this.setCropRectHeight(H);
            this.setCropRectWidth(H*currentScale);
        }else if(newWidth > rectPaneW-rectX && rectY+ (rectPaneW-rectX)/currentScale <= rectPaneH){
            double W = rectPaneW-rectX;
            this.setCropRectWidth(W);
            this.setCropRectHeight(W/currentScale);

        }
    }



    /**
     * 鼠标拖拽窗口事件处理,记录鼠标初始位置
     */
    @FXML
    private void handleMousePressed(MouseEvent event) {
        event.consume();
        if (event.getTarget() instanceof HBox) {
            startX = stage.getX() - event.getScreenX();
            startY = stage.getY() - event.getScreenY();
        }else if(event.getTarget() instanceof AnchorPane){
            startX = cropRect.getLayoutX() - event.getScreenX();
            startY = cropRect.getLayoutY() - event.getScreenY();
        }
    }


    /**
     * 鼠标拖拽窗口事件处理,窗口跟随鼠标拖拽位置
     * @param event
     */
    @FXML
    private void handleMouseDragged(MouseEvent event) {
        event.consume();
        if (event.getTarget() instanceof HBox) {
            this.stage.setX(event.getScreenX() + startX);
            this.stage.setY(event.getScreenY() + startY);
        }else if(event.getTarget() instanceof AnchorPane){
            cropRect.setLayoutX(getFixedOffset(event.getScreenX() + startX,0,cropRectPane.getWidth()-cropRect.getWidth()));
            cropRect.setLayoutY(getFixedOffset(event.getScreenY() + startY,0,cropRectPane.getHeight()-cropRect.getHeight()));
            this.updateLayout();
        }
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
     * 设置裁剪框调整大小初始状态
     * @param event
     */
    @FXML
    private void setResizing(MouseEvent event){
        this.isResizing = true;
        startX = event.getScreenX();
        startY = event.getScreenY();
        rectX = cropRect.getLayoutX();
        rectY = cropRect.getLayoutY();
        startWidth = cropRect.getWidth();
        startHeight = cropRect.getHeight();
    }

    /**
     * 设置裁剪框调整大小结束状态
     */
    @FXML
    private void updateResizing(){
        this.isResizing=false;
        this.updateLayout();
    }
    /**
     * 顶部调节窗口大小指示区域
     */
    @FXML
    private Region topResizeRect;
    /**
     * 底部调节窗口大小指示区域
     */
    @FXML
    private Region bottomResizeRect;
    /**
     * 左边调节窗口大小指示区域
     */
    @FXML
    private Region leftResizeRect;
    /**
     * 右边调节窗口大小指示区域
     */
    @FXML
    private Region rightResizeRect;
    /**
     * 左上角调节窗口大小指示区域
     */
    @FXML
    private Region leftTopResizeRect;
    /**
     * 右上角调节窗口大小指示区域
     */
    @FXML
    private Region rightTopResizeRect;
    /**
     * 左下角调节窗口大小指示区域
     */
    @FXML
    private Region leftBottomResizeRect;
    /**
     * 右下角调节窗口大小指示区域
     */
    @FXML
    private Region rightBottomResizeRect;


    private void setUpResizeRectListeners() {
        // 左侧调整
        leftResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.LEFT));
        // 右侧调整
        rightResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.RIGHT));
        // 顶部调整
        topResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.TOP));
        // 底部调整
        bottomResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.BOTTOM));
        // 左上角调整
        leftTopResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.LEFT_TOP));
        // 右上角调整
        rightTopResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.RIGHT_TOP));
        // 左下角调整
        leftBottomResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.LEFT_BOTTOM));
        // 右下角调整
        rightBottomResizeRect.setOnMouseDragged(e->handleResize(e, ResizeDirection.RIGHT_BOTTOM));
    }
}

// 加载图片
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("选择图片");
//        File file = fileChooser.showOpenDialog(stage);
//        if (file != null) {
//            try {
//                Image image = new Image(new FileInputStream(file));
//                mainImageView.setImage(image);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }