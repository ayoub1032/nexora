package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.Asset;
import tn.esprit.entities.Order;
import tn.esprit.entities.Portfolio;
import tn.esprit.entities.Wallet;
import tn.esprit.services.AssetService;
import tn.esprit.services.AuthService;
import tn.esprit.services.OrderService;
import tn.esprit.services.PortfolioService;
import tn.esprit.services.TransactionService;
import tn.esprit.services.WalletService;
import tn.esprit.entities.Transaction;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;
import java.math.BigDecimal;

public class UserViewController {

    @FXML
    private TableView<Asset> assetsTable;
    @FXML
    private TableColumn<Asset, Long> colAssetId;
    @FXML
    private TableColumn<Asset, String> colAssetName;
    @FXML
    private TableColumn<Asset, String> colAssetSymbol;
    @FXML
    private TableColumn<Asset, Double> colAssetValue;

    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Long> colOrderId;
    @FXML
    private TableColumn<Order, Long> colOrderAssetId;
    @FXML
    private TableColumn<Order, Integer> colOrderQuantity;
    @FXML
    private TableColumn<Order, Double> colOrderPrice;
    @FXML
    private TableColumn<Order, String> colOrderType;

    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, Long> colTxId;
    @FXML
    private TableColumn<Transaction, String> colTxType;
    @FXML
    private TableColumn<Transaction, String> colTxAmount;
    @FXML
    private TableColumn<Transaction, String> colTxStatus;
    @FXML
    private TableColumn<Transaction, String> colTxDate;

    @FXML
    private Label lblPortfolioValue;
    @FXML
    private Label lblPortfolioStatus;
    @FXML
    private Label lblWalletBalance;
    @FXML
    private Label lblWalletReserved;

    @FXML
    private ComboBox<Asset> cbAsset;
    @FXML
    private TextField tfQuantity;
    @FXML
    private TextField tfPrice;
    @FXML
    private ComboBox<String> cbOrderType;
    @FXML
    private Label lblMsg;

    private AssetService assetService;
    private AuthService authService;
    private OrderService orderService;
    private PortfolioService portfolioService;
    private TransactionService transactionService;
    private WalletService walletService;
    private long userId;

    @FXML
    public void initialize() {
        assetService = new AssetService();
        authService = new AuthService();
        orderService = new OrderService();
        portfolioService = new PortfolioService();
        transactionService = new TransactionService();
        walletService = new WalletService();
        userId = UserContext.getCurrentUserId();

        setupTableColumns();
        loadAssets();
        loadUserWallet();
        loadUserOrders();
        loadUserPortfolio();
        loadTransactions();
        setupOrderType();
    }

