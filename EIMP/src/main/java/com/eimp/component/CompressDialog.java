package com.eimp.component;


import cn.hutool.core.img.ImgUtil;
import com.eimp.util.DragUtil;
import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;

public class CompressDialog {
    private static Slider qualitySlider;

    /**
     * 显示压缩操作面板
     * @param owner 所属窗口
     * @param imageUtil 待压缩图片工具包
     * @param callback 回调函数
     */
    public static void show(Stage owner, ImageUtil imageUtil, Consumer<Boolean> callback) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // 布局
        VBox root = new VBox();
        root.setPadding(new javafx.geometry.Insets(15));
        root.setId("CustomPane");
        DragUtil dragUtil = new DragUtil(root,dialog);
        root.getStylesheets().setAll(RenameDialog.class.getResource("/css/CustomPane.css").toExternalForm());
        root.setPrefWidth(300);
        root.setSpacing(20);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        Label promptLabel = new Label("期望品质(存在误差): "+ FileUtil.fileSizeByString((long)(0.8*(double) imageUtil.getSizeOfBytes())));
        promptLabel.setPrefWidth(250);
        // 关闭按钮
        Button closeBtn = new Button();
        closeBtn.setId("closeBtn");
        closeBtn.setTooltip(new Tooltip("关闭"));
        closeBtn.setOnAction(e-> {dialog.close();});
        HBox topBox = new HBox(promptLabel, closeBtn);

        qualitySlider = new Slider(0.1, 1.0, 0.8);
        qualitySlider.setShowTickLabels(true);

        // 质量滑块监听
        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            promptLabel.setText(String.format("期望品质(存在误差): "+ FileUtil.fileSizeByString((long)(newVal.doubleValue() * (double)(imageUtil.getSizeOfBytes())))));
        });

        Label feedbackLabel = new Label();
        feedbackLabel.setMinWidth(200);
        HBox bottomBox = new HBox(10);

        Button confirmButton = new Button("压缩");
        confirmButton.setId("actionbutton");
        bottomBox.getChildren().addAll(feedbackLabel, confirmButton);

        root.getChildren().addAll(topBox,qualitySlider, bottomBox);
        confirmButton.setOnAction(e -> {
            if(compressImage(imageUtil.getFile(),qualitySlider.getValue(),feedbackLabel,dialog)){
                callback.accept(true);
                // 1秒后关闭窗口
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(1), event -> dialog.close())
                );
                timeline.play();
            }
        });

        dialog.setScene(new Scene(root));
        dialog.setTitle("图片压缩");
        dialog.show();
    }

    /**
     * 使用第三方压缩工具,效果一般
     * @param inputFile 输入文件
     * @param quality 压缩比
     * @param feedbackLabel 反馈标签
     * @param stage 所属窗口
     * @return 压缩结果
     */
    private static boolean compressImage(File inputFile, double quality,Label feedbackLabel,Stage stage) {
        try {
            // 输出文件路径（在原文件名后添加 "_压缩"）
            String outputPath = inputFile.getAbsolutePath()
                    .replace(".jpg", "_压缩.jpg")
                    .replace(".png", "_压缩.jpg")
                    .replace(".jpeg","_压缩.jpg");
            File outputFile = new File(outputPath);
            ImgUtil.compress(inputFile,outputFile,(float) quality);
            feedbackLabel.setTextFill(Color.GREEN);
            feedbackLabel.setText("压缩成功");
            return true;
        } catch (Exception e) {
            feedbackLabel.setTextFill(Color.RED);
            feedbackLabel.setText("压缩失败: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }



}

