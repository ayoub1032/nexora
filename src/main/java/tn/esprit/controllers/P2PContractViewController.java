package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.Asset;
import tn.esprit.entities.P2PContract;
import tn.esprit.entities.User;
import tn.esprit.services.AssetService;
import tn.esprit.services.P2PContractAcceptanceService;
import tn.esprit.services.P2PContractService;
import tn.esprit.services.PortfolioAssetService;
import tn.esprit.services.PortfolioService;
import tn.esprit.services.UserReputationService;
import tn.esprit.services.UserService;
import tn.esprit.utils.UserContext;

import java.util.List;
import java.util.Optional;
import java.awt.Desktop;
import java.io.File;
import tn.esprit.utils.PdfGeneratorService;

public class P2PContractViewController {

    @FXML
    private TableView<P2PContractRow> contractsTable;
    @FXML
    private TableColumn<P2PContractRow, String> colCreator;
    @FXML
    private TableColumn<P2PContractRow, String> colReputation;
    @FXML
    private TableColumn<P2PContractRow, String> colAsset;
    @FXML
    private TableColumn<P2PContractRow, String> colType;
    @FXML
    private TableColumn<P2PContractRow, Integer> colQuantity;
    @FXML
    private TableColumn<P2PContractRow, Double> colPrice;
    @FXML
    private TableColumn<P2PContractRow, Double> colTotal;
    @FXML
    private TableColumn<P2PContractRow, String> colStatus;

    @FXML
    private ComboBox<Asset> cbAsset;
    @FXML
    private TextField tfQuantity;
    @FXML
    private TextField tfPrice;
    @FXML
    private ComboBox<String> cbContractType;
    @FXML
    private Label lblMsg;

    private long userId;
    private P2PContractService contractService;
    private P2PContractAcceptanceService acceptanceService;
    private AssetService assetService;
    private PortfolioAssetService portfolioAssetService;
    private PortfolioService portfolioService;
    private UserService userService;
    private UserReputationService reputationService;

    public static class P2PContractRow {
        private final long contractId;
        private final long creatorId;
        private final String creatorName;
        private final String reputationLabel;
        private final String assetName;
        private final String contractType;
        private final int quantity;
        private final double pricePerUnit;
        private final String status;

        public P2PContractRow(long contractId, long creatorId, String creatorName, String reputationLabel,
                String assetName, String contractType, int quantity, double pricePerUnit, String status) {
            this.contractId = contractId;
            this.creatorId = creatorId;
            this.creatorName = creatorName;
            this.reputationLabel = reputationLabel;
            this.assetName = assetName;
            this.contractType = contractType;
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
            this.status = status;
        }

        public long getContractId() {
            return contractId;
        }

        public long getCreatorId() {
            return creatorId;
        }

        public String getCreatorName() {
            return creatorName;
        }

        public String getReputationLabel() {
            return reputationLabel;
        }

        public String getAssetName() {
            return assetName;
        }

        public String getContractType() {
            return contractType;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPricePerUnit() {
            return pricePerUnit;
        }

        public double getTotal() {
            return pricePerUnit * quantity;
        }

        public String getStatus() {
            return status;
        }
    }

    @FXML
    private Button btnShowQr;

    private String currentQrData = null;

    @FXML
    public void initialize() {
        userId = UserContext.getCurrentUserId();
        contractService = new P2PContractService();
        acceptanceService = new P2PContractAcceptanceService();
        assetService = new AssetService();
        portfolioAssetService = new PortfolioAssetService();
        portfolioService = new PortfolioService();
        userService = new UserService();
        reputationService = new UserReputationService();

        setupTableColumns();
        setupContractType();
        loadAssets();
        loadOpenContracts();

        // Listen for changes
        cbAsset.setOnAction(e -> updateTotalPrice());
        tfQuantity.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        tfPrice.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());

        // Setup QR Code Generator listener on table selection
        contractsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentQrData = String.format(
                        "Nexora P2P Contract\nID: %d\nAction: %s\nAsset: %s\nQty: %d\nPrice: $%.2f\nTotal: $%.2f\nCreator: %s",
                        newSelection.getContractId(),
                        newSelection.getContractType(),
                        newSelection.getAssetName(),
                        newSelection.getQuantity(),
                        newSelection.getPricePerUnit(),
                        newSelection.getTotal(),
                        newSelection.getCreatorName());

