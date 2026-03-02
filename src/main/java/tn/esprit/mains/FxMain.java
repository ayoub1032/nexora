package tn.esprit.mains;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.utils.DatabaseInitializer;
import tn.esprit.utils.SceneNavigator;

public class FxMain extends Application {

    @Override
    public void start(Stage stage) {
        // Initialize database schema
        DatabaseInitializer.initializeDatabase();
        
        SceneNavigator.init(stage);
        SceneNavigator.goTo("LoginView.fxml", "PiWeb - Authentication", 560, 500);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
