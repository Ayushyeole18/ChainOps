package com.supplychainx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SalesOrder {
    private int soId;
    private String soNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private int warehouseId;
    private String warehouseName;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private Integer createdByUserId;
    private String createdByName;
    private String notes;
    private Timestamp createdAt;
    private List<SalesOrderItem> items = new ArrayList<>();

    public SalesOrder() {
        this.orderDate = LocalDate.now();
        this.status = "PENDING";
        this.totalAmount = BigDecimal.ZERO;
    }

    public int getSoId() { return soId; }
    public void setSoId(int soId) { this.soId = soId; }

    public String getSoNumber() { return soNumber; }
    public void setSoNumber(String soNumber) { this.soNumber = soNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<SalesOrderItem> getItems() { return items; }
    public void setItems(List<SalesOrderItem> items) { this.items = items; }

    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SalesOrderItem item : items) {
            if (item.getSubtotal() != null) {
                sum = sum.add(item.getSubtotal());
            }
        }
        this.totalAmount = sum;
    }
}
