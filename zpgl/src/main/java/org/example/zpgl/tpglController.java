package org.example.zpgl;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class tpglController {
    @FXML
    private TreeView<String> myTreeView;//树视图

    @FXML
    public void initialize() {
        TreeItem<String> root =new TreeItem<String>("zhongguo");//添加树视图的树item
        root.setExpanded(true);//设置允许跟目录root展开


        myTreeView.setRoot(root);
    }

}
