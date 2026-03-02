package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.entities.Asset;
import tn.esprit.entities.PortfolioAsset;
import tn.esprit.services.AssetService;
import tn.esprit.services.PortfolioAssetService;
import tn.esprit.services.PortfolioService;
import tn.esprit.utils.UserContext;

/**
 * Controller for User Portfolio Details view
 */
public class PortfolioDetailsViewController {

    @FXML
    private Label lblPortfolioValue;
    @FXML
    private Label lblAssetCount;
    @FXML
    private javafx.scene.chart.PieChart distributionChart;
    @FXML
    private javafx.scene.chart.LineChart<String, Number> evolutionChart;
    @FXML
    private javafx.scene.chart.CategoryAxis xAxis;
    @FXML
    private javafx.scene.chart.NumberAxis yAxis;
    @FXML
    private TableView<PortfolioAssetRow> assetsTable;
    @FXML
    private TableColumn<PortfolioAssetRow, Long> colAssetId;
    @FXML
    private TableColumn<PortfolioAssetRow, String> colAssetName;
    @FXML
    private TableColumn<PortfolioAssetRow, String> colAssetSymbol;
    @FXML
    private TableColumn<PortfolioAssetRow, Double> colAssetPrice;
    @FXML
    private TableColumn<PortfolioAssetRow, Integer> colQty;
    @FXML
    private TableColumn<PortfolioAssetRow, Double> colTotalValue;

    private long userId;
    private long portfolioId;
    private PortfolioService portfolioService;
    private PortfolioAssetService portfolioAssetService;
    private AssetService assetService;

    /**
     * Helper class for displaying portfolio holdings
     */
    public static class PortfolioAssetRow {
        private final long assetId;
        private final String name;
        private final String symbol;
        private final double price;
        private final int quantity;

        public PortfolioAssetRow(long assetId, String name, String symbol, double price, int quantity) {
            this.assetId = assetId;
            this.name = name;
            this.symbol = symbol;
            this.price = price;
            this.quantity = quantity;
        }

        public long getAssetId() {
            return assetId;
        }

        public String getName() {
            return name;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotalValue() {
            return price * quantity;
        }
    }

    @FXML
    public void initialize() {
        portfolioService = new PortfolioService();
        portfolioAssetService = new PortfolioAssetService();
        assetService = new AssetService();
        userId = UserContext.getCurrentUserId();

        setupTableColumns();
        loadPortfolioDetails();
    }

    private void setupTableColumns() {
        colAssetId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colAssetName.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colAssetSymbol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSymbol()));
        colAssetPrice.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colQty.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colTotalValue.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalValue()));
    }

    private void loadPortfolioDetails() {
        try {
            var portfolio = portfolioService.findByUserId(userId);
            if (portfolio != null) {
                portfolioId = portfolio.getPortfolioId();
                lblPortfolioValue.setText(String.format("💰 %.2f", portfolio.getTotalValue()));

                // Load assets with quantities
                ObservableList<PortfolioAssetRow> assetsData = loadAssetsFromPortfolio();
                assetsTable.setItems(assetsData);
                lblAssetCount.setText(assetsData.size() + " assets");

                populateCharts(portfolio.getTotalValue(), assetsData);
            } else {
                lblPortfolioValue.setText("0.00");
                lblAssetCount.setText("No portfolio");
            }
        } catch (Exception e) {
            lblAssetCount.setText("❌ Error: " + e.getMessage());
        }
    }

    private ObservableList<PortfolioAssetRow> loadAssetsFromPortfolio() {
        ObservableList<PortfolioAssetRow> rows = FXCollections.observableArrayList();

        try {
            // Get all portfolio assets for this portfolio
            var portfolioAssets = portfolioAssetService.getByPortfolio(portfolioId);

            // For each portfolio asset, get asset details
            for (PortfolioAsset pa : portfolioAssets) {
                Asset asset = assetService.findById(pa.getAssetId());
                if (asset != null) {
                    rows.add(new PortfolioAssetRow(
                            asset.getAssetId(),
                            asset.getName(),
                            asset.getSymbol(),
                            asset.getValue(),
                            pa.getQuantity()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading portfolio assets: " + e.getMessage());
        }

        return rows;
    }

    private void populateCharts(double totalPortfolioValue, ObservableList<PortfolioAssetRow> assetsData) {
        // --- 1. Pie Chart: Répartition par actifs ---
        ObservableList<javafx.scene.chart.PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (PortfolioAssetRow row : assetsData) {
            if (row.getTotalValue() > 0) {
                pieChartData.add(new javafx.scene.chart.PieChart.Data(row.getSymbol(), row.getTotalValue()));
            }
        }
        distributionChart.setData(pieChartData);

        // --- 2. Line Chart: Evolution du capital (Mocked History) ---
        evolutionChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Capital");

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd")
                .withZone(java.time.ZoneId.systemDefault());
        long currentTime = java.time.Instant.now().toEpochMilli();
        long dayMs = 24L * 60 * 60 * 1000;
        long startTime = currentTime - (30 * dayMs);

        double currentSimulatedValue = totalPortfolioValue;

        // Generate values mathematically backwards to simulate trace to current value
        java.util.List<Double> historicalValues = new java.util.ArrayList<>();
        historicalValues.add(totalPortfolioValue);
        for (int i = 0; i < 29; i++) {
            // Random walk variance, slightly biased towards growth historically
            double fluctuation = currentSimulatedValue * (0.05 * Math.random() - 0.02);
            currentSimulatedValue = currentSimulatedValue - fluctuation;
            if (currentSimulatedValue < 0)
                currentSimulatedValue = 0;
            historicalValues.add(0, currentSimulatedValue); // Add to beginning
        }

        double min = totalPortfolioValue;
        double max = totalPortfolioValue;
        for (int i = 0; i < 30; i++) {
            String dateLabel = formatter.format(java.time.Instant.ofEpochMilli(startTime + (i * dayMs)));
            double val = historicalValues.get(i);
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(dateLabel, val));
            if (val < min)
                min = val;
            if (val > max)
                max = val;
        }

        evolutionChart.getData().add(series);

        // Adjust Y-Axis padding dynamically
        if (max > min) {
            double padding = (max - min) * 0.1;
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(Math.max(0, min - padding));
            yAxis.setUpperBound(max + padding);
            yAxis.setTickUnit((max - min) / 5);
        }
    }
}
