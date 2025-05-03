package com.eimp.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 图片信息处理工具
 *
 * @author Cyberangel2023
 */
public class ImageUtil {
    private static final String DEFAULT_VALUE = "NULL";
    // 文件
    private File file;
    // 所在文件夹
    private File directory;
    // 文件名
    private String fileName;
    // 文件类型
    private String fileType;
    // 绝对路径
    private String absolutePath;
    // 图片大小 单位为字节
    private long sizeOfBytes;
    // 时间格式
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");
    // 文件创建时间
    private String creationTime = DEFAULT_VALUE;
    // 修改时间
    private String lastModifiedTime = DEFAULT_VALUE;
    // 访问时间
    private String lastAccessTime = DEFAULT_VALUE;
    public ImageUtil(File file) {
        this.file = file;
        this.fileName = file.getName();
        this.fileType = this.fileName.toUpperCase().substring(this.fileName.lastIndexOf("."));
        this.absolutePath = file.getAbsolutePath();
        this.sizeOfBytes = file.length();
        this.directory = file.getParentFile();
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            LocalDateTime creationLocalTime = attr.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime lastModifiedLocalTime = attr.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime lastAccessLocalTime = attr.lastAccessTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (creationLocalTime != null) {
                creationTime = DATE_TIME_FORMATTER.format(creationLocalTime);
                lastModifiedTime = DATE_TIME_FORMATTER.format(lastModifiedLocalTime);
                lastAccessTime = DATE_TIME_FORMATTER.format(lastAccessLocalTime);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public long getSizeOfBytes() {
        return sizeOfBytes;
    }

    public void setSizeOfBytes(long sizeOfBytes) {
        this.sizeOfBytes = sizeOfBytes;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(String lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getURL() {
        return this.file.toPath().toUri().toString();
    }
}
