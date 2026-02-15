package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Order;
import tn.esprit.services.OrderService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.ValidationUtil;

public class OrderViewController {

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Long> colOrderId;
    @FXML
    private TableColumn<Order, Long> colAssetId;
    @FXML
    private TableColumn<Order, Long> colUserId;
    @FXML
    private TableColumn<Order, Integer> colQuantity;
    @FXML
    private TableColumn<Order, Double> colPrice;
    @FXML
    private TableColumn<Order, String> colType;

    @FXML
    private TextField tfAssetId;
    @FXML
    private TextField tfUserId;
    @FXML
    private TextField tfQuantity;
    @FXML
    private TextField tfPrice;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TextField tfFilterUserId;
    @FXML
    private Label lblMsg;

    private final OrderService orderService = new OrderService();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colAssetId.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        orderTable.setItems(orders);

        tfAssetId.setTextFormatter(ValidationUtil.numericLongFormatter());
        tfUserId.setTextFormatter(ValidationUtil.numericLongFormatter());
        tfQuantity.setTextFormatter(ValidationUtil.positiveIntegerFormatter());
        tfPrice.setTextFormatter(ValidationUtil.positiveDecimalFormatter());

        cbType.setItems(FXCollections.observableArrayList("BUY", "SELL"));

        refreshOrders();
    }

    @FXML
    public void refreshOrders() {
        try {
            orders.clear();
            orders.addAll(orderService.getAll());
            lblMsg.setText("");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void addOrder() {
        lblMsg.setText("");

        if (ValidationUtil.isBlank(tfAssetId.getText()) || ValidationUtil.isBlank(tfUserId.getText())) {
            lblMsg.setText("⚠️ Asset ID and User ID are required.");
            return;
        }

        try {
            Order o = new Order();
            o.setAssetId(Long.parseLong(tfAssetId.getText()));
            o.setUserId(Long.parseLong(tfUserId.getText()));
            o.setQuantity(Integer.parseInt(tfQuantity.getText().isEmpty() ? "1" : tfQuantity.getText()));
            o.setPrice(Double.parseDouble(tfPrice.getText().isEmpty() ? "0" : tfPrice.getText()));
            o.setType(cbType.getValue() != null ? cbType.getValue() : "BUY");

            orderService.add(o);
            lblMsg.setText("✅ Order added successfully.");
            clearFields();
            refreshOrders();

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Invalid number format.");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void updateOrder() {
        lblMsg.setText("");

        Order sel = orderTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("⚠️ Please select an order.");
            return;
        }

        try {
            sel.setAssetId(Long.parseLong(tfAssetId.getText()));
            sel.setUserId(Long.parseLong(tfUserId.getText()));
            sel.setQuantity(Integer.parseInt(tfQuantity.getText()));
            sel.setPrice(Double.parseDouble(tfPrice.getText()));
            sel.setType(cbType.getValue() != null ? cbType.getValue() : "BUY");

            orderService.update(sel);
            lblMsg.setText("✅ Order updated successfully.");
            clearFields();
            refreshOrders();

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Invalid number format.");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void deleteOrder() {
        lblMsg.setText("");

        Order sel = orderTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("⚠️ Please select an order.");
            return;
        }

        try {
            orderService.delete(sel.getOrderId());
            lblMsg.setText("✅ Order deleted successfully.");
            clearFields();
            refreshOrders();

        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void clearFields() {
        tfAssetId.clear();
        tfUserId.clear();
        tfQuantity.clear();
        tfPrice.clear();
        cbType.setValue("BUY");
        lblMsg.setText("");
        orderTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void back() {
        SceneNavigator.goTo("RoleSelection.fxml", "PiWeb - Start", 560, 360);
    }
}
