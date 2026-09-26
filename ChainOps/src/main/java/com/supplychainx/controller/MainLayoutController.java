package com.supplychainx.controller;

import com.supplychainx.model.Role;
import com.supplychainx.model.User;
import com.supplychainx.service.AuthService;
import com.supplychainx.ui.AlertUtil;
import com.supplychainx.ui.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

public class MainLayoutController {

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentArea;
    @FXML private Label currentViewTitle;
    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleBadge;

    // Sidebar navigation buttons
    @FXML private Button navDashboard;
    @FXML private Button navAiIntelligence;
    @FXML private Button navInventory;
    @FXML private Button navPurchaseOrders;
    @FXML private Button navSalesOrders;
    @FXML private Button navShipments;
    @FXML private Button navStockTransfers;
    @FXML private Button navProducts;
    @FXML private Button navCategories;
    @FXML private Button navSuppliers;
    @FXML private Button navWarehouses;
    @FXML private Button navReports;
    @FXML private Button navUsers;

    private final Map<String, Button> navButtons = new HashMap<>();

    @FXML
    public void initialize() {
        NavigationManager.setMainController(this);

        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            userFullNameLabel.setText(currentUser.getFullName());
            userRoleBadge.setText(currentUser.getRole().getDisplayName());

            // Apply role-based restrictions
            if (currentUser.getRole() != Role.ADMIN) {
                if (navUsers != null) {
                    navUsers.setDisable(true);
                    navUsers.setVisible(false);
                }
            }
        }

        // Register buttons
        navButtons.put("Dashboard", navDashboard);
        navButtons.put("AI / ML Intelligence", navAiIntelligence);
        navButtons.put("Inventory", navInventory);
        navButtons.put("Purchase Orders", navPurchaseOrders);
        navButtons.put("Sales Orders", navSalesOrders);
        navButtons.put("Shipments", navShipments);
        navButtons.put("Stock Transfers", navStockTransfers);
        navButtons.put("Products", navProducts);
        navButtons.put("Categories", navCategories);
        navButtons.put("Suppliers", navSuppliers);
        navButtons.put("Warehouses", navWarehouses);
        navButtons.put("Reports", navReports);
        navButtons.put("Users", navUsers);

        highlightActiveNav("Dashboard");
    }

    public void setContent(Node node, String title) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
        currentViewTitle.setText(title);
        highlightActiveNav(title);
    }

    private void highlightActiveNav(String title) {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().getStyleClass().remove("nav-button-active");
                if (entry.getKey().equalsIgnoreCase(title)) {
                    entry.getValue().getStyleClass().add("nav-button-active");
                }
            }
        }
    }

    // Navigation actions
    @FXML private void showDashboard() { NavigationManager.switchView("/fxml/Dashboard.fxml", "Dashboard"); }
    @FXML private void showAiIntelligence() { NavigationManager.switchView("/fxml/AiIntelligence.fxml", "AI / ML Intelligence"); }
    @FXML private void showInventory() { NavigationManager.switchView("/fxml/Inventory.fxml", "Inventory"); }
    @FXML private void showPurchaseOrders() { NavigationManager.switchView("/fxml/PurchaseOrders.fxml", "Purchase Orders"); }
    @FXML private void showSalesOrders() { NavigationManager.switchView("/fxml/SalesOrders.fxml", "Sales Orders"); }
    @FXML private void showShipments() { NavigationManager.switchView("/fxml/Shipments.fxml", "Shipments"); }
    @FXML private void showStockTransfers() { NavigationManager.switchView("/fxml/StockTransfers.fxml", "Stock Transfers"); }
    @FXML private void showProducts() { NavigationManager.switchView("/fxml/Products.fxml", "Products"); }
    @FXML private void showCategories() { NavigationManager.switchView("/fxml/Categories.fxml", "Categories"); }
    @FXML private void showSuppliers() { NavigationManager.switchView("/fxml/Suppliers.fxml", "Suppliers"); }
    @FXML private void showWarehouses() { NavigationManager.switchView("/fxml/Warehouses.fxml", "Warehouses"); }
    @FXML private void showReports() { NavigationManager.switchView("/fxml/Reports.fxml", "Reports"); }
    @FXML private void showUsers() { NavigationManager.switchView("/fxml/Users.fxml", "Users"); }

    @FXML
    private void handleLogout() {
        if (AlertUtil.confirm("Log Out", "Exit ChainOps Workspace?", "Are you sure you want to end your active session?")) {
            AuthService.logout();
            NavigationManager.showLoginView();
        }
    }
}
