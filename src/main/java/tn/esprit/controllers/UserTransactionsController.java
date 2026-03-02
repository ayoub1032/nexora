package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import tn.esprit.entities.Transaction;
import tn.esprit.entities.Wallet;
import tn.esprit.services.TransactionService;
import tn.esprit.services.WalletService;
import tn.esprit.utils.UserContext;

public class UserTransactionsController {

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
    private Label lblMsg;

    private TransactionService transactionService;
    private WalletService walletService;
    private long userId;

    @FXML
    public void initialize() {
        transactionService = new TransactionService();
        walletService = new WalletService();
        userId = UserContext.getCurrentUserId();

        setupTableColumns();
        loadTransactions();
    }

    private void setupTableColumns() {
        colTxId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTransactionId()));
        colTxType.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType()));
        colTxAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f", cellData.getValue().getAmount())));
        colTxStatus.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        colTxDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? cellData.getValue().getCreatedAt().toString() : ""));
    }

    private void loadTransactions() {
        try {
            Wallet wallet = walletService.findByUserId(userId);
            if (wallet != null) {
                ObservableList<Transaction> transactions = FXCollections.observableArrayList(
                        transactionService.getByWallet(wallet.getWalletId()));
                transactionsTable.setItems(transactions);
            } else {
                lblMsg.setText("No wallet found. Cannot load transactions.");
            }
        } catch (Exception e) {
            lblMsg.setText("Error loading transactions: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }
}
