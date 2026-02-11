package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Transaction;
import tn.esprit.entities.Wallet;
import tn.esprit.services.TransactionService;
import tn.esprit.services.WalletService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;
import tn.esprit.utils.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class UserDemoViewController {

    @FXML private TextField tfWalletId;
    @FXML private Label lblInfo;

    @FXML private Label lblWallet;
    @FXML private Label lblBalance;
    @FXML private Label lblReserved;

    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfAmount;
    @FXML private TextField tfRef;
    @FXML private Label lblOpMsg;

    @FXML private TableView<Transaction> txTable;
    @FXML private TableColumn<Transaction, Long> colTxId;
    @FXML private TableColumn<Transaction, BigDecimal> colTxAmount;
    @FXML private TableColumn<Transaction, String> colTxType;
    @FXML private TableColumn<Transaction, Long> colTxRef;
    @FXML private TableColumn<Transaction, String> colTxStatus;
    @FXML private TableColumn<Transaction, Timestamp> colTxCreated;

    private final WalletService walletService = new WalletService();
    private final TransactionService txService = new TransactionService();

    private final ObservableList<Transaction> txs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tfWalletId.setTextFormatter(ValidationUtil.numericLongFormatter());
        tfRef.setTextFormatter(ValidationUtil.numericLongFormatter());
        tfAmount.setTextFormatter(ValidationUtil.positiveDecimalFormatter());

        cbType.getItems().addAll("DEPOSIT", "WITHDRAW");
        cbType.getSelectionModel().selectFirst();

        colTxId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colTxAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTxType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        colTxRef.setCellValueFactory(new PropertyValueFactory<>("referenceId"));
        colTxStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTxCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        txTable.setItems(txs);

        renderEmpty();
        lblInfo.setText("ℹ️ Charge un wallet_id pour commencer.");
        lblOpMsg.setText("");
    }

    @FXML
    public void back() {
        UserContext.clear();
        SceneNavigator.goTo("RoleSelection.fxml", "Wallet & Transactions - Start", 560, 360);
    }

    @FXML
    public void loadWallet() {
        lblOpMsg.setText("");
        lblInfo.setText("");

        if (ValidationUtil.isBlank(tfWalletId.getText())) {
            lblInfo.setText("⚠️ wallet_id obligatoire.");
            return;
        }

        long walletId = Long.parseLong(tfWalletId.getText().trim());
        Wallet w = walletService.findById(walletId);

        if (w == null) {
            UserContext.clear();
            lblInfo.setText("❌ Wallet introuvable (wallet_id=" + walletId + ").");
            renderEmpty();
            txs.clear();
            return;
        }

        UserContext.setWalletId(walletId);
        lblInfo.setText("✅ Wallet chargé (wallet_id=" + walletId + ").");
        renderWallet(w);

        txs.setAll(txService.getByWallet(walletId));
    }

    @FXML
    public void refreshAll() {
        lblOpMsg.setText("");

        Long walletId = UserContext.getWalletId();
        if (walletId == null) {
            lblInfo.setText("⚠️ Charge un wallet d'abord.");
            return;
        }

        Wallet w = walletService.findById(walletId);
        if (w == null) {
            lblInfo.setText("❌ Wallet introuvable.");
            renderEmpty();
            txs.clear();
            UserContext.clear();
            return;
        }

        renderWallet(w);
        txs.setAll(txService.getByWallet(walletId));
        lblInfo.setText("🔄 Données mises à jour.");
    }

    @FXML
    public void submit() {
        lblOpMsg.setText("");

        Long walletId = UserContext.getWalletId();
        if (walletId == null) {
            lblOpMsg.setText("⚠️ Charge un wallet avant d'effectuer une opération.");
            return;
        }

        if (ValidationUtil.isBlank(tfAmount.getText())) {
            lblOpMsg.setText("⚠️ Montant obligatoire.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(tfAmount.getText().trim());
            String type = cbType.getValue();

            Long ref = null;
            if (!ValidationUtil.isBlank(tfRef.getText())) {
                ref = Long.parseLong(tfRef.getText().trim());
            }

            if ("DEPOSIT".equals(type)) {
                txService.deposit(walletId, amount, ref);
                lblOpMsg.setText("✅ Dépôt effectué.");
            } else {
                txService.withdraw(walletId, amount, ref);
                lblOpMsg.setText("✅ Retrait effectué.");
            }

            tfAmount.clear();
            tfRef.clear();
            refreshAll();

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) msg = "Erreur inconnue.";
            lblOpMsg.setText("❌ " + msg);
        }
    }

    @FXML
    public void clearSelection() {
        txTable.getSelectionModel().clearSelection();
        lblOpMsg.setText("");
    }

    private void renderWallet(Wallet w) {
        lblWallet.setText("Wallet: " + w.getWalletId());
        lblBalance.setText("Balance: " + w.getBalance());
        lblReserved.setText("Reserved: " + w.getReservedBalance());
    }

    private void renderEmpty() {
        lblWallet.setText("Wallet: -");
        lblBalance.setText("Balance: -");
        lblReserved.setText("Reserved: -");
    }
}