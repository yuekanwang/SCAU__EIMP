package org.example.zpgl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class zpglApplication extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("zpyl-view.fxml")));
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setTitle("电子照片管理系统");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}