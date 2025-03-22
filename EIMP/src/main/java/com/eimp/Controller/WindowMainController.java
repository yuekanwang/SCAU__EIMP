package com.eimp.Controller;

import com.eimp.component.DirectoryLoader;
import com.eimp.component.FileTreeItem;
import com.eimp.component.PreviewFlowPane;

import com.eimp.component.ThumbnailPanel;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class WindowMainController implements Initializable {

    @FXML
    public AnchorPane top;
    @FXML
    public AnchorPane mid;
    @FXML
    public AnchorPane bottom;//这些AnchorPane是布局框
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
            }
        });

        treeView.setRoot(treeItem);
        treeView.setShowRoot(false);
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
            menu.hide();

            // 处理点击空白区域后已选择的图片
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !event.isControlDown()) {
                if (previewFlowPane.equals(event.getPickResult().getIntersectedNode())) {
                    previewFlowPane.clearSelected();
                }
            }

            // 右键菜单
            if (event.getButton() == MouseButton.SECONDARY) {
                menu.show();
            }
        });
    }

    /**
     * 根据矩形选择图片
     */
    public void imageSelected() {
        previewFlowPane.clearSelected();
        for (ThumbnailPanel pane : previewFlowPane.getThumbnailPanels()) {
            if (rectangle.intersects(pane.getBoundsInParent())) {
                previewFlowPane.addSelected(pane);
            }

        }
    }
}
