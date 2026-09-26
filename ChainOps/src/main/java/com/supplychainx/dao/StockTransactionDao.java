package com.supplychainx.dao;

import com.supplychainx.model.StockTransaction;
import java.sql.Connection;
import java.util.List;

public interface StockTransactionDao {
    int insert(Connection conn, StockTransaction transaction);
    int insert(StockTransaction transaction);
    List<StockTransaction> findAll();
    List<StockTransaction> searchAndFilter(String query, Integer warehouseId, String transactionType);
    List<StockTransaction> findByProduct(int productId);
}
