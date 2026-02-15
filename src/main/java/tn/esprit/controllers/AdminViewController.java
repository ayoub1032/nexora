package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import tn.esprit.entities.Asset;
import tn.esprit.entities.Order;
import tn.esprit.entities.User;
import tn.esprit.services.AssetService;
import tn.esprit.services.AuthService;
import tn.esprit.services.OrderService;
import tn.esprit.services.UserManagementService;
import tn.esprit.utils.SceneNavigator;

public class AdminViewController {

    // ========== ASSETS CONTROLS ==========
    @FXML private TableView<Asset> assetsTable;
    @FXML private TableColumn<Asset, Long> colAssetId;
    @FXML private TableColumn<Asset, String> colAssetName;
    @FXML private TableColumn<Asset, String> colAssetSymbol;
    @FXML private TableColumn<Asset, Double> colAssetValue;
    @FXML private TableColumn<Asset, String> colAssetType;

    @FXML private TextField tfAssetId, tfAssetName, tfAssetSymbol, tfAssetValue, tfAssetType;
    @FXML private Label lblAssetMsg;

    // ========== ORDERS CONTROLS ==========
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Long> colOrderId;
    @FXML private TableColumn<Order, Long> colOrderAssetId;
    @FXML private TableColumn<Order, Long> colOrderUserId;
    @FXML private TableColumn<Order, Integer> colOrderQuantity;
    @FXML private TableColumn<Order, Double> colOrderPrice;
    @FXML private TableColumn<Order, String> colOrderType;

    @FXML private TextField tfOrderId, tfOrderUserId, tfOrderQuantity, tfOrderPrice;
    @FXML private ComboBox<Asset> cbOrderAsset;
    @FXML private ComboBox<String> cbOrderType;
    @FXML private Label lblOrderMsg;

    // ========== USERS CONTROLS ==========
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> colUserId;
    @FXML private TableColumn<User, String> colUserName;
    @FXML private TableColumn<User, String> colUserEmail;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserStatus;

    @FXML private TextField tfUserId, tfUserFullName, tfUserEmail, tfUserPassword;
    @FXML private ComboBox<String> cbUserRole, cbUserStatus;
    @FXML private Label lblUserMsg;

    // ========== SERVICES ==========
    private AssetService assetService;
    private OrderService orderService;
    private UserManagementService userManagementService;

    @FXML
    public void initialize() {
        assetService = new AssetService();
        orderService = new OrderService();
        userManagementService = new UserManagementService();

        setupTableColumns();
        loadAllData();
        setupTableRowSelection();
        setupOrderType();
        setupUserType();
    }

