package com.eimp.Controller;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class WindowSlideController implements Initializable{

    @FXML
    public ImageView Zoom_Button_icon;//用于放大图片的按钮的图标
    @FXML
    public ImageView Slide_View;//中间的幻灯片的图片

    @FXML
    public Label Percentage_Label;//用于显示当前图片缩放的百分比大小
    @FXML
    public Label Image_Name;//左上角的图片名字，当做窗口名字

    @FXML
    public Button Next_Picture_Button;//用于跳转下一张图的按钮
    @FXML
    public Button Last_Picture_Button;//用于跳转前一张图的按钮
    @FXML
    public Button Minimization_of_Window_Button;//右上角的最小化窗口的按钮
    @FXML
    public Button Window_Zoom_Button;//右上角的全屏窗口的按钮
    @FXML
    public Button Close_Button;//右上角的关闭窗口的按钮
    @FXML
    public Button Zoom_Button;//用于放大图片的按钮


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
