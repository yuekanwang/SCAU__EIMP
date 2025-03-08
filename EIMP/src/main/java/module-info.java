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

    opens com.eimp.Controller to javafx.fxml;
    exports com.eimp.Controller; // 可选，如果其他模块需要访问

    opens com.eimp to javafx.fxml;
    exports com.eimp;
}