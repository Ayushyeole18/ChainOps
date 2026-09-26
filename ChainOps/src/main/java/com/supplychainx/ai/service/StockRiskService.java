package com.supplychainx.ai.service;

import com.supplychainx.ai.dao.MlDao;
import com.supplychainx.ai.dao.MlDaoImpl;
import com.supplychainx.ai.model.DemandForecast;
import com.supplychainx.ai.model.StockRiskPrediction;
import com.supplychainx.dao.ProductDao;
import com.supplychainx.dao.ProductDaoImpl;
import com.supplychainx.model.Product;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StockRiskService {

    private final MlDao mlDao;
    private final ProductDao productDao;
    private final DemandForecastService forecastService;

    public StockRiskService() {
        this.mlDao = new MlDaoImpl();
        this.productDao = new ProductDaoImpl();
        this.forecastService = new DemandForecastService(mlDao, productDao);
    }

    public StockRiskService(MlDao mlDao, ProductDao productDao, DemandForecastService forecastService) {
        this.mlDao = mlDao;
        this.productDao = productDao;
        this.forecastService = forecastService;
    }

    public StockRiskPrediction evaluateProductRisk(int productId) {
        Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product ID " + productId + " not found."));

        int currentStock = mlDao.getProductTotalStock(productId);
        DemandForecast forecast = forecastService.getForecastForProduct(productId)
            .orElseGet(() -> forecastService.generateForecast(productId));

        double dailyVelocity = Math.max(0.1, forecast.getPredictedQuantity() / 30.0);
        int daysUntilStockout = (int) Math.round(currentStock / dailyVelocity);

        // Determine Risk Category
        String riskLevel;
        double riskScore;

        if (currentStock <= 0 || daysUntilStockout <= 7 || currentStock <= product.getReorderLevel() * 0.5) {
            riskLevel = "HIGH";
            riskScore = Math.min(100.0, 75.0 + Math.max(0, (10 - daysUntilStockout) * 2.5));
        } else if (daysUntilStockout <= 21 || currentStock <= product.getReorderLevel()) {
            riskLevel = "MEDIUM";
            riskScore = 40.0 + Math.max(0, (25 - daysUntilStockout) * 1.5);
        } else {
            riskLevel = "LOW";
            riskScore = Math.max(5.0, 35.0 - Math.min(30.0, (daysUntilStockout - 21) * 0.5));
        }

        // Recommended replenishment
        int safetyStock = product.getReorderLevel();
        int targetHolding = safetyStock + forecast.getPredictedQuantity();
        int recommendedOrderQty = Math.max(0, targetHolding - currentStock);

        StockRiskPrediction prediction = new StockRiskPrediction();
        prediction.setProductId(productId);
        prediction.setSku(product.getSku());
        prediction.setProductName(product.getProductName());
        prediction.setCurrentStock(currentStock);
        prediction.setReorderLevel(product.getReorderLevel());
        prediction.setDailyVelocity(Math.round(dailyVelocity * 100.0) / 100.0);
        prediction.setPredicted30dDemand(forecast.getPredictedQuantity());
        prediction.setDaysUntilStockout(daysUntilStockout);
        prediction.setRiskLevel(riskLevel);
        prediction.setRiskScore(Math.round(riskScore * 10.0) / 10.0);
        prediction.setRecommendedOrderQty(recommendedOrderQty);
        prediction.setEvaluatedAt(new Timestamp(System.currentTimeMillis()));

        mlDao.saveStockRiskPrediction(prediction);
        return prediction;
    }

    public List<StockRiskPrediction> evaluateAllProducts() {
        List<Product> products = productDao.findAll();
        List<StockRiskPrediction> list = new ArrayList<>();
        for (Product p : products) {
            list.add(evaluateProductRisk(p.getProductId()));
        }
        return list;
    }

    public List<StockRiskPrediction> getCachedRiskPredictions() {
        List<StockRiskPrediction> list = mlDao.getAllStockRiskPredictions();
        if (list.isEmpty()) {
            return evaluateAllProducts();
        }
        return list;
    }
}
