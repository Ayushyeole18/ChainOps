package com.supplychainx.dao;

import com.supplychainx.model.Shipment;
import java.util.List;
import java.util.Optional;

public interface ShipmentDao {
    Optional<Shipment> findById(int shipmentId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Optional<Shipment> findBySalesOrderId(int soId);
    List<Shipment> findAll();
    List<Shipment> searchAndFilter(String query, String status);
    int insert(Shipment shipment);
    boolean updateStatus(int shipmentId, String status, String notes);
    String generateTrackingNumber(String carrier);
}
