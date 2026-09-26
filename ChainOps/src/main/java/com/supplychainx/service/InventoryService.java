package com.supplychainx.service;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.dao.InventoryDao;
import com.supplychainx.dao.InventoryDaoImpl;
import com.supplychainx.dao.StockTransactionDao;
import com.supplychainx.dao.StockTransactionDaoImpl;
import com.supplychainx.dao.WarehouseDao;
import com.supplychainx.dao.WarehouseDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.InventoryItem;
import com.supplychainx.model.StockTransaction;
import com.supplychainx.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final InventoryDao inventoryDao;
    private final StockTransactionDao stockTransactionDao;
    private final WarehouseDao warehouseDao;

    public InventoryService() {
        this.inventoryDao = new InventoryDaoImpl();
        this.stockTransactionDao = new StockTransactionDaoImpl();
        this.warehouseDao = new WarehouseDaoImpl();
    }

    public InventoryService(InventoryDao inventoryDao, StockTransactionDao stockTransactionDao, WarehouseDao warehouseDao) {
        this.inventoryDao = inventoryDao;
        this.stockTransactionDao = stockTransactionDao;
        this.warehouseDao = warehouseDao;
    }

    public List<InventoryItem> getAllInventory() {
        return inventoryDao.findAll();
    }

    public List<InventoryItem> searchAndFilter(String query, Integer warehouseId, String stockStatus) {
        return inventoryDao.searchAndFilter(query, warehouseId, stockStatus);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryDao.findLowStockItems();
    }

    public Optional<InventoryItem> getInventory(int productId, int warehouseId) {
        return inventoryDao.findByProductAndWarehouse(productId, warehouseId);
    }

    /**
     * Executes manual Stock In adjustment (adds physical stock).
     */
    public boolean stockIn(int productId, int warehouseId, int quantity, String referenceNumber, String notes, Integer userId) {
        ValidationUtil.validatePositiveQuantity(quantity, "Stock In Quantity");
        return performStockAdjustment(productId, warehouseId, quantity, "ADJUSTMENT_IN", referenceNumber, notes, userId);
    }

    /**
     * Executes manual Stock Out adjustment (reduces physical stock).
     */
    public boolean stockOut(int productId, int warehouseId, int quantity, String referenceNumber, String notes, Integer userId) {
        ValidationUtil.validatePositiveQuantity(quantity, "Stock Out Quantity");
        return performStockAdjustment(productId, warehouseId, -quantity, "ADJUSTMENT_OUT", referenceNumber, notes, userId);
    }

    private boolean performStockAdjustment(int productId, int warehouseId, int delta, String txType, String ref, String notes, Integer userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Adjust inventory stock
            inventoryDao.adjustStock(conn, productId, warehouseId, delta, 0);

            // 2. Record mandatory stock audit transaction
            StockTransaction tx = new StockTransaction(
                    productId,
                    warehouseId,
                    txType,
                    delta,
                    ref != null ? ref : "MANUAL-ADJ",
                    notes,
                    userId
            );
            stockTransactionDao.insert(conn, tx);

            conn.commit();

            // Refresh warehouse utilization
            warehouseDao.updateUtilization(warehouseId);

            return true;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof BusinessRuleException) {
                throw (BusinessRuleException) e;
            }
            throw new DatabaseException("Failed to adjust inventory: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<StockTransaction> getStockHistory() {
        return stockTransactionDao.findAll();
    }

    public List<StockTransaction> searchStockHistory(String query, Integer warehouseId, String transactionType) {
        return stockTransactionDao.searchAndFilter(query, warehouseId, transactionType);
    }
}
