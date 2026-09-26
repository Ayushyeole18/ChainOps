package com.supplychainx.model;

import java.sql.Timestamp;

public class Category {
    private int categoryId;
    private String categoryName;
    private String code;
    private String description;
    private String status;
    private Timestamp createdAt;
    private int productCount;

    public Category() {
        this.status = "ACTIVE";
    }

    public Category(int categoryId, String categoryName, String code, String description, String status) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.code = code;
        this.description = description;
        this.status = status;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }

    @Override
    public String toString() {
        return categoryName + " (" + code + ")";
    }
}