                javafx.application.Platform.runLater(() -> btnShowQr.setVisible(true));
            } else {
                currentQrData = null;
                javafx.application.Platform.runLater(() -> btnShowQr.setVisible(false));
            }
        });
    }

    @FXML
    public void showQrCode() {
        if (currentQrData == null)
            return;

        javafx.scene.image.Image qrImg = tn.esprit.utils.QRCodeService.generateQRCodeImage(currentQrData, 300, 300);
        if (qrImg != null) {
            javafx.scene.control.Alert dialog = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            dialog.setTitle("Contract Details QR");
            dialog.setHeaderText("Scan to view contract information");

            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(qrImg);
            dialog.setGraphic(imageView);
            // removing default text
            dialog.setContentText(null);

            dialog.showAndWait();
        }
    }

    private void setupTableColumns() {
        colCreator.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatorName()));
        colReputation.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReputationLabel()));
        colAsset.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAssetName()));
        colType.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContractType()));
        colQuantity.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colPrice.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPricePerUnit()));
        colTotal.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotal()));
        colStatus.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private void setupContractType() {
        ObservableList<String> types = FXCollections.observableArrayList("BUY", "SELL");
        cbContractType.setItems(types);
        cbContractType.setValue("SELL");
    }

    private void loadAssets() {
        try {
            ObservableList<Asset> assets = FXCollections.observableArrayList(assetService.getAll());
            cbAsset.setItems(assets);
            cbAsset.setCellFactory(param -> new ListCell<Asset>() {
                @Override
                protected void updateItem(Asset asset, boolean empty) {
                    super.updateItem(asset, empty);
                    setText(empty ? "" : asset.getName() + " - $" + asset.getValue());
                }
            });
            cbAsset.setButtonCell(new ListCell<Asset>() {
                @Override
                protected void updateItem(Asset asset, boolean empty) {
                    super.updateItem(asset, empty);
                    setText(empty ? "" : asset.getName() + " - $" + asset.getValue());
                }
            });
        } catch (Exception e) {
            showError("Error loading assets: " + e.getMessage());
        }
    }

    @FXML
    private void updateTotalPrice() {
        try {
            if (tfQuantity.getText() != null && tfPrice.getText() != null &&
                    !tfQuantity.getText().isEmpty() && !tfPrice.getText().isEmpty()) {
                // Just to validate number format
                Double.parseDouble(tfPrice.getText());
                Integer.parseInt(tfQuantity.getText());
            }
        } catch (NumberFormatException e) {
            // Ignore parsing errors while typing
        }
    }

    private void loadOpenContracts() {
        try {
            List<P2PContract> openContracts = contractService.getOpenContracts();
            ObservableList<P2PContractRow> rows = FXCollections.observableArrayList();

            for (P2PContract contract : openContracts) {
                if (contract.getCreatorId() != userId) { // Don't show own contracts
                    Asset asset = assetService.findById(contract.getAssetId());
                    User creator = userService.findById(contract.getCreatorId());
                    String creatorName = creator != null ? creator.getFullName() : "Unknown User";
                    String assetName = asset != null ? asset.getName() : "Unknown Asset";
                    String repLabel = reputationService.getReputationLabel(contract.getCreatorId());
                    rows.add(new P2PContractRow(
                            contract.getContractId(),
                            contract.getCreatorId(),
                            creatorName,
                            repLabel,
                            assetName,
                            contract.getContractType(),
                            contract.getQuantity(),
                            contract.getPricePerUnit(),
                            contract.getStatus()));
                }
            }

            contractsTable.setItems(rows);
        } catch (Exception e) {
            showError("Error loading contracts: " + e.getMessage());
        }
    }

    @FXML
    public void createContract() {
        try {
            if (cbAsset.getValue() == null || tfQuantity.getText().isEmpty() || tfPrice.getText().isEmpty()
                    || cbContractType.getValue() == null) {
                showError("Please fill all fields");
                return;
            }

            String contractType = cbContractType.getValue();
            int quantity = Integer.parseInt(tfQuantity.getText());
            double price = Double.parseDouble(tfPrice.getText());

            // Validate based on contract type
            if ("SELL".equals(contractType)) {
                // Validate user has assets to sell
                var portfolio = portfolioService.findByUserId(userId);
                if (portfolio != null) {
                    var assetInPortfolio = portfolioAssetService.findByPortfolioAndAsset(portfolio.getPortfolioId(),
                            cbAsset.getValue().getAssetId());
                    if (assetInPortfolio == null || assetInPortfolio.getQuantity() < quantity) {
                        int available = assetInPortfolio != null ? assetInPortfolio.getQuantity() : 0;
                        showError("Insufficient assets. You have: " + available);
                        return;
                    }
                }
            } else if ("BUY".equals(contractType)) {
                // Validate user has money
                var wallet = new tn.esprit.services.WalletService().findByUserId(userId);
                if (wallet != null && wallet.getBalance().doubleValue() < (price * quantity)) {
                    showError("Insufficient funds. You need: " + (price * quantity));
                    return;
                }
            }

            // Create contract
            P2PContract contract = new P2PContract(userId, cbAsset.getValue().getAssetId(), quantity, price,
                    contractType);
            contractService.add(contract);
            showSuccess("✅ Contract created successfully!");
            clearFields();
            loadOpenContracts();
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        } catch (Exception e) {
            showError("Error creating contract: " + e.getMessage());
        }
    }

    @FXML
    public void acceptSelectedContract() {
        P2PContractRow selected = contractsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a contract");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Contract Acceptance");
        confirmDialog.setHeaderText("Accept " + selected.getContractType() + " Contract?");
        confirmDialog.setContentText(
                "Asset: " + selected.getAssetName() + "\n" +
                        "Quantity: " + selected.getQuantity() + "\n" +
                        "Price: $" + selected.getPricePerUnit() + " per unit\n" +
                        "Total: $" + String.format("%.2f", selected.getTotal()));

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                acceptanceService.acceptContract(selected.getContractId(), userId);

                String accepterName = "Unknown User";
                try {
                    User accepter = userService.findById(userId);
                    if (accepter != null) {
                        accepterName = accepter.getFullName();
                    }
                } catch (Exception ex) {
                    System.err.println("Could not fetch accepter name: " + ex.getMessage());
                }

                // Generate QR String for the receipt
                String qrData = String.format(
                        "Nexora P2P Official Receipt\nContract ID: %d\nAction: %s\nAsset: %s\nQty: %d\nPrice: $%.2f\nTotal: $%.2f\nPublisher: %s\nExecuting Party: %s",
                        selected.getContractId(),
                        selected.getContractType(),
                        selected.getAssetName(),
                        selected.getQuantity(),
                        selected.getPricePerUnit(),
                        selected.getTotal(),
                        selected.getCreatorName(),
                        accepterName);

                // Call the PDF Service
                String pdfPath = PdfGeneratorService.generateReceipt(
                        selected.getContractId(),
                        selected.getContractType(),
                        selected.getAssetName(),
                        selected.getQuantity(),
                        selected.getPricePerUnit(),
                        selected.getTotal(),
                        selected.getCreatorName(),
                        accepterName,
                        qrData);

                if (pdfPath != null) {
                    Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
                    successDialog.setTitle("Trade Executed");
                    successDialog.setHeaderText("Success! Your P2P trade is complete.");
                    successDialog.setContentText("An official PDF receipt has been generated for your records.");

                    ButtonType btnOpenPdf = new ButtonType("View Receipt", ButtonBar.ButtonData.OK_DONE);
                    successDialog.getButtonTypes().setAll(btnOpenPdf, ButtonType.CLOSE);

                    Optional<ButtonType> pdfResult = successDialog.showAndWait();
                    if (pdfResult.isPresent() && pdfResult.get() == btnOpenPdf) {
                        try {
                            Desktop.getDesktop().open(new File(pdfPath));
                        } catch (Exception ex) {
                            showError("Could not open PDF automatically. It is saved in the 'receipts' folder.");
                        }
                    }
                } else {
                    showSuccess("✅ Contract accepted! (Failed to generate PDF receipt)");
                }

                // ── Rating Dialog ──
                showRatingDialog(selected.getCreatorName(), selected.getCreatorId());

                loadOpenContracts();
            } catch (Exception e) {
                showError("Error accepting contract: " + e.getMessage());
            }
        }
    }

    @FXML
    public void clearFields() {
        cbAsset.setValue(null);
        tfQuantity.clear();
        tfPrice.clear();
        cbContractType.setValue("SELL");
    }

    private void showError(String message) {
        lblMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
        lblMsg.setText(message);
    }

    private void showSuccess(String message) {
        lblMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 700;");
        lblMsg.setText(message);
    }

    /**
     * Displays a star rating dialog so the user can rate their trading partner.
     */
    private void showRatingDialog(String tradingPartnerName, long tradingPartnerId) {
        javafx.scene.control.Dialog<Integer> ratingDialog = new javafx.scene.control.Dialog<>();
        ratingDialog.setTitle("Rate Your Trading Partner");
        ratingDialog.setHeaderText("How was your experience trading with " + tradingPartnerName + "?");

        javafx.scene.control.ComboBox<String> starBox = new javafx.scene.control.ComboBox<>();
        starBox.getItems().addAll("⭐ 1 Star", "⭐⭐ 2 Stars", "⭐⭐⭐ 3 Stars", "⭐⭐⭐⭐ 4 Stars", "⭐⭐⭐⭐⭐ 5 Stars");
        starBox.setValue("⭐⭐⭐⭐⭐ 5 Stars");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
                new javafx.scene.control.Label("Your rating:"),
                starBox);
        content.setStyle("-fx-padding: 10;");
        ratingDialog.getDialogPane().setContent(content);

        ButtonType submitBtn = new ButtonType("Submit Rating", ButtonBar.ButtonData.OK_DONE);
        ratingDialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        ratingDialog.setResultConverter(btn -> {
            if (btn == submitBtn) {
                return starBox.getSelectionModel().getSelectedIndex() + 1; // 1 to 5
            }
            return null;
        });

        Optional<Integer> ratingResult = ratingDialog.showAndWait();
        ratingResult.ifPresent(stars -> {
            try {
                reputationService.addRating(tradingPartnerId, stars);
                showSuccess("✅ Trade complete & rated! Thank you.");
            } catch (Exception e) {
                showSuccess("✅ Trade complete! Rating could not be saved.");
            }
        });

        if (!ratingResult.isPresent()) {
            showSuccess("✅ Contract accepted and completed!");
        }
    }
}
