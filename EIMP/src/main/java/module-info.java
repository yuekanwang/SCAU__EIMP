module com.eimp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.swing;
    requires org.apache.commons.imaging;
    requires com.madgag.gif.fmsware;
    requires thumbnailator;
    requires cn.hutool;


    opens com.eimp.controller to javafx.fxml;
    exports com.eimp.controller;


    opens com.eimp to javafx.fxml;
    exports com.eimp;
}