package com.eimp;

import com.eimp.component.ThumbnailPanel;
import com.eimp.controller.WindowMainController;
import com.eimp.controller.WindowSlideController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class App extends Application {
    private WindowMainController controller;
    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        App.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/WindowMain.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        stage.setTitle("电子图片管理系统");
        stage.setMinHeight(400);
        stage.setMinWidth(650);
        stage.setScene(scene);

        Image Appicon = new Image(getClass().getResourceAsStream("/icon2/EIMP.png"));
        stage.getIcons().add(Appicon);
//        stage.initStyle(StageStyle.UNDECORATED);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch();
    }
}

