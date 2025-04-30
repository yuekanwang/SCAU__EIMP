package com.eimp.util;

import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * 可拖拽逻辑
 */
public class DragUtil {
    private double startLayoutX, startLayoutY;
    private double startX,startY;

    /**
     * 通过拖拽结点控制窗口
     * @param node 拖拽结点
     * @param stage 关联窗口
     */
    public DragUtil(Node node, Stage stage) {
        if(node== null || stage == null) return;

        node.setOnMousePressed(e->{
            startX =e.getSceneX();
            startY =e.getSceneY();
        });
        node.setOnMouseDragged(e->{
            //控制窗口
            stage.setX(getFixedOffset(e.getScreenX() - startX,0, Screen.getPrimary().getBounds().getWidth()));
            stage.setY(getFixedOffset(e.getScreenY() - startY,0, Screen.getPrimary().getBounds().getHeight()));
        });
    }

    /**
     * 为节点添加拖拽功能（相对于父容器移动）
     * @param node 需要拖拽的节点
     */
    public DragUtil(Node node) {
        node.setOnMousePressed(e->{
            startX = e.getSceneX();
            startY = e.getSceneY();
            startLayoutX = node.getLayoutX();
            startLayoutY = node.getLayoutY();
        });
        node.setOnMouseDragged(e->{
            double deltaX = e.getSceneX() - startX;
            double deltaY = e.getSceneY() - startY;
            node.setLayoutX(getFixedOffset(startLayoutX + deltaX,0,node.getParent().prefWidth(-1)-node.prefWidth(-1)));
            node.setLayoutY(getFixedOffset(startLayoutY + deltaY,0,node.getParent().prefHeight(-1)-node.prefHeight(-1)));
        });
    }
    /**
     * 获得移动限制范围内的移动偏移量
     * @param value 原始偏移值
     * @param min 最小负偏移值
     * @param max 最大正偏移值
     * @return 修正偏移值
     */
    private double getFixedOffset(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
