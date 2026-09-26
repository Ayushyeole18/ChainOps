package com.supplychainx.service;

import com.supplychainx.dao.SalesOrderDao;
import com.supplychainx.dao.SalesOrderDaoImpl;
import com.supplychainx.dao.ShipmentDao;
import com.supplychainx.dao.ShipmentDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.Shipment;
import com.supplychainx.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class ShipmentService {

    private final ShipmentDao shipmentDao;
    private final SalesOrderDao salesOrderDao;

    public ShipmentService() {
        this.shipmentDao = new ShipmentDaoImpl();
        this.salesOrderDao = new SalesOrderDaoImpl();
    }

    public ShipmentService(ShipmentDao shipmentDao, SalesOrderDao salesOrderDao) {
        this.shipmentDao = shipmentDao;
        this.salesOrderDao = salesOrderDao;
    }

    public int createShipment(Shipment shipment) {
        if (shipment.getSoId() <= 0) {
            throw new ValidationException("A valid confirmed Sales Order must be selected.");
        }
        ValidationUtil.requireNonEmpty(shipment.getCarrier(), "Carrier");
        if (shipment.getShipmentDate() == null) {
            throw new ValidationException("Shipment Date is required.");
        }
        if (shipment.getExpectedDelivery() == null) {
            throw new ValidationException("Expected Delivery Date is required.");
        }

        // Verify that SO exists and is CONFIRMED or PROCESSING
        Optional<SalesOrder> soOpt = salesOrderDao.findById(shipment.getSoId());
        if (soOpt.isEmpty()) {
            throw new BusinessRuleException("Selected sales order does not exist.");
        }
        SalesOrder so = soOpt.get();
        if ("DELIVERED".equalsIgnoreCase(so.getStatus()) || "CANCELLED".equalsIgnoreCase(so.getStatus())) {
            throw new BusinessRuleException("Cannot create shipment for order in status: " + so.getStatus());
        }

        // Generate tracking number if not set
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().trim().isEmpty()) {
            shipment.setTrackingNumber(shipmentDao.generateTrackingNumber(shipment.getCarrier()));
        }

        int shipmentId = shipmentDao.insert(shipment);

        // Update sales order status to PROCESSING or SHIPPED
        salesOrderDao.updateStatus(so.getSoId(), "PROCESSING");

        return shipmentId;
    }

    public boolean updateShipmentStatus(int shipmentId, String newStatus, String notes) {
        Optional<Shipment> shipOpt = shipmentDao.findById(shipmentId);
        if (shipOpt.isEmpty()) {
            throw new BusinessRuleException("Shipment not found.");
        }
        Shipment shipment = shipOpt.get();

        boolean updated = shipmentDao.updateStatus(shipmentId, newStatus, notes);

        // Sync with Sales Order status
        if ("IN_TRANSIT".equalsIgnoreCase(newStatus) || "OUT_FOR_DELIVERY".equalsIgnoreCase(newStatus)) {
            salesOrderDao.updateStatus(shipment.getSoId(), "SHIPPED");
        } else if ("DELIVERED".equalsIgnoreCase(newStatus)) {
            salesOrderDao.updateStatus(shipment.getSoId(), "DELIVERED");
        }

        return updated;
    }

    public Optional<Shipment> getShipmentById(int shipmentId) {
        return shipmentDao.findById(shipmentId);
    }

    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentDao.findByTrackingNumber(trackingNumber);
    }

    public List<Shipment> getAllShipments() {
        return shipmentDao.findAll();
    }

    public List<Shipment> searchAndFilter(String query, String status) {
        return shipmentDao.searchAndFilter(query, status);
    }

    public String generateTrackingNumber(String carrier) {
        return shipmentDao.generateTrackingNumber(carrier);
    }
}
