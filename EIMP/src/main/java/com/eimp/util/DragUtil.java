package com.eimp.util;

import javafx.scene.Node;

/**
 * 给结点添加可拖拽逻辑
 */
public class DragUtil {
    private Node node;
    private double startLayoutX, startLayoutY;
    private double startX,startY;

    public DragUtil(Node node) {
        this.node = node;
        this.setUpDragHandler();
    }

    /**
     * 设置结点的拖拽逻辑处理
     */
    private void setUpDragHandler(){
        this.node.setOnMousePressed(e->{
            startX = e.getSceneX();
            startY = e.getSceneY();
            startLayoutX = node.getLayoutX();
            startLayoutY = node.getLayoutY();
        });
        this.node.setOnMouseDragged(e->{
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
