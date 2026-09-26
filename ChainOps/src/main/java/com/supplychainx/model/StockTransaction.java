package com.supplychainx.model;

import java.sql.Timestamp;

public class StockTransaction {
    private int transactionId;
    private int productId;
    private String productSku;
    private String productName;
    private int warehouseId;
    private String warehouseName;
    private String transactionType; // INBOUND_PO, OUTBOUND_SO, TRANSFER_OUT, TRANSFER_IN, ADJUSTMENT_IN, ADJUSTMENT_OUT
    private int quantity;
    private String referenceNumber;
    private String notes;
    private Integer performedByUserId;
    private String performedByName;
    private Timestamp createdAt;

    public StockTransaction() {}

    public StockTransaction(int productId, int warehouseId, String transactionType, int quantity, String referenceNumber, String notes, Integer performedByUserId) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.performedByUserId = performedByUserId;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getPerformedByUserId() { return performedByUserId; }
    public void setPerformedByUserId(Integer performedByUserId) { this.performedByUserId = performedByUserId; }

    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
