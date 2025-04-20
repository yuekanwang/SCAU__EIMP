package com.eimp.component;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * 裁剪框外围图片遮罩
 */
public class CropRectMasker{
    /**
     * 父面板
     */
    private final Pane parentPane;
    /**
     * 镂空子面板
     */
    private final AnchorPane targetPane;
    /**
     * 复合动态遮罩
     */
    private ObjectBinding<Shape> dynamicMask;

    /**
     * 构建复合镂空遮罩
     * @param parentPane 父面板
     * @param targetPane 镂空子面板
     */
    public CropRectMasker(Pane parentPane, AnchorPane targetPane) {
        this.parentPane = parentPane;
        this.targetPane = targetPane;
        initDynamicMask();
    }

    /**
     * 初始化遮罩属性加监听器
     */
    private void initDynamicMask() {
        // 动态绑定父容器和AnchorPane属性
        dynamicMask = Bindings.createObjectBinding(() -> {
                    // 创建全屏蒙版（匹配父Pane尺寸）
                    Rectangle fullArea = new Rectangle(
                            0, 0,
                            parentPane.getWidth(),
                            parentPane.getHeight()
                    );

                    // 创建挖空区域（匹配目标AnchorPane）
                    Rectangle hole = new Rectangle(
                            targetPane.getLayoutX()+targetPane.getTranslateX(),    // 使用绝对坐标
                            targetPane.getLayoutY()+targetPane.getTranslateY(),
                            targetPane.getWidth(),
                            targetPane.getHeight()
                    );

                    // 执行形状运算
                    Shape mask = Shape.subtract(fullArea, hole);
                    mask.setFill(Color.rgb(0,0,0,0.7));
                    // 添加事件穿透（允许操作目标AnchorPane）
                    mask.setMouseTransparent(true);

                    return mask;

                },  // 绑定以下依赖属性
                parentPane.widthProperty(),      // 父容器宽度变化
                parentPane.heightProperty(),     // 父容器高度变化
                targetPane.layoutXProperty(),    // AnchorPane位置变化
                targetPane.layoutYProperty(),
                targetPane.widthProperty(),      // AnchorPane尺寸变化
                targetPane.heightProperty(),
                targetPane.translateXProperty(),
                targetPane.translateYProperty()
        );
        // 添加蒙版更新监听
        dynamicMask.addListener((obs, old, newMask) -> {
            parentPane.getChildren().removeIf(n -> n instanceof Shape);
            parentPane.getChildren().add(newMask);
        });
    }

    // 获取动态蒙版引用
    public Shape getDynamicMask() {
        return dynamicMask.get();
    }
}
