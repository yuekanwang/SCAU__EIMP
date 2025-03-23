package com.eimp.controller;
import com.eimp.SlideWindow;
import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.*;

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
    @FXML private HBox thumbnailContainer;
    @FXML private Button rotate;
    @FXML private Button delete;
    @FXML private Button prePage;
    @FXML private Button nextPage;
    @FXML private Button play;
    @FXML private Button info;
    @FXML private Button share;
    /**
     * 图片缩小按钮
     */
    @FXML private Button zoomIn;
    /**
     * 图片放大按钮
     */
    @FXML private Button zoomOut;
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
    private List<ImageUtil> imageUtilList = new ArrayList<>();
    /**
     * 当前图片索引
     */
    private int currentIndex = 0;
    /**
     * 主窗口控制器
     */
    private WindowMainController windowMainController;
    /**
     * 用于控制持续触发事件的时间线
     */
    private Timeline timeline;
    /**
     * 图片放大比例
     */
    private int scale = 100;
    /**
     * 最大比例
     */
    private final static int MAX_SCALE = 1000;
    /**
     * 最小比例
     */
    private final static int MIN_SCALE = 100;
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
        this.initImageView();
        this.initMoreMenuButton();
        this.setUpDynamicButtonContainerListener();
        this.initButtonStyle();
        this.setUpWindowControls();
        this.secondaryPane.setVisible(false);// 暂时隐藏缩略图栏,待实现
    }

    /**
     * 导入图片及所在文件夹
     * @param imageUtil 导入的图片信息工具
     */
    public void importImage(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;

        File directory = imageUtil.getDirectory();
        File[] images = directory.listFiles(FileUtil::isSupportImageFormat);
        if (images != null) {
            for (File image : images) {
                ImageUtil imageFile = new ImageUtil(image);
                this.imageUtilList.add(imageFile);
            }
        }

        this.image = new Image(imageUtil.getURL().toString());
        this.updateMainImageView();
    }

    /**
     * 更新当前窗口显示的图片及相关信息
     */
    private void updateMainImageView() {
        this.updateWindowInfo();
        this.mainImageView.setImage(this.image);
    }


    /**
     * 更新窗口标题及图片相关信息
     */
    private void updateWindowInfo() {
        String str = this.imageUtil.getFileName();
        this.stage.setTitle(str);
        this.imageName.setText(str);
        this.updateControlTooltip(this.imageName,str,null);

        str = String.format("%d/%d",this.currentIndex+1,this.imageUtilList.size());
        this.orderNum.setText(str);
        this.updateControlTooltip(this.orderNum,str,"图片索引:");

        str = FileUtil.getFormatFileSize(this.imageUtil.getSizeOfBytes());
        this.fileSize.setText(str);
        this.updateControlTooltip(this.fileSize,str,"图片大小:");

        str = String.format("%d×%d",(int)this.image.getWidth(),(int)this.image.getHeight());
        this.imageArea.setText(str);
        this.updateControlTooltip(this.imageArea,str,"图片尺寸:");
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
            control.getTooltip().setShowDelay(javafx.util.Duration.millis(500));
        }
    }
    /**
     * 图片显示自适应窗口大小
     */
    private void initImageView(){
        // 绑定ImageView尺寸到StackPane可用空间
        mainImageView.fitWidthProperty().bind(
                ((StackPane)mainImageView.getParent()).widthProperty()
        );
        mainImageView.fitHeightProperty().bind(
                ((StackPane)mainImageView.getParent()).heightProperty()
        );

    }


    /**
     * 切换上一张图片
     * @param event
     */
    @FXML
    private void preImage(Event event) {
        this.currentIndex--;
        if(this.currentIndex<0){
            this.currentIndex=0;
            this.stopTimerLine(event);
        }
        this.imageUtil = this.imageUtilList.get(this.currentIndex);
        this.image = new Image(imageUtil.getURL().toString());
        this.updateMainImageView();
    }

    /**
     * 持续切换上一张图片
     * @param event
     */
    @FXML
    private void preImageConstantly(Event event) {
        if (this.timeline != null)
            this.timeline.stop();
        this.timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            preImage(event);
        }));
        this.timeline.setCycleCount(Timeline.INDEFINITE); // 无限循环
        this.timeline.play();
    }
    /**
     * 切换下一张图片
     * @param event
     */
    @FXML
    private void nextImage(Event event) {
        this.currentIndex++;
        if(this.currentIndex>=this.imageUtilList.size()){
            this.currentIndex=this.imageUtilList.size()-1;
            this.stopTimerLine(event);
        }
        this.imageUtil = this.imageUtilList.get(this.currentIndex);
        this.image = new Image(imageUtil.getURL().toString());
        this.updateMainImageView();
    }

    /**
     * 持续切换下一张图片
     * @param event
     */
    @FXML
    private void nextImageConstantly(Event event) {
        if (this.timeline != null)
            this.timeline.stop();
        this.timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            nextImage(event);
        }));
        this.timeline.setCycleCount(Timeline.INDEFINITE); // 无限循环
        this.timeline.play();
    }
    /**
     * 停止持续事件的时间线
     * @param event
     */
    @FXML
    private void stopTimerLine(Event event) {
        if(this.timeline != null){
            this.timeline.stop();
        }
    }





