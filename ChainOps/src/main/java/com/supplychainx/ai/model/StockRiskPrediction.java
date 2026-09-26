package com.supplychainx.ai.model;

import java.sql.Timestamp;

public class StockRiskPrediction {
    private int riskId;
    private int productId;
    private String sku;
    private String productName;
    private String categoryName;
    private int currentStock;
    private int reorderLevel;
    private double dailyVelocity;
    private int predicted30dDemand;
    private int daysUntilStockout;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private double riskScore; // 0 to 100
    private int recommendedOrderQty;
    private Timestamp evaluatedAt;

    public StockRiskPrediction() {}

    public StockRiskPrediction(int riskId, int productId, String sku, String productName,
                               String categoryName, int currentStock, int reorderLevel,
                               double dailyVelocity, int predicted30dDemand, int daysUntilStockout,
                               String riskLevel, double riskScore, int recommendedOrderQty,
                               Timestamp evaluatedAt) {
        this.riskId = riskId;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.categoryName = categoryName;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
        this.dailyVelocity = dailyVelocity;
        this.predicted30dDemand = predicted30dDemand;
        this.daysUntilStockout = daysUntilStockout;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.recommendedOrderQty = recommendedOrderQty;
        this.evaluatedAt = evaluatedAt;
    }

    public int getRiskId() { return riskId; }
    public void setRiskId(int riskId) { this.riskId = riskId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public double getDailyVelocity() { return dailyVelocity; }
    public void setDailyVelocity(double dailyVelocity) { this.dailyVelocity = dailyVelocity; }

    public int getPredicted30dDemand() { return predicted30dDemand; }
    public void setPredicted30dDemand(int predicted30dDemand) { this.predicted30dDemand = predicted30dDemand; }

    public int getDaysUntilStockout() { return daysUntilStockout; }
    public void setDaysUntilStockout(int daysUntilStockout) { this.daysUntilStockout = daysUntilStockout; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public int getRecommendedOrderQty() { return recommendedOrderQty; }
    public void setRecommendedOrderQty(int recommendedOrderQty) { this.recommendedOrderQty = recommendedOrderQty; }

    public Timestamp getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Timestamp evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
