package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {
    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void goTo(String fxml, String title, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/" + fxml));
            Scene scene = new Scene(loader.load(), w, h);

            // attach global CSS theme
            scene.getStylesheets().add(SceneNavigator.class.getResource("/styles/app.css").toExternalForm());

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Navigation error to " + fxml + ": " + e.getMessage(), e);
        }
    }
}