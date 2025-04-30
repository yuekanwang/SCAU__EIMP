package com.eimp.controller;

import com.eimp.App;
import com.eimp.SlideWindow;
import com.eimp.component.*;
import javafx.scene.image.Image;
import com.eimp.util.ImageUtil;
import com.eimp.util.SortOrder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import org.controlsfx.control.Notifications;

import javax.swing.filechooser.FileSystemView;
import javax.swing.text.html.ImageView;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.Scene;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class WindowMainController implements Initializable {
    @FXML
    public AnchorPane interface_Pane;//主界面基本面板
    @FXML
    public Button LightButton;//夜间/日间控制按钮

    public AnchorPane root;//根

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
    public Button About_Button;//右下角"关于"
    
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

    public SortOrder sortOrder;

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

        initButton();
        initSearch();
        initFileTreeView();
        initPreviewPane();
        intPaneColor();
        initSortImage();
        sortOrder = new SortOrder();
        updateTipsLabelText();
    }

    /**
     * 初始化主界面的功能按钮的作用
     *
     * @author yuekanwnag
     */
    private void initButton()
    {
        Flushed_Button.setOnAction(e->flushImage());
        SelectAll_Button.setOnAction(e->selectedAll());
        Delete_Button.setOnAction(e->deleteImage());
        Help_Button.setOnAction(e->HelpWindow());
        About_Button.setOnAction(e->AboutWindow());

        Delete_Button.setOnKeyPressed(e->{//键盘视奸
            if(e.getCode() == KeyCode.DELETE)
                Delete_Button.fire();//触发按钮的点击
        });
        Left_Button.setOnAction(e->goBack());
        Right_Button.setOnAction(e->goForward());

/*        @FXML
        public Button About_Button;//有下角"关于"

        @FXML
        public Button SelectAll_Button;//全选按钮
        @FXML
        public Button Delete_Button;//删除按钮
        @FXML
        public Button Flushed_Button;//刷新按钮

        public Button Help_Button;//帮助按钮*/
    }

    private void goBack() {
    }

    private void goForward() {
    }


    private  void AboutWindow()
    {
        Stage AboutStage = new Stage();
        AboutStage.setTitle("关于");

        AboutStage.setResizable(false);

        AboutStage.setWidth(430);
        AboutStage.setHeight(330);

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        String markdownContent = """
# 关于EIMP
---
### 开发者：shanmu，yuekanwang，Cyberangel2023
### 源码网址：https://github.com/yuekanwang/EIMP
        """;

        webEngine.loadContent(convertMarkdownToHtml(markdownContent));
        javafx.scene.image.Image Appicon = new Image(getClass().getResourceAsStream("/icon2/EIMP.png"));
        AboutStage.getIcons().add(Appicon);
        Scene scene = new Scene(webView, 400, 400);
        AboutStage.setScene(scene);
        AboutStage.show();

    }

    private void HelpWindow() {
        Stage helpStage = new Stage();
        helpStage.setTitle("帮助信息");

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        String markdownContent = """
# EIMP 图像处理软件帮助文档

## 1. 软件介绍

EIMP (Enhanced Image Management and Processing) 是一款功能强大的图像处理软件，采用JavaFX技术开发，提供直观的用户界面和丰富的图像处理功能。软件支持多种图像格式，并提供专业的图像编辑工具。

### 1.1 主要特点

- 现代化的用户界面设计
- 支持多种图像格式（如：.JPG、.JPEG、.GIF、.PNG、和.BMP。）
- 提供专业的图像编辑工具
- 支持批量处理功能
- 提供图像预览和缩略图功能

## 2. 功能介绍与教程

### 2.1 主界面功能

**主界面包含以下主要组件：**

- 文件树：用于浏览和管理图像文件	
- 缩略图面板：显示当前目录下的图像缩略图
- 工具栏：提供常用功能的快捷访问，包括排序方式、全选、删除、刷新、幻灯片播放、帮助。部分功能可以参考下面对幻灯片界面功能的说明。
- 文件路径框：显示缩略图面板的所在路径
- 搜索框：用于搜索功能
- 日间或夜间模式转换按钮：能够转换主界面的样式。
- 左下角显示文件里的所有图片数量及其总大小和选中的图片数量及其总大小。
- 右下角“关于”键是对该软件开发的一些情况交代。

**主界面可能需要提及到的功能：**

- 关于文件树：文件树只会显示文件夹和文件夹的快捷模式。点击文件夹，便是进入了该文件夹，若文件夹里有图片或.gif文件，则在缩略图面板加载出来。
- 关于删除功能：图片必须在被选中后才能进行删除，除了工具栏里的删除键，还有快捷键“**delete**”键。
- 关于选中功能：
	1. 单击图片，即可单个选中图片
	2. 按住clrl，可单击多个图片，选中它们
	3. 在缩略图面版空白处拖动(鼠标左键或右键)形成一个矩形，该矩形内的图片将被选中。
	4. 点击工具栏的“全选按钮”，可选中全部图片。
- 关于重命名功能：
	1. 双击图片的名字，可以修改图片文件字。
	2. 右键图片，可以选择修改图片文件名
	3. 注意，**小心修改文件后缀名**，如果修改后的后缀名不符合该软件的识别范围，改文件将在缩略图面板被隐藏。
- 关于搜索功能：在搜索框里输入文字，根据这些文字，寻找文件名符合这些文字的图片，缩略图面板将显示这些图片。

---

### 2.2 幻灯片界面功能

**幻灯片界面包含以下主要组件：**

- 对需要说明的顶部的组件的说明（从左到右）：
	1. 该图片的文件名
	2. 该图片在该文件夹里所有图片的排位、该图片的大小和该图片的像素大小
	3. 选项键里有另存为和裁剪的选项。
	4. 旋转键，能够让图片顺时针旋转90°。
	5. 删除键，删除当前查看的图片。
	6. 查看上或下一张的图片。
	7. 按顺序播放图片。
	8. 查看该图片的属性。
	9. 压缩该图片。**注意**，这不是把图片做成压缩包的形式，而是修改图片的像素，让图片在降低质量的同时，降低文件大小。
- 中间呈现图片的查看。
- 底部是对图片上面几个或下面几个图片的缩略图、

**幻灯片界面可能需要提及的功能：**

- 进入幻灯片的界面方式：在主界面里双击一张图片。
- 对图片放大缩小查看的功能：
	1. 可通过点击工具栏的“放大“键、”缩小“键进行查看。
	2. 可按住“ctrl”键，滑动鼠标滚轮来控制图片的放大缩小进行查看。
	3. 放大后，可鼠标左键拖动图片进行查看

        """;

        webEngine.loadContent(convertMarkdownToHtml(markdownContent));
        javafx.scene.image.Image Appicon = new Image(getClass().getResourceAsStream("/icon2/EIMP.png"));
        helpStage.getIcons().add(Appicon);
        Scene scene = new Scene(webView, 600, 400);
        helpStage.setScene(scene);
        helpStage.show();
    }

    private String convertMarkdownToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String htmlContent = renderer.render(parser.parse(markdown));

        return "<html><head>"
                + "<style>"
                + "body { font-family: Microsoft YaHei; padding: 25px; }"
                + "h1 { color: #2B579A; }"
                + "</style>"
                + "</head><body>"
                + htmlContent
                + "</body></html>";
    }


    /**
     * 初始化界面主题的设置和控制
     *
     * @author yuekanwnag
     */
    private boolean f;//标志位，用以表示日间或夜间

    public boolean getF() {
        return f;
    }

    private void intPaneColor()
    {
        f=true;//true表示日间，false表示夜间
        root.getStylesheets().setAll(//初始化为日间模式
                getClass().getResource("/css/Main_Sun.css").toExternalForm());

        LightButton.setOnAction(e->{//按钮控制日间模式或夜间模式的切换
            if(f)
            {
                f=false;
                root.getStylesheets().clear();//清楚掉之前的样式
                root.getStylesheets().setAll(//加入新的样式
                        getClass().getResource("/css/Main_Night.css").toExternalForm());
            }
            else
            {
                f=true;
                root.getStylesheets().clear();
                root.getStylesheets().setAll(
                        getClass().getResource("/css/Main_Sun.css").toExternalForm());
            }
            if (!Search_Path.getText().isEmpty()) {
                updateFlowPaneOfSearch();
            } else {
                updateFlowPane();
            }
        });
    }

    /**
     * 为搜索框加上监听事件，实现搜索内容的高亮显示
     * 当前为实时搜索，不用按下回车键
     *
     * @author Cyberangel2023
     */
    private void initSearch() {
        Search_Path.textProperty().addListener((observable, oldValue, newValue) -> {
            previewFlowPane.setSearch_Text(newValue);
            //为空时显示全部
            if (Search_Path.getText().isEmpty()) {
                updateFlowPane();
                return;
            }
            updateFlowPaneOfSearch();
        });
    }

    public TextField getSearch_Path() {
        return Search_Path;
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
                updateTipsLabelText();
                previewFlowPane.clearSelected();
                previewFlowPane.setIsShift(false);
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
     * 初始化排序面板
     */
    private void initSortImage() {
        Sort_Name.setStyle("-fx-background-color: #C3C3C3;");
        SortOrder_Time.setOnAction(e -> setSortOrder(4));//按时间倒序
        SortOrder_Size.setOnAction(e -> setSortOrder(6));//按大小倒序
        SortOrder_Name.setOnAction(e -> setSortOrder(2));//按名字倒序
        Sort_Time.setOnAction(e -> setSortOrder(3));//按时间顺序
        Sort_SIze.setOnAction(e -> setSortOrder(5));//按大小顺序
        Sort_Name.setOnAction(e -> setSortOrder(1));//按名字顺序
    }

    private void setSortOrder(int op) {
        sortOrder.setOldOp(sortOrder.getOp());
        sortOrder.setOp(op);
        sortOrder.setNowString();
        changeSortCSS();
        if (!Search_Path.getText().isEmpty()) {
            updateFlowPaneOfSearch();
        } else {
            updateFlowPane();
        }
    }

    private void changeSortCSS() {
        switch (sortOrder.getOp()) {
            case 1:
                Sort_Name.setStyle("-fx-background-color: #C3C3C3;");
                break;
            case 2:
                SortOrder_Name.setStyle("-fx-background-color: #C3C3C3;");
                break;
            case 3:
                Sort_Time.setStyle("-fx-background-color: #C3C3C3;");
                break;
            case 4:
                SortOrder_Time.setStyle("-fx-background-color: #C3C3C3;");
                break;
            case 5:
                Sort_SIze.setStyle("-fx-background-color: #C3C3C3;");
                break;
            case 6:
                SortOrder_Size.setStyle("-fx-background-color: #C3C3C3;");
                break;
        }
        switch (sortOrder.getOldOp()) {
            case 1:
                Sort_Name.setStyle("-fx-background-color: transparent;");
                break;
            case 2:
                SortOrder_Name.setStyle("-fx-background-color: transparent;");
                break;
            case 3:
                Sort_Time.setStyle("-fx-background-color: transparent;");
                break;
            case 4:
                SortOrder_Time.setStyle("-fx-background-color: transparent;");
                break;
            case 5:
                Sort_SIze.setStyle("-fx-background-color: transparent;");
                break;
            case 6:
                SortOrder_Size.setStyle("-fx-background-color: transparent;");
                break;
        }
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    /**
     * 鼠标事件处理
     *
     * @author Cyberangel2023
     */
    // 鼠标x，y坐标
    private double x;
    private double y;
    private double mouseScreenY;
    // 滑动速度
    private double offsetUp;
    private double offsetDown;
    // 用于周期性更新ScrollPane面板
    private Timeline scrollTimeLine;
    // 是否在图片外
    boolean flagMenu;
    // 右键弹窗菜单
    public final Menu menu = new Menu();

    public void setFlagMenu(boolean flag) {
        flagMenu = flag;
    }

    public void addHandle() {
        Platform.runLater(() -> {
            Point2D positionOfMid = mid.localToScreen(0, 0);
            offsetUp = positionOfMid.getY() + 46;
            Point2D positionOfButtom = buttom.localToScreen(0, 0);
            offsetDown = positionOfButtom.getY();
        });

        // 初始化Timeline
        scrollTimeLine = new Timeline(
                new KeyFrame(Duration.millis(10), event -> {
                    // 设置滚动速度
                    if (mouseScreenY < offsetUp) {
                        imagePreviewScrollPane.setVvalue(imagePreviewScrollPane.getVvalue() +
                                (mouseScreenY - offsetUp) / 2000);
                    }
                    if (mouseScreenY > offsetDown) {
                        imagePreviewScrollPane.setVvalue(imagePreviewScrollPane.getVvalue() +
                                (mouseScreenY - offsetDown) / 2000);
                    }
                })
        );
        // 设置无限循环
        scrollTimeLine.setCycleCount(Timeline.INDEFINITE);
        previewFlowPane.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.V) {
                    pasteAll();
                } else if (event.getCode() == KeyCode.C) {
                    copyImage();
                }
            }
        });
        // 对previewFlowPane进行鼠标监听
        previewFlowPane.setOnMousePressed(event -> {
            x = event.getX();
            y = event.getY();
            menu.close();
            mouseScreenY = event.getScreenY();

            // 处理点击空白区域后已选择的图片
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !event.isControlDown()) {
                if (previewFlowPane.equals(event.getPickResult().getIntersectedNode())) {
                    previewFlowPane.clearSelected();
                    previewFlowPane.setIsShift(false);
                }
                updateTipsLabelText();
            }

            // 右键菜单
            if (event.getButton() == MouseButton.SECONDARY) {
                flagMenu = true;
                for (ThumbnailPanel pane : previewFlowPane.getThumbnailPanels()) {
                    if (pane.isContains(x, y)) {
                        flagMenu = false;
                        previewFlowPane.clearSelected();
                        previewFlowPane.addSelected(previewFlowPane.getThumbnailPanels().get(previewFlowPane.getThumbnailPanels().indexOf(pane)));
                        menu.show(imagePreviewPane, event.getScreenX(), event.getScreenY());
                        break;
                    }
                }
                if (flagMenu) {

                }
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

            mouseScreenY = event.getScreenY();
            // 鼠标超出边界时，ScrollPane上滑/下滑
            if (event.getScreenY() < offsetUp || event.getScreenY() > offsetDown) {
                scrollTimeLine.play();
            } else {
                scrollTimeLine.stop();
            }
        });

        previewFlowPane.setOnMouseReleased(event -> {
            rectangle.setVisible(false);
            scrollTimeLine.stop();
        });

        // 快捷键监听事件
        imagePreviewPane.setFocusTraversable(true);
        imagePreviewPane.setOnMouseClicked(event -> {
            imagePreviewPane.requestFocus();
        });

        imagePreviewPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!imagePreviewPane.isFocused()) {
                return;
            }
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN).match(event)) {
                copyImage();
            } else if (new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN).match(event)) {
                pasteAll();
            } else if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN).match(event)) {
                copyAddress();
            } else if (new KeyCodeCombination(KeyCode.DELETE).match(event)) {
                deleteImage();
            } else if (new KeyCodeCombination(KeyCode.ENTER, KeyCodeCombination.ALT_DOWN).match(event)) {
                showImageAttribute();
            }
            event.consume();
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
        updateTipsLabelText();
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

    /**
     * 更新左下角提示栏
     */
    public void updateTipsLabelText() {
        updateTipsLabelText(previewFlowPane.getTotalCount(), previewFlowPane.getTotalSize(),
                previewFlowPane.newSelectedSize(), previewFlowPane.getSelectedSize());
    }

    /**
     * 更新左下角提示栏
     *
     * @param totalCount    当前目录的图片总数
     * @param size          总大小
     * @param selectedCount 被选中的图片的数量
     * @param selectedSize  被选中的图片的大小
     */
    public void updateTipsLabelText(int totalCount, String size, int selectedCount, String selectedSize) {
        imageSize_Label.setText(String.format("共 %d 张照片(%s) - 选中 %d 张照片(%s)",
                totalCount,
                size,
                selectedCount,
                selectedSize));
    }

    public TextField getSearchPath() {
        return Search_Path;
    }

    public class Menu extends ContextMenu {
        MenuItem copy = new MenuItem("复制");

        MenuItem copyaddress = new MenuItem("复制文件地址");

        MenuItem paste = new MenuItem("粘贴");

        MenuItem delete = new MenuItem("删除");

        MenuItem rename = new MenuItem("重命名");

        MenuItem attribute = new MenuItem("属性");

        Menu() {
            copy.setOnAction(e -> copyImage());
            copyaddress.setOnAction(e -> copyAddress());
            paste.setOnAction(e -> pasteAll());
            delete.setOnAction(e -> deleteImage());
            rename.setOnAction(e -> renameImage());
            attribute.setOnAction(e -> showImageAttribute());
            getItems().addAll(copy, copyaddress, paste, delete, rename, attribute);
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
        if (!previewFlowPane.newSelectedIsEmpty())  {
            boolean yes = true;
            // 获取用户选中的多个图片文件
            File src;
            File directory = previewFlowPane.getDirectory();
            List<File> selectedFiles = new ArrayList<>();
            String text = null;
            for (ThumbnailPanel image : previewFlowPane.getNewSelected()) {
                if (image.isRename()) {
                    text = image.getImageName().getSelectedText();
                    yes = false;
                    break;
                }
                src = image.getImageUtil().getFile();
                String in = directory.getAbsolutePath() + "\\" + src.getName();
                selectedFiles.add(new File(in));
            }
            // 将文件列表存入剪贴板
            if (yes) {
                content.putFiles(selectedFiles);
            } else {
                content.putString(text);
            }
        }
        clipboard.clear();
        clipboard.setContent(content);
        menu.close();
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
        menu.close();
    }

    /**
     * 粘贴图片
     */
    @FXML
    public void pasteAll() {
        if (!Search_Path.getText().isEmpty()) {
            return;
        }
        // 获取复制图片内容
        if (clipboard.hasFiles())  {
            List<File> files = clipboard.getFiles();
            File target;
            File directory = previewFlowPane.getDirectory();
            for (File file : files) {
                // 过滤非图片文件
                if (file.getName().matches(".*\\.(png|jpg|jpeg|gif|bmp)")) {
                    // 添加到界面容器
                    String out = directory.getAbsolutePath() + "\\" + file.getName();
                    target = new File(out);
                    try {
                        //目标文件已存在时，在文件名后加"副本后缀"
                        while (target.exists()) {
                            String suffix = out.substring(out.lastIndexOf("."));
                            out = out.replace(suffix, "-副本") + suffix;
                            target = new File(out);
                        }
                        Files.copy(file.toPath(), target.toPath());
                        SlideWindow.flushSlideWindows(null,new ImageUtil(target));      //幻灯片同步
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (clipboard.hasString()) {
            pastedText = clipboard.getString();
        }
        menu.close();
        updateFlowPane();
    }

    /**
     * 刷新缩略图面板
     */
    public void updateFlowPane() {
        previewFlowPane.update();
    }

    private void updateFlowPaneOfSearch() {
        previewFlowPane.updateOfSearch();
    }

    /**
     * 删除图片
     */
    @FXML
    public void deleteImage() {
        List<String> oldPaths = new ArrayList<>();
        boolean flag=false;
        File choose;
        for (ThumbnailPanel image : previewFlowPane.getNewSelected()) {
            choose = image.getImageUtil().getFile();
            if (choose.exists() && choose.isFile()) {
                if (Desktop.isDesktopSupported())  {
                    if(Desktop.getDesktop().moveToTrash(choose)){
                        oldPaths.add(choose.getAbsolutePath());
                        flag=true;
                    }
                }
                boolean isDeleted = choose.delete();  // 返回删除结果
                if (isDeleted) {
                    // System.out.println(" 文件删除成功");
                } else {
                    // System.out.println(" 文件被占用或无权限");
                }
            }
        }
        menu.close();
        // 如果幻灯片窗口存在则刷新
        if(flag){
            SlideWindow.flushSlideWindows(oldPaths,previewFlowPane.getNewSelected().get(0).getImageUtil().getDirectory().getAbsolutePath());
        }
        if (!Search_Path.getText().isEmpty()) {
            updateFlowPaneOfSearch();
        } else {
            updateFlowPane();
        }
    }

    /**
     * 重命名图片
     */
    @FXML
    public void renameImage() {
        if (!previewFlowPane.getNewSelected().isEmpty()) {
            ThumbnailPanel image = previewFlowPane.getNewSelected().getLast();
            image.startReName();
        }
        menu.close();
    }

    /**
     * 打开图片属性
     */
    @FXML
    public void showImageAttribute() {
        // 图片信息面板
        ImageInfoWindow.main(previewFlowPane.getNewSelected().getLast().getImageUtil(),340,250,this.stage);
        menu.close();
    }


    /**
     * 压缩图片
     */
    @FXML
    public void compressImage() {
    }
    /**
     * 播放幻灯片
     */
    @FXML
    private void playingSlide(){
        List<ThumbnailPanel> newSelected = this.previewFlowPane.getNewSelected();
        ImageUtil curImage;
        if(newSelected==null || newSelected.isEmpty()){
            List<ThumbnailPanel> thumbnailPanels = this.previewFlowPane.getThumbnailPanels();
            if(thumbnailPanels==null || thumbnailPanels.isEmpty()){
                Notifications.create()
                        .text("当前文件夹没有图片!")
                        .hideAfter(Duration.seconds(0.5))
                        .position(Pos.BASELINE_RIGHT)
                        .owner(this.stage)
                        .show();
                return;
            }
            curImage = thumbnailPanels.getFirst().getImageUtil();
        }else{
            curImage = newSelected.getFirst().getImageUtil();
        }
        Platform.runLater(() -> {
            SlideWindow.main(curImage,SlideWindow.LaunchMethodEnum.PLAY);
        });
    }

    // 全选函数
    @FXML
    public void selectedAll() {
        previewFlowPane.clearSelected();
        for (ThumbnailPanel pane : previewFlowPane.getThumbnailPanels()) {
            previewFlowPane.addSelectedtoList(pane);
        }
        updateTipsLabelText();
    }

    // 刷新函数
    @FXML
    public void flushImage() {
        if (!Search_Path.getText().isEmpty()) {
            updateFlowPaneOfSearch();
        } else {
            updateFlowPane();
        }
    }
}