    // ========== ASSET METHODS ==========
    private void setupTableColumns() {
        // Assets
        colAssetId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colAssetName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colAssetSymbol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSymbol()));
        colAssetValue.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getValue()));
        colAssetType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));

        // Orders
        colOrderId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        colOrderAssetId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetId()));
        colOrderUserId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUserId()));
        colOrderQuantity.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colOrderPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colOrderType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));

        // Users
        colUserId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUserId()));
        colUserName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        colUserEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        colUserRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        colUserStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAccountStatus()));
    }

    private void loadAllData() {
        loadAssets();
        loadOrders();
        loadUsers();
    }

    private void loadAssets() {
        try {
            ObservableList<Asset> assets = FXCollections.observableArrayList(assetService.getAll());
            assetsTable.setItems(assets);
        } catch (Exception e) {
            showAssetError("Error loading assets: " + e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            ObservableList<Order> orders = FXCollections.observableArrayList(orderService.getAll());
            ordersTable.setItems(orders);
        } catch (Exception e) {
            showOrderError("Error loading orders: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(userManagementService.getAllUsers());
            usersTable.setItems(users);
        } catch (Exception e) {
            showUserError("Error loading users: " + e.getMessage());
        }
    }

    private void setupTableRowSelection() {
        assetsTable.setOnMouseClicked(event -> {
            Asset selected = assetsTable.getSelectionModel().getSelectedItem();
            if (selected != null) populateAssetForm(selected);
        });

        ordersTable.setOnMouseClicked(event -> {
            Order selected = ordersTable.getSelectionModel().getSelectedItem();
            if (selected != null) populateOrderForm(selected);
        });

        usersTable.setOnMouseClicked(event -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected != null) populateUserForm(selected);
        });
    }

    private void populateAssetForm(Asset asset) {
        tfAssetId.setText(String.valueOf(asset.getAssetId()));
        tfAssetName.setText(asset.getName());
        tfAssetSymbol.setText(asset.getSymbol());
        tfAssetValue.setText(String.valueOf(asset.getValue()));
        tfAssetType.setText(asset.getType());
    }

    @FXML
    public void createAsset() {
        try {
            if (tfAssetName.getText().isEmpty() || tfAssetSymbol.getText().isEmpty() || tfAssetValue.getText().isEmpty()) {
                showAssetError("Please fill all required fields");
                return;
            }

            Asset asset = new Asset();
            asset.setName(tfAssetName.getText());
            asset.setSymbol(tfAssetSymbol.getText());
            asset.setValue(Double.parseDouble(tfAssetValue.getText()));
            asset.setType(tfAssetType.getText());

            assetService.add(asset);
            showAssetSuccess("Asset created successfully!");
            clearAssetForm();
            loadAssets();
        } catch (NumberFormatException e) {
            showAssetError("Invalid value format");
        } catch (Exception e) {
            showAssetError("Error creating asset: " + e.getMessage());
        }
    }

    @FXML
    public void updateAsset() {
        try {
            if (tfAssetId.getText().isEmpty()) {
                showAssetError("Please select an asset to update");
                return;
            }

            Asset asset = new Asset();
            asset.setAssetId(Long.parseLong(tfAssetId.getText()));
            asset.setName(tfAssetName.getText());
            asset.setSymbol(tfAssetSymbol.getText());
            asset.setValue(Double.parseDouble(tfAssetValue.getText()));
            asset.setType(tfAssetType.getText());

            assetService.update(asset);
            showAssetSuccess("Asset updated successfully!");
            clearAssetForm();
            loadAssets();
        } catch (Exception e) {
            showAssetError("Error updating asset: " + e.getMessage());
        }
    }

    @FXML
    public void deleteAsset() {
        try {
            if (tfAssetId.getText().isEmpty()) {
                showAssetError("Please select an asset to delete");
                return;
            }

            long assetId = Long.parseLong(tfAssetId.getText());
            assetService.delete(assetId);
            showAssetSuccess("Asset deleted successfully!");
            clearAssetForm();
            loadAssets();
        } catch (Exception e) {
            showAssetError("Error deleting asset: " + e.getMessage());
        }
    }

    @FXML
    public void clearAssetForm() {
        tfAssetId.clear();
        tfAssetName.clear();
        tfAssetSymbol.clear();
        tfAssetValue.clear();
        tfAssetType.clear();
        lblAssetMsg.setText("");
    }

    // ========== ORDER METHODS ==========
    private void setupOrderType() {
        cbOrderType.setItems(FXCollections.observableArrayList("BUY", "SELL"));
        
        // Setup asset combobox display
        cbOrderAsset.setCellFactory(param -> new javafx.scene.control.ListCell<Asset>() {
            @Override
            protected void updateItem(Asset asset, boolean empty) {
                super.updateItem(asset, empty);
                setText(empty ? "" : asset.getName() + " (" + asset.getSymbol() + ") - " + asset.getValue());
            }
        });
        cbOrderAsset.setButtonCell(new javafx.scene.control.ListCell<Asset>() {
            @Override
            protected void updateItem(Asset asset, boolean empty) {
                super.updateItem(asset, empty);
                setText(empty ? "" : asset.getName() + " (" + asset.getSymbol() + ") - " + asset.getValue());
            }
        });
        
        // Load assets into combobox and setup listener
        try {
            ObservableList<Asset> assets = FXCollections.observableArrayList(assetService.getAll());
            cbOrderAsset.setItems(assets);
            
            // When asset is selected, auto-fill price
            cbOrderAsset.setOnAction(event -> {
                Asset selected = cbOrderAsset.getValue();
                if (selected != null) {
                    tfOrderPrice.setText(String.valueOf(selected.getValue()));
                }
            });
        } catch (Exception e) {
            showOrderError("Error loading assets: " + e.getMessage());
        }
    }

    private void populateOrderForm(Order order) {
        tfOrderId.setText(String.valueOf(order.getOrderId()));
        
        // Find and select the asset in combobox
        Asset selectedAsset = assetService.findById(order.getAssetId());
        if (selectedAsset != null) {
            cbOrderAsset.setValue(selectedAsset);
        }
        
        tfOrderUserId.setText(String.valueOf(order.getUserId()));
        tfOrderQuantity.setText(String.valueOf(order.getQuantity()));
        tfOrderPrice.setText(String.valueOf(order.getPrice()));
        cbOrderType.setValue(order.getType());
    }

    @FXML
    public void createOrder() {
        try {
            if (cbOrderAsset.getValue() == null || tfOrderUserId.getText().isEmpty() || 
                tfOrderQuantity.getText().isEmpty() || cbOrderType.getValue() == null) {
                showOrderError("Please fill all required fields");
                return;
            }

            Order order = new Order();
            order.setAssetId(cbOrderAsset.getValue().getAssetId());
            order.setUserId(Long.parseLong(tfOrderUserId.getText()));
            order.setQuantity(Integer.parseInt(tfOrderQuantity.getText()));
            order.setPrice(cbOrderAsset.getValue().getValue()); // Price from asset
            order.setType(cbOrderType.getValue());

            orderService.add(order);
            showOrderSuccess("Order created successfully!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            showOrderError("Error creating order: " + e.getMessage());
        }
    }

    @FXML
    public void updateOrder() {
        try {
            if (tfOrderId.getText().isEmpty() || cbOrderAsset.getValue() == null || 
                tfOrderUserId.getText().isEmpty() || tfOrderQuantity.getText().isEmpty()) {
                showOrderError("Please fill all required fields");
                return;
            }

            Order order = new Order();
            order.setOrderId(Long.parseLong(tfOrderId.getText()));
            order.setAssetId(cbOrderAsset.getValue().getAssetId());
            order.setUserId(Long.parseLong(tfOrderUserId.getText()));
            order.setQuantity(Integer.parseInt(tfOrderQuantity.getText()));
            order.setPrice(cbOrderAsset.getValue().getValue()); // Price from asset
            order.setType(cbOrderType.getValue());

            orderService.update(order);
            showOrderSuccess("Order updated successfully!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            showOrderError("Error updating order: " + e.getMessage());
        }
    }

    @FXML
    public void deleteOrder() {
        try {
            if (tfOrderId.getText().isEmpty()) {
                showOrderError("Please select an order to delete");
                return;
            }

            long orderId = Long.parseLong(tfOrderId.getText());
            orderService.delete(orderId);
            showOrderSuccess("Order deleted successfully!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            showOrderError("Error deleting order: " + e.getMessage());
        }
    }

    @FXML
    public void clearOrderForm() {
        tfOrderId.clear();
        cbOrderAsset.setValue(null);
        tfOrderUserId.clear();
        tfOrderQuantity.clear();
        tfOrderPrice.clear();
        cbOrderType.setValue(null);
        lblOrderMsg.setText("");
    }

    // ========== USER METHODS ==========
    private void setupUserType() {
        cbUserRole.setItems(FXCollections.observableArrayList("ADMIN", "USER"));
        cbUserStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        cbUserRole.setValue("USER");
        cbUserStatus.setValue("ACTIVE");
    }

    private void populateUserForm(User user) {
        tfUserId.setText(String.valueOf(user.getUserId()));
        tfUserFullName.setText(user.getFullName());
        tfUserEmail.setText(user.getEmail());
        tfUserPassword.clear(); // Don't show password
        cbUserRole.setValue(user.getRole());
        cbUserStatus.setValue(user.getAccountStatus());
    }

    @FXML
    public void createUser() {
        try {
            if (tfUserFullName.getText().isEmpty() || tfUserEmail.getText().isEmpty() || tfUserPassword.getText().isEmpty()) {
                showUserError("Please fill all required fields (password required for creation)");
                return;
            }

            // Simple password hashing (in production, use bcrypt or similar)
            String passwordHash = String.valueOf(tfUserPassword.getText().hashCode());

            User user = new User(
                tfUserFullName.getText(),
                tfUserEmail.getText(),
                passwordHash,
                cbUserRole.getValue() != null ? cbUserRole.getValue() : "USER"
            );
            user.setAccountStatus(cbUserStatus.getValue() != null ? cbUserStatus.getValue() : "ACTIVE");

            // This will auto-create wallet and portfolio
            userManagementService.createUserWithDependencies(user);
            showUserSuccess("User created successfully! (Auto-created: wallet & portfolio)");
            clearUserForm();
            loadUsers();
        } catch (Exception e) {
            showUserError("Error creating user: " + e.getMessage());
        }
    }

    @FXML
    public void updateUser() {
        try {
            if (tfUserId.getText().isEmpty()) {
                showUserError("Please select a user to update");
                return;
            }

            long userId = Long.parseLong(tfUserId.getText());

            // Use UserManagementService to handle role/status updates
            userManagementService.updateUserRole(userId, cbUserRole.getValue());
            userManagementService.updateUserStatus(userId, cbUserStatus.getValue());

            showUserSuccess("User updated successfully!");
            clearUserForm();
            loadUsers();
        } catch (Exception e) {
            showUserError("Error updating user: " + e.getMessage());
        }
    }

    @FXML
    public void deleteUser() {
        try {
            if (tfUserId.getText().isEmpty()) {
                showUserError("Please select a user to delete");
                return;
            }

            long userId = Long.parseLong(tfUserId.getText());
            userManagementService.deleteUserWithDependencies(userId);
            showUserSuccess("User deleted successfully! (Cascade deleted: wallet, portfolio, orders)");
            clearUserForm();
            loadUsers();
        } catch (Exception e) {
            showUserError("Error deleting user: " + e.getMessage());
        }
    }

    @FXML
    public void clearUserForm() {
        tfUserId.clear();
        tfUserFullName.clear();
        tfUserEmail.clear();
        tfUserPassword.clear();
        cbUserRole.setValue("USER");
        cbUserStatus.setValue("ACTIVE");
        lblUserMsg.setText("");
    }

    // ========== UTILITY METHODS ==========
    private void showAssetError(String message) {
        lblAssetMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
        lblAssetMsg.setText(message);
    }

    private void showAssetSuccess(String message) {
        lblAssetMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 700;");
        lblAssetMsg.setText(message);
    }

    private void showOrderError(String message) {
        lblOrderMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
        lblOrderMsg.setText(message);
    }

    private void showOrderSuccess(String message) {
        lblOrderMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 700;");
        lblOrderMsg.setText(message);
    }

    private void showUserError(String message) {
        lblUserMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
        lblUserMsg.setText(message);
    }

    private void showUserSuccess(String message) {
        lblUserMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 700;");
        lblUserMsg.setText(message);
    }

    @FXML
    public void back() {
        AuthService authService = new AuthService();
        authService.logout();
        SceneNavigator.goTo("LoginView.fxml", "PiWeb - Authentication", 560, 500);
    }
}
