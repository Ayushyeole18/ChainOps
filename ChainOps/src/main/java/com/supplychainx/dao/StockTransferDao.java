package com.supplychainx.dao;

import com.supplychainx.model.StockTransfer;
import java.util.List;
import java.util.Optional;

public interface StockTransferDao {
    Optional<StockTransfer> findById(int transferId);
    List<StockTransfer> findAll();
    List<StockTransfer> searchAndFilter(String query, String status);
    int createAndExecuteTransfer(StockTransfer transfer, int performedByUserId);
    String generateNextTransferNumber();
}
