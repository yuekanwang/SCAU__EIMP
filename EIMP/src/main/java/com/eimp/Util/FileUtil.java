package com.eimp.Util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

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
}
