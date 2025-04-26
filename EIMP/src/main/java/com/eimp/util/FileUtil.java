package com.eimp.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 文件处理工具
 *
 * @author Cyberangel2023
 */
public class FileUtil {
    /**
     * 判断照片是否是支持的格式
     */
    public static boolean isSupportImageFormat(File file) {
        String fileName = file.getName().toUpperCase();
        // 支持的照片格式：.JPG、.JPEG、.GIF、.PNG、和.BMP。
        return fileName.endsWith("JPG") ||
                fileName.endsWith("JPEG") ||
                fileName.endsWith("GIF") ||
                fileName.endsWith("PNG") ||
                fileName.endsWith("BMP");
    }

    /**
     * 获取图片名称
     */
    public static String getFileName(File file) {
        return FileSystemView.getFileSystemView().getSystemDisplayName(file);
    }

    /**
     *  获取文件图标
     */
    public static ImageView getFileIcon(File file) {
        Image image = ((ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file)).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.BITMASK);
        bufferedImage.createGraphics().drawImage(image, 0, 0, null);
        WritableImage writableImage = SwingFXUtils.toFXImage(bufferedImage, null);
        return new ImageView(writableImage);
    }

    /**
     * 常用文件大小单位
     */
    private static final double KB = 1024.0;
    private static final double MB = 1024.0*1024.0;
    private static final double GB = 1024.0*1024.0*1024.0;

    /**
     * 获取文件大小格式化字符串
     * @param fileLength 文件大小
     * @return 文件大小格式化字符串
     */
    public static String getFormatFileSize(long fileLength){
        String fileStandardSize = null;
        if (fileLength < KB){
            fileStandardSize = String.format("%dByte", fileLength);
        }else if(fileLength < MB){
            fileStandardSize = String.format("%.2fKB", fileLength/KB);
        }else if (fileLength < GB){
            fileStandardSize = String.format("%.2fMB", fileLength/MB);
        }else {
            fileStandardSize = String.format("%.2fGB", fileLength/GB);
        }
        return fileStandardSize;
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

    private static final double ONE_ZERO_TWO_FOUR = 1024;

    public static String fileSizeByString(long sizeInBytes) {
        String size;
        if (sizeInBytes < ONE_ZERO_TWO_FOUR) {
            size = String.format("%d B", sizeInBytes);
        } else if (sizeInBytes < ONE_ZERO_TWO_FOUR * ONE_ZERO_TWO_FOUR) {
            size = String.format("%.2f KB",  (sizeInBytes / ONE_ZERO_TWO_FOUR));
        } else {
            size = String.format("%.2f MB",  (sizeInBytes / ONE_ZERO_TWO_FOUR / ONE_ZERO_TWO_FOUR));
        }
        return size;
    }
}
