package com.eimp.Controller;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.net.URL;
import java.util.*;

public class WindowSlideController implements Initializable {

    private Stage stage;

    @FXML private ImageView mainImageView;
    @FXML private HBox thumbnailContainer;
    @FXML private Button zoomIn;
    @FXML private Button zoomOut;
    @FXML private Button originalScale;
    @FXML private Button closeBtn;
    @FXML private Button minBtn;
    @FXML private Button maxBtn;
    private URL location;
    private ResourceBundle resources;
    @FXML private HBox dynamicButtonContainer;
    private Map<String,String> urlMap = new HashMap<>();
    private Map<String,String> textMap = new HashMap<>();
    @FXML
    private Button moreMenuButton;

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
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.location = location;
        this.resources = resources;
        this.initMap();

        // 图片尺寸绑定
        // 绑定ImageView尺寸到StackPane可用空间
        mainImageView.fitWidthProperty().bind(
                ((StackPane)mainImageView.getParent()).widthProperty()
        );
        mainImageView.fitHeightProperty().bind(
                ((StackPane)mainImageView.getParent()).heightProperty()
        );


        this.initMoreMenuButton();
        this.setUpDynamicButtonContainerListener();


    }

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

    // 延迟初始化方法，在场景加载完成后调用
    public void notifyPreloader() {
        // 获取当前Stage
        stage = (Stage) closeBtn.getScene().getWindow();
        // 设置窗口最小尺寸
        stage.setMinWidth(590);
        stage.setMinHeight(580);

        this.initButtonStyle();
        this.setupWindowControls();
        this.adjustVisibleButtons();
    }
    // 窗口最大化切换
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
    
    private void setUpDynamicButtonContainerListener(){
        dynamicButtonContainer.widthProperty().addListener((observableValue, oldValue, newValue) -> {this.adjustVisibleButtons();});
    }




    // TODO 修复BUG动态按钮被隐藏的时候再按全屏会出现顺序打乱的bug,改进算法,后续支持增加菜单按钮的默认按钮
     private void adjustVisibleButtons()  {
        int MAX_BUTTON_NUM = 7;

        // 计算可用宽度（容器宽度 - 菜单按钮宽度 - 边距）
        double availableWidth = dynamicButtonContainer.getWidth()
                - moreMenuButton.getWidth()
                - dynamicButtonContainer.getPadding().getLeft()
                - dynamicButtonContainer.getPadding().getRight()
                - dynamicButtonContainer.getSpacing();

        double reqiredWidth =moreMenuButton.getPrefWidth() + dynamicButtonContainer.getSpacing();
        List<Button> visibleButtons = new ArrayList<>();
        List<Button> hiddenButtons = new ArrayList<>();
        List<Button> preButtons = new ArrayList<>();
        double usedWidth = 0;

        List<Node> actionButtons = dynamicButtonContainer.getChildren().subList(1, dynamicButtonContainer.getChildren().size());

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

        for(Button btn : visibleButtons){
            String id = btn.getId();
            btn.getStyleClass().add("funtionalButton");

            String style = String.format(
                    "-fx-background-image: url('%s');-fx-background-size: 20,20",
                    getClass().getResource(urlMap.get(id)).toExternalForm()
            );
            btn.setStyle(style);
            btn.setTooltip(new Tooltip(textMap.get(id)));
            btn.getTooltip().setShowDelay(javafx.util.Duration.millis(500));
        }
        // 更新容器显示
        dynamicButtonContainer.getChildren().clear();
        dynamicButtonContainer.getChildren().add(moreMenuButton);
        dynamicButtonContainer.getChildren().addAll(visibleButtons);

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

    // TODO 菜单显示



    /**
     * 窗口大小拖拽放缩类
     */
//    private Stage stage;
//    private double xOffset = 0;
//    private double yOffset = 0;
//    private double resizeDelta = 5;
    private double startX, startY;
    private double startWidth, startHeight;

    @FXML
    private Region topResize;
    @FXML
    private Region bottomResize;
    @FXML
    private Region leftResize;
    @FXML
    private Region rightResize;
    @FXML
    private Region leftTopResize;
    @FXML
    private Region rightTopResize;
    @FXML
    private Region leftBottomResize;
    @FXML
    private Region rightBottomResize;

    @FXML
    private AnchorPane rootPane;


    private void setupResizeListeners() {
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

    private void setupResizeHandler(Region region, ResizeDirection direction) {
        region.setOnMousePressed(event -> startResize(event));
        region.setOnMouseDragged(event -> handleResize(event, direction));
    }

    private void startResize(MouseEvent event) {
        startX = event.getScreenX();
        startY = event.getScreenY();
        startWidth = stage.getWidth();
        startHeight = stage.getHeight();
        event.consume();
    }

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

    private void handleLeftResize(double deltaX) {
        double newWidth = startWidth - deltaX;
        if (newWidth > stage.getMinWidth()) {
            stage.setWidth(newWidth);
            stage.setX(startX + deltaX);
        }
    }

    private void handleRightResize(double deltaX) {
        double newWidth = startWidth + deltaX;
        if (newWidth > stage.getMinWidth()) {
            stage.setWidth(newWidth);
        }
    }

    private void handleTopResize(double deltaY) {
        double newHeight = startHeight - deltaY;
        if (newHeight > stage.getMinHeight()) {
            stage.setHeight(newHeight);
            stage.setY(startY + deltaY);
        }
    }

    private void handleBottomResize(double deltaY) {
        double newHeight = startHeight + deltaY;
        if (newHeight > stage.getMinHeight()) {
            stage.setHeight(newHeight);
        }
    }


    private enum ResizeDirection {
        LEFT, RIGHT, TOP, BOTTOM,
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }
    private double xOffset = 0;
    private double yOffset = 0;
    // 鼠标拖拽事件处理
    @FXML
    private void handleMousePressed(MouseEvent event) {
        // 仅当点击在空白区域时记录坐标
        if (event.getTarget() instanceof HBox) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        }
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        // 仅当初始点击在顶部功能栏上时执行拖拽
        if (event.getTarget() instanceof HBox) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    // 图片切换方法
    @FXML
    private void prevImage(ActionEvent event) {
        // 加载前一张图片逻辑
    }

    @FXML
    private void nextImage(ActionEvent event) {
        // 加载下一张图片逻辑
    }






    // 窗口控制逻辑（同前）
    private void setupWindowControls() {
        setupResizeListeners();
        maxBtn.getStyleClass().add("maxBtn-full");
        // 窗口控制按钮事件
        closeBtn.setOnAction(e -> stage.close());
        minBtn.setOnAction(e -> stage.setIconified(true));
        maxBtn.setOnAction(e -> toggleMaximize());
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




}


