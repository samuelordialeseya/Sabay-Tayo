package com.sabaytayo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sabaytayo/primary.fxml"));
        Parent root = loader.load();

        scene = new Scene(root, 1280, 720);
        stage.setTitle("Sabay Tayo? — a branching bus-ride dialogue game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/sabaytayo/" + fxml + ".fxml"));
        scene.setRoot(loader.load());
    }

    public static void main(String[] args) {
        launch();
    }
}
