package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.services.WalletService;
import tn.esprit.utils.ValidationUtil;

public class CreateWalletController {

    @FXML private TextField tfUserId;
    @FXML private TextField tfBalance;
    @FXML private TextField tfReserved;
    @FXML private Label lblMsg;

    private final WalletService walletService = new WalletService();

    private Runnable onWalletCreated;
    public void setOnWalletCreated(Runnable onWalletCreated) {
        this.onWalletCreated = onWalletCreated;
    }

    @FXML
    public void initialize() {
        tfUserId.setTextFormatter(ValidationUtil.numericLongFormatter());
        tfBalance.setText("0");
        tfReserved.setText("0");
        lblMsg.setText("");
    }

    @FXML
    private void create() {
        lblMsg.setText("");

        if (ValidationUtil.isBlank(tfUserId.getText())) {
            lblMsg.setText("⚠️ user_id obligatoire.");
            return;
        }

        try {
            long userId = Long.parseLong(tfUserId.getText().trim());
            walletService.createWallet(userId);

            if (onWalletCreated != null) onWalletCreated.run();
            close();

        } catch (Exception e) {
            lblMsg.setText("❌ " + (e.getMessage() == null ? "Erreur." : e.getMessage()));
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) tfUserId.getScene().getWindow();
        stage.close();
    }
}