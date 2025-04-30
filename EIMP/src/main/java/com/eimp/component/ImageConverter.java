package com.eimp.component;
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
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class ImageConverter {
    /**
     * 显示图片格式转换操作窗口
     * @param owner 所属窗口
     * @param imageUtil 待操作图片属性包
     * @param callback 回调函数
     */
    public static void show(Stage owner, ImageUtil imageUtil, Consumer<File> callback) {
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

        Label promptLabel = new Label("目标格式");
        promptLabel.setPrefWidth(250);
        // 关闭按钮
        Button closeBtn = new Button();
        closeBtn.setId("closeBtn");
        closeBtn.setTooltip(new Tooltip("关闭"));
        closeBtn.setOnAction(e-> {dialog.close();});
        HBox topBox = new HBox(promptLabel, closeBtn);
        // 格式菜单
        ComboBox<String> formatComboBox;
        formatComboBox = new ComboBox<>();
        formatComboBox.setPrefWidth(280);
        formatComboBox.getItems().addAll("JPG格式(*.jpg,*.jpeg)", "PNG格式(*.png)", "BMP格式(*.bmp)");
        formatComboBox.setValue("PNG格式(*.png)");

        Label feedbackLabel = new Label();
        feedbackLabel.setMinWidth(200);
        HBox bottomBox = new HBox(10);

        Button confirmButton = new Button("保存");
        confirmButton.setId("actionbutton");
        bottomBox.getChildren().addAll(feedbackLabel, confirmButton);

        root.getChildren().addAll(topBox,formatComboBox, bottomBox);
        confirmButton.setOnAction(e -> {
            File ret = convertImage(imageUtil,formatComboBox.getValue(),feedbackLabel,dialog);
            if(ret != null){
                callback.accept(ret);
                // 1秒后关闭窗口
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(1), event -> dialog.close())
                );
                timeline.play();
            }
        });

        dialog.setScene(new Scene(root));
        dialog.setTitle("图片格式转换");
        dialog.show();
    }

    /**
     * 图片格式转换
     * @param imageUtil 待操作图片工具包
     * @param format 转换成的格式
     * @param feedbackLabel 反馈标签
     * @param stage 所属窗口
     * @return 结果
     */
    private static File convertImage(ImageUtil imageUtil,String format,Label feedbackLabel,Stage stage) {
        try {
            // 读取原始图片
            BufferedImage image = Imaging.getBufferedImage(imageUtil.getFile());

            // 创建文件夹选择器保存转换后的图片
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("保存转换后的图片");
            directoryChooser.setInitialDirectory(imageUtil.getDirectory());

            File outDirectory = directoryChooser.showDialog(stage);
            if (outDirectory == null) return null;

            File outputFile = FileUtil.getOutputFile(outDirectory.getAbsolutePath(), imageUtil.getFileName(),format.substring(0,format.indexOf("格")));
            // 转换并保存图片
            ImageFormats targetFormat = getFormatFromString(format);
            Imaging.writeImage(image, outputFile, targetFormat);

            feedbackLabel.setTextFill(Color.GREEN);
            feedbackLabel.setText("转换成功");
            return outputFile;
        } catch (IOException e) {
            feedbackLabel.setTextFill(Color.RED);
            feedbackLabel.setText("转换失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            feedbackLabel.setTextFill(Color.RED);
            feedbackLabel.setText("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取转换格式
     * @param format 转换格式字符串
     * @return 图片格式
     */
    private static ImageFormats getFormatFromString(String format) {
        switch (format.toUpperCase()) {
            case "JPG格式(*.jpg,*.jpeg)": return ImageFormats.JPEG;
            case "PNG格式(*.png)": return ImageFormats.PNG;
            case "BMP格式(*.bmp)": return ImageFormats.BMP;
            default: return ImageFormats.PNG;
        }
    }
}
