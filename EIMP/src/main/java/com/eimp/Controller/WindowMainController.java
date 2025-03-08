package com.eimp.Controller;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
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
    public TreeView treeView;//树视图，用于做目录的


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


}
