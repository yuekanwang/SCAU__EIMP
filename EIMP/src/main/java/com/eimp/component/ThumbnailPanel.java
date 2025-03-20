package com.eimp.component;

import com.eimp.Util.ImageUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class ThumbnailPanel extends BorderPane {
    // 图片能展示的最长名字
    private static final int MAX_NAME = 20;
    // 图片
    private ImageView imageView;
    // 图片工具
    private ImageUtil imageUtil;

    public ThumbnailPanel(ImageUtil imageUtil) {
        setMaxWidth(10);
        setMaxHeight(10);
        setCache(false);
        this.imageUtil = imageUtil;
        this.imageView = new ImageView(new Image(imageUtil.getURL(), 90, 90, true, true, true));
        this.imageView.setPreserveRatio(true);

        setCenter(imageView);
    }
}
