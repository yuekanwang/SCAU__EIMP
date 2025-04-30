package com.eimp.component;

import com.eimp.CropWindow;
import com.eimp.SlideWindow;
import com.eimp.controller.ControllerMap;
import com.eimp.controller.WindowMainController;
import com.eimp.util.DragUtil;
import com.eimp.util.FileUtil;
import com.eimp.util.ImageUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.util.*;
import java.util.List;


public class ImageInfoWindow extends Application {
    /**
     * 属性窗口映射
     */
    private static Map<String,Stage> imageInfoWindowMap = new HashMap<>();
    /**
     * 当前图片属性面板
     */
    private static ImageInfoPane imageInfoPane;
    @Override
    public void start(Stage stage) throws Exception {
        imageInfoPane.getStylesheets().setAll(getClass().getResource("/css/CustomPane.css").toExternalForm());
        Scene scene = new Scene(imageInfoPane);
        stage.setScene(scene);
        stage.setTitle("图像属性");

        Image Appicon = new Image(getClass().getResourceAsStream("/icon2/EIMP.png"));
        stage.getIcons().add(Appicon);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    /**
     * 构造图像属性窗口
     * @param imageUtil 图像属性工具包
     * @param width 窗口宽
     * @param height 窗口高
     */
    public static void main(ImageUtil imageUtil, double width, double height, Window Owner) {
        if (Platform.isFxApplicationThread()&&!imageInfoWindowMap.containsKey(imageUtil.getAbsolutePath())) {
            Stage stage = new Stage();
            stage.initOwner(Owner);
            imageInfoWindowMap.put(imageUtil.getAbsolutePath(),stage);
            stage.setWidth(width);
            stage.setHeight(height);
            ImageInfoWindow.imageInfoPane = new ImageInfoPane(imageUtil, width, width);
            ImageInfoWindow imageInfoWindow = new ImageInfoWindow();
            try {
                imageInfoWindow.start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            imageInfoWindowMap.get(imageUtil.getAbsolutePath()).requestFocus();
        }
    }

    /**
     * 获取窗口
     * @param absolutePath 图像绝对路径
     * @return 窗口
     */
    public static Stage getStage(String absolutePath) {
        return imageInfoWindowMap.get(absolutePath);
    }

    /**
     * 关闭并移除窗口的映射
     * @param absolutePath 图像绝对路径
     * @return 结果
     */
    public static boolean removeStage(String absolutePath) {
        if (imageInfoWindowMap.containsKey(absolutePath)) {
            imageInfoWindowMap.get(absolutePath).close();
            imageInfoWindowMap.remove(absolutePath);
            return true;
        }
        return false;
    }

    /**
     * 更新图片属性
     * @param newImageUtil 图片属性工具包
     */
    public static void updateImageInfo(String originalKey,ImageUtil newImageUtil) {
        if(originalKey==null||newImageUtil==null) {return;}
        if (imageInfoWindowMap.size()>0) {
            Stage stage = imageInfoWindowMap.get(originalKey);
            if(stage!=null) {
                stage.requestFocus();
                imageInfoWindowMap.remove(originalKey);
                if(imageInfoWindowMap.containsKey(newImageUtil.getAbsolutePath())) {
                    ImageInfoWindow.removeStage(newImageUtil.getAbsolutePath());
                }
                imageInfoPane.setImageUtil(newImageUtil);
                imageInfoWindowMap.put(newImageUtil.getAbsolutePath(), stage);
            }
        }
    }
}


/**
 * 图片属性面板
 */
class ImageInfoPane extends VBox{
    private ImageUtil imageUtil;
    private Image image;
    private Stage stage;// 所属窗口
    /**
     * 色卡列表
     */
    private List<Color> dominantColors;


    public ImageInfoPane(double width, double height) {
        this.setId("CustomPane");
        this.setPrefWidth(width);
        this.setPrefHeight(height);
        this.stage = ImageInfoWindow.getStage(imageUtil.getAbsolutePath());
        DragUtil dragUtil = new DragUtil(this,stage);
    }

    public ImageInfoPane(ImageUtil imageUtil,double width, double height) {
        this.setId("CustomPane");
        this.stage = ImageInfoWindow.getStage(imageUtil.getAbsolutePath());
        DragUtil dragUtil = new DragUtil(this,stage);       //添加拖拽逻辑
        this.imageUtil = imageUtil;
        this.setPrefWidth(width);
        this.setPrefHeight(height);
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
        closeBtn.setOnAction(e-> {
            if(!ImageInfoWindow.removeStage(imageUtil.getAbsolutePath())){
                System.out.println("图片属性面板移除bug");
            }
        });
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
            case "改尺寸":
                CropWindow.main(this.imageUtil);
                break;
            case "压缩":
//                compressImage();
                break;
            case "打开":
                FileUtil.openContainingFolder(imageUtil.getAbsolutePath());
                break;

            // 其他操作处理...
        }
    }

    private void showRenameDialog() {
        RenameDialog.show(this.stage, imageUtil,result->{
            WindowMainController controller = (WindowMainController) ControllerMap.getController(WindowMainController.class);
            controller.flushImage();
            ImageUtil newImageUtil = new ImageUtil(new File(result));
            SlideWindow.flushSlideWindows(imageUtil.getAbsolutePath(),newImageUtil);
            ImageInfoWindow.removeStage(imageUtil.getAbsolutePath());
            ImageInfoWindow.main(newImageUtil,340,250,stage.getOwner());
            Notifications.create()
                    .text("重命名成功")
                    .hideAfter(Duration.seconds(1))
                    .position(Pos.CENTER)
                    .owner(ImageInfoWindow.getStage(result))
                    .darkStyle()
                    .show();
        });
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
