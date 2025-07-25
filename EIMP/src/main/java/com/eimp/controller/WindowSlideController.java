package com.eimp.controller;
import com.eimp.CropWindow;
import com.eimp.SlideWindow;
import com.eimp.component.*;
import com.eimp.util.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.coobird.thumbnailator.Thumbnails;
import org.controlsfx.control.Notifications;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class WindowSlideController implements Initializable {
    /**
     * 所属窗口stage
     */
    private Stage stage;
    /**
     * 主照片视图
     */
    @FXML private ImageView mainImageView;
    /**
     * 图片缩略图栏容器
     */
    @FXML private ThumbnailGallery thumbnailGallery;
    @FXML private HBox thumbnailGalleryContainer;
    @FXML private Button rotate;
    @FXML private Button delete;
    @FXML private Button prePage;
    @FXML private Button nextPage;
    @FXML private Button play;
    @FXML private Button info;
    @FXML private Button compress;
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
     * 恢复图片原始比例按钮
     */
    @FXML private Button originalScale;
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
    private URL location;
    private ResourceBundle resources;
    /**
     * 顶部主功能按钮栏,动态伸缩
     */
    @FXML private HBox dynamicButtonContainer;
    /**
     * ID与图标映射关系
     */
    private Map<String,String> urlMap = new HashMap<>();
    /**
     * ID与提示文本映射关系
     */
    private Map<String,String> textMap = new HashMap<>();
    /**
     * ID与顶部功能按钮映射关系
     */
    private Map<String,Button> buttonMap = new HashMap<>();
    /**
     * 更多功能按钮
      */
    @FXML
    private Button moreMenuButton;
    /**
     * 当前图片的信息工具
     */
    private ImageUtil imageUtil;
    /**
     * 当前图片
     */
    private Image image;
    /**
     * 当前文件夹的图片列表
     */
    private ObservableList<ImageUtil> imageUtilList = FXCollections.observableArrayList();
    /**
     * 当前图片索引
     */
    private IntegerProperty currentIndex = new SimpleIntegerProperty(0);
    /**
     * 主窗口控制器
     */
    private WindowMainController windowMainController;
    /**
     * 用于控制持续触发事件的时间线
     */
    private Timeline timeline;
    /**
     * 幻灯片播放时间线
     */
    private Timeline playingTimeLine;
    /**
     * 图片放缩时间线
     */
    private Timeline zoomingTimeLine;
    /**
     * 图片显示模式枚举值
     */
    private enum DisplayMode {FIT, ORIGINAL, ZOOMING};
    /**
     * 初始图片显示模式
     */
    private DisplayMode displayMode = DisplayMode.FIT;
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
    private  int MIN_SCALE;
    /**
     * 图片面板
     */
    @FXML
    private StackPane imagePane;
    /**
     * 缩略图栏面板
     */
    @FXML
    private AnchorPane secondaryPane;
    /**
     * 图片名标签
     */
    @FXML
    private Label imageName;
    /**
     * 图片索引提示按钮
     */
    @FXML
    private Button orderNum;
    /**
     * 图片大小提示按钮
     */
    @FXML
    private Button fileSize;
    /**
     * 图片尺寸提示按钮
     */
    @FXML
    private Button imageArea;
    /**
     * 图片宽高
     */
    private double originalWidth, originalHeight;
    @FXML private HBox infoColumn;
    /**
     * 窗口布局初始化,对fxml布局组件初始化
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.location = location;
        this.resources = resources;
        this.windowMainController = (WindowMainController) ControllerMap.getController(WindowMainController.class);
        this.stage = SlideWindow.getStage();

        this.initMap();
        this.initMoreMenuButton();
        this.setUpDynamicButtonContainerListener();
        this.initButtonStyle();
        this.setUpWindowControls();
        this.setUpFullScreenListener();
    }
    /**
     * 缓存JavaFX的Image对象
      */
    public static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();
    /**
     * 导入图片及所在文件夹
     * @param imageUtil 导入的图片信息工具
     */
    public void importImage(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;

        File directory = imageUtil.getDirectory();
        File[] images = directory.listFiles(FileUtil::isSupportImageFormat);
        if (images != null) {
            imageUtilList.setAll(
                    Arrays.stream(imageUtil.getDirectory().listFiles(FileUtil::isSupportImageFormat))
                            .map(ImageUtil::new)
                            .collect(Collectors.toList())
            );
        }
        this.preLoadImages();
        this.setUpZoomScaleListener();
        this.setUpRotationListener();
        this.initButtonStatus();
        this.setUpPageButtonListener();
        this.setUpMovementConstraints();
        this.initImageListOrder(imageUtil);
        this.updateMainImageView();
        this.initThumbnailGallery(); // 逐步加载缩略图
    }
    /**
     * 线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * 线程池异步预加载图片
     */
    private void preLoadImages(){
        imageUtilList.stream().map(ImageUtil::getAbsolutePath).collect(Collectors.toList()).forEach(imagePath->{
            executorService.execute(() -> {
                try (InputStream is = Files.newInputStream(Path.of(imagePath))) {
                    Image image = new Image(is);
                    imageCache.put(imagePath, image);
                } catch (IOException e) {
                    System.err.println("加载JavaFX Image失败: " + imagePath);
                    e.printStackTrace();
                }
            });
        });
        executorService.shutdown();
    }
    /**
     * 初始化缩略图栏
     */
    private void initThumbnailGallery(){
        thumbnailGalleryContainer.setOnMouseEntered(e->thumbnailGallery.setVisible(true));
        thumbnailGalleryContainer.setOnMouseExited(e->{
            thumbnailGallery.setVisible(false);
        });
        thumbnailGallery = new ThumbnailGallery(currentIndex.get());
        thumbnailGallery.setVisible(false);
        thumbnailGalleryContainer.getChildren().add(thumbnailGallery);
        thumbnailGallery.setThumbnailSize(60);
        if(imageUtilList.size()<=0) return;

        for (ImageUtil imageUtil : imageUtilList) {
            thumbnailGallery.addImage(imageUtil.getFile());
        }
        // 设置选中事件处理
        thumbnailGallery.setSelectionHandler(idx -> {
            try {
                currentIndex.set(idx);
                imageUtil = imageUtilList.get(idx);
                this.updateMainImageView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 刷新缩略图栏
     */
    private void flushThumbnailGallery() {
        thumbnailGallery.clear();
        for (ImageUtil imageUtil : imageUtilList) {
            thumbnailGallery.addImage(imageUtil.getFile());
        }
        thumbnailGallery.updateSelectedThumbnail(currentIndex.get());
    }
    /**
     * 调用第三方库压缩图像
     */
    @FXML
    private void compressImage(){
        CompressDialog.show(this.stage,this.imageUtil, result->{
            if(result){
                synchronize(null,imageUtil);
            }
        });
    }
    /**
     * 刷新幻灯片所有内容
     * 可能存在未知bug
     */
    public void flush(String oldImageAbsolutePath,ImageUtil newImageUtil){
        this.imageUtilList.clear();
        if(imageUtil.getAbsolutePath().equals(oldImageAbsolutePath))
        this.imageUtil = newImageUtil;
        File directory = imageUtil.getDirectory();
        File[] images = directory.listFiles(FileUtil::isSupportImageFormat);
        if (images != null) {
            for (File image : images) {
                ImageUtil imageFile = new ImageUtil(image);
                this.imageUtilList.add(imageFile);
            }
        }
        if(this.imageUtilList.isEmpty()){                       // 幻灯片无内容关闭窗口
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
                this.stage.close();
            }));
            timeline.play();
        }

        this.initImageListOrder(imageUtil);
        this.initButtonStatus();
        this.updateMainImageView();
        this.flushThumbnailGallery();
    }

    /**
     * 刷新被删除的幻灯片
     * @param oldPaths 被删除的图片路径
     */
    public void flush(List<String> oldPaths){
        int oldIndex = -1;
        for(String oldPath : oldPaths){
            if(imageUtil.getAbsolutePath().equals(oldPath)) {
                oldIndex = currentIndex.get();
            }
        }
        this.imageUtilList.clear();

        File directory = imageUtil.getDirectory();
        File[] images = directory.listFiles(FileUtil::isSupportImageFormat);
        if (images != null) {
            for (File image : images) {
                ImageUtil imageFile = new ImageUtil(image);
                this.imageUtilList.add(imageFile);
            }
        }
        if(this.imageUtilList.isEmpty()){                       // 幻灯片无内容关闭窗口
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
                this.stage.close();
            }));
            timeline.play();
        }else if(oldIndex != -1 && oldIndex>imageUtilList.size()-1){
            imageUtil = imageUtilList.getFirst();
        }else if(oldIndex !=-1){
            imageUtil = imageUtilList.get(oldIndex);
        }
        this.initImageListOrder(imageUtil);
        this.initButtonStatus();
        this.updateMainImageView();
        this.flushThumbnailGallery();
    }

    /**
     * 主窗口控制器
     */
    private WindowMainController mainController;

    /**
     * 按照主界面的排序规则初始化图像列表顺序,获得当前图片下标
     */
    private void initImageListOrder(ImageUtil imageUtil){
        if(mainController == null){
            mainController = (WindowMainController) ControllerMap.getController(WindowMainController.class);
            SortUtil.sortImageFile(this.imageUtilList,mainController.getSortOrder().getNowString());
        }else{
            SortUtil.sortImageFile(this.imageUtilList, SortOrder.ASC_SORT_BY_NAME);
        }
        for (int i = 0; i < imageUtilList.size(); i++) {
            if (imageUtilList.get(i).getAbsolutePath().equals(imageUtil.getAbsolutePath())) {
                currentIndex.set(i);
                break;
            }
        }
    }

    /**
     * 设置全屏监听器
     */
    private void setUpFullScreenListener(){
        stage.fullScreenProperty().addListener((obs,wasFullScreen,isNowFullScreen) -> {
            if(isNowFullScreen){
                this.setSlidePlayingStatus(false);
            }else{
                // 退出循环播放
                if(this.currentIndex.get()==-1){
                    this.currentIndex.set(this.imageUtilList.size()-1);
                }

                this.updateMainImageView();
                this.setSlidePlayingStatus(true);
                if(this.playingTimeLine!=null && this.zoomingTimeLine!=null){
                    this.playingTimeLine.stop();
                    this.zoomingTimeLine.stop();
                }
                // 退出全屏时的动画效果
                FadeTransition ft = new FadeTransition(Duration.millis(500), rootPane);
                ft.setFromValue(0.8);
                ft.setToValue(1.0);
                ft.play();
            }
        });

    }



    /**
     * 显示图片信息面板
     */
    @FXML
    private void showImageInfo(){
        if(ImageInfoWindow.getStage(imageUtil.getAbsolutePath())!=null){
            if(!ImageInfoWindow.removeStage(imageUtil.getAbsolutePath())){
                System.out.println("图像属性面板显示bug");
            }
        }else{
            ImageInfoWindow.main(this.imageUtil,340,250,this.stage);
        }
    }

    /**
     * 旋转变换类
     */
    private Rotate rotateTransform = new Rotate(0);
    /**
     * 图片旋转
     */
    @FXML
    private void rotate(){
        this.recoverTranslattion(50);
        rotateTransform.setAngle(rotateTransform.getAngle() + 90);
        this.updateCursor();
    }

    /**
     * 删除图片
     */
    @FXML
    private void delete(){
        DeleteDialog.show(this.stage,imageUtil,result->{
            if(result){
                if(ImageInfoWindow.getStage(imageUtil.getAbsolutePath())!=null) {
                    if (!ImageInfoWindow.removeStage(imageUtil.getAbsolutePath())) {
                        System.out.println("图像属性面板图片删除bug");
                    }
                }
                String oldImageAbsolutePath = imageUtil.getAbsolutePath();
                imageUtil = imageUtilList.get((currentIndex.get()+1)%imageUtilList.size());
                synchronize(oldImageAbsolutePath,imageUtil);
                showNotification("删除成功");
            }else{
                showNotification("删除失败,文件被占用或无权限");
            }
        });
    }


    /**
     * 幻灯片混合播放
     */
    @FXML
    public void playing(){
        stage.setFullScreen(true);
        this.playingTimeLine = new Timeline(new KeyFrame(Duration.millis(3000),e->{
            nextImage(e);
            // 循环播放
            if(this.currentIndex.get() == this.imageUtilList.size() - 1){
                this.currentIndex.set(-1);
            }
        }));
        this.zoomingTimeLine = new Timeline(new KeyFrame(Duration.millis(100),e->{
            if(this.MIN_SCALE<100 || this.isAboutToFitScreen(0.7)){
                this.zoom(1/1.01,null);
            }else{
                this.zoom(1.01,null);
            }
        }));

        mainImageView.getScene().addEventFilter(KeyEvent.KEY_PRESSED,e->{
            switch (e.getCode()){
                case ESCAPE -> {

                }
                case SPACE -> {
                    if(this.playingTimeLine.getStatus() == Animation.Status.PAUSED){
                        this.playingTimeLine.play();
                        this.zoomingTimeLine.play();
                    }else{
                        this.playingTimeLine.pause();
                        this.zoomingTimeLine.pause();
                    }
                }
            }

        });

        this.playingTimeLine.setCycleCount(Timeline.INDEFINITE);
        this.playingTimeLine.play();
        this.zoomingTimeLine.setCycleCount(Timeline.INDEFINITE);
        this.zoomingTimeLine.play();
    }

    /**
     * 判断原始图像是否在给定的影响因子条件下,接近适应屏幕大小
     * 例如 factor为0.8,则返回原始图像的宽高大于适应屏幕的0.8大小宽高时为真,表示原始图像很接近适应屏幕大小,否则返回假
     * @param factor 适应屏幕大小的比例
     * @return 结果
     */
    private boolean isAboutToFitScreen(double factor){
        return (originalWidth>=imagePane.getWidth()*factor) || (originalHeight>=imagePane.getHeight()*factor);
    }

    /**
     * 顶部左功能栏
     */
    @FXML
    private HBox topFunctionalColumn;

    /**
     * 顶部右窗口控制栏
     */
    @FXML
    private HBox windowControlColumn;

    /**
     * 设置幻灯片播放界面其他组件可见性
     * @param visible 可见性
     */
    private void setSlidePlayingStatus(boolean visible){
        this.secondaryPane.setVisible(visible);
        this.topFunctionalColumn.setVisible(visible);
        this.windowControlColumn.setVisible(visible);
        if(visible){
            AnchorPane.setTopAnchor(this.imagePane,35.0);
        }else{
            AnchorPane.setTopAnchor(this.imagePane,0.0);
        }
    }


    /**
     * 监听图片旋转中心
     */
    private void setUpRotationListener(){
        // 监听ImageView的尺寸变化，设置旋转中心点
        mainImageView.boundsInLocalProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.getWidth();
            double height = newVal.getHeight();
            if (width > 0 && height > 0) {
                rotateTransform.setPivotX(width / 2); // 中心点X坐标
                rotateTransform.setPivotY(height / 2); // 中心点Y坐标
            }
        });
    }

    /**
     * 初始化左右切换图片按钮禁用状态
     */
    private void initButtonStatus(){
        this.prePage.setDisable(this.currentIndex.get() <= 0);
        this.nextPage.setDisable(this.currentIndex.get() >= imageUtilList.size() - 1);
    }

    /**
     * 设置换页按钮禁用监听器
     */
    private void setUpPageButtonListener(){
        // 待同步功能实现再进一步完善
        this.currentIndex.addListener((obs,oldvalue,newvalue)->{
            if(newvalue.intValue()<=0){
                this.setMenuItemDisable(this.prePage,true);
            } else {
                this.setMenuItemDisable(this.prePage,false);
            }
            if (newvalue.intValue()>=imageUtilList.size()-1) {
                this.setMenuItemDisable(this.nextPage,true);
            }else{
                this.setMenuItemDisable(this.nextPage,false);
            }
        });
    }
    /**
     * 初始化并监听放缩比例及功能禁用状态
     */
    private void setUpZoomScaleListener(){
        this.scaleInteger.addListener((obs,oldvalue,newvalue)->{
            if(newvalue.intValue()<=MIN_SCALE){
                this.zoomOut.setDisable(true);
                if(this.MIN_SCALE<100){
                    this.displayMode = DisplayMode.FIT;
                    this.updateOriginalSacleStyle();
                }else{
                    this.displayMode = DisplayMode.ORIGINAL;
                    this.updateOriginalSacleStyle();
                }
            } else if (newvalue.intValue()>=MAX_SCALE) {
                this.zoomIn.setDisable(true);
            } else {
                this.zoomIn.setDisable(false);
                this.zoomOut.setDisable(false);
            }
            this.zoomScale.setText(newvalue.toString()+"%");
        });
    }

    /**
     * 更新图片自适应和原比例按钮样式
     */
    private void updateOriginalSacleStyle(){
        switch (displayMode) {
            case FIT->{
                this.originalScale.getTooltip().setText("原始比例 | Ctrl+O");
            }
            default->{
                this.originalScale.getTooltip().setText("适应窗口 | Ctrl+F");
            }
        }

    }

    /**
     * 设置按钮对应的菜单项的禁用状态
     * @param button 按钮
     * @param disable 是否禁用
     */
    private void setMenuItemDisable(Button button,boolean disable){
        button.setDisable(disable);
        for(MenuItem item: this.moreMenu.getItems()){
            if(item.getId().equals(button.getId())){
                item.setDisable(disable);
                break;
            }
        }
    }

    String oldImageAbsolutePath = null;
    /**
     * 更新当前窗口显示的图片及相关信息
     */
    private void updateMainImageView() {
        if(WindowSlideController.imageCache.containsKey(imageUtil.getAbsolutePath())){
            this.image = WindowSlideController.imageCache.get(imageUtil.getAbsolutePath());
        }else{
            this.image = new Image(imageUtil.getURL(),true);
            ProgressBar progressBar = new ProgressBar(0);
            imagePane.getChildren().add(progressBar);
            progressBar.progressProperty().bind(image.progressProperty()); // 绑定进度
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    imagePane.getChildren().remove(progressBar);
                    WindowSlideController.imageCache.put(imageUtil.getAbsolutePath(), image);
                    mainImageView.setImage(image);
                    originalWidth = image.getWidth();
                    originalHeight = image.getHeight();
                    ImageInfoWindow.updateImageInfo(oldImageAbsolutePath, imageUtil);
                    updateWindowInfo();
                    initImageScale();
                    updateCursor();
                    banCompressButton();
                    if(thumbnailGallery!=null) {
                        updateThumbnailGallery();
                    }
                }
            });
            // 设置图片加载失败的处理
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    System.err.println("加载失败: " + image.getException());
                    showNotification("图片加载失败");
                    // 1秒后关闭窗口
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.seconds(1), event -> this.stage.close())
                    );
                    timeline.play();
                }
            });
        }
        mainImageView.setImage(image);
        originalWidth = image.getWidth();
        originalHeight = image.getHeight();
        ImageInfoWindow.updateImageInfo(oldImageAbsolutePath, imageUtil);
        updateWindowInfo();
        initImageScale();
        updateCursor();
        banCompressButton();
        if(thumbnailGallery!=null) {
            updateThumbnailGallery();
        }
    }

    /**
     * 更新缩略图显示
     */
    private void updateThumbnailGallery(){
        thumbnailGallery.updateSelectedThumbnail(this.currentIndex.get());
    }

    /**
     * 禁用gif,bmp格式的图片压缩功能,未实现
     */
    private void banCompressButton(){
        if(".GIF".equals(imageUtil.getFileType())||".BMP".equals(imageUtil.getFileType())){
            this.compress.setDisable(true);
        }else{
            this.compress.setDisable(false);
        }
    }

    /**
     * 清空上次设置后遗留下的数据
     */
    private void clearImageTransforms(){
        this.mainImageView.setScaleX(1.0);
        this.mainImageView.setScaleY(1.0);
        this.mainImageView.setTranslateX(0);
        this.mainImageView.setTranslateY(0);
        this.mainImageView.getTransforms().clear();
        this.rotateTransform.setAngle(0);
        this.totalScale = 1.0;
    }

    /**
     * 图片自适应窗口大小更新放缩比例
     */
    private void initImageScale(){
        this.clearImageTransforms();
        mainImageView.getTransforms().add(rotateTransform);
        double paneWidth = imagePane.getWidth();
        double paneHeight = imagePane.getHeight();

        // 图片比面板大图片自适应窗口
        if(this.originalWidth>paneWidth || this.originalHeight>paneHeight){
            this.setDisplayMode(DisplayMode.FIT);
            this.MIN_SCALE = this.getAdaptedPercent(paneWidth, paneHeight);
        }else{  // 图片比面板小原始比例
            this.setDisplayMode(DisplayMode.ORIGINAL);
            this.MIN_SCALE = 100;
        }
        this.scaleInteger.set(this.MIN_SCALE);

        this.totalScale = 1.0;
    }

    /**
     * 更新图片显示大小并绑定
     */
    private void updateImageSize() {
        switch (displayMode) {
            case FIT -> {
                mainImageView.fitWidthProperty().bind(imagePane.widthProperty());
                mainImageView.fitHeightProperty().bind(imagePane.heightProperty());
            }
            case ORIGINAL -> {

                mainImageView.fitWidthProperty().bind(image.widthProperty());
                mainImageView.fitHeightProperty().bind(image.heightProperty());
            }
        }
    }

    /**
     * 设置图片显示模式
     * @param mode 显示模式
     */
    private void setDisplayMode(DisplayMode mode) {
        displayMode = mode;
        updateImageSize();
    }

    /**
     * 根据图片显示模式更新
     */
    @FXML
    private void updateOriginalScaleStatus(){
        switch(displayMode){
            case FIT -> {
                this.setOriginalScale();
                this.displayMode = DisplayMode.ORIGINAL;
            }
            default -> {
                this.adaptScene();
                this.displayMode = DisplayMode.FIT;
            }
        }
        this.updateOriginalSacleStyle();
    }

    /**
     * 图片适应窗口
     */
    private void adaptScene(){
        int adaptedPercent = this.getAdaptedPercent(imagePane.getWidth(), imagePane.getHeight());
        Timeline[] timelines = new Timeline[1];
        if(adaptedPercent>this.scaleInteger.get()){
            int curScale = this.MIN_SCALE;
            timelines[0] = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        zoomIn();
                        if(curScale * totalScale >= adaptedPercent){
                            timelines[0].stop();
                            zoomOut();
                        }
                    })
            );
            timelines[0].setCycleCount(Timeline.INDEFINITE);
            timelines[0].play();
        }else if(adaptedPercent<this.scaleInteger.get() && this.MIN_SCALE<100){
            int curScale = this.MIN_SCALE;
            timelines[0] = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        zoomOut();
                        if(this.scaleInteger.get()==this.MIN_SCALE){
                            timelines[0].stop();
                        }
                    })
            );
            timelines[0].setCycleCount(Timeline.INDEFINITE);
            timelines[0].play();
        }else{
            int curScale = this.MIN_SCALE;
            timelines[0] = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        zoomOut();
                        if(curScale * totalScale < adaptedPercent){
                            timelines[0].stop();
                        }
                    })
            );
            timelines[0].setCycleCount(Timeline.INDEFINITE);
            timelines[0].play();
        }

        this.scaleInteger.set(adaptedPercent);
    }

    /**
     * 恢复图片原始比例
     */
    private void setOriginalScale(){
        Timeline[] timelines = new Timeline[1];
        if(this.MIN_SCALE < 100 && this.scaleInteger.get() < 100){
            int curScale = this.MIN_SCALE;
            timelines[0] = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        zoomIn();
                        if(curScale * totalScale >= 100){
                            timelines[0].stop();
                        }
                    })
            );
            timelines[0].setCycleCount(Timeline.INDEFINITE);
            timelines[0].play();
        }else{
            int curScale = this.MIN_SCALE;
            timelines[0] = new Timeline(
                    new KeyFrame(Duration.millis(10), event -> {
                        zoomOut();
                        if(this.scaleInteger.get()==this.MIN_SCALE){
                            timelines[0].stop();
                        }
                    })
            );
            timelines[0].setCycleCount(Timeline.INDEFINITE);
            timelines[0].play();
        }
    }


    /**
     * 计算图片自适应窗口后的缩小比例
     * @param fitWidth imageview适应宽
     * @param fitHeight imageview适应高
     * @return 适应后的缩小比例
     */
    private int getAdaptedPercent(double fitWidth, double fitHeight) {
        return (int)(100 * Math.min(fitWidth / originalWidth, fitHeight / originalHeight));
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
    @FXML
    private void zoomIn() {
        this.zoom(this.zoomInFactor,null);
    }

    /**
     * 持续中心放大
     * @param event
     */
    @FXML
    private void zoomInConstantly(Event event){
        this.fireConstantly(e->this.zoomIn(),80);
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
    @FXML
    private void zoomOut() {
        this.zoom(this.zoomOutFactor,null);
    }

    /**
     * 持续中心缩小
     * @param event
     */
    @FXML
    private void zoomOutConstantly(Event event){
        this.fireConstantly(e->this.zoomOut(),80);
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
    @FXML
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
     * 更新窗口标题及图片相关信息
     */
    private void updateWindowInfo() {
        String str = this.imageUtil.getFileName();
        this.stage.setTitle(str);
        this.imageName.setText(str);
        this.updateControlTooltip(this.imageName,str,null);

        str = String.format("%d/%d",this.currentIndex.get()+1,this.imageUtilList.size());
        this.orderNum.setText(str);
        this.updateControlTooltip(this.orderNum,str,"图片索引:");

        str = FileUtil.getFormatFileSize(this.imageUtil.getSizeOfBytes());
        this.fileSize.setText(str);
        this.updateControlTooltip(this.fileSize,str,"图片大小:");

        str = String.format("%d×%d",(int)this.image.getWidth(),(int)this.image.getHeight());
        this.imageArea.setText(str);
        this.updateControlTooltip(this.imageArea,str,"图片尺寸:");

        // 图片比例
        this.zoomScale.setText(this.scaleInteger.getValue().toString()+"%");
        this.zoomOut.setDisable(this.scaleInteger.get() <= MIN_SCALE);
    }

    /**
     * 更新控制组件的提示词
     * @param control  控制组件
     * @param tooltipText   提示文本
     * @param extraStr  额外文本
     */
    private void updateControlTooltip(Control control, String tooltipText,String extraStr){
        if(control.getTooltip()!=null){
            if(extraStr!=null){
                control.getTooltip().setText(extraStr+" "+tooltipText);
            }else{
                control.getTooltip().setText(tooltipText);
            }
        }else {
            if(extraStr!=null){
                control.setTooltip(new Tooltip(extraStr+" "+tooltipText));
            }else{
                control.setTooltip(new Tooltip(tooltipText));
            }
            control.getTooltip().setShowDelay(Duration.millis(500));
        }
    }

    /**
     * 触发持续事件
     * @param handler 单点击触发事件处理逻辑
     * @param ms 间隔毫秒数
     */
    private void fireConstantly(EventHandler<ActionEvent> handler,double ms){
        if (this.timeline != null)
            this.timeline.stop();
        this.timeline = new Timeline(new KeyFrame(Duration.millis(ms),handler));
        this.timeline.setCycleCount(Timeline.INDEFINITE); // 无限循环
        this.timeline.play();
    }

    /**
     * 切换上一张图片
     * @param event
     */
    @FXML
    private void preImage(Event event) {
        oldImageAbsolutePath = imageUtil.getAbsolutePath();
        this.currentIndex.set(this.currentIndex.get()-1);
        if(this.currentIndex.get()<=0){
            this.currentIndex.set(0);
            this.stopTimerLine();
            this.showPageTip();
        }
        this.imageUtil = this.imageUtilList.get(this.currentIndex.get());
        this.updateMainImageView();
    }

    /**
     * 持续切换上一张图片
     * @param event
     */
    @FXML
    private void preImageConstantly(Event event) {
        this.fireConstantly(e->this.preImage(event),300);
    }
    /**
     * 切换下一张图片
     * @param event
     */
    @FXML
    private void nextImage(Event event) {
        oldImageAbsolutePath = imageUtil.getAbsolutePath();
        this.currentIndex.set(this.currentIndex.get()+1);
        if(this.currentIndex.get()>=this.imageUtilList.size()-1){
            this.currentIndex.set(imageUtilList.size()-1);
            this.stopTimerLine();
            this.showPageTip();
        }
        this.imageUtil = this.imageUtilList.get(this.currentIndex.get());
        this.updateMainImageView();
    }

    /**
     * 持续切换下一张图片
     * @param event
     */
    @FXML
    private void nextImageConstantly(Event event) {
        this.fireConstantly(e->this.nextImage(event),300);
    }
    /**
     * 停止持续事件的时间线
     */
    @FXML
    private void stopTimerLine() {
        if(this.timeline != null){
            this.timeline.stop();
        }
    }

    /**
     * 显示页面范围提示
     */
    private void showPageTip(){
        if(this.currentIndex.get()==0){
            Notifications.create()
                    .text("第一张")
                    .hideAfter(Duration.seconds(0.5))
                    .position(Pos.CENTER)
                    .owner(this.secondaryPane)
                    .darkStyle()
                    .show();

        } else if (this.currentIndex.get() == this.imageUtilList.size()-1) {
            Notifications.create()
                    .text("最后一张")
                    .hideAfter(Duration.seconds(0.5))
                    .position(Pos.CENTER)
                    .owner(this.secondaryPane)
                    .darkStyle()
                    .show();

        }
    }

    /**
     * 设置全局scene键盘事件
     */
    public void setUpKeyEvent(Scene scene){
        Platform.runLater(() -> {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                this.initShortcutKey(event);
            });
            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                this.stopTimerLine();
            });
        });

    }

    /**
     * 初始化快捷键方法
     * @param event
     */
    private void initShortcutKey(KeyEvent event){
        if(stage.isFullScreen()) {
            return;
        }

        KeyCode code = event.getCode();

        if (event.isControlDown()) {
            switch (code) {
                case O -> {
                    if(this.displayMode==DisplayMode.ORIGINAL){
                        return;
                    }
                    this.displayMode = DisplayMode.FIT;
                    this.updateOriginalScaleStatus();
                }
                case F -> {
                    if(this.displayMode==DisplayMode.FIT){
                        return;
                    }
                    this.displayMode = DisplayMode.ORIGINAL;
                    this.updateOriginalScaleStatus();
                }
                case D->{
                    this.delete();
                }
                case S->{
                    this.saveAs();
                }
                case R-> {
                    this.rotate();
                }
                case I->{
                    this.showImageInfo();
                }
                case P->{
                    this.playing();
                }
                case C->{
                    if(compress.isDisabled()) return;
                    this.compressImage();
                }
            }
        }else{
            switch(code){
                case LEFT -> {              //左方向键
                    if(this.currentIndex.get()<=0){
                        event.consume();
                    }else{
                        preImage(event);
                    }
                }
                case RIGHT -> {             //右方向键
                    if(this.currentIndex.get()>=imageUtilList.size()-1){
                        event.consume();
                    }else{
                        nextImage(event);
                    }
                }
                case OPEN_BRACKET -> {      //左中括号
                    if(this.scaleInteger.get()>=MAX_SCALE){
                        event.consume();
                    }else {
                        zoomIn();
                    }
                }
                case CLOSE_BRACKET -> {     //右中括号
                    if(this.scaleInteger.get()<=MIN_SCALE){
                        event.consume();
                    }else{
                        zoomOut();
                    }
                }
            }
        }

    }


    /**
     * 初始化图标url和提示文本映射关系
     */
    private void initMap(){
        urlMap.put("moreMenuButton", "/icon/MoreMenu.png");
        urlMap.put("rotate", "/icon/rotate.png");
        urlMap.put("delete", "/icon/ashbin.png");
        urlMap.put("prePage", "/icon/arrow-left-circle.png");
        urlMap.put("nextPage", "/icon/arrow-right-circle.png");
        urlMap.put("play", "/icon/play.png");
        urlMap.put("info", "/icon/prompt.png");
        urlMap.put("compress",  "/icon/compress.png");
        urlMap.put("zoomIn", "/icon/zoom-in.png");
        urlMap.put("zoomOut", "/icon/zoom-out.png");
        urlMap.put("originalScale","/icon/original-fit.png");
        urlMap.put("saveAs","/icon/saveAs.png");
        urlMap.put("crop","/icon/crop.png");
        textMap.put("moreMenuButton", "更多功能");
        textMap.put("rotate", "顺时针旋转90°|Ctrl+R");
        textMap.put("delete", "删除|Ctrl+D");
        textMap.put("prePage", "上一张|方向键⬅");
        textMap.put("nextPage", "下一张|方向键➡");
        textMap.put("play", "播放|Ctrl+P");
        textMap.put("info", "信息|Ctrl+I");
        textMap.put("compress",  "压缩|Ctrl+C");
        textMap.put("zoomIn", "放大|开括号[|Ctrl+滚轮⬆");
        textMap.put("zoomOut", "缩小|闭括号]|Ctrl+滚轮⬇");
        textMap.put("originalScale","原始比例");
        textMap.put("minBtn","最小化");
        textMap.put("maxBtn","最大化");
        textMap.put("closeBtn","关闭");
        textMap.put("saveAs","另存为|Ctrl+S");
        textMap.put("crop","裁剪");

        buttonMap.put("rotate",this.rotate );
        buttonMap.put("delete", this.delete );
        buttonMap.put("prePage",this.prePage );
        buttonMap.put("nextPage", this.nextPage);
        buttonMap.put("play", this.play);
        buttonMap.put("info", this.info);
        buttonMap.put("compress", this.compress);
    }

    /**
     * 初始化顶部功能按钮样式,添加鼠标悬停文本提示,
     */
    private void initButtonStyle(){
        Tooltip[] tooltips = {
                new Tooltip("更多功能"),
                new Tooltip("顺时针旋转90°|Ctrl+R"),
                new Tooltip("删除|Ctrl+D"),
                new Tooltip("上一张|方向键⬅"),
                new Tooltip("下一张|方向键➡"),
                new Tooltip("播放|Ctrl+P"),
                new Tooltip("信息|Ctrl+I"),
                new Tooltip("压缩|Ctrl+C"),
                new Tooltip( "放大|开括号[|Ctrl+滚轮⬆"),
                new Tooltip("缩小|闭括号]|Ctrl+滚轮⬇"),
                new Tooltip("原始比例"),
                new Tooltip("最小化"),
                new Tooltip("最大化"),
                new Tooltip("关闭"),
        };

        // 文本提示弹窗延时
        for(Tooltip tooltip : tooltips){
            tooltip.setShowDelay(Duration.millis(500));
        }

        ObservableList<Node> children = dynamicButtonContainer.getChildren();
        int size = children.size();
        for (int i=0; i < size; i++) {
            Button btn = (Button) children.get(i);
            String style = String.format(
                    "-fx-background-image: url('%s');-fx-background-size: 20,20",
                    getClass().getResource(urlMap.get(btn.getId())).toExternalForm()
            );

            btn.setStyle(style);
            btn.setTooltip(tooltips[i]);
        }

        // 基本固定不动的按钮,手动设置样式和文本提示
        zoomIn.setStyle(String.format(
                "-fx-background-image: url('%s');-fx-background-size: 20,20",
                getClass().getResource(urlMap.get("zoomIn")).toExternalForm()
        ));
        zoomIn.setTooltip(tooltips[8]);
        zoomOut.setStyle(String.format(
                "-fx-background-image: url('%s');-fx-background-size: 20,20",
                getClass().getResource(urlMap.get("zoomOut")).toExternalForm()
        ));
        zoomOut.setTooltip(tooltips[9]);
        originalScale.setStyle(String.format(
                "-fx-background-image: url('%s');-fx-background-size: 20,20",
                getClass().getResource(urlMap.get("originalScale")).toExternalForm()
        ));
        originalScale.setTooltip(tooltips[10]);
        minBtn.setTooltip(tooltips[11]);
        maxBtn.setTooltip(tooltips[12]);
        closeBtn.setTooltip(tooltips[13]);


    }



    // 创建菜单项
    private CustomMenuItem saveAsBtn  =createMenuItem("另存为|Ctrl+S","/icon/saveAs.png");
    private CustomMenuItem cropBtn  =createMenuItem("裁剪","/icon/crop.png");
    private ContextMenu moreMenu = new ContextMenu();
    /**
     * 菜单按钮初始化
     */
    private void initMoreMenuButton() {
        saveAsBtn.setId("saveAs");
        cropBtn.setId("crop");
        saveAsBtn.setOnAction(e->saveAs());
        cropBtn.setOnAction(e-> CropWindow.main(this.imageUtil));
        moreMenu.setAutoHide(true);
        moreMenu.setAutoFix(true);
        moreMenu.getItems().addAll(saveAsBtn, cropBtn);
        // 点击按钮切换菜单
        moreMenuButton.setOnAction(e -> toggleMenu());
    }

    private CustomMenuItem createMenuItem(String text, String iconPath) {
        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        Label label = new Label(text);
        HBox content = new HBox(2, icon, label);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        CustomMenuItem item = new CustomMenuItem(content);
        content.setPrefWidth(180);
        item.setHideOnClick(true);
        return item;
    }

    /**
     * 另存为功能
     */
    private void saveAs(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图像");
        fileChooser.setInitialDirectory(imageUtil.getDirectory());
        fileChooser.setInitialFileName(imageUtil.getFileName().substring(0,imageUtil.getFileName().lastIndexOf("."))+"-副本");
        // 设置支持的格式过滤器
        this.setTypeFilter(fileChooser);
        File file = fileChooser.showSaveDialog(this.stage);

        if (file != null) {
            // gif图像单独处理
            if(file.getName().endsWith("gif")){
                if(WindowCropController.cropGIFImage(0,0,(int)image.getWidth(),(int)image.getHeight(),imageUtil.getDirectory().getAbsolutePath(),imageUtil)){
                    synchronize(null,new ImageUtil(file));
                    this.showNotification("保存成功");
                }
                return;
            }
            try {
                String format = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                if (format != null) {
                    if(ImageIO.write(SwingFXUtils.fromFXImage(mainImageView.getImage(), null), format, file)){
                        synchronize(null,new ImageUtil(file));
                        this.showNotification("保存成功");
                    }else{
                        this.showNotification("保存失败");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 主界面与幻灯片窗口同步
     * @param newImageUtil 幻灯片所在目录下的任意图片工具包
     */
    private void synchronize(String oldPath,ImageUtil newImageUtil){
        WindowMainController controller = (WindowMainController) ControllerMap.getController(WindowMainController.class);
        controller.flushImage();
        SlideWindow.flushSlideWindows(oldPath,newImageUtil);
    }
    /**
     * 显示通知消息并同步刷新
     */
    private void showNotification(String message) {
        Notifications.create()
                .text(message)
                .hideAfter(Duration.seconds(1))
                .position(Pos.CENTER)
                .owner(this.stage)
                .darkStyle()
                .show();
    }

    /**
     * 设置文件过滤器
     * @param fileChooser 文件选择器
     */
    private void setTypeFilter(FileChooser fileChooser){
       String format = imageUtil.getFileType().substring(1).toLowerCase();
        switch (format){
            case "jpg","jpeg" -> fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JPEG 图像", "*.jpg", "*.jpeg"));
            case "png" -> fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG 图像", "*.png"));
            case "bmp" -> fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BMP 图像", "*.bmp"));
            case "gif" -> fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GIF 图像", "*.gif"));
       }
    }

    /**
     * 在菜单按钮下方打开菜单
     */
    private void toggleMenu() {
        if (moreMenu.isShowing()) {
            moreMenu.hide();
        } else {
            // 计算按钮的屏幕坐标
            Bounds bounds = moreMenuButton.localToScreen(moreMenuButton.getBoundsInLocal());
            // 设置菜单位置（在按钮下方显示）
            moreMenu.show(moreMenuButton, bounds.getMinX(), bounds.getMaxY());

        }
    }


    // TODO 有精力再改进顶部信息栏地动态显示

    /**
     * 设置顶部功能按钮栏宽度监听器,用于动态显示功能按钮
     */
    private void setUpDynamicButtonContainerListener(){
        dynamicButtonContainer.widthProperty().addListener((observableValue, oldValue, newValue) -> {this.adjustVisibleButtons();});
    }


    // TODO 修复BUG动态按钮被隐藏的时候再按全屏会出现顺序打乱的bug,改进算法,后续支持增加菜单按钮的默认按钮

    /**
     * 计算当前宽度下可以显示的按钮数量显示出来,多的隐藏到更多功能菜单里,后续能显示了再恢复出来
     */
    private void adjustVisibleButtons()  {
        // 最多显示按钮数量
        int MAX_BUTTON_NUM = 7;

        // 计算可用宽度（容器宽度 - 菜单按钮宽度 - 边距-左右填充）
        double availableWidth = dynamicButtonContainer.getWidth()
                - moreMenuButton.getWidth()
                - dynamicButtonContainer.getPadding().getLeft()
                - dynamicButtonContainer.getPadding().getRight()
                - dynamicButtonContainer.getSpacing();
        // 每个按钮显示需要的宽度
        double reqiredWidth =moreMenuButton.getPrefWidth() + dynamicButtonContainer.getSpacing();
        List<Button> visibleButtons = new ArrayList<>();    // 放可以显示的按钮
        List<Button> hiddenButtons = new ArrayList<>(); // 放需要隐藏的按钮
        List<Button> preButtons = new ArrayList<>(); // 放初始时从菜单里转出来的按钮
        double usedWidth = 0;

        List<Node> actionButtons = dynamicButtonContainer.getChildren().subList(1, dynamicButtonContainer.getChildren().size());

        // 先把更多功能菜单的菜单项导出来转成按钮,再进行后续处理
        ObservableList<MenuItem> items = moreMenu.getItems();

        if(items != null) {
            for (MenuItem item : items) {
                String id = item.getId();
                Button btn = new Button();
                btn.setId(id);
                btn.setOnAction(item.getOnAction());
                preButtons.add(btn);
            }
            items.clear();
        }

        // 计算可见按钮
        for (Node btn : actionButtons) {
            if (usedWidth + reqiredWidth <= availableWidth) {
                usedWidth += reqiredWidth ;
                visibleButtons.add(((Button)(btn)));
            } else {
                hiddenButtons.add(((Button)(btn)));
            }
        }

        // 如果可以显示更多按钮则从菜单项拉出来的按钮再添加进去
        if (usedWidth < availableWidth && visibleButtons.size() < MAX_BUTTON_NUM && !preButtons.isEmpty()) {
            actionButtons.clear();
            actionButtons.addAll(preButtons);

            for (Node btn : actionButtons) {
                if (usedWidth + reqiredWidth <= availableWidth && visibleButtons.size() < MAX_BUTTON_NUM) {
                    usedWidth += reqiredWidth;
                    visibleButtons.add(((Button) (btn)));
                    preButtons.remove(((Button) (btn)));
                }else{
                    break;
                }
            }

        }

        // 更新容器显示
        dynamicButtonContainer.getChildren().clear();
        dynamicButtonContainer.getChildren().add(moreMenuButton);
        for(Button btn : visibleButtons){
            String id = btn.getId();
            dynamicButtonContainer.getChildren().add(buttonMap.get(id));
        }


        // 更新弹出菜单内容
        for(Button btn : hiddenButtons) {
            String id = btn.getId();
            CustomMenuItem item = this.createMenuItem(textMap.get(id),urlMap.get(id));
            item.setId(id);
            item.setOnAction(btn.getOnAction());
            if(buttonMap.get(id)!=null&&buttonMap.get(id).isDisabled()){
                item.setDisable(true);
            }
            items.add(item);
        }

        for (Button btn : preButtons) {
            String id = btn.getId();
            CustomMenuItem item = this.createMenuItem(textMap.get(id),urlMap.get(id));
            item.setId(id);
            item.setOnAction(btn.getOnAction());
            if(buttonMap.get(id)!=null&&buttonMap.get(id).isDisabled()){
                item.setDisable(true);
            }
            items.add(item);
        }

    }

    /**
     *  窗口控制逻辑
     */
    private void setUpWindowControls() {
        this.setUpResizeListeners();
        maxBtn.getStyleClass().add("maxBtn-full");
        // 窗口控制按钮事件
        closeBtn.setOnAction(e -> {
            SlideWindow.removeSlideWindowController(imageUtil.getDirectory().getAbsolutePath(), this);
            stage.close();
        });
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
     * 窗口拖拽时的初始横坐标
     */
    private double startX;
    /**
     * 窗口拖拽时的初始纵坐标
     */
    private double startY;
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
     * 根面板,锚定其他组件
     */
    @FXML
    public AnchorPane rootPane;

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
     * 记录鼠标初始坐标,窗口初始大小
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
                handleLeftResize(deltaX);
                handleTopResize(deltaY);
                break;
            case RIGHT_TOP:
                handleRightResize(deltaX);
                handleTopResize(deltaY);
                break;
            case LEFT_BOTTOM:
                handleLeftResize(deltaX);
                handleBottomResize(deltaY);
                break;
            case RIGHT_BOTTOM:
                handleRightResize(deltaX);
                handleBottomResize(deltaY);
                break;
        }
        event.consume();
    }

    /**
     * 原子调节操作,窗口在左边区域放缩
     * @param deltaX x坐标偏移量
     */
    private void handleLeftResize(double deltaX) {
        double newWidth = startWidth - deltaX;
        if (newWidth > stage.getMinWidth()) {
            stage.setWidth(newWidth);
            stage.setX(startX + deltaX);
        }
    }

    /**
     * 原子调节操作,窗口在右边区域放缩
     * @param deltaX x坐标偏移量
     */
    private void handleRightResize(double deltaX) {
        double newWidth = startWidth + deltaX;
        if (newWidth > stage.getMinWidth()) {
            stage.setWidth(newWidth);
        }
    }

    /**
     * 原子调节操作,窗口在顶部区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleTopResize(double deltaY) {
        double newHeight = startHeight - deltaY;
        if (newHeight > stage.getMinHeight()) {
            stage.setHeight(newHeight);
            stage.setY(startY + deltaY);
        }
    }

    /**
     * 原子调节操作,窗口在底部区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleBottomResize(double deltaY) {
        double newHeight = startHeight + deltaY;
        if (newHeight > stage.getMinHeight()) {
            stage.setHeight(newHeight);
        }
    }

    /**
     * 调节方向枚举值
     */
    private enum ResizeDirection {
        LEFT, RIGHT, TOP, BOTTOM,
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }


    private double xOffset = 0;
    private double yOffset = 0;
    /**
     * 鼠标拖拽窗口事件处理,记录鼠标初始位置
     */
    @FXML
    private void handleMousePressed(MouseEvent event) {
        // 仅当点击在空白区域时记录坐标
        if (event.getTarget() instanceof HBox) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        }
    }

    /**
     * 鼠标拖拽窗口事件处理,窗口跟随鼠标拖拽位置
     * @param event
     */
    @FXML
    private void handleMouseDragged(MouseEvent event) {
        // 仅当初始点击在顶部功能栏上时执行拖拽
        if (event.getTarget() instanceof HBox) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
}


