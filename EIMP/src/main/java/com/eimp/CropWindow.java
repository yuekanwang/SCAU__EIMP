package com.eimp;

import com.eimp.controller.WindowCropController;
import com.eimp.util.ImageUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class CropWindow extends Application {
    private static Stage stage;
    /**
     * 从主窗口导入的图片所属信息工具
     */
    private static ImageUtil imageUtil;
    /**
     * 图片裁剪窗口控制器
     */
    private WindowCropController controller;
    @Override
    public void start(Stage stage) throws IOException {
        CropWindow.stage = stage;
        // 设置窗口最小尺寸
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/WindowCrop.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);

        // 获取控制器实例
        controller = fxmlLoader.getController();

        Image Appicon = new Image(getClass().getResourceAsStream("/icon2/EIMP.png"));
        stage.getIcons().add(Appicon);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("图像裁剪");
        stage.show();
        Platform.runLater(()->controller.importImage(CropWindow.imageUtil));
    }

    public static void main(ImageUtil imageUtil) {
        CropWindow.imageUtil = imageUtil;
        if (Platform.isFxApplicationThread()) {
            Stage stage = new Stage();
            CropWindow cropWindow = new CropWindow();
            try {
                cropWindow.start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            launch();
        }
    }

    public static Stage getStage() {
        return stage;
    }
}

// TODO 添加撤销/重做功能  实现裁剪历史记录