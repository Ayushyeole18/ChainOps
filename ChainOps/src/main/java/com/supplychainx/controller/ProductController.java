package com.supplychainx.controller;

import com.supplychainx.model.Category;
import com.supplychainx.model.Product;
import com.supplychainx.service.CategoryService;
import com.supplychainx.service.ProductService;
import com.supplychainx.ui.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

public class ProductController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colSku;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, Integer> colReorder;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colStatus;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;

    // Form controls
    @FXML private TextField skuField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextField reorderField;
    @FXML private TextField unitField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea descriptionArea;
    @FXML private Label formTitleLabel;
    @FXML private Button saveButton;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private Product selectedProduct;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colReorder.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("totalStock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        productTable.setItems(productData);

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadCategories();
        filterStatusCombo.setItems(FXCollections.observableArrayList("ALL", "ACTIVE", "DISCONTINUED", "OUT_OF_STOCK"));
        filterStatusCombo.setValue("ALL");

        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "DISCONTINUED", "OUT_OF_STOCK"));
        statusCombo.setValue("ACTIVE");

        unitField.setText("Units");

        loadProducts();
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));

        ObservableList<String> filterCats = FXCollections.observableArrayList("ALL");
        for (Category c : categories) {
            filterCats.add(c.getCategoryName());
        }
        filterCategoryCombo.setItems(filterCats);
        filterCategoryCombo.setValue("ALL");
    }

    private void loadProducts() {
        productData.clear();
        productData.addAll(productService.getAllProducts());
    }

    @FXML
    private void handleSearchAndFilter() {
        String query = searchField.getText();
        String selectedCatName = filterCategoryCombo.getValue();
        Integer catId = null;

        if (selectedCatName != null && !selectedCatName.equalsIgnoreCase("ALL")) {
            for (Category c : categoryCombo.getItems()) {
                if (c.getCategoryName().equalsIgnoreCase(selectedCatName)) {
                    catId = c.getCategoryId();
                    break;
                }
            }
        }

        String status = filterStatusCombo.getValue();
        productData.clear();
        productData.addAll(productService.searchAndFilter(query, catId, status));
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        filterCategoryCombo.setValue("ALL");
        filterStatusCombo.setValue("ALL");
        loadProducts();
    }

    @FXML
    private void handleNewProduct() {
        clearForm();
    }

    @FXML
    private void handleSaveProduct() {
        try {
            boolean isNew = (selectedProduct == null);
            Product p = isNew ? new Product() : selectedProduct;

            p.setSku(skuField.getText());
            p.setProductName(nameField.getText());

            Category selCat = categoryCombo.getValue();
            if (selCat == null) {
                AlertUtil.showWarning("Missing Input", "Please select a Category.");
                return;
            }
            p.setCategoryId(selCat.getCategoryId());

            try {
                p.setUnitPrice(new BigDecimal(priceField.getText().trim()));
            } catch (Exception e) {
                AlertUtil.showWarning("Invalid Input", "Unit Price must be a valid decimal number.");
                return;
            }

            try {
                p.setReorderLevel(Integer.parseInt(reorderField.getText().trim()));
            } catch (Exception e) {
                AlertUtil.showWarning("Invalid Input", "Reorder Level must be a valid non-negative integer.");
                return;
            }

            p.setUnit(unitField.getText().trim());
            p.setStatus(statusCombo.getValue());
            p.setDescription(descriptionArea.getText());

            if (isNew) {
                productService.createProduct(p);
                AlertUtil.showSuccess("Product Created", "Product " + p.getProductName() + " was successfully created.");
            } else {
                productService.updateProduct(p);
                AlertUtil.showSuccess("Product Updated", "Product " + p.getProductName() + " was successfully updated.");
            }

            clearForm();
            loadProducts();
        } catch (Exception e) {
            AlertUtil.showError("Save Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showWarning("Selection Required", "Please select a product from the table to delete.");
            return;
        }

        if (AlertUtil.confirm("Delete Product", "Delete " + p.getProductName() + "?", "This action will permanently remove the product record.")) {
            try {
                productService.deleteProduct(p.getProductId());
                AlertUtil.showSuccess("Deleted", "Product was successfully deleted.");
                clearForm();
                loadProducts();
            } catch (Exception e) {
                AlertUtil.showError("Delete Failed", e.getMessage());
            }
        }
    }

    private void populateForm(Product p) {
        selectedProduct = p;
        formTitleLabel.setText("Edit Product (" + p.getSku() + ")");
        skuField.setText(p.getSku());
        nameField.setText(p.getProductName());

        for (Category c : categoryCombo.getItems()) {
            if (c.getCategoryId() == p.getCategoryId()) {
                categoryCombo.setValue(c);
                break;
            }
        }

        priceField.setText(p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "0.00");
        reorderField.setText(String.valueOf(p.getReorderLevel()));
        unitField.setText(p.getUnit());
        statusCombo.setValue(p.getStatus());
        descriptionArea.setText(p.getDescription());
    }

    private void clearForm() {
        selectedProduct = null;
        formTitleLabel.setText("Add New Product");
        skuField.clear();
        nameField.clear();
        categoryCombo.setValue(null);
        priceField.setText("0.00");
        reorderField.setText("10");
        unitField.setText("Units");
        statusCombo.setValue("ACTIVE");
        descriptionArea.clear();
        productTable.getSelectionModel().clearSelection();
    }
}
