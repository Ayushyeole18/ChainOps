package com.supplychainx.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {
    private int productId;
    private String sku;
    private String productName;
    private int categoryId;
    private String categoryName;
    private String description;
    private BigDecimal unitPrice;
    private int reorderLevel;
    private String unit;
    private String status;
    private Timestamp createdAt;
    private int totalStock; // Computed aggregated stock across warehouses

    public Product() {
        this.unitPrice = BigDecimal.ZERO;
        this.reorderLevel = 10;
        this.unit = "Units";
        this.status = "ACTIVE";
    }

    public Product(int productId, String sku, String productName, int categoryId, BigDecimal unitPrice, int reorderLevel, String unit, String status) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.categoryId = categoryId;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel;
        this.unit = unit;
        this.status = status;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    public String getStockStatus() {
        if (totalStock == 0) return "OUT OF STOCK";
        if (totalStock <= reorderLevel) return "LOW STOCK";
        return "IN STOCK";
    }

    @Override
    public String toString() {
        return productName + " (" + sku + ")";
    }
}
