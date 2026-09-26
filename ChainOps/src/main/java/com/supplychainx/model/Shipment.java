package com.supplychainx.model;

import java.time.LocalDate;
import java.sql.Timestamp;

public class Shipment {
    private int shipmentId;
    private int soId;
    private String soNumber;
    private String customerName;
    private String trackingNumber;
    private String carrier;
    private LocalDate shipmentDate;
    private LocalDate expectedDelivery;
    private LocalDate actualDelivery;
    private String shippingNotes;
    private String status; // READY, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, DELAYED, CANCELLED
    private Timestamp createdAt;

    public Shipment() {
        this.shipmentDate = LocalDate.now();
        this.status = "READY";
    }

    public int getShipmentId() { return shipmentId; }
    public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }

    public int getSoId() { return soId; }
    public void setSoId(int soId) { this.soId = soId; }

    public String getSoNumber() { return soNumber; }
    public void setSoNumber(String soNumber) { this.soNumber = soNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public LocalDate getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(LocalDate shipmentDate) { this.shipmentDate = shipmentDate; }

    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDate expectedDelivery) { this.expectedDelivery = expectedDelivery; }

    public LocalDate getActualDelivery() { return actualDelivery; }
    public void setActualDelivery(LocalDate actualDelivery) { this.actualDelivery = actualDelivery; }

    public String getShippingNotes() { return shippingNotes; }
    public void setShippingNotes(String shippingNotes) { this.shippingNotes = shippingNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
