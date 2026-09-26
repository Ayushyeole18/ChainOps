package com.supplychainx.controller;

import com.supplychainx.model.Warehouse;
import com.supplychainx.service.WarehouseService;
import com.supplychainx.ui.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class WarehouseController {

    @FXML private TableView<Warehouse> warehouseTable;
    @FXML private TableColumn<Warehouse, Integer> colId;
    @FXML private TableColumn<Warehouse, String> colCode;
    @FXML private TableColumn<Warehouse, String> colName;
    @FXML private TableColumn<Warehouse, String> colLocation;
    @FXML private TableColumn<Warehouse, String> colManager;
    @FXML private TableColumn<Warehouse, Integer> colCapacity;
    @FXML private TableColumn<Warehouse, Integer> colUtil;
    @FXML private TableColumn<Warehouse, String> colPct;
    @FXML private TableColumn<Warehouse, String> colStatus;

    @FXML private TextField searchField;

    // Form
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField managerField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label formTitleLabel;

    private final WarehouseService warehouseService = new WarehouseService();
    private final ObservableList<Warehouse> warehouseData = FXCollections.observableArrayList();
    private Warehouse selectedWarehouse;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("warehouseId"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colManager.setCellValueFactory(new PropertyValueFactory<>("managerName"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colUtil.setCellValueFactory(new PropertyValueFactory<>("currentUtilization"));
        colPct.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.1f%%", cell.getValue().getUtilizationPercentage())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        warehouseTable.setItems(warehouseData);

        warehouseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "MAINTENANCE", "INACTIVE"));
        statusCombo.setValue("ACTIVE");

        loadWarehouses();
    }

    private void loadWarehouses() {
        warehouseData.clear();
        warehouseData.addAll(warehouseService.getAllWarehouses());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            loadWarehouses();
        } else {
            warehouseData.clear();
            warehouseData.addAll(warehouseService.searchWarehouses(query));
        }
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        loadWarehouses();
    }

    @FXML
    private void handleNewWarehouse() {
        clearForm();
    }

    @FXML
    private void handleSaveWarehouse() {
        try {
            boolean isNew = (selectedWarehouse == null);
            Warehouse w = isNew ? new Warehouse() : selectedWarehouse;

            w.setCode(codeField.getText().trim());
            w.setWarehouseName(nameField.getText().trim());
            w.setLocation(locationField.getText().trim());
            w.setManagerName(managerField.getText().trim());

            try {
                w.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            } catch (Exception e) {
                AlertUtil.showWarning("Invalid Input", "Capacity must be a positive integer.");
                return;
            }

            w.setStatus(statusCombo.getValue());

            if (isNew) {
                warehouseService.createWarehouse(w);
                AlertUtil.showSuccess("Warehouse Created", "Warehouse " + w.getWarehouseName() + " created successfully.");
            } else {
                warehouseService.updateWarehouse(w);
                AlertUtil.showSuccess("Warehouse Updated", "Warehouse details updated successfully.");
            }

            clearForm();
            loadWarehouses();
        } catch (Exception e) {
            AlertUtil.showError("Save Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteWarehouse() {
        Warehouse w = warehouseTable.getSelectionModel().getSelectedItem();
        if (w == null) {
            AlertUtil.showWarning("Selection Required", "Please select a warehouse to delete.");
            return;
        }

        if (AlertUtil.confirm("Delete Warehouse", "Delete " + w.getWarehouseName() + "?", "This will remove the warehouse if no stock or orders exist.")) {
            try {
                warehouseService.deleteWarehouse(w.getWarehouseId());
                AlertUtil.showSuccess("Warehouse Deleted", "Warehouse deleted successfully.");
                clearForm();
                loadWarehouses();
            } catch (Exception e) {
                AlertUtil.showError("Delete Failed", e.getMessage());
            }
        }
    }

    private void populateForm(Warehouse w) {
        selectedWarehouse = w;
        formTitleLabel.setText("Edit Warehouse: " + w.getWarehouseName());
        codeField.setText(w.getCode());
        nameField.setText(w.getWarehouseName());
        locationField.setText(w.getLocation());
        managerField.setText(w.getManagerName());
        capacityField.setText(String.valueOf(w.getCapacity()));
        statusCombo.setValue(w.getStatus());
    }

    private void clearForm() {
        selectedWarehouse = null;
        formTitleLabel.setText("Add New Warehouse Hub");
        codeField.clear();
        nameField.clear();
        locationField.clear();
        managerField.clear();
        capacityField.setText("10000");
        statusCombo.setValue("ACTIVE");
        warehouseTable.getSelectionModel().clearSelection();
    }
}
