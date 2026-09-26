package com.supplychainx.controller;

import com.supplychainx.model.Category;
import com.supplychainx.service.CategoryService;
import com.supplychainx.ui.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CategoryController {

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colCode;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDesc;
    @FXML private TableColumn<Category, Integer> colProdCount;
    @FXML private TableColumn<Category, String> colStatus;

    @FXML private TextField searchField;

    // Form
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextArea descArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label formTitleLabel;

    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Category> categoryData = FXCollections.observableArrayList();
    private Category selectedCategory;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colProdCount.setCellValueFactory(new PropertyValueFactory<>("productCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        categoryTable.setItems(categoryData);

        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        statusCombo.setValue("ACTIVE");

        loadCategories();
    }

    private void loadCategories() {
        categoryData.clear();
        categoryData.addAll(categoryService.getAllCategories());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            loadCategories();
        } else {
            categoryData.clear();
            categoryData.addAll(categoryService.searchCategories(query));
        }
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        loadCategories();
    }

    @FXML
    private void handleNewCategory() {
        clearForm();
    }

    @FXML
    private void handleSaveCategory() {
        try {
            boolean isNew = (selectedCategory == null);
            Category c = isNew ? new Category() : selectedCategory;

            c.setCode(codeField.getText().trim());
            c.setCategoryName(nameField.getText().trim());
            c.setDescription(descArea.getText());
            c.setStatus(statusCombo.getValue());

            if (isNew) {
                categoryService.createCategory(c);
                AlertUtil.showSuccess("Category Added", "Category " + c.getCategoryName() + " created successfully.");
            } else {
                categoryService.updateCategory(c);
                AlertUtil.showSuccess("Category Updated", "Category " + c.getCategoryName() + " updated successfully.");
            }

            clearForm();
            loadCategories();
        } catch (Exception e) {
            AlertUtil.showError("Save Failure", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category c = categoryTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            AlertUtil.showWarning("Selection Required", "Please select a category to delete.");
            return;
        }

        if (AlertUtil.confirm("Delete Category", "Delete " + c.getCategoryName() + "?", "This will permanently remove the category if no products are assigned.")) {
            try {
                categoryService.deleteCategory(c.getCategoryId());
                AlertUtil.showSuccess("Category Deleted", "Category removed successfully.");
                clearForm();
                loadCategories();
            } catch (Exception e) {
                AlertUtil.showError("Delete Failed", e.getMessage());
            }
        }
    }

    private void populateForm(Category c) {
        selectedCategory = c;
        formTitleLabel.setText("Edit Category (" + c.getCode() + ")");
        codeField.setText(c.getCode());
        nameField.setText(c.getCategoryName());
        descArea.setText(c.getDescription());
        statusCombo.setValue(c.getStatus());
    }

    private void clearForm() {
        selectedCategory = null;
        formTitleLabel.setText("Add New Category");
        codeField.clear();
        nameField.clear();
        descArea.clear();
        statusCombo.setValue("ACTIVE");
        categoryTable.getSelectionModel().clearSelection();
    }
}
