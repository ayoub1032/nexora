package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import tn.esprit.entities.Order;
import tn.esprit.services.OrderService;
import tn.esprit.utils.UserContext;

public class UserOrdersController {

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
    private Label lblMsg;

    private OrderService orderService;
    private long userId;

    @FXML
    public void initialize() {
        orderService = new OrderService();
        userId = UserContext.getCurrentUserId();
        setupTableColumns();
        loadUserOrders();
    }

    private void setupTableColumns() {
        colOrderId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        colOrderAssetId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colOrderQuantity.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colOrderPrice.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colOrderType.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
    }

    private void loadUserOrders() {
        try {
            ObservableList<Order> orders = FXCollections.observableArrayList(orderService.findByUserId(userId));
            ordersTable.setItems(orders);
        } catch (Exception e) {
            lblMsg.setText("Error loading orders: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }
}
