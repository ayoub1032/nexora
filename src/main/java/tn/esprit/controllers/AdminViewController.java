package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Transaction;
import tn.esprit.entities.Wallet;
import tn.esprit.services.TransactionService;
import tn.esprit.services.WalletService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class AdminViewController {

    @FXML
    private TableView<Wallet> walletTable;
    @FXML
    private TableColumn<Wallet, Long> colWalletId;
    @FXML
    private TableColumn<Wallet, Long> colUserId;
    @FXML
    private TableColumn<Wallet, BigDecimal> colBalance;
    @FXML
    private TableColumn<Wallet, BigDecimal> colReserved;
    @FXML
    private Label lblWalletMsg;

    @FXML
    private TextField tfFilterWalletId;
    @FXML
    private TableView<Transaction> txTable;
    @FXML
    private TableColumn<Transaction, Long> colTxId;
    @FXML
    private TableColumn<Transaction, Long> colTxWalletId;
    @FXML
    private TableColumn<Transaction, BigDecimal> colTxAmount;
    @FXML
    private TableColumn<Transaction, String> colTxType;
    @FXML
    private TableColumn<Transaction, Long> colTxRef;
    @FXML
    private TableColumn<Transaction, String> colTxStatus;
    @FXML
    private TableColumn<Transaction, Timestamp> colTxCreated;
    @FXML
    private Label lblTxMsg;

    // KPI Labels
    @FXML
    private Label lblTotalKpiWallets;
    @FXML
    private Label lblTotalKpiBalance;
    @FXML
    private Label lblTotalKpiTx;

    private final WalletService walletService = new WalletService();
    private final TransactionService txService = new TransactionService();

    private final ObservableList<Wallet> wallets = FXCollections.observableArrayList();
    private final ObservableList<Transaction> txs = FXCollections.observableArrayList();
    private final ObservableList<String> activityLog = FXCollections.observableArrayList();

    private void log(String action) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        activityLog.add(0, "[" + timestamp + "] " + action); // Add to top
    }

    private void updateKPIs() {
        // Total Wallets
        lblTotalKpiWallets.setText(String.valueOf(wallets.size()));

        // Total Balance
        BigDecimal totalBalance = wallets.stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalKpiBalance.setText(totalBalance.toString() + " DT");

        // Total Transactions
        lblTotalKpiTx.setText(String.valueOf(txs.size()));
    }

    @FXML
    public void viewLogs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Activity Logs (Session)");
        alert.setHeaderText("Actions performed in this session");

        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        area.setMaxHeight(Double.MAX_VALUE);

        StringBuilder content = new StringBuilder();
        if (activityLog.isEmpty()) {
            content.append("No activity recorded yet.");
        } else {
            for (String log : activityLog) {
                content.append(log).append("\n");
            }
        }
        area.setText(content.toString());

        alert.getDialogPane().setContent(area);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(480, 320);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        log("Admin dashboard opened.");
        tfFilterWalletId.setTextFormatter(ValidationUtil.numericLongFormatter());

        colWalletId.setCellValueFactory(new PropertyValueFactory<>("walletId"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colReserved.setCellValueFactory(new PropertyValueFactory<>("reservedBalance"));
        walletTable.setItems(wallets);

        colTxId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colTxWalletId.setCellValueFactory(new PropertyValueFactory<>("walletId"));
        colTxAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTxType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        colTxRef.setCellValueFactory(new PropertyValueFactory<>("referenceId"));
        colTxStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTxCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        txTable.setItems(txs);

        refreshWallets();
        refreshTx();
        // updateKPIs is called inside refresh methods
    }

    @FXML
    public void back() {
        log("Navigated back to Role Selection.");
        SceneNavigator.goTo("RoleSelection.fxml", "Wallet & Transactions - Start", 560, 360);
    }

    @FXML
    public void refreshWallets() {
        lblWalletMsg.setText("");
        wallets.setAll(walletService.getAll());
        log("Refreshed wallet list. Count: " + wallets.size());
        updateKPIs();
    }

    @FXML
    public void clearSelection() {
        walletTable.getSelectionModel().clearSelection();
        lblWalletMsg.setText("");
        log("Cleared wallet selection.");
    }

    @FXML
    public void deleteWallet() {
        Wallet selected = walletTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblWalletMsg.setText("⚠️ Sélectionne un wallet d'abord.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer wallet ?");
        confirm.setContentText("wallet_id=" + selected.getWalletId());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                long wid = selected.getWalletId();
                walletService.delete(wid);
                lblWalletMsg.setText("🗑️ Wallet supprimé.");
                log("Deleted wallet ID: " + wid);
                refreshWallets();
            } catch (Exception e) {
                String err = cleanMsg(e.getMessage());
                lblWalletMsg.setText("❌ " + err);
                log("Error deleting wallet: " + err);
            }
        }
    }

    // ✅ Opens popup window for wallet creation
    @FXML
    public void openCreateWalletWindow() {
        lblWalletMsg.setText("");
        log("Opened Create Wallet window.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateWallet.fxml"));
            Scene scene = new Scene(loader.load(), 460, 310);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

            CreateWalletController controller = loader.getController();
            controller.setOnWalletCreated(() -> {
                log("New wallet created via popup.");
                this.refreshWallets();
            });

            Stage popup = new Stage();
            popup.setTitle("Créer Wallet");
            popup.setScene(scene);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setResizable(false);
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            lblWalletMsg.setText("❌ Impossible d'ouvrir CreateWallet.fxml : " + cleanMsg(e.getMessage()));
            log("Error opening create window: " + e.getMessage());
        }
    }

    @FXML
    public void refreshTx() {
        lblTxMsg.setText("");
        txs.setAll(txService.getAll());
        log("Refreshed transaction list. Count: " + txs.size());
        updateKPIs();
    }

    @FXML
    public void filterTx() {
        lblTxMsg.setText("");

        if (ValidationUtil.isBlank(tfFilterWalletId.getText())) {
            lblTxMsg.setText("⚠️ wallet_id obligatoire pour filtrer.");
            return;
        }

        try {
            long walletId = Long.parseLong(tfFilterWalletId.getText().trim());
            txs.setAll(txService.getByWallet(walletId));
            lblTxMsg.setText("✅ Filtre appliqué (wallet_id=" + walletId + ")");
            log("Filtered transactions by Wallet ID: " + walletId);
        } catch (Exception e) {
            String err = cleanMsg(e.getMessage());
            lblTxMsg.setText("❌ " + err);
            log("Error filtering transactions: " + err);
        }
    }

    private String cleanMsg(String msg) {
        if (msg == null)
            return "Erreur.";
        return msg.replace("\n", " ").trim();
    }
}