package com.supplychainx.controller;

import com.supplychainx.model.Supplier;
import com.supplychainx.service.SupplierService;
import com.supplychainx.ui.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class SupplierController {

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colCity;
    @FXML private TableColumn<Supplier, String> colCountry;
    @FXML private TableColumn<Supplier, String> colStatus;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatusCombo;

    // Form
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField countryField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label formTitleLabel;

    private final SupplierService supplierService = new SupplierService();
    private final ObservableList<Supplier> supplierData = FXCollections.observableArrayList();
    private Supplier selectedSupplier;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        supplierTable.setItems(supplierData);

        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        filterStatusCombo.setItems(FXCollections.observableArrayList("ALL", "ACTIVE", "INACTIVE", "BLACKLISTED"));
        filterStatusCombo.setValue("ALL");

        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "BLACKLISTED"));
        statusCombo.setValue("ACTIVE");

        loadSuppliers();
    }

    private void loadSuppliers() {
        supplierData.clear();
        supplierData.addAll(supplierService.getAllSuppliers());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String status = filterStatusCombo.getValue();
        supplierData.clear();
        supplierData.addAll(supplierService.searchAndFilter(query, status));
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        filterStatusCombo.setValue("ALL");
        loadSuppliers();
    }

    @FXML
    private void handleNewSupplier() {
        clearForm();
    }

    @FXML
    private void handleSaveSupplier() {
        try {
            boolean isNew = (selectedSupplier == null);
            Supplier s = isNew ? new Supplier() : selectedSupplier;

            s.setSupplierName(nameField.getText().trim());
            s.setContactPerson(contactField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setPhone(phoneField.getText().trim());
            s.setAddress(addressField.getText().trim());
            s.setCity(cityField.getText().trim());
            s.setCountry(countryField.getText().trim());
            s.setStatus(statusCombo.getValue());

            if (isNew) {
                supplierService.createSupplier(s);
                AlertUtil.showSuccess("Supplier Registered", "Supplier " + s.getSupplierName() + " registered successfully.");
            } else {
                supplierService.updateSupplier(s);
                AlertUtil.showSuccess("Supplier Updated", "Supplier details updated successfully.");
            }

            clearForm();
            loadSuppliers();
        } catch (Exception e) {
            AlertUtil.showError("Save Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSupplier() {
        Supplier s = supplierTable.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Selection Required", "Please select a supplier to delete.");
            return;
        }

        if (AlertUtil.confirm("Delete Supplier", "Delete " + s.getSupplierName() + "?", "This will remove the supplier record.")) {
            try {
                supplierService.deleteSupplier(s.getSupplierId());
                AlertUtil.showSuccess("Supplier Deleted", "Supplier deleted successfully.");
                clearForm();
                loadSuppliers();
            } catch (Exception e) {
                AlertUtil.showError("Delete Failed", e.getMessage());
            }
        }
    }

    private void populateForm(Supplier s) {
        selectedSupplier = s;
        formTitleLabel.setText("Edit Supplier: " + s.getSupplierName());
        nameField.setText(s.getSupplierName());
        contactField.setText(s.getContactPerson());
        emailField.setText(s.getEmail());
        phoneField.setText(s.getPhone());
        addressField.setText(s.getAddress());
        cityField.setText(s.getCity());
        countryField.setText(s.getCountry());
        statusCombo.setValue(s.getStatus());
    }

    private void clearForm() {
        selectedSupplier = null;
        formTitleLabel.setText("Register New Supplier");
        nameField.clear();
        contactField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        cityField.clear();
        countryField.clear();
        statusCombo.setValue("ACTIVE");
        supplierTable.getSelectionModel().clearSelection();
    }
}
