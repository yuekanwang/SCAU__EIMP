package com.eimp.component;

import com.eimp.util.DragUtil;
import com.eimp.util.ImageUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class DeleteDialog {
    /**
     * 显示删除操作面板
     * @param owner 所属窗口
     * @param imageUtil 待删除图片工具包
     * @param callback 回调函数
     */
    public static void show(Stage owner, ImageUtil imageUtil, Consumer<Boolean> callback) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // 布局
        VBox root = new VBox();
        DragUtil dragUtil = new DragUtil(root,dialog);
        root.setPadding(new javafx.geometry.Insets(15));
        root.setId("CustomPane");
        root.getStylesheets().setAll(RenameDialog.class.getResource("/css/CustomPane.css").toExternalForm());
        root.setPrefWidth(320);
        root.setSpacing(20);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("移至回收站");
        titleLabel.setMinWidth(310);
        // 关闭按钮
        Button closeBtn = new Button();
        closeBtn.setId("closeBtn");
        closeBtn.setTooltip(new Tooltip("关闭"));
        closeBtn.setOnAction(e-> {dialog.close();});
        HBox topBox = new HBox(titleLabel, closeBtn);

        Label promptLabel = new Label("确认要删除\""+imageUtil.getFileName()+"\"吗?删除后将移至回收站");
        promptLabel.setAlignment(Pos.CENTER);
        promptLabel.setWrapText(true);
        HBox bottomBox = new HBox(10);
        Label feedbackLabel = new Label();
        feedbackLabel.setMinWidth(180);
        Button cancelButton = new Button("取消");
        cancelButton.setId("actionbutton");
        cancelButton.setOnAction(event -> dialog.close());
        Button confirmButton = new Button("确认");
        confirmButton.setId("actionbutton");
        bottomBox.getChildren().addAll(feedbackLabel,cancelButton, confirmButton);

        root.getChildren().addAll(topBox, promptLabel, bottomBox);
        confirmButton.setOnAction(e -> {
            if(deleteImage(imageUtil)){
                callback.accept(true);
            }else{
                callback.accept(false);
            }
            dialog.close();
        });

        dialog.setScene(new Scene(root));
        dialog.setTitle("确认删除图片需求");
        dialog.show();
    }

    /**
     * 删除图片
     * @param imageUtil 待删除图片
     * @return 结果
     */
    private static boolean deleteImage(ImageUtil imageUtil){
        File file = imageUtil.getFile();
        if (file.exists() && file.isFile()) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().moveToTrash(file))  {
                return true;
            }
        }
        return false;
    }


}
