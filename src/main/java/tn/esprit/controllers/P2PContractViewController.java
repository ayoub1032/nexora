package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.Asset;
import tn.esprit.entities.P2PContract;
import tn.esprit.entities.PortfolioAsset;
import tn.esprit.entities.User;
import tn.esprit.services.AssetService;
import tn.esprit.services.P2PContractAcceptanceService;
import tn.esprit.services.P2PContractService;
import tn.esprit.services.PortfolioAssetService;
import tn.esprit.services.PortfolioService;
import tn.esprit.services.UserService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;

import java.util.List;
import java.util.Optional;

public class P2PContractViewController {

    @FXML
    private TableView<P2PContractRow> contractsTable;
    @FXML
    private TableColumn<P2PContractRow, String> colCreator;
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

    public static class P2PContractRow {
        private final long contractId;
        private final long creatorId;
        private final String creatorName;
        private final String assetName;
        private final String contractType;
        private final int quantity;
        private final double pricePerUnit;
        private final String status;

        public P2PContractRow(long contractId, long creatorId, String creatorName, String assetName,
                String contractType, int quantity, double pricePerUnit, String status) {
            this.contractId = contractId;
            this.creatorId = creatorId;
            this.creatorName = creatorName;
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
    public void initialize() {
        userId = UserContext.getCurrentUserId();
        contractService = new P2PContractService();
        acceptanceService = new P2PContractAcceptanceService();
        assetService = new AssetService();
        portfolioAssetService = new PortfolioAssetService();
        portfolioService = new PortfolioService();
        userService = new UserService();

        setupTableColumns();
        setupContractType();
        loadAssets();
        loadOpenContracts();

        // Listen for changes
        cbAsset.setOnAction(e -> updateTotalPrice());
        tfQuantity.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        tfPrice.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
    }

    private void setupTableColumns() {
        colCreator.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatorName()));
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
                // Just to validate number format, logic can be added here if needed to show
                // total
                double price = Double.parseDouble(tfPrice.getText());
                int qty = Integer.parseInt(tfQuantity.getText());
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
                    rows.add(new P2PContractRow(
                            contract.getContractId(),
                            contract.getCreatorId(),
                            creatorName,
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
                showSuccess("✅ Contract accepted and completed!");
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

    @FXML
    public void back() {
        SceneNavigator.goTo("UserView.fxml", "User Dashboard", 1200, 750);
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
