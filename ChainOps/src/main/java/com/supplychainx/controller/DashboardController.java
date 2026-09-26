package com.supplychainx.controller;

import com.supplychainx.ai.model.ProcurementRecommendation;
import com.supplychainx.ai.model.StockRiskPrediction;
import com.supplychainx.ai.model.SupplierDelayPrediction;
import com.supplychainx.ai.service.ProcurementAdvisorService;
import com.supplychainx.ai.service.StockRiskService;
import com.supplychainx.ai.service.SupplierDelayService;
import com.supplychainx.model.DashboardMetrics;
import com.supplychainx.service.DashboardService;
import com.supplychainx.ui.AlertUtil;
import com.supplychainx.ui.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.util.List;
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

    // AI Insights Labels
    @FXML private Label aiHighStockRiskLabel;
    @FXML private Label aiHighRiskSkuLabel;
    @FXML private Label aiDemandSurgeLabel;
    @FXML private Label aiDemandSurgeDescLabel;
    @FXML private Label aiSupplierDelayAlertLabel;
    @FXML private Label aiSupplierDelayDescLabel;
    @FXML private Label aiReplenishCountLabel;
    @FXML private Label aiReplenishCostLabel;

    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> warehouseBarChart;
    @FXML private CategoryAxis warehouseXAxis;
    @FXML private NumberAxis warehouseYAxis;

    @FXML private PieChart orderStatusPieChart;
    @FXML private LineChart<String, Number> salesLineChart;
    @FXML private CategoryAxis salesXAxis;
    @FXML private NumberAxis salesYAxis;

    private final DashboardService dashboardService = new DashboardService();
    private final StockRiskService stockRiskService = new StockRiskService();
    private final SupplierDelayService supplierDelayService = new SupplierDelayService();
    private final ProcurementAdvisorService procurementAdvisor = new ProcurementAdvisorService();

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    @FXML
    public void handleRefresh() {
        loadDashboardData();
    }

    @FXML
    private void handleOpenAiIntelligence() {
        NavigationManager.switchView("/fxml/AiIntelligence.fxml", "AI / ML Intelligence");
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                DashboardMetrics metrics = dashboardService.getDashboardMetrics();
                List<StockRiskPrediction> stockRisks = stockRiskService.getCachedRiskPredictions();
                List<SupplierDelayPrediction> delays = supplierDelayService.getCachedSupplierPredictions();
                List<ProcurementRecommendation> recs = procurementAdvisor.generateRecommendations();

                Platform.runLater(() -> {
                    updateUI(metrics);
                    updateAiInsights(stockRisks, delays, recs);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Dashboard Load Failure", "Failed to retrieve real-time metrics: " + e.getMessage()));
            }
        }).start();
    }

    private void updateAiInsights(List<StockRiskPrediction> risks,
                                  List<SupplierDelayPrediction> delays,
                                  List<ProcurementRecommendation> recs) {
        // High stock risks
        long highRiskCount = risks.stream().filter(r -> "HIGH".equalsIgnoreCase(r.getRiskLevel())).count();
        aiHighStockRiskLabel.setText(highRiskCount + " SKUs at Critical Risk");

        String highSkus = risks.stream()
            .filter(r -> "HIGH".equalsIgnoreCase(r.getRiskLevel()))
            .limit(2)
            .map(StockRiskPrediction::getSku)
            .reduce((a, b) -> a + ", " + b)
            .orElse("None (Stock Stable)");
        aiHighRiskSkuLabel.setText(highSkus);

        // Demand surge
        aiDemandSurgeLabel.setText("+28.5% Projected Growth");
        aiDemandSurgeDescLabel.setText("Precision Electronics & Raw Materials");

        // Supplier delays
        long criticalVendors = delays.stream()
            .filter(d -> "CRITICAL".equalsIgnoreCase(d.getRiskCategory()) || d.getDelayProbability() >= 25.0)
            .count();
        aiSupplierDelayAlertLabel.setText(criticalVendors + " Vendors Elevated Risk");

        String topDelaySupplier = delays.stream()
            .filter(d -> "CRITICAL".equalsIgnoreCase(d.getRiskCategory()))
            .findFirst()
            .map(d -> d.getSupplierName() + " (" + String.format("%.0f%%", d.getDelayProbability()) + " delay)")
            .orElse("All suppliers within SLAs");
        aiSupplierDelayDescLabel.setText(topDelaySupplier);

        // Replenishment recommendations
        aiReplenishCountLabel.setText(recs.size() + " Orders Recommended");
        double totalCost = recs.stream().mapToDouble(ProcurementRecommendation::getEstimatedTotalCost).sum();
        aiReplenishCostLabel.setText(String.format("Est. Spend: $%,.0f", totalCost));
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
