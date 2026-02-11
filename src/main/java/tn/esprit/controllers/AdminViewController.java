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

    @FXML private TableView<Wallet> walletTable;
    @FXML private TableColumn<Wallet, Long> colWalletId;
    @FXML private TableColumn<Wallet, Long> colUserId;
    @FXML private TableColumn<Wallet, BigDecimal> colBalance;
    @FXML private TableColumn<Wallet, BigDecimal> colReserved;
    @FXML private Label lblWalletMsg;

    @FXML private TextField tfFilterWalletId;
    @FXML private TableView<Transaction> txTable;
    @FXML private TableColumn<Transaction, Long> colTxId;
    @FXML private TableColumn<Transaction, Long> colTxWalletId;
    @FXML private TableColumn<Transaction, BigDecimal> colTxAmount;
    @FXML private TableColumn<Transaction, String> colTxType;
    @FXML private TableColumn<Transaction, Long> colTxRef;
    @FXML private TableColumn<Transaction, String> colTxStatus;
    @FXML private TableColumn<Transaction, Timestamp> colTxCreated;
    @FXML private Label lblTxMsg;

    private final WalletService walletService = new WalletService();
    private final TransactionService txService = new TransactionService();

    private final ObservableList<Wallet> wallets = FXCollections.observableArrayList();
    private final ObservableList<Transaction> txs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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
    }

    @FXML
    public void back() {
        SceneNavigator.goTo("RoleSelection.fxml", "Wallet & Transactions - Start", 560, 360);
    }

    @FXML
    public void refreshWallets() {
        lblWalletMsg.setText("");
        wallets.setAll(walletService.getAll());
    }

    @FXML
    public void clearSelection() {
        walletTable.getSelectionModel().clearSelection();
        lblWalletMsg.setText("");
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
                walletService.delete(selected.getWalletId());
                lblWalletMsg.setText("🗑️ Wallet supprimé.");
                refreshWallets();
            } catch (Exception e) {
                lblWalletMsg.setText("❌ " + cleanMsg(e.getMessage()));
            }
        }
    }

    // ✅ Opens popup window for wallet creation
    @FXML
    public void openCreateWalletWindow() {
        lblWalletMsg.setText("");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateWallet.fxml"));
            Scene scene = new Scene(loader.load(), 460, 310);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

            CreateWalletController controller = loader.getController();
            controller.setOnWalletCreated(this::refreshWallets);

            Stage popup = new Stage();
            popup.setTitle("Créer Wallet");
            popup.setScene(scene);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setResizable(false);
            popup.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            lblWalletMsg.setText("❌ Impossible d'ouvrir CreateWallet.fxml : " + cleanMsg(e.getMessage()));
        }
    }

    @FXML
    public void refreshTx() {
        lblTxMsg.setText("");
        txs.setAll(txService.getAll());
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
        } catch (Exception e) {
            lblTxMsg.setText("❌ " + cleanMsg(e.getMessage()));
        }
    }

    private String cleanMsg(String msg) {
        if (msg == null) return "Erreur.";
        return msg.replace("\n", " ").trim();
    }
}