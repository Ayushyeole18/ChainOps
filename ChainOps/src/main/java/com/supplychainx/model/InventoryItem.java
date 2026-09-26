package com.supplychainx.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class InventoryItem {
    private int inventoryId;
    private int productId;
    private String productSku;
    private String productName;
    private String categoryName;
    private BigDecimal unitPrice;
    private int warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private int quantityAvailable;
    private int quantityReserved;
    private int reorderLevel;
    private Timestamp lastCountedAt;

    public InventoryItem() {}

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    public int getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }

    public int getTotalPhysicalQuantity() {
        return quantityAvailable + quantityReserved;
    }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public Timestamp getLastCountedAt() { return lastCountedAt; }
    public void setLastCountedAt(Timestamp lastCountedAt) { this.lastCountedAt = lastCountedAt; }

    /**
     * Business rule for stock status calculation:
     * Available == 0 -> OUT OF STOCK
     * Available <= Reorder Level -> LOW STOCK
     * Available > Reorder Level -> IN STOCK
     */
    public String getStockStatus() {
        if (quantityAvailable <= 0) {
            return "OUT OF STOCK";
        } else if (quantityAvailable <= reorderLevel) {
            return "LOW STOCK";
        } else {
            return "IN STOCK";
        }
    }
}
