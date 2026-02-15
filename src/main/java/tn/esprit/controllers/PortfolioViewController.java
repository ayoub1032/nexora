package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Portfolio;
import tn.esprit.services.PortfolioService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.ValidationUtil;

public class PortfolioViewController {

    @FXML
    private TableView<Portfolio> portfolioTable;
    @FXML
    private TableColumn<Portfolio, Long> colPortfolioId;
    @FXML
    private TableColumn<Portfolio, Long> colUserId;
    @FXML
    private TableColumn<Portfolio, Double> colTotalValue;

    @FXML
    private TextField tfUserId;
    @FXML
    private TextField tfTotalValue;
    @FXML
    private TextField tfFilterUserId;
    @FXML
    private Label lblMsg;

    private final PortfolioService portfolioService = new PortfolioService();
    private final ObservableList<Portfolio> portfolios = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colPortfolioId.setCellValueFactory(new PropertyValueFactory<>("portfolioId"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colTotalValue.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        portfolioTable.setItems(portfolios);

        tfUserId.setTextFormatter(ValidationUtil.numericLongFormatter());
        tfTotalValue.setTextFormatter(ValidationUtil.positiveDecimalFormatter());

        refreshPortfolios();
    }

    @FXML
    public void refreshPortfolios() {
        try {
            portfolios.clear();
            portfolios.addAll(portfolioService.getAll());
            lblMsg.setText("");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void addPortfolio() {
        lblMsg.setText("");

        if (ValidationUtil.isBlank(tfUserId.getText())) {
            lblMsg.setText("⚠️ User ID is required.");
            return;
        }

        try {
            Portfolio p = new Portfolio();
            p.setUserId(Long.parseLong(tfUserId.getText()));
            p.setTotalValue(Double.parseDouble(tfTotalValue.getText().isEmpty() ? "0" : tfTotalValue.getText()));

            portfolioService.add(p);
            lblMsg.setText("✅ Portfolio added successfully.");
            clearFields();
            refreshPortfolios();

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Invalid number format.");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void updatePortfolio() {
        lblMsg.setText("");

        Portfolio sel = portfolioTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("⚠️ Please select a portfolio.");
            return;
        }

        try {
            sel.setUserId(Long.parseLong(tfUserId.getText()));
            sel.setTotalValue(Double.parseDouble(tfTotalValue.getText()));

            portfolioService.update(sel);
            lblMsg.setText("✅ Portfolio updated successfully.");
            clearFields();
            refreshPortfolios();

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Invalid number format.");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void deletePortfolio() {
        lblMsg.setText("");

        Portfolio sel = portfolioTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("⚠️ Please select a portfolio.");
            return;
        }

        try {
            portfolioService.delete(sel.getPortfolioId());
            lblMsg.setText("✅ Portfolio deleted successfully.");
            clearFields();
            refreshPortfolios();

        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void clearFields() {
        tfUserId.clear();
        tfTotalValue.clear();
        lblMsg.setText("");
        portfolioTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void back() {
        SceneNavigator.goTo("RoleSelection.fxml", "PiWeb - Start", 560, 360);
    }
}
