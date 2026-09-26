package com.supplychainx.model;

import java.util.HashMap;
import java.util.Map;

public class DashboardMetrics {
    private int totalProducts;
    private int totalInventoryUnits;
    private int lowStockItemsCount;
    private int activeSuppliersCount;
    private int pendingPurchaseOrdersCount;
    private int pendingSalesOrdersCount;
    private int activeShipmentsCount;
    private int totalWarehousesCount;

    // Charts data
    private Map<String, Integer> inventoryByCategory = new HashMap<>();
    private Map<String, Integer> orderStatusDistribution = new HashMap<>();
    private Map<String, Double> monthlySales = new HashMap<>();
    private Map<String, Integer> warehouseInventory = new HashMap<>();

    public DashboardMetrics() {}

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public int getTotalInventoryUnits() { return totalInventoryUnits; }
    public void setTotalInventoryUnits(int totalInventoryUnits) { this.totalInventoryUnits = totalInventoryUnits; }

    public int getLowStockItemsCount() { return lowStockItemsCount; }
    public void setLowStockItemsCount(int lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }

    public int getActiveSuppliersCount() { return activeSuppliersCount; }
    public void setActiveSuppliersCount(int activeSuppliersCount) { this.activeSuppliersCount = activeSuppliersCount; }

    public int getPendingPurchaseOrdersCount() { return pendingPurchaseOrdersCount; }
    public void setPendingPurchaseOrdersCount(int pendingPurchaseOrdersCount) { this.pendingPurchaseOrdersCount = pendingPurchaseOrdersCount; }

    public int getPendingSalesOrdersCount() { return pendingSalesOrdersCount; }
    public void setPendingSalesOrdersCount(int pendingSalesOrdersCount) { this.pendingSalesOrdersCount = pendingSalesOrdersCount; }

    public int getActiveShipmentsCount() { return activeShipmentsCount; }
    public void setActiveShipmentsCount(int activeShipmentsCount) { this.activeShipmentsCount = activeShipmentsCount; }

    public int getTotalWarehousesCount() { return totalWarehousesCount; }
    public void setTotalWarehousesCount(int totalWarehousesCount) { this.totalWarehousesCount = totalWarehousesCount; }

    public Map<String, Integer> getInventoryByCategory() { return inventoryByCategory; }
    public void setInventoryByCategory(Map<String, Integer> inventoryByCategory) { this.inventoryByCategory = inventoryByCategory; }

    public Map<String, Integer> getOrderStatusDistribution() { return orderStatusDistribution; }
    public void setOrderStatusDistribution(Map<String, Integer> orderStatusDistribution) { this.orderStatusDistribution = orderStatusDistribution; }

    public Map<String, Double> getMonthlySales() { return monthlySales; }
    public void setMonthlySales(Map<String, Double> monthlySales) { this.monthlySales = monthlySales; }

    public Map<String, Integer> getWarehouseInventory() { return warehouseInventory; }
    public void setWarehouseInventory(Map<String, Integer> warehouseInventory) { this.warehouseInventory = warehouseInventory; }
}
