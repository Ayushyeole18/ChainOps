package com.supplychainx.controller;

import com.supplychainx.model.DashboardMetrics;
import com.supplychainx.service.DashboardService;
import com.supplychainx.ui.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.util.Map;

public class DashboardController {

    @FXML private Label totalProductsLabel;
    @FXML private Label totalInventoryLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label activeSuppliersLabel;
    @FXML private Label pendingPoLabel;
    @FXML private Label pendingSoLabel;
    @FXML private Label activeShipmentsLabel;
    @FXML private Label totalWarehousesLabel;

    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> warehouseBarChart;
    @FXML private CategoryAxis warehouseXAxis;
    @FXML private NumberAxis warehouseYAxis;

    @FXML private PieChart orderStatusPieChart;
    @FXML private LineChart<String, Number> salesLineChart;
    @FXML private CategoryAxis salesXAxis;
    @FXML private NumberAxis salesYAxis;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    @FXML
    public void handleRefresh() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                DashboardMetrics metrics = dashboardService.getDashboardMetrics();
                Platform.runLater(() -> updateUI(metrics));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Dashboard Load Failure", "Failed to retrieve real-time metrics: " + e.getMessage()));
            }
        }).start();
    }

    private void updateUI(DashboardMetrics m) {
        totalProductsLabel.setText(String.format("%,d", m.getTotalProducts()));
        totalInventoryLabel.setText(String.format("%,d", m.getTotalInventoryUnits()));
        lowStockLabel.setText(String.format("%,d", m.getLowStockItemsCount()));
        activeSuppliersLabel.setText(String.format("%,d", m.getActiveSuppliersCount()));
        pendingPoLabel.setText(String.format("%,d", m.getPendingPurchaseOrdersCount()));
        pendingSoLabel.setText(String.format("%,d", m.getPendingSalesOrdersCount()));
        activeShipmentsLabel.setText(String.format("%,d", m.getActiveShipmentsCount()));
        totalWarehousesLabel.setText(String.format("%,d", m.getTotalWarehousesCount()));

        // Chart 1: Inventory by category
        ObservableList<PieChart.Data> catData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : m.getInventoryByCategory().entrySet()) {
            catData.add(new PieChart.Data(e.getKey(), e.getValue()));
        }
        categoryPieChart.setData(catData);

        // Chart 2: Warehouse Inventory
        warehouseBarChart.getData().clear();
        XYChart.Series<String, Number> whSeries = new XYChart.Series<>();
        whSeries.setName("Available Units");
        for (Map.Entry<String, Integer> e : m.getWarehouseInventory().entrySet()) {
            whSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        warehouseBarChart.getData().add(whSeries);

        // Chart 3: Order Status Distribution
        ObservableList<PieChart.Data> orderData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : m.getOrderStatusDistribution().entrySet()) {
            orderData.add(new PieChart.Data(e.getKey(), e.getValue()));
        }
        orderStatusPieChart.setData(orderData);

        // Chart 4: Monthly Sales
        salesLineChart.getData().clear();
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Revenue ($)");
        for (Map.Entry<String, Double> e : m.getMonthlySales().entrySet()) {
            salesSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        salesLineChart.getData().add(salesSeries);
    }
}
