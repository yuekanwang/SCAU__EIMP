package com.eimp;

import com.eimp.controller.WindowSlideController;
import com.eimp.util.ImageUtil;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlideWindow extends Application {
    private static Map<String, List<WindowSlideController>> slideWindowControllers = new HashMap<>();
    private WindowSlideController controller;
    private static Stage stage;
    /**
     * 从主窗口导入的图片所属信息工具
     */
    private static ImageUtil imageUtil;
    /**
     * 从主窗口打开幻灯片的方式
     */
    private static LaunchMethodEnum launchMethodEnum;
    /**
     * 从主窗口打开幻灯片的方式枚举值
     */
    public enum LaunchMethodEnum{
        PLAY(1), CLICK(2);
        final int num;
        LaunchMethodEnum(int num){
            this.num = num;
        }
    }

    private double width;
    private double height;
    @Override
    public void start(Stage stage) throws IOException {
        SlideWindow.stage = stage;
        // 设置窗口最小尺寸
        stage.setMinWidth(590);
        stage.setMinHeight(580);
        stage.setTitle("EIMP-图片裁剪");
        // 默认窗口大小
//        double width = 900;
//        double height = 700;
        // 根据屏幕大小自适应设置长宽
        try {
            Rectangle2D bounds = Screen.getScreens().getFirst().getBounds();
            width = bounds.getWidth() / 1.6;
            height = bounds.getHeight() / 1.4;
        } catch (Exception e){
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(SlideWindow.class.getResource("/fxml/WindowSlide.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setScene(scene);

        // 获取控制器实例
        controller = fxmlLoader.getController();
        Image Appicon = new Image(getClass().getResourceAsStream("/icon2/EIMP.png"));
        stage.getIcons().add(Appicon);
        stage.initStyle(StageStyle.UNDECORATED);
        controller.setUpKeyEvent(scene);

        // 同文件夹的controller映射
        if(!slideWindowControllers.containsKey(imageUtil.getDirectory().getAbsolutePath())){
            slideWindowControllers.put(imageUtil.getDirectory().getAbsolutePath(), new ArrayList<>());
        }
        slideWindowControllers.get(imageUtil.getDirectory().getAbsolutePath()).add(controller);

        switch(launchMethodEnum){
            case PLAY:
                stage.show();
                //导入所选图片的信息工具
                controller.importImage(SlideWindow.imageUtil);
                controller.playing();

                break;
            case CLICK:
                // 添加淡入效果
                controller.rootPane.setOpacity(0.5);
                stage.show();
                //导入所选图片的信息工具
                controller.importImage(SlideWindow.imageUtil);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), controller.rootPane);
                fadeIn.setFromValue(0.5);
                fadeIn.setToValue(1);
                fadeIn.play();
                break;
        }

    }

    public static void main(ImageUtil imageUtil, LaunchMethodEnum launchMethodEnum) {
        SlideWindow.launchMethodEnum = launchMethodEnum;
        SlideWindow.imageUtil = imageUtil;
        if (Platform.isFxApplicationThread()) {
            Stage stage = new Stage();
            SlideWindow slideWindow = new SlideWindow();
            try {
                slideWindow.start(stage);
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

    /**
     * 刷新所有同一文件夹下的幻灯片窗口
     * @param oldPath 旧的图像绝对路径
     * @param newImageUtil 更新后的图片属性包
     */
    public static void flushSlideWindows(String oldPath,ImageUtil newImageUtil){
        if(slideWindowControllers.containsKey(newImageUtil.getDirectory().getAbsolutePath())){
            for(WindowSlideController windowSlideController : slideWindowControllers.get(newImageUtil.getDirectory().getAbsolutePath())){
                windowSlideController.flush(oldPath, newImageUtil);
            }
        }
    }

    /**
     * 刷新被删除的幻灯片
     * @param oldPaths 被删除的照片路径
     * @param directory 原目录
     */
    public static void flushSlideWindows(List<String> oldPaths,String directory){
        if(slideWindowControllers.containsKey(directory)){
            for(WindowSlideController windowSlideController : slideWindowControllers.get(directory)){
                windowSlideController.flush(oldPaths);
            }
        }
    }
    public static void removeSlideWindowController(String key,WindowSlideController controller){
        slideWindowControllers.get(key).remove(controller);
    }
}