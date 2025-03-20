package com.eimp;

import com.eimp.Controller.WindowSlideController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
/*
 *
 * @author yuekanwang
 */
public class SlideWindow extends Application {
    @Override
    public void start(Stage stage) throws IOException {


        //根据屏幕大小自适应设置长宽
        double width = 900;
        double height = 700;
        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            width = bounds.getWidth() / 1.6;
            height = bounds.getHeight() / 1.4;
        } catch (Exception e){
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(SlideWindow.class.getResource("/fxml/WindowSlide.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();

        // 获取控制器实例
        WindowSlideController controller = fxmlLoader.getController();
        // 延迟操作
        Platform.runLater(() -> {
            controller.notifyPreloader();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}