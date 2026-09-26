package com.supplychainx.service;

import com.supplychainx.dao.SalesOrderDao;
import com.supplychainx.dao.SalesOrderDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.SalesOrderItem;
import com.supplychainx.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SalesOrderService {

    private final SalesOrderDao soDao;

    public SalesOrderService() {
        this.soDao = new SalesOrderDaoImpl();
    }

    public SalesOrderService(SalesOrderDao soDao) {
        this.soDao = soDao;
    }

    public void validateSalesOrder(SalesOrder so) {
        ValidationUtil.requireNonEmpty(so.getCustomerName(), "Customer Name");
        ValidationUtil.validateEmail(so.getCustomerEmail());
        ValidationUtil.requireNonEmpty(so.getShippingAddress(), "Shipping Address");
        if (so.getWarehouseId() <= 0) {
            throw new ValidationException("A fulfillment Warehouse must be selected.");
        }
        if (so.getItems() == null || so.getItems().isEmpty()) {
            throw new ValidationException("At least one line item is required for a Sales Order.");
        }
        for (SalesOrderItem item : so.getItems()) {
            ValidationUtil.validatePositiveQuantity(item.getQuantity(), "Order item quantity for " + item.getProductName());
            ValidationUtil.validatePrice(item.getUnitPrice(), "Unit price for " + item.getProductName());
        }
    }

    public int createSalesOrder(SalesOrder so) {
        if (so.getSoNumber() == null || so.getSoNumber().trim().isEmpty()) {
            so.setSoNumber(soDao.generateNextSoNumber());
        }
        so.recalculateTotal();
        validateSalesOrder(so);
        return soDao.insert(so);
    }

    public boolean confirmSalesOrder(int soId, int confirmedByUserId) {
        return soDao.confirmOrder(soId, confirmedByUserId);
    }

    public boolean updateStatus(int soId, String newStatus) {
        return soDao.updateStatus(soId, newStatus);
    }

    public boolean cancelSalesOrder(int soId) {
        return soDao.cancelOrder(soId);
    }

    public Optional<SalesOrder> getSalesOrderById(int soId) {
        return soDao.findById(soId);
    }

    public List<SalesOrder> getAllSalesOrders() {
        return soDao.findAll();
    }

    public List<SalesOrder> searchAndFilter(String query, String status, Integer warehouseId, LocalDate fromDate, LocalDate toDate) {
        return soDao.searchAndFilter(query, status, warehouseId, fromDate, toDate);
    }

    public String generateNextSoNumber() {
        return soDao.generateNextSoNumber();
    }
}
