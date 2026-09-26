package com.supplychainx.model;

import java.math.BigDecimal;

public class PurchaseOrderItem {
    private int itemId;
    private int poId;
    private int productId;
    private String productSku;
    private String productName;
    private int quantityOrdered;
    private int quantityReceived;
    private BigDecimal unitCost;
    private BigDecimal subtotal;

    public PurchaseOrderItem() {
        this.unitCost = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public PurchaseOrderItem(int productId, String productSku, String productName, int quantityOrdered, BigDecimal unitCost) {
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = 0;
        this.unitCost = unitCost;
        this.subtotal = unitCost.multiply(BigDecimal.valueOf(quantityOrdered));
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getPoId() { return poId; }
    public void setPoId(int poId) { this.poId = poId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
        if (this.unitCost != null) {
            this.subtotal = this.unitCost.multiply(BigDecimal.valueOf(quantityOrdered));
        }
    }

    public int getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
        if (unitCost != null) {
            this.subtotal = unitCost.multiply(BigDecimal.valueOf(this.quantityOrdered));
        }
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
