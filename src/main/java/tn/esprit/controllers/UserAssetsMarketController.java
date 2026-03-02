package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.Asset;
import tn.esprit.entities.Order;
import tn.esprit.services.AssetService;
import tn.esprit.services.OrderService;
import tn.esprit.utils.UserContext;

public class UserAssetsMarketController {

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
    private javafx.scene.chart.LineChart<String, Number> priceChart;
    @FXML
    private javafx.scene.chart.CategoryAxis xAxis;
    @FXML
    private javafx.scene.chart.NumberAxis yAxis;
    @FXML
    private Label lblChartTitle;

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
    private long userId;

    @FXML
    public void initialize() {
        assetService = new AssetService();
        orderService = new OrderService();
        userId = UserContext.getCurrentUserId();

        setupTableColumns();
        setupOrderType();
        loadAssets();
        setupChartListener();
    }

    private void setupChartListener() {
        assetsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Pre-fill the order form selection to match the table selection for
                // convenience
                cbAsset.setValue(newSelection);

                if ("CRYPTO".equalsIgnoreCase(newSelection.getType())) {
                    loadChartData(newSelection);
                } else {
                    lblChartTitle.setText(newSelection.getName() + " chart not available (Fiat/Other)");
                    priceChart.getData().clear();
                }
            }
        });
    }

    private void loadChartData(Asset asset) {
        lblChartTitle.setText("Loading 30-day history for " + asset.getSymbol() + "...");
        priceChart.getData().clear();

        // Fetch data asynchronously so UI doesn't freeze
        new Thread(() -> {
            java.util.Map<String, Double> history = tn.esprit.utils.HistoricalPriceService
                    .getHistoricalPrices(asset.getSymbol());

            javafx.application.Platform.runLater(() -> {
                if (history != null && !history.isEmpty()) {
                    javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                    series.setName(asset.getSymbol() + " Price");

                    for (java.util.Map.Entry<String, Double> entry : history.entrySet()) {
                        series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }

                    priceChart.getData().add(series);
                    // Dynamically format Y-axis bounds based on data
                    double min = history.values().stream().min(Double::compareTo).orElse(0.0);
                    double max = history.values().stream().max(Double::compareTo).orElse(100.0);
                    double padding = (max - min) * 0.1;

                    yAxis.setAutoRanging(false);
                    yAxis.setLowerBound(Math.max(0, min - padding));
                    yAxis.setUpperBound(max + padding);
                    // Rough tick unit calculation
                    yAxis.setTickUnit((max - min) / 5);

                    lblChartTitle.setText(asset.getName() + " Price Evolution (Last 30 Days)");
                } else {
                    lblChartTitle.setText("Failed to load historical data for " + asset.getSymbol());
                }
            });
        }).start();
    }

    private void setupTableColumns() {
        colAssetId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colAssetName.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colAssetSymbol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSymbol()));
        colAssetValue.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getValue()));
    }

    private void setupOrderType() {
        cbOrderType.setItems(FXCollections.observableArrayList("BUY", "SELL"));
        cbOrderType.setValue("BUY");

        tfQuantity.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
    }

    private void loadAssets() {
        try {
            ObservableList<Asset> assets = FXCollections.observableArrayList(assetService.getAll());
            assetsTable.setItems(assets);
            cbAsset.setItems(assets);

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

            cbAsset.setOnAction(event -> updateTotalPrice());
        } catch (Exception e) {
            showError("Error loading assets: " + e.getMessage());
        }
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
            // Ignored
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
            order.setPrice(cbAsset.getValue().getValue());
            order.setType(cbOrderType.getValue());

            long orderId = orderService.add(order);

            if (orderId > 0) {
                boolean success = orderService.processOrder(orderId);
                if (success) {
                    showSuccess("✅ Order " + order.getType() + " executed successfully!");
                    clearFields();
                } else {
                    showError("Order created but execution failed (Insufficient balance/assets). Check History.");
                }
            } else {
                showError("Failed to create order");
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        } catch (Exception e) {
            showError("Error processing order: " + e.getMessage());
        }
    }

    @FXML
    public void clearFields() {
        cbAsset.setValue(null);
        tfQuantity.clear();
        tfPrice.clear();
        cbOrderType.setValue("BUY");
        lblMsg.setText("");
        loadAssets(); // Refresh prices just in case
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
