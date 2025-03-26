package com.eimp;

import com.eimp.component.ThumbnailPanel;
import com.eimp.controller.WindowMainController;
import com.eimp.controller.WindowSlideController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Screen;
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
        configureSmartMaximize(stage);//配置智能最大化
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    private void configureSmartMaximize(Stage stage) {
        // 获取屏幕可视区域（扣除任务栏），用Screen得到屏幕可视化区域，去除掉操作系
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        // 窗口最大化监听器,这里是把窗口最大化的范围改了

/*      stage.maximizedProperty()：获取窗口的“是否最大化”属性（BooleanProperty 类型）。
        addListener：添加监听器，当最大化状态变化时触发回调。
        newVal：表示窗口是否进入最大化状态（true 为最大化，false 为取消最大化）。*/
        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // 应用修正后的最大化尺寸
                stage.setX(visualBounds.getMinX());
                stage.setY(visualBounds.getMinY());
                stage.setWidth(visualBounds.getWidth());
                stage.setHeight(visualBounds.getHeight());
            }
        });

        // 初始化时设置最大化参数
        stage.setMaxWidth(visualBounds.getWidth());
        stage.setMaxHeight(visualBounds.getHeight());
    }

    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch();
    }
}

