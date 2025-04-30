package com.eimp.component;

import com.eimp.util.DragUtil;
import com.eimp.util.ImageUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.util.function.Consumer;


public class RenameDialog {
    /**
     * 显示重命名操作窗口
     * @param owner 所属舞台
     * @param imageUtil 待操作图片工具包
     * @param callback 回调函数
     */
    public static void show(Stage owner, ImageUtil imageUtil, Consumer<String> callback) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        String nameWithoutExtension = imageUtil.getFileName().substring(0, imageUtil.getFileName().lastIndexOf("."));
        final String fileExtension = imageUtil.getFileName().substring(imageUtil.getFileName().lastIndexOf(".")).toLowerCase();

        VBox root = new VBox();
        root.setId("CustomPane");
        DragUtil dragUtil = new DragUtil(root,dialog);
        root.getStylesheets().setAll(RenameDialog.class.getResource("/css/CustomPane.css").toExternalForm());
        root.setPrefWidth(350);
        root.setSpacing(20);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        Label promptLabel = new Label("输入图片名称");
        promptLabel.setPrefWidth(325);
        // 关闭按钮
        Button closeBtn = new Button();
        closeBtn.setId("closeBtn");
        closeBtn.setTooltip(new Tooltip("关闭"));
        closeBtn.setOnAction(e-> {dialog.close();});
        HBox topBox = new HBox(promptLabel, closeBtn);

        HBox inputBox = new HBox(5);
        TextField nameField = new TextField(nameWithoutExtension);
        nameField.requestFocus();
        nameField.selectAll();
        nameField.setPrefWidth(300);
        Label extensionLabel = new Label(fileExtension);
        inputBox.getChildren().addAll(nameField, extensionLabel);
        Label feedbackLabel = new Label();
        feedbackLabel.setMinWidth(200);
        HBox bottomBox = new HBox(10);
        Button cancelButton = new Button("取消");
        cancelButton.setId("actionbutton");
        Button confirmButton = new Button("确定");
        confirmButton.setId("actionbutton");
        bottomBox.getChildren().addAll(feedbackLabel,cancelButton, confirmButton);

        root.getChildren().addAll(topBox, inputBox, bottomBox);

        cancelButton.setOnAction(e -> dialog.close());
        confirmButton.setOnAction(e -> {
            File newFile = new File(imageUtil.getDirectory(), nameField.getText() + fileExtension);
            if (imageUtil.getFile().renameTo(newFile)) {
                // 更新UI
                feedbackLabel.setText("");
                callback.accept(newFile.getAbsolutePath());
                dialog.close();
            }else{
                feedbackLabel.setText("文件名已存在");
                feedbackLabel.setStyle("-fx-text-fill: red;");
            }
        });

        dialog.setScene(new Scene(root));
        dialog.setTitle("重命名");
        dialog.show();
    }

}