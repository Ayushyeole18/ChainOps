package com.supplychainx.ai.model;

import java.sql.Timestamp;

public class DemandForecast {
    private int forecastId;
    private int productId;
    private String sku;
    private String productName;
    private String categoryName;
    private int forecastPeriodDays;
    private double historicalDailyAvg;
    private int predictedQuantity;
    private int confidenceLower;
    private int confidenceUpper;
    private String trendDirection; // UP, DOWN, STABLE
    private double trendSlope;
    private double mae;
    private double rmse;
    private Timestamp calculatedAt;

    public DemandForecast() {}

    public DemandForecast(int forecastId, int productId, String sku, String productName,
                          String categoryName, int forecastPeriodDays, double historicalDailyAvg,
                          int predictedQuantity, int confidenceLower, int confidenceUpper,
                          String trendDirection, double trendSlope, Timestamp calculatedAt) {
        this.forecastId = forecastId;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.categoryName = categoryName;
        this.forecastPeriodDays = forecastPeriodDays;
        this.historicalDailyAvg = historicalDailyAvg;
        this.predictedQuantity = predictedQuantity;
        this.confidenceLower = confidenceLower;
        this.confidenceUpper = confidenceUpper;
        this.trendDirection = trendDirection;
        this.trendSlope = trendSlope;
        this.calculatedAt = calculatedAt;
    }

    public int getForecastId() { return forecastId; }
    public void setForecastId(int forecastId) { this.forecastId = forecastId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getForecastPeriodDays() { return forecastPeriodDays; }
    public void setForecastPeriodDays(int forecastPeriodDays) { this.forecastPeriodDays = forecastPeriodDays; }

    public double getHistoricalDailyAvg() { return historicalDailyAvg; }
    public void setHistoricalDailyAvg(double historicalDailyAvg) { this.historicalDailyAvg = historicalDailyAvg; }

    public int getPredictedQuantity() { return predictedQuantity; }
    public void setPredictedQuantity(int predictedQuantity) { this.predictedQuantity = predictedQuantity; }

    public int getConfidenceLower() { return confidenceLower; }
    public void setConfidenceLower(int confidenceLower) { this.confidenceLower = confidenceLower; }

    public int getConfidenceUpper() { return confidenceUpper; }
    public void setConfidenceUpper(int confidenceUpper) { this.confidenceUpper = confidenceUpper; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

    public double getTrendSlope() { return trendSlope; }
    public void setTrendSlope(double trendSlope) { this.trendSlope = trendSlope; }

    public double getMae() { return mae; }
    public void setMae(double mae) { this.mae = mae; }

    public double getRmse() { return rmse; }
    public void setRmse(double rmse) { this.rmse = rmse; }

    public Timestamp getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Timestamp calculatedAt) { this.calculatedAt = calculatedAt; }
}
