package com.supplychainx.dao;

import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.SalesOrderItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesOrderDao {
    Optional<SalesOrder> findById(int soId);
    Optional<SalesOrder> findByNumber(String soNumber);
    List<SalesOrder> findAll();
    List<SalesOrder> searchAndFilter(String query, String status, Integer warehouseId, LocalDate fromDate, LocalDate toDate);
    int insert(SalesOrder so);
    boolean confirmOrder(int soId, int confirmedByUserId);
    boolean updateStatus(int soId, String newStatus);
    boolean cancelOrder(int soId);
    String generateNextSoNumber();
}
