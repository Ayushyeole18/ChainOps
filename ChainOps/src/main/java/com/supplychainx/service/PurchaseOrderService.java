package com.supplychainx.service;

import com.supplychainx.dao.PurchaseOrderDao;
import com.supplychainx.dao.PurchaseOrderDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.PurchaseOrder;
import com.supplychainx.model.PurchaseOrderItem;
import com.supplychainx.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderService {

    private final PurchaseOrderDao poDao;

    public PurchaseOrderService() {
        this.poDao = new PurchaseOrderDaoImpl();
    }

    public PurchaseOrderService(PurchaseOrderDao poDao) {
        this.poDao = poDao;
    }

    public void validatePurchaseOrder(PurchaseOrder po) {
        if (po.getSupplierId() <= 0) {
            throw new ValidationException("A valid Supplier must be selected.");
        }
        if (po.getWarehouseId() <= 0) {
            throw new ValidationException("A destination Warehouse must be selected.");
        }
        if (po.getOrderDate() == null) {
            throw new ValidationException("Order Date is required.");
        }
        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new ValidationException("At least one product line item is required for a Purchase Order.");
        }
        for (PurchaseOrderItem item : po.getItems()) {
            ValidationUtil.validatePositiveQuantity(item.getQuantityOrdered(), "Ordered Quantity for item " + item.getProductName());
            ValidationUtil.validatePrice(item.getUnitCost(), "Unit Cost for item " + item.getProductName());
        }
    }

    public int createPurchaseOrder(PurchaseOrder po) {
        if (po.getPoNumber() == null || po.getPoNumber().trim().isEmpty()) {
            po.setPoNumber(poDao.generateNextPoNumber());
        }
        po.recalculateTotal();
        validatePurchaseOrder(po);
        return poDao.insert(po);
    }

    public boolean approvePurchaseOrder(int poId, int approverUserId) {
        Optional<PurchaseOrder> poOpt = poDao.findById(poId);
        if (poOpt.isEmpty()) {
            throw new BusinessRuleException("Purchase order not found.");
        }
        PurchaseOrder po = poOpt.get();
        if ("RECEIVED".equalsIgnoreCase(po.getStatus()) || "CANCELLED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessRuleException("Cannot approve a purchase order that is already " + po.getStatus());
        }
        return poDao.updateStatus(poId, "APPROVED", approverUserId);
    }

    public boolean receivePurchaseOrder(int poId, int receivedByUserId) {
        return poDao.receivePurchaseOrder(poId, receivedByUserId);
    }

    public boolean cancelPurchaseOrder(int poId) {
        return poDao.cancelPurchaseOrder(poId);
    }

    public Optional<PurchaseOrder> getPurchaseOrderById(int poId) {
        return poDao.findById(poId);
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return poDao.findAll();
    }

    public List<PurchaseOrder> searchAndFilter(String query, String status, Integer supplierId, LocalDate fromDate, LocalDate toDate) {
        return poDao.searchAndFilter(query, status, supplierId, fromDate, toDate);
    }

    public String generateNextPoNumber() {
        return poDao.generateNextPoNumber();
    }
}
