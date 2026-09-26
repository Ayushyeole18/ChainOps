package com.supplychainx.controller;

import com.supplychainx.model.*;
import com.supplychainx.service.*;
import com.supplychainx.ui.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockTransferController {

    @FXML private TableView<StockTransfer> transferTable;
    @FXML private TableColumn<StockTransfer, String> colTransferNum;
    @FXML private TableColumn<StockTransfer, String> colSource;
    @FXML private TableColumn<StockTransfer, String> colDest;
    @FXML private TableColumn<StockTransfer, LocalDate> colDate;
    @FXML private TableColumn<StockTransfer, String> colStatus;
    @FXML private TableColumn<StockTransfer, String> colUser;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;

    // Transfer Form
    @FXML private ComboBox<Warehouse> sourceWarehouseCombo;
    @FXML private ComboBox<Warehouse> destWarehouseCombo;
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private Label availableStockLabel;
    @FXML private TextArea notesArea;

    private final StockTransferService transferService = new StockTransferService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();

    private final ObservableList<StockTransfer> transferData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTransferNum.setCellValueFactory(new PropertyValueFactory<>("transferNumber"));
        colSource.setCellValueFactory(new PropertyValueFactory<>("sourceWarehouseName"));
        colDest.setCellValueFactory(new PropertyValueFactory<>("destinationWarehouseName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("transferDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("initiatedByName"));

        transferTable.setItems(transferData);

        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        sourceWarehouseCombo.setItems(FXCollections.observableArrayList(warehouses));
        destWarehouseCombo.setItems(FXCollections.observableArrayList(warehouses));

        productCombo.setItems(FXCollections.observableArrayList(productService.getAllProducts()));

        sourceWarehouseCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateStockAvailability());
        productCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateStockAvailability());

        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "PENDING", "IN_TRANSIT", "COMPLETED", "CANCELLED"));
        statusFilterCombo.setValue("ALL");

        loadTransfers();
    }

    private void updateStockAvailability() {
        Warehouse w = sourceWarehouseCombo.getValue();
        Product p = productCombo.getValue();
        if (w != null && p != null) {
            inventoryService.getInventory(p.getProductId(), w.getWarehouseId()).ifPresentOrElse(
                    inv -> availableStockLabel.setText("Available in " + w.getCode() + ": " + inv.getQuantityAvailable() + " " + p.getUnit()),
                    () -> availableStockLabel.setText("No inventory in source warehouse.")
            );
        } else {
            availableStockLabel.setText("");
        }
    }

    private void loadTransfers() {
        transferData.clear();
        transferData.addAll(transferService.getAllTransfers());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String status = statusFilterCombo.getValue();
        transferData.clear();
        transferData.addAll(transferService.searchAndFilter(query, status));
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        statusFilterCombo.setValue("ALL");
        loadTransfers();
    }

    @FXML
    private void handleExecuteTransfer() {
        Warehouse src = sourceWarehouseCombo.getValue();
        Warehouse dest = destWarehouseCombo.getValue();
        Product prod = productCombo.getValue();

        if (src == null || dest == null) {
            AlertUtil.showWarning("Missing Warehouse", "Please select both Source and Destination warehouses.");
            return;
        }
        if (src.getWarehouseId() == dest.getWarehouseId()) {
            AlertUtil.showWarning("Invalid Warehouses", "Source and Destination warehouses must be different facilities.");
            return;
        }
        if (prod == null) {
            AlertUtil.showWarning("Missing Product", "Please choose a product to transfer.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) {
                AlertUtil.showWarning("Invalid Quantity", "Transfer quantity must be greater than zero.");
                return;
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Invalid Quantity", "Please enter a valid positive integer quantity.");
            return;
        }

        if (AlertUtil.confirm("Confirm Stock Transfer", "Initiate transfer of " + qty + " units?",
                "This will atomically decrease inventory in " + src.getWarehouseName() +
                " and increase inventory in " + dest.getWarehouseName() + ".")) {
            try {
                StockTransfer st = new StockTransfer();
                st.setSourceWarehouseId(src.getWarehouseId());
                st.setDestinationWarehouseId(dest.getWarehouseId());
                st.setTransferDate(LocalDate.now());
                st.setNotes(notesArea.getText());

                StockTransferItem item = new StockTransferItem(prod.getProductId(), prod.getSku(), prod.getProductName(), qty);
                List<StockTransferItem> items = new ArrayList<>();
                items.add(item);
                st.setItems(items);

                int userId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUserId() : 1;
                transferService.executeStockTransfer(st, userId);

                AlertUtil.showSuccess("Transfer Completed", "Transferred " + qty + " units of " + prod.getProductName() +
                        " from " + src.getCode() + " to " + dest.getCode() + " successfully.");

                quantityField.clear();
                notesArea.clear();
                updateStockAvailability();
                loadTransfers();
            } catch (Exception e) {
                AlertUtil.showError("Transfer Failed", e.getMessage());
            }
        }
    }
}
