package com.eimp.controller;

import com.eimp.App;
import com.eimp.component.DirectoryLoader;
import com.eimp.component.FileTreeItem;
import com.eimp.component.PreviewFlowPane;

import com.eimp.component.ThumbnailPanel;
import com.eimp.util.ImageUtil;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class WindowMainController implements Initializable {

    private Stage stage;

    @FXML
    public AnchorPane top;
    @FXML
    public AnchorPane mid;
    @FXML
    public AnchorPane buttom;//这些AnchorPane是布局框
    @FXML
    public TreeView<String> treeView;//树视图，用于做目录树
    @FXML
    public ScrollPane imagePreviewScrollPane;//滚动面板，能起到滚轮条的作用
    @FXML
    public Pane imagePreviewPane;//用于显示缩略图的面板
    @FXML
    public Label imageSize_Label;//左下次角显示图片大小信息

    // 树形目录占位符
    public static final String HOLDER_TEXT = "Loading...";

    // 缩略图展示面板
    public PreviewFlowPane previewFlowPane;

    /*这些是按钮*/
    @FXML
    public Button minBtn;//窗口最小化按钮
    @FXML
    public Button maxBtn;//窗口最大化按钮
    @FXML
    public Button closeBtn;//窗口关闭按钮
    @FXML
    public Button About_Button;//有下角“关于”
    
    @FXML
    public Button SelectAll_Button;//全选按钮
    @FXML
    public Button Delete_Button;//删除按钮
    @FXML
    public Button Flushed_Button;//刷新按钮
    @FXML
    public Button Slideshow_Button;//幻灯片播放按钮
    @FXML
    public Button Help_Button;//帮助按钮

    /*这些是主界面的边界控制*/
    @FXML
    public Region leftResize;
    @FXML
    public Region topResize;
    @FXML
    public Region rightResize;
    @FXML
    public Region bottomResize;
    @FXML
    public Region leftTopResize;
    @FXML
    public Region rightTopResize;
    @FXML
    public Region leftBottomResize;
    @FXML
    public Region rightBottomResize;

    private Rectangle rectangle;

    @FXML//这些都是排序功能的
    public MenuButton SortMenu;//排序菜单
    @FXML
    public MenuItem SortOrder_Time;//按时间倒序
    @FXML
    public MenuItem SortOrder_Size;//按大小倒序
    @FXML
    public MenuItem SortOrder_Name;//按名字倒序
    @FXML
    public MenuItem Sort_Time;//按时间顺序
    @FXML
    public MenuItem Sort_SIze;//按大小顺序
    @FXML
    public MenuItem Sort_Name;//按名字顺序

    @FXML//顶部的部分
    public Button Left_Button;//路径后退按钮
    @FXML
    public Button Right_Button;//路径前进按钮
    @FXML
    public TextField File_URL;//文件路径条
    @FXML
    public TextField Search_Path;//搜索条
    @FXML
    public Button SearchButton;//搜索键



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = App.getStage();//这里的stage写上App里的stage
        this.setUpWindowControls();//调用窗口控制函数
        initFileTreeView();
        initPreviewPane();
    }

    /**
     * 初始化目录树
     *
     * @author Cyberangel2023
     */
    private void initFileTreeView() {
        // 设置懒加载
        treeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> stringTreeView) {
                return new TreeCell<>() {
                    // 重写更新方法
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else if (item == null && getTreeItem().getParent() instanceof FileTreeItem) {
                            setText(HOLDER_TEXT);
                        } else {
                            setText(item);
                            setGraphic(getTreeItem().getGraphic());
                        }
                    }
                };
            }
        });
        TreeItem<String> treeItem = new TreeItem<>();

        // 读取文件系统的根目录 -- 桌面目录
        File root = FileSystemView.getFileSystemView().getRoots()[0];
        // 读取文件
        File[] allFiles = root.listFiles();
        // 读取目录
        File[] directorFiles = root.listFiles(File::isDirectory);
        List<File> list = null;
        if (allFiles != null) {
            list = new ArrayList<>(Arrays.asList(allFiles));
        }
        // 过滤桌面的文件夹
        if (directorFiles != null) {
            if (list != null) {
                list.removeAll(Arrays.asList(directorFiles));
            }
        }
        if (list != null) {
            for (File file : list) {
                //过滤桌面的文件及快捷方式
                if (file.isDirectory() && !file.getName().endsWith("lnk")) {
                    treeItem.getChildren().add(new FileTreeItem(file, new DirectoryLoader(file)));
                }
            }
        }

        // 更新图片预览面板
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof FileTreeItem) {
                previewFlowPane.update(((FileTreeItem) newValue).getDirectory());
                File_URL.setText(((FileTreeItem) newValue).getDirectory().getAbsolutePath());
            }
        });

        File_URL.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                goPath();
            }
        });

        treeView.setRoot(treeItem);
        treeView.setShowRoot(false);
    }

    /**
     * 导航到指定路径
     *
     * @author Cyberangel2023
     */
    @FXML
    private void goPath() {
        String path = File_URL.getText();
        if (path == null) {
            return;
        }

        System.out.println(path);
        File file = new File(path);
        previewFlowPane.update(file);
    }

    /**
     * 初始化缩略图面板
     *
     * @author Cyberangel2023
     */
    public void initPreviewPane() {
        previewFlowPane = new PreviewFlowPane();
        rectangle = new Rectangle();
        rectangle.setFill(Color.rgb(70, 170, 227, 0.6));
        rectangle.setVisible(false);
        imagePreviewPane.getChildren().setAll(previewFlowPane, rectangle);
        addHandle();

        previewFlowPane.heightProperty()
                .addListener((observable, oldValue, newValue) -> imagePreviewPane.setMinHeight(previewFlowPane.getHeight()));

        imagePreviewScrollPane.viewportBoundsProperty()
                .addListener((observable, oldValue, newValue) -> {
                    previewFlowPane.setPrefWidth(newValue.getWidth());
                    if (previewFlowPane.getHeight() < imagePreviewScrollPane.getHeight()) {
                        previewFlowPane.setPrefHeight(imagePreviewScrollPane.getHeight());
                    }
                });

        rectangle.setVisible(false);
    }


    /**
     * 鼠标事件处理
     *
     * @author Cyberangel2023
     */
    // 鼠标x，y坐标
    private double x;
    private double y;
    // 右键弹窗菜单
    private final Menu menu = new Menu();

    public void addHandle() {
        // 对previewFlowPane进行鼠标监听
        previewFlowPane.setOnMousePressed(event -> {
            x = event.getX();
            y = event.getY();
            menu.close();

            // 处理点击空白区域后已选择的图片
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !event.isControlDown()) {
                if (previewFlowPane.equals(event.getPickResult().getIntersectedNode())) {
                    previewFlowPane.clearSelected();
                    previewFlowPane.setIsShift(false);
                }
            }

            // 右键菜单
            if (event.getButton() == MouseButton.SECONDARY) {
                menu.show(imagePreviewPane, event.getScreenX(), event.getScreenY());
            }
        });

        // 鼠标拖拽矩阵选中图片
        previewFlowPane.setOnMouseDragged(event -> {
            // 计算矩形大小
            double x2 = event.getX();
            double y2 = event.getY();
            double startX = Math.min(x, x2);
            double startY = Math.min(y, y2);
            rectangle.setX(startX);
            rectangle.setY(startY);
            double width = Math.abs(x - x2);
            double height = Math.abs(y - y2);
            rectangle.setWidth(width);
            rectangle.setHeight(height);
            rectangle.setVisible(true);
            if (width >= 10 && height >= 10){
                imageSelected();
            }
        });
        previewFlowPane.setOnMouseReleased(event -> {
            rectangle.setVisible(false);
        });
    }

    /**
     * 根据矩形选择图片
     */
    public void imageSelected() {
        previewFlowPane.clearSelected();
        ThumbnailPanel endPane = null;
        boolean firstSelectedFlag = false;
        for (ThumbnailPanel pane : previewFlowPane.getThumbnailPanels()) {
            if (rectangle.intersects(pane.getBoundsInParent())) {
                previewFlowPane.addSelected(pane);
                if (!firstSelectedFlag) {
                    endPane = pane;
                    firstSelectedFlag = true;
                }
            }
        }
        previewFlowPane.setIsShift(true);
        previewFlowPane.setFrom(previewFlowPane.getThumbnailPanels().indexOf(endPane));
    }

    /**
     *  窗口控制逻辑
     *  作者：yuekanwang
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

    private double WidthX;
    private double HightY;
    /*
    *  窗口左上角坐标
    * */


    /**
     * 设置所有可调节窗口大小指示区域的监听器
     */
    private void setUpResizeListeners() {
        // 左侧调整
        setupResizeHandler(leftResize, WindowMainController.ResizeDirection.LEFT);
        // 右侧调整
        setupResizeHandler(rightResize, WindowMainController.ResizeDirection.RIGHT);
        // 顶部调整
        setupResizeHandler(topResize, WindowMainController.ResizeDirection.TOP);
        // 底部调整
        setupResizeHandler(bottomResize, WindowMainController.ResizeDirection.BOTTOM);
        // 左上角调整
        setupResizeHandler(leftTopResize, WindowMainController.ResizeDirection.LEFT_TOP);
        // 右上角调整
        setupResizeHandler(rightTopResize, WindowMainController.ResizeDirection.RIGHT_TOP);
        // 左下角调整
        setupResizeHandler(leftBottomResize, WindowMainController.ResizeDirection.LEFT_BOTTOM);
        // 右下角调整
        setupResizeHandler(rightBottomResize, WindowMainController.ResizeDirection.RIGHT_BOTTOM);
    }

    /**
     * 调节方向枚举值
     */
    private enum ResizeDirection {
        LEFT, RIGHT, TOP, BOTTOM,
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }

    /**
     * 鼠标拖拽事件分发处理逻辑
     * @param region 调节区域
     * @param direction 调节方向
     */
    private void setupResizeHandler(Region region, WindowMainController.ResizeDirection direction) {
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
        WidthX=stage.getX();
        HightY=stage.getY();
        event.consume();
    }

    /**
     * 主要处理事件分发逻辑
     * @param event
     * @param direction 调节方向
     */
    private void handleResize(MouseEvent event, WindowMainController.ResizeDirection direction) {
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
                handleLeftTopResize(deltaX,deltaY);
                break;
            case RIGHT_TOP:
                handleRightTopResize(deltaX,deltaY);
                break;
            case LEFT_BOTTOM:
                handleLeftBottomResize(deltaX,deltaY);
                break;
            case RIGHT_BOTTOM:
                handleRightBottomResize(deltaX,deltaY);

                break;
        }
        event.consume();
    }

    /**
     * 原子调节操作,窗口在左边区域放缩
     * @param deltaX x坐标偏移量
     */
    //读懂了这个函数，下面7个函数都能读懂
    private void handleLeftResize(double deltaX) {
        double newWidth = startWidth - deltaX; //获取当前的窗口width
        if (newWidth > stage.getMinWidth()) {//不能超过窗口最大值
            if(stage.isMaximized())//如果窗口已经最大化，此时改动窗口，则取消窗口最大化，并改变按钮
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setWidth(newWidth);//设置长度为新的长度
            stage.setHeight(startHeight);//设置高度为原来的高度
            stage.setY(HightY);//设置左上角的Y坐标为原来的Y坐标
            stage.setX(startX + deltaX);//设置左上角的X坐标为新的X坐标
        }
    }

    /**
     * 原子调节操作,窗口在右边区域放缩
     * @param deltaX x坐标偏移量
     */
    private void handleRightResize(double deltaX) {
        double newWidth = startWidth + deltaX;
        if (newWidth > stage.getMinWidth()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setWidth(newWidth);
            stage.setHeight(startHeight);
            stage.setY(HightY);
            stage.setX(WidthX);
        }
    }

    /**
     * 原子调节操作,窗口在顶部区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleTopResize(double deltaY) {
        double newHeight = startHeight - deltaY;
        if (newHeight > stage.getMinHeight()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setHeight(newHeight);
            stage.setWidth(startWidth);
            stage.setY(startY + deltaY);
            stage.setX(WidthX);
        }
    }

    /**
     * 原子调节操作,窗口在底部区域放缩
     * @param deltaY y坐标偏移量
     */
    private void handleBottomResize(double deltaY) {
        double newHeight = startHeight + deltaY;
        if (newHeight > stage.getMinHeight()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setHeight(newHeight);
            stage.setWidth(startWidth);
            stage.setY(HightY);
            stage.setX(WidthX);
        }
    }

    /**
     * 原子调节操作,窗口在左上边区域放缩
     */
    private void handleLeftTopResize(double deltaX,double deltaY)
    {
        double newWidth = startWidth - deltaX;
        double newHeight = startHeight - deltaY;
        if (newWidth > stage.getMinWidth()&&newHeight > stage.getMinHeight()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setWidth(newWidth);
            stage.setHeight(newHeight);
            stage.setY(startY + deltaY);
            stage.setX(startX + deltaX);
        }
    }

    /**
     * 原子调节操作,窗口在右上边区域放缩
     */
    private void handleRightTopResize(double deltaX,double deltaY)
    {
        double newWidth = startWidth + deltaX;
        double newHeight = startHeight - deltaY;
        if (newWidth > stage.getMinWidth()&&newHeight > stage.getMinHeight()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setWidth(newWidth);
            stage.setHeight(newHeight);
            stage.setY(startY + deltaY);
            stage.setX(WidthX);
        }
    }
    /**
     * 原子调节操作,窗口在右边区域放缩
     */
    private void handleLeftBottomResize(double deltaX,double deltaY)
    {
        double newWidth = startWidth - deltaX;
        double newHeight = startHeight + deltaY;
        if (newWidth > stage.getMinWidth()&&newHeight > stage.getMinHeight()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setWidth(newWidth);
            stage.setHeight(newHeight);
            stage.setY(HightY);
            stage.setX(startX + deltaX);
        }
    }
    /**
     * 原子调节操作,窗口在右边区域放缩
     */
    private void handleRightBottomResize(double deltaX,double deltaY)
    {
        double newWidth = startWidth + deltaX; //获取当前的窗口width
        double newHeight = startHeight + deltaY;
        if (newWidth > stage.getMinWidth()&&newHeight > stage.getMinHeight()) {
            if(stage.isMaximized())
            {
                stage.setMaximized(false);
                MaxBtn_Change();
            }
            stage.setWidth(newWidth);
            stage.setHeight(newHeight);
            stage.setY(HightY);
            stage.setX(WidthX);
        }
    }


    private double xOffset = 0;
    private double yOffset = 0;
    /**
     * 鼠标拖拽窗口事件处理,记录鼠标初始位置
     */
    private boolean isMaximized;
    @FXML
    private void handleMousePressed(MouseEvent event) {
        // 仅当点击在空白区域时记录坐标
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();

    }


    /**
     * 鼠标拖拽窗口事件处理,窗口跟随鼠标拖拽位置
     * @param event
     */
    @FXML
    private void handleMouseDragged(MouseEvent event) {
        if (stage.isMaximized())
        {
            startResize(event);
            stage.setMaximized(false);
            stage.setWidth(startWidth);
            stage.setHeight(startHeight);
            MaxBtn_Change();
        }
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
    /**
     * 窗口最大化切换
     */
    private void toggleMaximize() {
        stage.setMaximized(!stage.isMaximized());
        MaxBtn_Change();
    }
    /**
     * 这是最大化按钮样式改变的函数
     */
    private void MaxBtn_Change(){
        if(stage.isMaximized()) {
            maxBtn.getStyleClass().remove("maxBtn-full");
            maxBtn.getStyleClass().add("maxBtn-recover");
        }else {
            maxBtn.getStyleClass().remove("maxBtn-recover");
            maxBtn.getStyleClass().add("maxBtn-full");
        }
    }

    public TextField getSearchPath() {
        return Search_Path;
    }

    class Menu extends ContextMenu {
        MenuItem copy = new MenuItem("复制");

        MenuItem copyaddress = new MenuItem("复制文件地址");

        MenuItem paste = new MenuItem("粘贴");

        MenuItem delete = new MenuItem("删除");

        MenuItem rename = new MenuItem("重命名");

        MenuItem attribute = new MenuItem("属性");

        MenuItem compress = new MenuItem("压缩到");

        Menu() {
            copy.setOnAction(e -> copyImage());
            copyaddress.setOnAction(e -> copyAddress());
            paste.setOnAction(e -> pasteAll());
            delete.setOnAction(e -> deleteImage());
            rename.setOnAction(e -> renameImage());
            attribute.setOnAction(e -> showImageAttribute());
            compress.setOnAction(e -> compressImage());
            getItems().addAll(copy, copyaddress, paste, delete, rename, attribute, compress);
        }

        // 菜单显示
        @Override
        public void show(Node pane, double x, double y) {
            super.show(pane, x, y);
        }

        // 菜单关闭
        public void close() {
            hide();
        }
    }

    // 存储复制的图片
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();

    /**
     * 复制图片
     */
    @FXML
    public void copyImage() {
        if (!previewFlowPane.getNewSelected().isEmpty())  {
            // 获取用户选中的多个图片文件
            File src;
            File directory = previewFlowPane.getDirectory();
            List<File> selectedFiles = new ArrayList<>();
            for (ThumbnailPanel image : previewFlowPane.getNewSelected()) {
                src = image.getImageUtil().getFile();
                String in = directory.getAbsolutePath() + "\\" + src.getName();
                selectedFiles.add(new File(in));
            }
            // 将文件列表存入剪贴板
            content.putFiles(selectedFiles);
            clipboard.setContent(content);
        }
    }

    // 存储复制的图片地址
    private String copyAddr = null;
    private String pastedText;

    public String getCopyAddr() {
        return copyAddr;
    }

    public String getPastedText() {
        return pastedText;
    }

    /**
     * 复制图片地址
     */
    @FXML
    public void copyAddress() {
        copyAddr = previewFlowPane.getDirectory().getAbsolutePath() +
                previewFlowPane.getNewSelected().getLast().getImageUtil().getFileName();
        // 将字符串存入剪贴板
        content.putString(copyAddr);
        clipboard.setContent(content);
    }

    /**
     * 粘贴图片
     */
    @FXML
    public void pasteAll() {
        // 获取复制图片内容
        if (clipboard.hasFiles())  {
            List<File> files = clipboard.getFiles();
            for (File file : files) {
                // 过滤非图片文件
                if (file.getName().matches(".*\\.(png|jpg|jpeg|gif|bmp)")) {
                    // 添加到界面容器
                    ImageUtil imageUtil = new ImageUtil(file);
                    ThumbnailPanel thumbnailPanel = new ThumbnailPanel(imageUtil);
                    previewFlowPane.getThumbnailPanels().add(thumbnailPanel);
                }
            }
        } else if (clipboard.hasString()) {
            pastedText = clipboard.getString();
        }
        updateFlowPane();
    }

    /**
     * 刷新缩略图面板
     */
    public void updateFlowPane() {
        previewFlowPane.update();
    }


    /**
     * 删除图片
     */
    @FXML
    public void deleteImage() {
    }

    /**
     * 重命名图片
     */
    @FXML
    public void renameImage() {
    }

    /**
     * 打开图片属性
     */
    @FXML
    public void showImageAttribute() {
    }

    /**
     * 压缩图片
     */
    @FXML
    public void compressImage() {
    }
}


