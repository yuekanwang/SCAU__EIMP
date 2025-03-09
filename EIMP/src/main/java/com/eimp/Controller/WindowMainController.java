package com.eimp.Controller;

import com.eimp.component.DirectoryLoader;
import com.eimp.component.FileTreeItem;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
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
    public AnchorPane tree;
    @FXML
    public AnchorPane thumbnail;
    @FXML
    public AnchorPane bottom;//这些AnchorPane是布局框

    @FXML
    public TreeView<String> treeView;//树视图，用于做目录树

    // 树形目录占位符
    public static final String HOLDER_TEXT = "Loading...";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initFileTreeView();
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

        treeView.setRoot(treeItem);
        treeView.setShowRoot(false);
    }
}
