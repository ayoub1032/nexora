package tn.esprit.controllers;

import javafx.fxml.FXML;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;

public class RoleSelectionController {

    @FXML
    public void goAdmin() {
        UserContext.setRole("ADMIN");
        SceneNavigator.goTo("AdminView.fxml", "Admin Dashboard", 1200, 750);
    }

    @FXML
    public void goUser() {
        UserContext.setRole("USER");
        SceneNavigator.goTo("UserView.fxml", "User Dashboard", 1200, 750);
    }

    @FXML
    public void goToAssets() {
        SceneNavigator.goTo("AssetView.fxml", "Assets Management", 900, 700);
    }

    @FXML
    public void goToOrders() {
        SceneNavigator.goTo("OrderView.fxml", "Orders Management", 900, 700);
    }

    @FXML
    public void goToPortfolios() {
        SceneNavigator.goTo("PortfolioView.fxml", "Portfolios Management", 900, 700);
    }
}
