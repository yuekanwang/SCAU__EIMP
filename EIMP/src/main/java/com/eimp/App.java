package com.eimp;

import com.eimp.Controller.ControllerMap;
import com.eimp.component.ThumbnailPanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/WindowMain.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        stage.setTitle("电子图片管理系统");
        stage.setMinHeight(400);
        stage.setMinWidth(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void newStageTo(Object obj) throws IOException {
        Stage stage = new Stage();
        ThumbnailPanel img = (ThumbnailPanel) obj;
        String imgName = img.getImageUtil().getFileName();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/WindowSlide.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(imgName);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}