//    private static final int MAX_VISIBLE_THUMBNAILS = 10;
//    private static final double THUMBNAIL_WIDTH = 120;
//
//    // 添加缩略图的方法
//    private void addThumbnail(Image image) {
//        ImageView thumbnail = new ImageView(image);
//        thumbnail.setFitHeight(90);
//        thumbnail.setPreserveRatio(true);
//        thumbnailContainer.getChildren().add(thumbnail);
//
//        // 当超过10个时启用固定宽度
//        if (thumbnailContainer.getChildren().size() > MAX_VISIBLE_THUMBNAILS) {
//            thumbnailContainer.setPrefWidth(MAX_VISIBLE_THUMBNAILS * THUMBNAIL_WIDTH);
//        }
//    }



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
        urlMap.put("share",  "/icon/share.png");
        urlMap.put("zoomIn", "/icon/zoom-in.png");
        urlMap.put("zoomOut", "/icon/zoom-out.png");
        urlMap.put("originalScale","/icon/original.png");
        urlMap.put("item1","/icon/MoreMenu.png");
        urlMap.put("item2","/icon/MoreMenu.png");
        urlMap.put("item3","/icon/MoreMenu.png");
        textMap.put("moreMenuButton", "更多功能");
        textMap.put("rotate", "顺时针旋转90°");
        textMap.put("delete", "删除");
        textMap.put("prePage", "上一张");
        textMap.put("nextPage", "下一张");
        textMap.put("play", "播放");
        textMap.put("info", "信息");
        textMap.put("share",  "转发");
        textMap.put("zoomIn", "缩小");
        textMap.put("zoomOut", "放大");
        textMap.put("originalScale","原始比例");
        textMap.put("minBtn","最小化");
        textMap.put("maxBtn","最大化");
        textMap.put("closeBtn","关闭");
        textMap.put("item1","选项1");
        textMap.put("item2","选项2");
        textMap.put("item3","选项3");
        buttonMap.put("rotate",this.rotate );
        buttonMap.put("delete", this.delete );
        buttonMap.put("prePage",this.prePage );
        buttonMap.put("nextPage", this.nextPage);
        buttonMap.put("play", this.play);
        buttonMap.put("info", this.info);
        buttonMap.put("share", this.share );
    }

    /**
     * 初始化顶部功能按钮样式,添加鼠标悬停文本提示,
     */
    private void initButtonStyle(){
        Tooltip[] tooltips = {
                new Tooltip("更多功能"),
                new Tooltip("顺时针旋转90°"),
                new Tooltip("删除"),
                new Tooltip("上一张"),
                new Tooltip("下一张"),
                new Tooltip("播放"),
                new Tooltip("信息"),
                new Tooltip("转发"),
                new Tooltip("缩小"),
                new Tooltip("放大"),
                new Tooltip("原始比例"),
                new Tooltip("最小化"),
                new Tooltip("最大化"),
                new Tooltip("关闭"),
        };

        // 文本提示弹窗延时
        for(Tooltip tooltip : tooltips){
            tooltip.setShowDelay(javafx.util.Duration.millis(500));
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



    // 创建菜单项
    private CustomMenuItem item1  =createMenuItem("选项1","/icon/prompt.png");
    private CustomMenuItem item2  =createMenuItem("选项2","/icon/prompt.png");
    private CustomMenuItem item3  =createMenuItem("选项3","/icon/prompt.png");
    private ContextMenu moreMenu = new ContextMenu();
    /**
     * 菜单按钮初始化
     */
    private void initMoreMenuButton() {
        item1.setId("item1");
        item2.setId("item2");
        item3.setId("item3");
        moreMenu.setAutoHide(true);
        moreMenu.setAutoFix(true);
        moreMenu.getItems().addAll(item1, item2, item3);
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
        item.setHideOnClick(true);
        return item;
    }

    /**
     *  窗口控制逻辑
     */
    private void setUpWindowControls() {
        this.setUpResizeListeners();
        maxBtn.getStyleClass().add("maxBtn-full");
        // 窗口控制按钮事件
        closeBtn.setOnAction(e -> stage.close());
        minBtn.setOnAction(e -> stage.setIconified(true));
        maxBtn.setOnAction(e -> toggleMaximize());
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
            items.add(item);
        }

        for (Button btn : preButtons) {
            String id = btn.getId();
            CustomMenuItem item = this.createMenuItem(textMap.get(id),urlMap.get(id));
            item.setId(id);
            item.setOnAction(btn.getOnAction());
            items.add(item);
        }

    }

    /**
     * 窗口大小拖拽放缩逻辑
     */

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
    private AnchorPane rootPane;

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


