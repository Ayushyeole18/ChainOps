package com.supplychainx.ai.service;

import com.supplychainx.ai.model.DemandForecast;
import com.supplychainx.ai.model.ProcurementRecommendation;
import com.supplychainx.ai.model.StockRiskPrediction;
import com.supplychainx.ai.model.SupplierDelayPrediction;
import com.supplychainx.dao.ProductDao;
import com.supplychainx.dao.ProductDaoImpl;
import com.supplychainx.dao.SupplierDao;
import com.supplychainx.dao.SupplierDaoImpl;
import com.supplychainx.model.Product;
import com.supplychainx.model.Supplier;

import java.util.ArrayList;
import java.util.List;

public class ProcurementAdvisorService {

    private final ProductDao productDao;
    private final SupplierDao supplierDao;
    private final DemandForecastService forecastService;
    private final StockRiskService stockRiskService;
    private final SupplierDelayService supplierDelayService;

    public ProcurementAdvisorService() {
        this.productDao = new ProductDaoImpl();
        this.supplierDao = new SupplierDaoImpl();
        this.forecastService = new DemandForecastService();
        this.stockRiskService = new StockRiskService();
        this.supplierDelayService = new SupplierDelayService();
    }

    public ProcurementAdvisorService(ProductDao productDao, SupplierDao supplierDao,
                                     DemandForecastService forecastService,
                                     StockRiskService stockRiskService,
                                     SupplierDelayService supplierDelayService) {
        this.productDao = productDao;
        this.supplierDao = supplierDao;
        this.forecastService = forecastService;
        this.stockRiskService = stockRiskService;
        this.supplierDelayService = supplierDelayService;
    }

    /**
     * Generates algorithmic procurement recommendations dynamically synthesizing:
     * Current Stock, Safety Stock, Daily Velocity, 30-day ML Forecast, Supplier Lead-Time, and Delay Risk.
     */
    public List<ProcurementRecommendation> generateRecommendations() {
        List<Product> products = productDao.findAll();
        List<Supplier> suppliers = supplierDao.findAll();
        List<ProcurementRecommendation> recommendations = new ArrayList<>();

        for (Product product : products) {
            StockRiskPrediction risk = stockRiskService.evaluateProductRisk(product.getProductId());
            DemandForecast forecast = forecastService.getForecastForProduct(product.getProductId())
                .orElseGet(() -> forecastService.generateForecast(product.getProductId()));

            // Identify matching supplier based on category or default mapping
            Supplier preferredSupplier = resolveSupplierForProduct(product, suppliers);

            SupplierDelayPrediction delayPrediction = (preferredSupplier != null)
                ? supplierDelayService.evaluateSupplierDelay(preferredSupplier.getSupplierId())
                : null;

            double leadTimeDays = (delayPrediction != null) ? delayPrediction.getAverageLeadTimeDays() : 7.0;
            double delayRiskPct = (delayPrediction != null) ? delayPrediction.getDelayProbability() : 10.0;

            // Rigorous Supply Chain Reorder Formula:
            // Lead-time demand = (daily_velocity * lead_time_days)
            // Buffer for delay risk: safety_stock * (1 + delayRiskPct/100)
            int safetyStock = product.getReorderLevel();
            double leadTimeDemand = risk.getDailyVelocity() * leadTimeDays;
            double adjustedSafetyStock = safetyStock * (1.0 + (delayRiskPct / 100.0));

            // Gross requirement = 30-day forecasted demand + Lead-time demand + adjusted safety stock
            double grossRequirement = forecast.getPredictedQuantity() + leadTimeDemand + adjustedSafetyStock;
            int netReplenishment = (int) Math.max(0, Math.ceil(grossRequirement - risk.getCurrentStock()));

            // Only generate advice if replenishment is genuinely recommended or stock is at risk
            if (netReplenishment > 0 || "HIGH".equalsIgnoreCase(risk.getRiskLevel())) {
                ProcurementRecommendation rec = new ProcurementRecommendation();
                rec.setProductId(product.getProductId());
                rec.setSku(product.getSku());
                rec.setProductName(product.getProductName());
                rec.setCategoryName(product.getCategoryName());

                if (preferredSupplier != null) {
                    rec.setSupplierId(preferredSupplier.getSupplierId());
                    rec.setSupplierName(preferredSupplier.getSupplierName());
                } else {
                    rec.setSupplierId(1);
                    rec.setSupplierName("Apex Metallurgical Corp");
                }

                rec.setCurrentStock(risk.getCurrentStock());
                rec.setSafetyStock(safetyStock);
                rec.setPredicted30dDemand(forecast.getPredictedQuantity());
                rec.setDailyVelocity(risk.getDailyVelocity());
                rec.setSupplierLeadTimeDays(leadTimeDays);
                rec.setSupplierDelayRiskPct(delayRiskPct);
                rec.setRecommendedOrderQty(Math.max(product.getReorderLevel(), netReplenishment));
                rec.setUnitPrice(product.getUnitPrice());
                rec.setEstimatedTotalCost(Math.round(rec.getRecommendedOrderQty() * product.getUnitPrice() * 100.0) / 100.0);

                if (risk.getDaysUntilStockout() <= 7 || risk.getCurrentStock() <= safetyStock * 0.5) {
                    rec.setUrgency("CRITICAL");
                } else if (risk.getDaysUntilStockout() <= 21 || risk.getCurrentStock() <= safetyStock) {
                    rec.setUrgency("HIGH");
                } else {
                    rec.setUrgency("NORMAL");
                }

                recommendations.add(rec);
            }
        }

        // Sort: CRITICAL first, then HIGH, then highest estimated order cost
        recommendations.sort((a, b) -> {
            int scoreA = "CRITICAL".equals(a.getUrgency()) ? 3 : "HIGH".equals(a.getUrgency()) ? 2 : 1;
            int scoreB = "CRITICAL".equals(b.getUrgency()) ? 3 : "HIGH".equals(b.getUrgency()) ? 2 : 1;
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            return Double.compare(b.getEstimatedTotalCost(), a.getEstimatedTotalCost());
        });

        return recommendations;
    }

    private Supplier resolveSupplierForProduct(Product product, List<Supplier> suppliers) {
        if (suppliers.isEmpty()) return null;
        // Map category ID to preferred supplier
        int supplierIndex = (product.getCategoryId() - 1) % suppliers.size();
        return suppliers.get(Math.max(0, supplierIndex));
    }
}
