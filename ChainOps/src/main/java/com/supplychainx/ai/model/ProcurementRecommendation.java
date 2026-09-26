package com.supplychainx.ai.model;

public class ProcurementRecommendation {
    private int productId;
    private String sku;
    private String productName;
    private String categoryName;
    private int supplierId;
    private String supplierName;
    private int currentStock;
    private int safetyStock;
    private int predicted30dDemand;
    private double dailyVelocity;
    private double supplierLeadTimeDays;
    private double supplierDelayRiskPct;
    private int recommendedOrderQty;
    private double unitPrice;
    private double estimatedTotalCost;
    private String urgency; // CRITICAL, HIGH, NORMAL

    public ProcurementRecommendation() {}

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getSafetyStock() { return safetyStock; }
    public void setSafetyStock(int safetyStock) { this.safetyStock = safetyStock; }

    public int getPredicted30dDemand() { return predicted30dDemand; }
    public void setPredicted30dDemand(int predicted30dDemand) { this.predicted30dDemand = predicted30dDemand; }

    public double getDailyVelocity() { return dailyVelocity; }
    public void setDailyVelocity(double dailyVelocity) { this.dailyVelocity = dailyVelocity; }

    public double getSupplierLeadTimeDays() { return supplierLeadTimeDays; }
    public void setSupplierLeadTimeDays(double supplierLeadTimeDays) { this.supplierLeadTimeDays = supplierLeadTimeDays; }

    public double getSupplierDelayRiskPct() { return supplierDelayRiskPct; }
    public void setSupplierDelayRiskPct(double supplierDelayRiskPct) { this.supplierDelayRiskPct = supplierDelayRiskPct; }

    public int getRecommendedOrderQty() { return recommendedOrderQty; }
    public void setRecommendedOrderQty(int recommendedOrderQty) { this.recommendedOrderQty = recommendedOrderQty; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getEstimatedTotalCost() { return estimatedTotalCost; }
    public void setEstimatedTotalCost(double estimatedTotalCost) { this.estimatedTotalCost = estimatedTotalCost; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
}
