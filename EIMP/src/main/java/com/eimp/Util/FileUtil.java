package com.eimp.Util;

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
}
