package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Asset;
import tn.esprit.entities.Order;
import tn.esprit.entities.Portfolio;
import tn.esprit.services.AssetService;
import tn.esprit.services.OrderService;
import tn.esprit.services.PortfolioService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.UserContext;

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
    private Label lblPortfolioValue;
    @FXML
    private Label lblPortfolioStatus;

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
    private OrderService orderService;
    private PortfolioService portfolioService;
    private long userId;

    @FXML
    public void initialize() {
        assetService = new AssetService();
        orderService = new OrderService();
        portfolioService = new PortfolioService();
        userId = UserContext.getCurrentUserId();

        setupTableColumns();
        loadAssets();
        loadUserOrders();
        loadUserPortfolio();
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

            orderService.add(order);
            showSuccess("Order created successfully!");
            clearFields();
            loadUserOrders();
            loadUserPortfolio();
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for quantity");
        } catch (Exception e) {
            showError("Error creating order: " + e.getMessage());
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
        SceneNavigator.goTo("RoleSelection.fxml", "Role Selection", 600, 400);
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
