package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Asset;
import tn.esprit.services.AssetService;
import tn.esprit.utils.SceneNavigator;
import tn.esprit.utils.ValidationUtil;

public class AssetViewController {

    @FXML
    private TableView<Asset> assetTable;
    @FXML
    private TableColumn<Asset, Long> colAssetId;
    @FXML
    private TableColumn<Asset, String> colName;
    @FXML
    private TableColumn<Asset, String> colSymbol;
    @FXML
    private TableColumn<Asset, Double> colValue;
    @FXML
    private TableColumn<Asset, String> colType;

    @FXML
    private TextField tfName;
    @FXML
    private TextField tfSymbol;
    @FXML
    private TextField tfValue;
    @FXML
    private TextField tfType;
    @FXML
    private TextField tfFilterName;
    @FXML
    private Label lblMsg;

    private final AssetService assetService = new AssetService();
    private final ObservableList<Asset> assets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colAssetId.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        assetTable.setItems(assets);

        tfValue.setTextFormatter(ValidationUtil.positiveDecimalFormatter());

        refreshAssets();
    }

    @FXML
    public void refreshAssets() {
        try {
            assets.clear();
            assets.addAll(assetService.getAll());
            lblMsg.setText("");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void addAsset() {
        lblMsg.setText("");

        if (ValidationUtil.isBlank(tfName.getText())) {
            lblMsg.setText("⚠️ Name is required.");
            return;
        }

        try {
            Asset a = new Asset();
            a.setName(tfName.getText().trim());
            a.setSymbol(tfSymbol.getText().trim());
            a.setValue(Double.parseDouble(tfValue.getText().isEmpty() ? "0" : tfValue.getText()));
            a.setType(tfType.getText().trim());

            assetService.add(a);
            lblMsg.setText("✅ Asset added successfully.");
            clearFields();
            refreshAssets();

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Invalid value format.");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void updateAsset() {
        lblMsg.setText("");

        Asset sel = assetTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("⚠️ Please select an asset.");
            return;
        }

        try {
            sel.setName(tfName.getText().trim());
            sel.setSymbol(tfSymbol.getText().trim());
            sel.setValue(Double.parseDouble(tfValue.getText().isEmpty() ? "0" : tfValue.getText()));
            sel.setType(tfType.getText().trim());

            assetService.update(sel);
            lblMsg.setText("✅ Asset updated successfully.");
            clearFields();
            refreshAssets();

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Invalid value format.");
        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void deleteAsset() {
        lblMsg.setText("");

        Asset sel = assetTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMsg.setText("⚠️ Please select an asset.");
            return;
        }

        try {
            assetService.delete(sel.getAssetId());
            lblMsg.setText("✅ Asset deleted successfully.");
            clearFields();
            refreshAssets();

        } catch (Exception e) {
            lblMsg.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void clearFields() {
        tfName.clear();
        tfSymbol.clear();
        tfValue.clear();
        tfType.clear();
        lblMsg.setText("");
        assetTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void back() {
        SceneNavigator.goTo("RoleSelection.fxml", "PiWeb - Start", 560, 360);
    }
}
