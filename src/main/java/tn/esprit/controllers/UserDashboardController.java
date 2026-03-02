package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.entities.Portfolio;
import tn.esprit.entities.Wallet;
import tn.esprit.services.PortfolioService;
import tn.esprit.services.WalletService;
import tn.esprit.services.TransactionService;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;
import java.math.BigDecimal;

public class UserDashboardController {

    @FXML
    private Label lblWalletBalance;
    @FXML
    private Label lblWalletReserved;
    @FXML
    private Label lblPortfolioValue;
    @FXML
    private Label lblPortfolioStatus;
    @FXML
    private Label lblMsg;

    private WalletService walletService;
    private PortfolioService portfolioService;
    private TransactionService transactionService;
    private long userId;

    @FXML
    public void initialize() {
        walletService = new WalletService();
        portfolioService = new PortfolioService();
        transactionService = new TransactionService();
        userId = UserContext.getCurrentUserId();

        loadUserWallet();
        loadUserPortfolio();
    }

    private void loadUserWallet() {
        try {
            Wallet wallet = walletService.findByUserId(userId);
            if (wallet != null) {
                lblWalletBalance.setText(String.format("%.2f", wallet.getBalance()));
                lblWalletReserved.setText(String.format("%.2f", wallet.getReservedBalance()));
            } else {
                lblWalletBalance.setText("0.00");
                lblWalletReserved.setText("0.00");
            }
        } catch (Exception e) {
            showError("Error loading wallet: " + e.getMessage());
        }
    }

    private void loadUserPortfolio() {
        try {
            Portfolio portfolio = portfolioService.findByUserId(userId);
            if (portfolio != null) {
                lblPortfolioValue.setText(String.format("%.2f", portfolio.getTotalValue()));
                lblPortfolioStatus.setText("Active - " + portfolio.getTotalValue() + " in assets");
            } else {
                lblPortfolioValue.setText("0.00");
                lblPortfolioStatus.setText("No portfolio yet");
            }
        } catch (Exception e) {
            showError("Error loading portfolio: " + e.getMessage());
        }
    }

    @FXML
    public void showDepositDialog() {
        javafx.scene.control.Dialog<Double> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Deposit to Wallet");
        dialog.setHeaderText("Enter amount to deposit");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        javafx.scene.control.TextField amountField = new javafx.scene.control.TextField();
        amountField.setPromptText("Enter amount");
        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);

        javafx.scene.control.ButtonType buttonTypeOk = new javafx.scene.control.ButtonType("OK",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk && !amountField.getText().isEmpty()) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    showError("Invalid amount");
                }
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get() > 0) {
            try {
                Wallet wallet = walletService.findByUserId(userId);
                if (wallet != null) {
                    transactionService.deposit(wallet.getWalletId(), new BigDecimal(result.get()), null);
                    showSuccess("✅ Deposited " + result.get() + " successfully!");
                    loadUserWallet();
                } else {
                    showError("Wallet not found");
                }
            } catch (Exception e) {
                showError("Error depositing: " + e.getMessage());
            }
        }
    }

    @FXML
    public void showWithdrawDialog() {
        javafx.scene.control.Dialog<Double> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Withdraw from Wallet");
        dialog.setHeaderText("Enter amount to withdraw");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        javafx.scene.control.TextField amountField = new javafx.scene.control.TextField();
        amountField.setPromptText("Enter amount");
        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);

        javafx.scene.control.ButtonType buttonTypeOk = new javafx.scene.control.ButtonType("OK",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk && !amountField.getText().isEmpty()) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    showError("Invalid amount");
                }
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get() > 0) {
            try {
                Wallet wallet = walletService.findByUserId(userId);
                if (wallet != null && wallet.getBalance().doubleValue() >= result.get()) {
                    transactionService.withdraw(wallet.getWalletId(), new BigDecimal(result.get()), null);
                    showSuccess("✅ Withdrew " + result.get() + " successfully!");
                    loadUserWallet();
                } else {
                    showError("Insufficient balance");
                }
            } catch (Exception e) {
                showError("Error withdrawing: " + e.getMessage());
            }
        }
    }

    @FXML
    public void showPortfolioDetails() {
        UserMainController.getInstance().loadPortfolio();
    }

    private void showError(String message) {
        lblMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
        lblMsg.setText(message);
    }

    @FXML
    public void logout() {
        new AuthService().logout();
        SceneNavigator.goTo("LoginView.fxml", "PiWeb - Authentication", 560, 500);
    }

    private void showSuccess(String message) {
        lblMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 700;");
        lblMsg.setText(message);
    }
}
