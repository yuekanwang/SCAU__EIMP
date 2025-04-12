package com.eimp.component;

import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class ImageInfoPane extends VBox{
    private ImageUtil imageUtil;
    private Image image;
    /**
     * 色卡列表
     */
    private List<Color> dominantColors;

    public ImageInfoPane() {
        this.setId("imageInfoPane");
    }

    public ImageInfoPane(ImageUtil imageUtil) {
        this.setId("imageInfoPane");
        this.imageUtil = imageUtil;
        initialize();
    }

    /**
     * 面板初始化,提取色卡,构建所有组件并布局
     */
    private void initialize() {
        this.image = new Image(imageUtil.getURL());
        extractDominantColors(5,50);
        setSpacing(10);

        createColorPalette();
        createInfoSection();
    }

    /**
     * 提取图片色彩频率前五的互斥色号
     * @param step 采样步长
     * @param threshold 色差阈值 - 颜色差异阈值，决定颜色是否互斥。基于RGB欧氏距离，值越大允许的颜色差异越小。
     */
    private void extractDominantColors(int step,double threshold) {
        PixelReader pixelReader = image.getPixelReader();
        Map<Color, Integer> colorCount = new HashMap<>();

        // 采样策略（减少计算量）
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                Color color = pixelReader.getColor(x, y);
                // 忽略透明或半透明像素
                if (color.getOpacity() < 0.5) {
                    continue;
                }
                colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
            }
        }

        // 按频率排序
        List<Map.Entry<Color, Integer>> sortedEntries = new ArrayList<>(colorCount.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 选择互斥颜色
        List<Color> selectedColors = new ArrayList<>();
        for (Map.Entry<Color, Integer> entry : sortedEntries) {
            Color current = entry.getKey();
            if (isDifferentEnough(current, selectedColors, threshold)) {
                selectedColors.add(current);
                if (selectedColors.size() >= 5) {
                    break;
                }
            }
        }

//        dominantColors = colorCount.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//                .limit(5) // 取前5种主要颜色
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
//        List<Color> dominantColorsSorted = new ArrayList<>();
//        threshold = 50;
//        for(Color c : selectedColors) {
//            if (isDifferentEnough(c, dominantColorsSorted, threshold)) {
//                dominantColorsSorted.add(c);
//            }
//        }
//
//        dominantColors = dominantColorsSorted;
        dominantColors = selectedColors;
    }


    /**
     * 检查颜色差异是否足够大
     * @param color 待选择的的颜色
     * @param selectedColors 已选择的色卡列表
     * @param threshold 阈值
     * @return 待选择的色差是否大
     */
    private boolean isDifferentEnough(Color color, List<Color> selectedColors, double threshold) {
        for (Color selected : selectedColors) {
            if (colorDistance(color, selected) <= threshold) {
                return false;
            }
        }
        return true;
    }


    /**
     * 计算RGB欧氏距离
     * @param c1 颜色1
     * @param c2 颜色2
     * @return RGB欧氏距离
     */
    private double colorDistance(Color c1, Color c2) {
        double r1 = c1.getRed() * 255;
        double g1 = c1.getGreen() * 255;
        double b1 = c1.getBlue() * 255;
        double r2 = c2.getRed() * 255;
        double g2 = c2.getGreen() * 255;
        double b2 = c2.getBlue() * 255;
        double dr = r1 - r2;
        double dg = g1 - g2;
        double db = b1 - b2;
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /**
     * 构建信息组件
     */
    private void createInfoSection() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        // 添加信息行
        addInfoRow(grid, 0, "图片名称：", imageUtil.getFileName().substring(0,imageUtil.getFileName().lastIndexOf(".")) , "重命名");
        addInfoRow(grid, 1, "图片类型：", imageUtil.getFileType(), "转格式");
        addInfoRow(grid, 2, "图片大小：", FileUtil.getFormatFileSize(imageUtil.getSizeOfBytes()), "压缩");
        addInfoRow(grid, 3, "图片尺寸：", getImageSize(this.image), "改尺寸");
        addInfoRow(grid, 4, "图片位置：", imageUtil.getAbsolutePath(), "打开");
        Label label = new Label("修改日期："+imageUtil.getLastModifiedTime());

        Separator separator = new Separator();

        this.getChildren().addAll(separator, grid,label);
    }

    /**
     * 获取图片尺寸格式字符串
     * @param image 图片
     * @return 格式字符串
     */
    private String getImageSize(Image image) {
        return String.format("%d×%d", (int)image.getWidth(), (int)image.getHeight());
    }

    /**
     * 栅格布局添加信息行
     * @param grid 栅格布局
     * @param row 行号
     * @param label 属性
     * @param value 属性值
     * @param buttonText 按钮功能
     */
    private void addInfoRow(GridPane grid, int row, String label, String value, String buttonText) {
        Label infoLabel = new Label(label + value);
        grid.add(infoLabel, 0, row);

        Button actionButton = new Button(buttonText);
        actionButton.setId("actionbutton");

        // 按钮事件绑定
        actionButton.setOnAction(e -> handleAction(buttonText));
        grid.add(actionButton, 1, row);
    }

    /**
     * 创建顶部栏,色卡组件
     */
    private void createColorPalette() {
        HBox topBox = new HBox(10);
        HBox colorBox = new HBox(5);
        colorBox.setMinWidth(230);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        // 色卡组件
        dominantColors.forEach(color -> {
            Rectangle rect = new Rectangle(40,20);
            rect.setFill(color);
            rect.setOnMouseEntered(e->{
                Tooltip tooltip = new Tooltip("#" + color.toString().substring(2,8).toUpperCase());
                Tooltip.install(rect, tooltip);
                tooltip.setShowDelay(Duration.millis(10));
            });
            colorBox.getChildren().add(rect);
        });
        // 关闭按钮
        Button closeBtn = new Button();
        closeBtn.setId("closeBtn");
        closeBtn.setTooltip(new Tooltip("关闭"));
        closeBtn.setOnAction(e-> this.setVisible(false));
        // 色卡标签
        Label label = new Label("色卡: ");
        label.setStyle("-fx-min-width: 40;-fx-pref-width: 40");
        topBox.getChildren().addAll(label,colorBox,closeBtn);

        this.getChildren().add(0, topBox);
    }

    /**
     * 处理功能按钮点击事件
     * @param action 功能
     */
    private void handleAction(String action) {
        switch (action) {
            case "重命名":
                showRenameDialog();
                break;
            case "转格式":
//                showConvertDialog();
                break;
            case "压缩":
//                compressImage();
                break;
            case "打开":
                this.openContainingFolder(imageUtil.getAbsolutePath());
                break;

            // 其他操作处理...
        }
    }

    private void showRenameDialog() {
        TextInputDialog dialog = new TextInputDialog(imageUtil.getFileName().substring(0,imageUtil.getFileName().lastIndexOf(".")));
        dialog.setTitle("重命名");
        dialog.setHeaderText("输入新文件名：");
        dialog.showAndWait().ifPresent(newName -> {
            File newFile = new File(imageUtil.getDirectory(), newName+imageUtil.getFileType().toLowerCase());
            if (imageUtil.getFile().renameTo(newFile)) {
                // 更新UI

            }
        });
    }

    /**
     * 打开图片所在文件资源管理器位置,支持多操作系统
     * @param filePath 图片绝对路径
     */
    public static void openContainingFolder(String filePath) {
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.exists()) {
            System.out.println("文件不存在: " + filePath);
            return;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String command;

            if (os.contains("win")) {
                // Windows: 高亮选中文件
                command = "explorer /select,\"" + file.getAbsolutePath() + "\"";
            } else if (os.contains("mac")) {
                // macOS: 定位到文件所在目录并选中
                command = "open -R \"" + file.getAbsolutePath() + "\"";
            } else {
                // Linux: 只能打开目录（无法高亮文件）
                command = "xdg-open \"" + file.getParent() + "\"";
            }

            // 执行命令
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新图片信息面板
     * @param imageUtil 图片信息工具
     */
    public void setImageUtil(ImageUtil imageUtil) {
        this.getChildren().clear();
        this.imageUtil = imageUtil;
        this.image = new Image(imageUtil.getURL());
        initialize();
    }
}
