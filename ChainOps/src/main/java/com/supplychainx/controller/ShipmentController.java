package com.supplychainx.controller;

import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.Shipment;
import com.supplychainx.service.SalesOrderService;
import com.supplychainx.service.ShipmentService;
import com.supplychainx.ui.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class ShipmentController {

    @FXML private TableView<Shipment> shipmentTable;
    @FXML private TableColumn<Shipment, String> colTracking;
    @FXML private TableColumn<Shipment, String> colSoNumber;
    @FXML private TableColumn<Shipment, String> colCustomer;
    @FXML private TableColumn<Shipment, String> colCarrier;
    @FXML private TableColumn<Shipment, LocalDate> colShipDate;
    @FXML private TableColumn<Shipment, LocalDate> colExpDelivery;
    @FXML private TableColumn<Shipment, LocalDate> colActDelivery;
    @FXML private TableColumn<Shipment, String> colStatus;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;

    // New Shipment Form
    @FXML private ComboBox<SalesOrder> soCombo;
    @FXML private ComboBox<String> carrierCombo;
    @FXML private TextField trackingField;
    @FXML private DatePicker shipDatePicker;
    @FXML private DatePicker expectedDeliveryPicker;
    @FXML private TextArea shippingNotesArea;

    // Quick Status Update
    @FXML private ComboBox<String> updateStatusCombo;
    @FXML private TextField updateNotesField;

    private final ShipmentService shipmentService = new ShipmentService();
    private final SalesOrderService soService = new SalesOrderService();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTracking.setCellValueFactory(new PropertyValueFactory<>("trackingNumber"));
        colSoNumber.setCellValueFactory(new PropertyValueFactory<>("soNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCarrier.setCellValueFactory(new PropertyValueFactory<>("carrier"));
        colShipDate.setCellValueFactory(new PropertyValueFactory<>("shipmentDate"));
        colExpDelivery.setCellValueFactory(new PropertyValueFactory<>("expectedDelivery"));
        colActDelivery.setCellValueFactory(new PropertyValueFactory<>("actualDelivery"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        shipmentTable.setItems(shipmentData);

        carrierCombo.setItems(FXCollections.observableArrayList("FedEx Logistics Direct", "DHL Express Freight", "UPS Supply Chain", "Maersk Intermodal"));
        carrierCombo.setValue("FedEx Logistics Direct");

        carrierCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                trackingField.setText(shipmentService.generateTrackingNumber(newVal));
            }
        });

        trackingField.setText(shipmentService.generateTrackingNumber("FedEx Logistics Direct"));

        shipDatePicker.setValue(LocalDate.now());
        expectedDeliveryPicker.setValue(LocalDate.now().plusDays(4));

        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "READY", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "DELAYED", "CANCELLED"));
        statusFilterCombo.setValue("ALL");

        updateStatusCombo.setItems(FXCollections.observableArrayList("READY", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "DELAYED", "CANCELLED"));
        updateStatusCombo.setValue("IN_TRANSIT");

        loadConfirmedOrders();
        loadShipments();
    }

    private void loadConfirmedOrders() {
        List<SalesOrder> orders = soService.getAllSalesOrders().stream()
                .filter(o -> "CONFIRMED".equalsIgnoreCase(o.getStatus()) || "PROCESSING".equalsIgnoreCase(o.getStatus()))
                .toList();
        soCombo.setItems(FXCollections.observableArrayList(orders));
    }

    private void loadShipments() {
        shipmentData.clear();
        shipmentData.addAll(shipmentService.getAllShipments());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String status = statusFilterCombo.getValue();
        shipmentData.clear();
        shipmentData.addAll(shipmentService.searchAndFilter(query, status));
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        statusFilterCombo.setValue("ALL");
        loadShipments();
    }

    @FXML
    private void handleCreateShipment() {
        SalesOrder so = soCombo.getValue();
        if (so == null) {
            AlertUtil.showWarning("Missing Sales Order", "Please select a confirmed Sales Order.");
            return;
        }

        try {
            Shipment s = new Shipment();
            s.setSoId(so.getSoId());
            s.setCarrier(carrierCombo.getValue());
            s.setTrackingNumber(trackingField.getText().trim());
            s.setShipmentDate(shipDatePicker.getValue());
            s.setExpectedDelivery(expectedDeliveryPicker.getValue());
            s.setShippingNotes(shippingNotesArea.getText());
            s.setStatus("READY");

            shipmentService.createShipment(s);
            AlertUtil.showSuccess("Shipment Dispatched", "Created shipment with tracking number: " + s.getTrackingNumber());

            shippingNotesArea.clear();
            trackingField.setText(shipmentService.generateTrackingNumber(carrierCombo.getValue()));

            loadConfirmedOrders();
            loadShipments();
        } catch (Exception e) {
            AlertUtil.showError("Shipment Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        Shipment selected = shipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a shipment from the table to update.");
            return;
        }

        String newStatus = updateStatusCombo.getValue();
        String notes = updateNotesField.getText();

        try {
            shipmentService.updateShipmentStatus(selected.getShipmentId(), newStatus, notes);
            AlertUtil.showSuccess("Status Updated", "Shipment " + selected.getTrackingNumber() + " transitioned to " + newStatus);
            updateNotesField.clear();
            loadShipments();
        } catch (Exception e) {
            AlertUtil.showError("Update Failed", e.getMessage());
        }
    }
}
