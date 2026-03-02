package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;

import java.io.IOException;

public class UserMainController {

    @FXML
    private Label lblWelcome;
    @FXML
    private VBox sidebarMenu;
    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard, btnMarket, btnOrders, btnHistory, btnP2P;

    private AuthService authService;
    private static UserMainController instance;

    public static UserMainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        authService = new AuthService();
        lblWelcome.setText("Welcome User " + UserContext.getCurrentUserId());

        // Select the Dashboard as default screen
        loadView("UserDashboardView.fxml", btnDashboard);
    }

    @FXML
    public void showDashboard() {
        loadView("UserDashboardView.fxml", btnDashboard);
    }

    @FXML
    public void showMarket() {
        loadView("UserAssetsMarketView.fxml", btnMarket);
    }

    @FXML
    public void showOrders() {
        loadView("UserOrdersView.fxml", btnOrders);
    }

    @FXML
    public void showHistory() {
        loadView("UserTransactionsView.fxml", btnHistory);
    }

    @FXML
    public void showP2P() {
        loadView("P2PContractView.fxml", btnP2P);
    }

    public void loadPortfolio() {
        loadView("PortfolioDetailsView.fxml", null);
    }

    @FXML
    public void logout() {
        authService.logout();
        SceneNavigator.goTo("LoginView.fxml", "PiWeb - Authentication", 560, 500);
    }

    private void loadView(String fxmlFile, Button clickedButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Node view = loader.load();

            contentArea.getChildren().setAll(view);
            updateSidebarSelection(clickedButton);

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void updateSidebarSelection(Button selectedButton) {
        // Remove active class from all
        btnDashboard.getStyleClass().remove("sidebar-btn-active");
        btnMarket.getStyleClass().remove("sidebar-btn-active");
        btnOrders.getStyleClass().remove("sidebar-btn-active");
        btnHistory.getStyleClass().remove("sidebar-btn-active");

        // Add to selected
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}