    private void setupTableColumns() {
        colAssetId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colAssetName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colAssetSymbol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSymbol()));
        colAssetValue.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getValue()));

        colOrderId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        colOrderAssetId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colOrderQuantity.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colOrderPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colOrderType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));

        colTxId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTransactionId()));
        colTxType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType()));
        colTxAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", cellData.getValue().getAmount())));
        colTxStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        colTxDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getCreatedAt() != null ? cellData.getValue().getCreatedAt().toString() : ""
        ));
    }

    private void loadAssets() {
        try {
            ObservableList<Asset> assets = FXCollections.observableArrayList(assetService.getAll());
            assetsTable.setItems(assets);
            cbAsset.setItems(assets);

            // Display asset name and value in combobox
            cbAsset.setCellFactory(param -> new ListCell<Asset>() {
                @Override
                protected void updateItem(Asset asset, boolean empty) {
                    super.updateItem(asset, empty);
                    setText(empty ? "" : asset.getName() + " (" + asset.getSymbol() + ") - " + asset.getValue());
                }
            });
            cbAsset.setButtonCell(new ListCell<Asset>() {
                @Override
                protected void updateItem(Asset asset, boolean empty) {
                    super.updateItem(asset, empty);
                    setText(empty ? "" : asset.getName() + " (" + asset.getSymbol() + ") - " + asset.getValue());
                }
            });
            
            // When asset changes, recalculate total price
            cbAsset.setOnAction(event -> updateTotalPrice());
        } catch (Exception e) {
            showError("Error loading assets: " + e.getMessage());
        }
    }

    private void loadUserOrders() {
        try {
            ObservableList<Order> orders = FXCollections.observableArrayList(orderService.findByUserId(userId));
            ordersTable.setItems(orders);
        } catch (Exception e) {
            showError("Error loading orders: " + e.getMessage());
        }
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

    private void setupOrderType() {
        cbOrderType.setItems(FXCollections.observableArrayList("BUY", "SELL"));
        cbOrderType.setValue("BUY");
        
        // Listen for quantity changes to update total price
        tfQuantity.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
    }
    
    private void updateTotalPrice() {
        try {
            if (cbAsset.getValue() != null) {
                double assetPrice = cbAsset.getValue().getValue();
                
                if (tfQuantity.getText().isEmpty() || tfQuantity.getText().equals("0")) {
                    tfPrice.setText(String.format("%.2f", assetPrice));
                } else {
                    int quantity = Integer.parseInt(tfQuantity.getText());
                    double totalPrice = assetPrice * quantity;
                    tfPrice.setText(String.format("%.2f", totalPrice));
                }
            } else {
                tfPrice.setText("");
            }
        } catch (NumberFormatException e) {
            // Invalid quantity format, don't update
        }
    }

    @FXML
    public void createOrder() {
        try {
            if (cbAsset.getValue() == null || tfQuantity.getText().isEmpty()) {
                showError("Please fill all fields");
                return;
            }

            Order order = new Order();
            order.setAssetId(cbAsset.getValue().getAssetId());
            order.setUserId(userId);
            order.setQuantity(Integer.parseInt(tfQuantity.getText()));
            order.setPrice(cbAsset.getValue().getValue()); // Price per unit from asset
            order.setType(cbOrderType.getValue());

            // Insert order and get ID
            long orderId = orderService.add(order);
            
            if (orderId > 0) {
                // Process the order (BUY/SELL with atomic transaction)
                orderService.processOrder(orderId);
                showSuccess("✅ Order " + order.getType() + " executed successfully!");
            } else {
                showError("Failed to create order");
                return;
            }
            
            clearFields();
            loadUserOrders();
            loadUserWallet();
            loadUserPortfolio();
            loadTransactions();
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for quantity");
        } catch (Exception e) {
            showError("Error creating/processing order: " + e.getMessage());
        }
    }

    @FXML
    public void clearFields() {
        cbAsset.setValue(null);
        tfQuantity.clear();
        tfPrice.clear();
        cbOrderType.setValue("BUY");
        lblMsg.setText("");
    }

    @FXML
    public void back() {
        authService.logout();
        SceneNavigator.goTo("LoginView.fxml", "PiWeb - Authentication", 560, 500);
    }

    @FXML
    public void showPortfolioDetails() {
        SceneNavigator.goTo("PortfolioDetailsView.fxml", "My Portfolio Assets", 1200, 700);
    }

    @FXML
    public void showP2PContracts() {
        SceneNavigator.goTo("P2PContractView.fxml", "P2P Contracts Marketplace", 1400, 800);
    }

    private void loadTransactions() {
        try {
            Wallet wallet = walletService.findByUserId(userId);
            if (wallet != null) {
                ObservableList<Transaction> transactions = FXCollections.observableArrayList(
                    transactionService.getByWallet(wallet.getWalletId())
                );
                transactionsTable.setItems(transactions);
            }
        } catch (Exception e) {
            showError("Error loading transactions: " + e.getMessage());
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

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");
        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);

        javafx.scene.control.ButtonType buttonTypeOk = new javafx.scene.control.ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
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
                    loadTransactions();
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

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");
        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);

        javafx.scene.control.ButtonType buttonTypeOk = new javafx.scene.control.ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
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
                    loadTransactions();
                } else {
                    showError("Insufficient balance");
                }
            } catch (Exception e) {
                showError("Error withdrawing: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        lblMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
        lblMsg.setText(message);
    }

    private void showSuccess(String message) {
        lblMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 700;");
        lblMsg.setText(message);
    }
}
