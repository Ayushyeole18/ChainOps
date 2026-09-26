package com.supplychainx.controller;

import com.supplychainx.model.Product;
import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.SalesOrderItem;
import com.supplychainx.model.Warehouse;
import com.supplychainx.service.AuthService;
import com.supplychainx.service.ProductService;
import com.supplychainx.service.SalesOrderService;
import com.supplychainx.service.WarehouseService;
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

public class SalesOrderController {

    @FXML private TableView<SalesOrder> soTable;
    @FXML private TableColumn<SalesOrder, String> colSoNumber;
    @FXML private TableColumn<SalesOrder, String> colCustomer;
    @FXML private TableColumn<SalesOrder, String> colWarehouse;
    @FXML private TableColumn<SalesOrder, LocalDate> colOrderDate;
    @FXML private TableColumn<SalesOrder, BigDecimal> colTotal;
    @FXML private TableColumn<SalesOrder, String> colStatus;
    @FXML private TableColumn<SalesOrder, String> colCreatedBy;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;

    // New Order Form
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private TextField customerPhoneField;
    @FXML private TextArea shippingAddressArea;
    @FXML private ComboBox<Warehouse> warehouseCombo;
    @FXML private DatePicker orderDatePicker;
    @FXML private TextArea notesArea;

    // Item controls
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private TableView<SalesOrderItem> itemsTable;
    @FXML private TableColumn<SalesOrderItem, String> colItemSku;
    @FXML private TableColumn<SalesOrderItem, String> colItemName;
    @FXML private TableColumn<SalesOrderItem, Integer> colItemQty;
    @FXML private TableColumn<SalesOrderItem, BigDecimal> colItemPrice;
    @FXML private TableColumn<SalesOrderItem, BigDecimal> colItemSubtotal;
    @FXML private Label totalAmountLabel;

    private final SalesOrderService soService = new SalesOrderService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final ProductService productService = new ProductService();

    private final ObservableList<SalesOrder> soData = FXCollections.observableArrayList();
    private final ObservableList<SalesOrderItem> draftItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSoNumber.setCellValueFactory(new PropertyValueFactory<>("soNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdByName"));

        soTable.setItems(soData);

        // Items table
        colItemSku.setCellValueFactory(new PropertyValueFactory<>("productSku"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colItemSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        itemsTable.setItems(draftItems);

        warehouseCombo.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));
        productCombo.setItems(FXCollections.observableArrayList(productService.getAllProducts()));

        productCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getUnitPrice() != null) {
                unitPriceField.setText(newVal.getUnitPrice().toPlainString());
            }
        });

        orderDatePicker.setValue(LocalDate.now());

        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"));
        statusFilterCombo.setValue("ALL");

        loadSalesOrders();
    }

    private void loadSalesOrders() {
        soData.clear();
        soData.addAll(soService.getAllSalesOrders());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String status = statusFilterCombo.getValue();
        soData.clear();
        soData.addAll(soService.searchAndFilter(query, status, null, null, null));
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        statusFilterCombo.setValue("ALL");
        loadSalesOrders();
    }

    @FXML
    private void handleAddItem() {
        Product p = productCombo.getValue();
        if (p == null) {
            AlertUtil.showWarning("Missing Product", "Please choose a product.");
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
            AlertUtil.showWarning("Invalid Quantity", "Please enter a valid positive integer.");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(unitPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                AlertUtil.showWarning("Invalid Price", "Unit Price cannot be negative.");
                return;
            }
        } catch (Exception e) {
            AlertUtil.showWarning("Invalid Price", "Please enter a valid decimal price.");
            return;
        }

        for (SalesOrderItem item : draftItems) {
            if (item.getProductId() == p.getProductId()) {
                item.setQuantity(item.getQuantity() + qty);
                itemsTable.refresh();
                recalculateTotal();
                return;
            }
        }

        SalesOrderItem newItem = new SalesOrderItem(p.getProductId(), p.getSku(), p.getProductName(), qty, price);
        draftItems.add(newItem);
        recalculateTotal();

        quantityField.clear();
    }

    @FXML
    private void handleRemoveItem() {
        SalesOrderItem item = itemsTable.getSelectionModel().getSelectedItem();
        if (item != null) {
            draftItems.remove(item);
            recalculateTotal();
        }
    }

    private void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SalesOrderItem item : draftItems) {
            sum = sum.add(item.getSubtotal());
        }
        totalAmountLabel.setText(String.format("$%,.2f", sum.doubleValue()));
    }

    @FXML
    private void handleCreateOrder() {
        Warehouse w = warehouseCombo.getValue();
        if (w == null) {
            AlertUtil.showWarning("Missing Warehouse", "Please choose a fulfillment warehouse.");
            return;
        }
        if (draftItems.isEmpty()) {
            AlertUtil.showWarning("Missing Items", "Please add at least one product item to the sales order.");
            return;
        }

        try {
            SalesOrder so = new SalesOrder();
            so.setCustomerName(customerNameField.getText().trim());
            so.setCustomerEmail(customerEmailField.getText().trim());
            so.setCustomerPhone(customerPhoneField.getText().trim());
            so.setShippingAddress(shippingAddressArea.getText().trim());
            so.setWarehouseId(w.getWarehouseId());
            so.setOrderDate(orderDatePicker.getValue());
            so.setNotes(notesArea.getText());
            so.setStatus("PENDING");
            if (AuthService.getCurrentUser() != null) {
                so.setCreatedByUserId(AuthService.getCurrentUser().getUserId());
            }

            so.setItems(new ArrayList<>(draftItems));

            soService.createSalesOrder(so);
            AlertUtil.showSuccess("Sales Order Placed", "Sales order " + so.getSoNumber() + " submitted in PENDING state.");

            // Clear inputs
            customerNameField.clear();
            customerEmailField.clear();
            customerPhoneField.clear();
            shippingAddressArea.clear();
            notesArea.clear();
            draftItems.clear();
            recalculateTotal();

            loadSalesOrders();
        } catch (Exception e) {
            AlertUtil.showError("Order Submission Failed", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmSelected() {
        SalesOrder selected = soTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a sales order from the table to confirm.");
            return;
        }

        if (AlertUtil.confirm("Confirm Sales Order", "Confirm " + selected.getSoNumber() + "?",
                "This will verify warehouse inventory, reserve stock, record an outbound audit transaction, and change status to CONFIRMED.")) {
            try {
                int userId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getUserId() : 1;
                soService.confirmSalesOrder(selected.getSoId(), userId);
                AlertUtil.showSuccess("Order Confirmed", "Sales Order " + selected.getSoNumber() +
                        " confirmed successfully! Stock reserved in fulfillment warehouse.");
                loadSalesOrders();
            } catch (Exception e) {
                AlertUtil.showError("Confirmation Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelSelected() {
        SalesOrder selected = soTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a sales order to cancel.");
            return;
        }

        if (AlertUtil.confirm("Cancel Sales Order", "Cancel " + selected.getSoNumber() + "?",
                "Are you sure? Any reserved inventory will be restored.")) {
            try {
                soService.cancelSalesOrder(selected.getSoId());
                AlertUtil.showSuccess("Order Cancelled", "Sales order " + selected.getSoNumber() + " has been cancelled.");
                loadSalesOrders();
            } catch (Exception e) {
                AlertUtil.showError("Cancellation Failed", e.getMessage());
            }
        }
    }
}
