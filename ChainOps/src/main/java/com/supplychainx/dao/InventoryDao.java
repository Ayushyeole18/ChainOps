package com.supplychainx.dao;

import com.supplychainx.model.InventoryItem;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface InventoryDao {
    Optional<InventoryItem> findByProductAndWarehouse(int productId, int warehouseId);
    Optional<InventoryItem> findByProductAndWarehouse(Connection conn, int productId, int warehouseId);
    List<InventoryItem> findAll();
    List<InventoryItem> searchAndFilter(String query, Integer warehouseId, String stockStatus);
    List<InventoryItem> findLowStockItems();
    boolean adjustStock(Connection conn, int productId, int warehouseId, int deltaAvailable, int deltaReserved);
    boolean upsertInitialStock(int productId, int warehouseId, int quantityAvailable);
}
