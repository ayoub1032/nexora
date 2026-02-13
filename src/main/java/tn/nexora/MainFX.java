package tn.nexora;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application Entry Point
 */
public class MainFX extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Nexora - Crypto Trading Platform");

        // Load login screen
        showLoginScreen();

        primaryStage.show();
    }

    /**
     * Show login screen
     */
    public static void showLoginScreen() {
        try {
            Parent root = FXMLLoader.load(MainFX.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(MainFX.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("❌ Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show registration screen
     */
    public static void showRegisterScreen() {
        try {
            Parent root = FXMLLoader.load(MainFX.class.getResource("/fxml/register.fxml"));
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(MainFX.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("❌ Error loading register screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show profile screen
     */
    public static void showProfileScreen() {
        try {
            Parent root = FXMLLoader.load(MainFX.class.getResource("/fxml/profile.fxml"));
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(MainFX.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("❌ Error loading profile screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
