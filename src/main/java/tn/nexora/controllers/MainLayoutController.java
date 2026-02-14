package tn.nexora.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Load default view (Profile)
        showProfile(null);
    }

    @FXML
    void showProfile(ActionEvent event) {
        loadView("/fxml/profile.fxml");
    }

    @FXML
    void showWallet(ActionEvent event) {
        try {
            tn.nexora.entities.User currentUser = tn.nexora.utils.SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Check user role
                String userRole = currentUser.getRole();

                if ("ADMIN".equalsIgnoreCase(userRole)) {
                    // Admin: Show AdminView with all wallets
                    System.out.println("✅ Loading AdminView for admin user");
                    loadView("/fxml/wallet/AdminView.fxml");
                } else {
                    // Trader/User: Show UserDemoView with their wallet
                    tn.nexora.wallet.services.WalletService walletService = new tn.nexora.wallet.services.WalletService();
                    tn.nexora.wallet.entities.Wallet wallet = walletService.findByUserId(currentUser.getUser_id());

                    if (wallet != null) {
                        tn.nexora.wallet.utils.UserContext.setWalletId(wallet.getWalletId());
                        System.out.println("✅ Auto-loaded wallet_id: " + wallet.getWalletId() + " for user_id: "
                                + currentUser.getUser_id());
                    } else {
                        System.out.println("⚠️ No wallet found for user_id: " + currentUser.getUser_id());
                    }
                    loadView("/fxml/wallet/UserDemoView.fxml");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading wallet view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void showTransactions(ActionEvent event) {
        // Same logic as showWallet - they use the same view
        showWallet(event);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Cannot find resource: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
