package tn.esprit.mains;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.utils.SceneNavigator;

public class FxMain extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        SceneNavigator.goTo("RoleSelection.fxml", "PiWeb - Start", 560, 360);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
