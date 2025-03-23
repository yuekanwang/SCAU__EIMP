package com.eimp.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * 用来存放controller的全局类
 * 用于在其他地方获取controller中的组件
 *
 * @author Cyberangel2023
 */
public class ControllerMap {
    private static final Map<String, Object> controllerMap = new HashMap<>();

    private ControllerMap() {

    }

    public static void addController(Object controller) {
        controllerMap.put(controller.getClass().getName(), controller);
    }

    public static Object getController(Class<?> ControllerClass) {
        return controllerMap.get(ControllerClass.getName());
    }
}
