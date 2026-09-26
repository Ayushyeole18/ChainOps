package com.supplychainx;

import com.supplychainx.dao.PurchaseOrderDao;
import com.supplychainx.dao.SalesOrderDao;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.PurchaseOrder;
import com.supplychainx.model.PurchaseOrderItem;
import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.SalesOrderItem;
import com.supplychainx.service.PurchaseOrderService;
import com.supplychainx.service.SalesOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBusinessRuleTest {

    private PurchaseOrderService poService;
    private SalesOrderService soService;

    @BeforeEach
    public void setup() {
        PurchaseOrderDao mockPoDao = new PurchaseOrderDao() {
            @Override public Optional<PurchaseOrder> findById(int poId) {
                if (poId == 99) {
                    PurchaseOrder po = new PurchaseOrder();
                    po.setPoId(99);
                    po.setStatus("RECEIVED");
                    return Optional.of(po);
                }
                return Optional.empty();
            }
            @Override public Optional<PurchaseOrder> findByNumber(String poNumber) { return Optional.empty(); }
            @Override public List<PurchaseOrder> findAll() { return List.of(); }
            @Override public List<PurchaseOrder> searchAndFilter(String query, String status, Integer supplierId, LocalDate fromDate, LocalDate toDate) { return List.of(); }
            @Override public int insert(PurchaseOrder po) { return 1; }
            @Override public boolean updateStatus(int poId, String newStatus, Integer approvedByUserId) { return true; }
            @Override public boolean receivePurchaseOrder(int poId, int receivedByUserId) {
                if (poId == 99) {
                    throw new BusinessRuleException("This purchase order has already been received! Duplicate receiving prevented.");
                }
                return true;
            }
            @Override public boolean cancelPurchaseOrder(int poId) { return true; }
            @Override public String generateNextPoNumber() { return "PO-2026-999"; }
        };

        SalesOrderDao mockSoDao = new SalesOrderDao() {
            @Override public Optional<SalesOrder> findById(int soId) { return Optional.empty(); }
            @Override public Optional<SalesOrder> findByNumber(String soNumber) { return Optional.empty(); }
            @Override public List<SalesOrder> findAll() { return List.of(); }
            @Override public List<SalesOrder> searchAndFilter(String query, String status, Integer warehouseId, LocalDate fromDate, LocalDate toDate) { return List.of(); }
            @Override public int insert(SalesOrder so) { return 1; }
            @Override public boolean confirmOrder(int soId, int confirmedByUserId) { return true; }
            @Override public boolean updateStatus(int soId, String newStatus) { return true; }
            @Override public boolean cancelOrder(int soId) { return true; }
            @Override public String generateNextSoNumber() { return "SO-2026-999"; }
        };

        poService = new PurchaseOrderService(mockPoDao);
        soService = new SalesOrderService(mockSoDao);
    }

    @Test
    @DisplayName("Purchase order total amount automatically recalculates from line items")
    public void testPoTotalCalculation() {
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplierId(1);
        po.setWarehouseId(1);

        PurchaseOrderItem item1 = new PurchaseOrderItem(1, "SKU-01", "Item 1", 10, new BigDecimal("25.00"));
        PurchaseOrderItem item2 = new PurchaseOrderItem(2, "SKU-02", "Item 2", 5, new BigDecimal("50.00"));

        po.getItems().add(item1);
        po.getItems().add(item2);
        po.recalculateTotal();

        assertEquals(new BigDecimal("500.00"), po.getTotalAmount());
    }

    @Test
    @DisplayName("Duplicate purchase order receiving throws BusinessRuleException")
    public void testPreventDuplicatePoReceiving() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> poService.receivePurchaseOrder(99, 1));
        assertTrue(ex.getMessage().contains("already been received"));
    }

    @Test
    @DisplayName("Sales order validation fails on invalid email or empty items")
    public void testSalesOrderValidation() {
        SalesOrder so = new SalesOrder();
        so.setCustomerName("Acme Corp");
        so.setCustomerEmail("invalid-email-format");
        so.setShippingAddress("123 Test St");
        so.setWarehouseId(1);

        assertThrows(ValidationException.class, () -> soService.validateSalesOrder(so));

        // Fix email but have no items
        so.setCustomerEmail("buyer@acme.com");
        assertThrows(ValidationException.class, () -> soService.validateSalesOrder(so));

        // Add valid item
        SalesOrderItem item = new SalesOrderItem(1, "SKU-01", "Item 1", 4, new BigDecimal("20.00"));
        so.getItems().add(item);
        so.recalculateTotal();

        assertDoesNotThrow(() -> soService.validateSalesOrder(so));
        assertEquals(new BigDecimal("80.00"), so.getTotalAmount());
    }
}
