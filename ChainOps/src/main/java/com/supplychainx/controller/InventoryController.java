package com.supplychainx.controller;

import com.supplychainx.model.InventoryItem;
import com.supplychainx.model.Product;
import com.supplychainx.model.StockTransaction;
import com.supplychainx.model.Warehouse;
import com.supplychainx.service.AuthService;
import com.supplychainx.service.InventoryService;
import com.supplychainx.service.ProductService;
import com.supplychainx.service.WarehouseService;
import com.supplychainx.ui.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;

public class InventoryController {

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colSku;
    @FXML private TableColumn<InventoryItem, String> colProduct;
    @FXML private TableColumn<InventoryItem, String> colCategory;
    @FXML private TableColumn<InventoryItem, String> colWarehouse;
    @FXML private TableColumn<InventoryItem, Integer> colAvailable;
    @FXML private TableColumn<InventoryItem, Integer> colReserved;
    @FXML private TableColumn<InventoryItem, Integer> colReorder;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, Timestamp> colUpdated;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> warehouseFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;

    // Stock Adjustment Form
    @FXML private ComboBox<Product> adjProductCombo;
    @FXML private ComboBox<Warehouse> adjWarehouseCombo;
    @FXML private ComboBox<String> adjTypeCombo; // Stock In, Stock Out, Recalibration
    @FXML private TextField adjQuantityField;
    @FXML private TextField adjReferenceField;
    @FXML private TextArea adjNotesArea;

    // Audit History Table
    @FXML private TableView<StockTransaction> auditTable;
    @FXML private TableColumn<StockTransaction, Timestamp> colAuditDate;
    @FXML private TableColumn<StockTransaction, String> colAuditSku;
    @FXML private TableColumn<StockTransaction, String> colAuditProduct;
    @FXML private TableColumn<StockTransaction, String> colAuditWarehouse;
    @FXML private TableColumn<StockTransaction, String> colAuditType;
    @FXML private TableColumn<StockTransaction, Integer> colAuditQty;
    @FXML private TableColumn<StockTransaction, String> colAuditRef;
    @FXML private TableColumn<StockTransaction, String> colAuditUser;

    private final InventoryService inventoryService = new InventoryService();
    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();

    private final ObservableList<InventoryItem> inventoryData = FXCollections.observableArrayList();
    private final ObservableList<StockTransaction> auditData = FXCollections.observableArrayList();
    private List<Warehouse> allWarehouses;

    @FXML
    public void initialize() {
        // Inventory Table Columns
        colSku.setCellValueFactory(new PropertyValueFactory<>("productSku"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("quantityAvailable"));
        colReserved.setCellValueFactory(new PropertyValueFactory<>("quantityReserved"));
        colReorder.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStockStatus()));
        colUpdated.setCellValueFactory(new PropertyValueFactory<>("lastCountedAt"));

        inventoryTable.setItems(inventoryData);

        // Audit Table Columns
        colAuditDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colAuditSku.setCellValueFactory(new PropertyValueFactory<>("productSku"));
        colAuditProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colAuditWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colAuditType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        colAuditQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAuditRef.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
        colAuditUser.setCellValueFactory(new PropertyValueFactory<>("performedByName"));

        auditTable.setItems(auditData);

        // Load Combos
        allWarehouses = warehouseService.getAllWarehouses();
        adjWarehouseCombo.setItems(FXCollections.observableArrayList(allWarehouses));

        ObservableList<String> whFilterList = FXCollections.observableArrayList("ALL");
        for (Warehouse w : allWarehouses) {
            whFilterList.add(w.getWarehouseName());
        }
        warehouseFilterCombo.setItems(whFilterList);
        warehouseFilterCombo.setValue("ALL");

        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"));
        statusFilterCombo.setValue("ALL");

        adjProductCombo.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
        adjTypeCombo.setItems(FXCollections.observableArrayList("Stock In (Inbound/Restock)", "Stock Out (Damage/Loss/Return)"));
        adjTypeCombo.setValue("Stock In (Inbound/Restock)");

        loadInventory();
        loadAuditHistory();
    }

    private void loadInventory() {
        inventoryData.clear();
        inventoryData.addAll(inventoryService.getAllInventory());
    }

    private void loadAuditHistory() {
        auditData.clear();
        auditData.addAll(inventoryService.getStockHistory());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String selectedWh = warehouseFilterCombo.getValue();
        Integer whId = null;

        if (selectedWh != null && !selectedWh.equalsIgnoreCase("ALL")) {
            for (Warehouse w : allWarehouses) {
                if (w.getWarehouseName().equalsIgnoreCase(selectedWh)) {
                    whId = w.getWarehouseId();
                    break;
                }
            }
        }

        String status = statusFilterCombo.getValue();
        inventoryData.clear();
        inventoryData.addAll(inventoryService.searchAndFilter(query, whId, status));
    }

    @FXML
    private void handleFilterLowStockOnly() {
        statusFilterCombo.setValue("LOW_STOCK");
        handleSearchAndFilter();
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        warehouseFilterCombo.setValue("ALL");
        statusFilterCombo.setValue("ALL");
        loadInventory();
    }

    @FXML
    private void handleExecuteAdjustment() {
        Product p = adjProductCombo.getValue();
        Warehouse w = adjWarehouseCombo.getValue();
        String type = adjTypeCombo.getValue();
        String qtyText = adjQuantityField.getText();
        String ref = adjReferenceField.getText();
        String notes = adjNotesArea.getText();

        if (p == null || w == null) {
            AlertUtil.showWarning("Missing Selection", "Please choose both a Product and a Warehouse.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText.trim());
            if (quantity <= 0) {
                AlertUtil.showWarning("Invalid Quantity", "Quantity must be greater than zero.");
                return;
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Invalid Quantity", "Please enter a valid positive whole number.");
            return;
        }

        try {
            Integer userId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUserId() : null;
            if (type.contains("Stock In")) {
                inventoryService.stockIn(p.getProductId(), w.getWarehouseId(), quantity, ref, notes, userId);
                AlertUtil.showSuccess("Stock In Executed", "Added " + quantity + " units of " + p.getProductName() + " to " + w.getWarehouseName());
            } else {
                inventoryService.stockOut(p.getProductId(), w.getWarehouseId(), quantity, ref, notes, userId);
                AlertUtil.showSuccess("Stock Out Executed", "Deducted " + quantity + " units of " + p.getProductName() + " from " + w.getWarehouseName());
            }

            // Clear inputs
            adjQuantityField.clear();
            adjReferenceField.clear();
            adjNotesArea.clear();

            loadInventory();
            loadAuditHistory();
        } catch (Exception e) {
            AlertUtil.showError("Adjustment Failed", e.getMessage());
        }
    }
}
