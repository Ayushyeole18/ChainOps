package com.supplychainx.controller;

import com.supplychainx.model.*;
import com.supplychainx.service.*;
import com.supplychainx.ui.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderController {

    @FXML private TableView<PurchaseOrder> poTable;
    @FXML private TableColumn<PurchaseOrder, String> colPoNumber;
    @FXML private TableColumn<PurchaseOrder, String> colSupplier;
    @FXML private TableColumn<PurchaseOrder, String> colWarehouse;
    @FXML private TableColumn<PurchaseOrder, LocalDate> colOrderDate;
    @FXML private TableColumn<PurchaseOrder, LocalDate> colExpectedDate;
    @FXML private TableColumn<PurchaseOrder, BigDecimal> colTotal;
    @FXML private TableColumn<PurchaseOrder, String> colStatus;
    @FXML private TableColumn<PurchaseOrder, String> colCreatedBy;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;

    // New PO Form
    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private ComboBox<Warehouse> warehouseCombo;
    @FXML private DatePicker orderDatePicker;
    @FXML private DatePicker expectedDatePicker;
    @FXML private TextArea notesArea;

    // Line Item controls
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private TextField unitCostField;
    @FXML private TableView<PurchaseOrderItem> itemsTable;
    @FXML private TableColumn<PurchaseOrderItem, String> colItemSku;
    @FXML private TableColumn<PurchaseOrderItem, String> colItemName;
    @FXML private TableColumn<PurchaseOrderItem, Integer> colItemQty;
    @FXML private TableColumn<PurchaseOrderItem, BigDecimal> colItemCost;
    @FXML private TableColumn<PurchaseOrderItem, BigDecimal> colItemSubtotal;
    @FXML private Label totalAmountLabel;

    private final PurchaseOrderService poService = new PurchaseOrderService();
    private final SupplierService supplierService = new SupplierService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final ProductService productService = new ProductService();

    private final ObservableList<PurchaseOrder> poData = FXCollections.observableArrayList();
    private final ObservableList<PurchaseOrderItem> draftItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colPoNumber.setCellValueFactory(new PropertyValueFactory<>("poNumber"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colExpectedDate.setCellValueFactory(new PropertyValueFactory<>("expectedDeliveryDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdByName"));

        poTable.setItems(poData);

        // Item Table
        colItemSku.setCellValueFactory(new PropertyValueFactory<>("productSku"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantityOrdered"));
        colItemCost.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        colItemSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        itemsTable.setItems(draftItems);

        // Load Suppliers, Warehouses, Products
        supplierCombo.setItems(FXCollections.observableArrayList(supplierService.getAllSuppliers()));
        warehouseCombo.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));
        productCombo.setItems(FXCollections.observableArrayList(productService.getAllProducts()));

        productCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getUnitPrice() != null) {
                unitCostField.setText(newVal.getUnitPrice().toPlainString());
            }
        });

        orderDatePicker.setValue(LocalDate.now());
        expectedDatePicker.setValue(LocalDate.now().plusDays(7));

        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "DRAFT", "PENDING", "APPROVED", "RECEIVED", "CANCELLED"));
        statusFilterCombo.setValue("ALL");

        loadPurchaseOrders();
    }

    private void loadPurchaseOrders() {
        poData.clear();
        poData.addAll(poService.getAllPurchaseOrders());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String status = statusFilterCombo.getValue();
        poData.clear();
        poData.addAll(poService.searchAndFilter(query, status, null, null, null));
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        statusFilterCombo.setValue("ALL");
        loadPurchaseOrders();
    }

    @FXML
    private void handleAddItem() {
        Product p = productCombo.getValue();
        if (p == null) {
            AlertUtil.showWarning("Missing Product", "Please select a product to add to the order.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) {
                AlertUtil.showWarning("Invalid Quantity", "Quantity must be greater than zero.");
                return;
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Invalid Quantity", "Please enter a valid integer quantity.");
            return;
        }

        BigDecimal cost;
        try {
            cost = new BigDecimal(unitCostField.getText().trim());
            if (cost.compareTo(BigDecimal.ZERO) < 0) {
                AlertUtil.showWarning("Invalid Cost", "Unit Cost cannot be negative.");
                return;
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Invalid Cost", "Please enter a valid price amount.");
            return;
        }

        // Check if item already exists in draft, if so update quantity
        for (PurchaseOrderItem item : draftItems) {
            if (item.getProductId() == p.getProductId()) {
                item.setQuantityOrdered(item.getQuantityOrdered() + qty);
                itemsTable.refresh();
                recalculateTotal();
                return;
            }
        }

        PurchaseOrderItem newItem = new PurchaseOrderItem(p.getProductId(), p.getSku(), p.getProductName(), qty, cost);
        draftItems.add(newItem);
        recalculateTotal();

        quantityField.clear();
    }

    @FXML
    private void handleRemoveItem() {
        PurchaseOrderItem item = itemsTable.getSelectionModel().getSelectedItem();
        if (item != null) {
            draftItems.remove(item);
            recalculateTotal();
        }
    }

    private void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PurchaseOrderItem item : draftItems) {
            sum = sum.add(item.getSubtotal());
        }
        totalAmountLabel.setText(String.format("$%,.2f", sum.doubleValue()));
    }

    @FXML
    private void handleCreateOrder() {
        Supplier s = supplierCombo.getValue();
        Warehouse w = warehouseCombo.getValue();

        if (s == null) {
            AlertUtil.showWarning("Missing Supplier", "Please choose a supplier.");
            return;
        }
        if (w == null) {
            AlertUtil.showWarning("Missing Warehouse", "Please choose a destination warehouse.");
            return;
        }
        if (draftItems.isEmpty()) {
            AlertUtil.showWarning("Missing Items", "Please add at least one product item to the order.");
            return;
        }

        try {
            PurchaseOrder po = new PurchaseOrder();
            po.setSupplierId(s.getSupplierId());
            po.setWarehouseId(w.getWarehouseId());
            po.setOrderDate(orderDatePicker.getValue());
            po.setExpectedDeliveryDate(expectedDatePicker.getValue());
            po.setNotes(notesArea.getText());
            po.setStatus("PENDING");
            if (AuthService.getCurrentUser() != null) {
                po.setCreatedByUserId(AuthService.getCurrentUser().getUserId());
            }

            po.setItems(new ArrayList<>(draftItems));

            int poId = poService.createPurchaseOrder(po);
            AlertUtil.showSuccess("PO Created", "Purchase Order #" + po.getPoNumber() + " created successfully!");

            // Clear draft
            draftItems.clear();
            recalculateTotal();
            notesArea.clear();
            loadPurchaseOrders();
        } catch (Exception e) {
            AlertUtil.showError("Creation Failed", e.getMessage());
        }
    }

    @FXML
    private void handleApproveSelected() {
        PurchaseOrder selected = poTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a purchase order from the table to approve.");
            return;
        }

        try {
            int userId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUserId() : 1;
            poService.approvePurchaseOrder(selected.getPoId(), userId);
            AlertUtil.showSuccess("Order Approved", "Purchase order " + selected.getPoNumber() + " is now APPROVED.");
            loadPurchaseOrders();
        } catch (Exception e) {
            AlertUtil.showError("Approval Error", e.getMessage());
        }
    }

    @FXML
    private void handleReceiveSelected() {
        PurchaseOrder selected = poTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a purchase order from the table to receive.");
            return;
        }

        if (AlertUtil.confirm("Receive Purchase Order", "Receive " + selected.getPoNumber() + "?",
                "This will atomically update inventory stock levels and record an inbound audit transaction.")) {
            try {
                int userId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUserId() : 1;
                poService.receivePurchaseOrder(selected.getPoId(), userId);
                AlertUtil.showSuccess("Stock Received", "Purchase Order " + selected.getPoNumber() +
                        " received successfully! Warehouse inventory levels have been updated.");
                loadPurchaseOrders();
            } catch (Exception e) {
                AlertUtil.showError("Receiving Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelSelected() {
        PurchaseOrder selected = poTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a purchase order to cancel.");
            return;
        }

        if (AlertUtil.confirm("Cancel Purchase Order", "Cancel " + selected.getPoNumber() + "?", "Are you sure you want to cancel this order?")) {
            try {
                poService.cancelPurchaseOrder(selected.getPoId());
                AlertUtil.showSuccess("Order Cancelled", "Purchase Order " + selected.getPoNumber() + " has been cancelled.");
                loadPurchaseOrders();
            } catch (Exception e) {
                AlertUtil.showError("Cancellation Failed", e.getMessage());
            }
        }
    }
}
