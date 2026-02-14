package tn.esprit.controllers;

import javafx.fxml.FXML;
import tn.esprit.utils.SceneNavigator;

public class RoleSelectionController {

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
