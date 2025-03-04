module org.example.zpgl {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.zpgl to javafx.fxml;
    exports org.example.zpgl;
}