package tn.esprit.controllers;

import javafx.fxml.FXML;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;

public class RoleSelectionController {

    @FXML
    private void goAdmin() {
        UserContext.clear();
        SceneNavigator.goTo("AdminView.fxml", "Admin - Wallet & Transactions", 1150, 680);
    }

    @FXML
    private void goUserDemo() {
        UserContext.clear();
        SceneNavigator.goTo("UserDemoView.fxml", "User Mode (Demo)", 1150, 680);
    }
}