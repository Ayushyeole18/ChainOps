package com.supplychainx.dao;

import com.supplychainx.model.PurchaseOrder;
import com.supplychainx.model.PurchaseOrderItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderDao {
    Optional<PurchaseOrder> findById(int poId);
    Optional<PurchaseOrder> findByNumber(String poNumber);
    List<PurchaseOrder> findAll();
    List<PurchaseOrder> searchAndFilter(String query, String status, Integer supplierId, LocalDate fromDate, LocalDate toDate);
    int insert(PurchaseOrder po);
    boolean updateStatus(int poId, String newStatus, Integer approvedByUserId);
    boolean receivePurchaseOrder(int poId, int receivedByUserId);
    boolean cancelPurchaseOrder(int poId);
    String generateNextPoNumber();
}
