package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.Shipment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ShipmentDaoImpl implements ShipmentDao {

    private static final String SELECT_BASE =
            "SELECT s.shipment_id, s.so_id, so.so_number, so.customer_name, " +
            "s.tracking_number, s.carrier, s.shipment_date, s.expected_delivery, " +
            "s.actual_delivery, s.shipping_notes, s.status, s.created_at " +
            "FROM shipments s " +
            "JOIN sales_orders so ON s.so_id = so.so_id ";

    @Override
    public Optional<Shipment> findById(int shipmentId) {
        String sql = SELECT_BASE + "WHERE s.shipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding shipment by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        String sql = SELECT_BASE + "WHERE LOWER(s.tracking_number) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trackingNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding shipment by tracking number", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Shipment> findBySalesOrderId(int soId) {
        String sql = SELECT_BASE + "WHERE s.so_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding shipment by sales order ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Shipment> findAll() {
        List<Shipment> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY s.shipment_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching shipments", e);
        }
        return list;
    }

    @Override
    public List<Shipment> searchAndFilter(String query, String status) {
        List<Shipment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(s.tracking_number) LIKE ? OR LOWER(so.so_number) LIKE ? OR LOWER(so.customer_name) LIKE ? OR LOWER(s.carrier) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND s.status = ? ");
            params.add(status.toUpperCase());
        }

        sql.append("ORDER BY s.shipment_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching shipments", e);
        }
        return list;
    }

    @Override
    public int insert(Shipment shipment) {
        String sql = "INSERT INTO shipments (so_id, tracking_number, carrier, shipment_date, expected_delivery, shipping_notes, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, shipment.getSoId());
            ps.setString(2, shipment.getTrackingNumber());
            ps.setString(3, shipment.getCarrier());
            ps.setDate(4, Date.valueOf(shipment.getShipmentDate()));
            ps.setDate(5, Date.valueOf(shipment.getExpectedDelivery()));
            ps.setString(6, shipment.getShippingNotes());
            ps.setString(7, shipment.getStatus() != null ? shipment.getStatus() : "READY");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    shipment.setShipmentId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating shipment: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public boolean updateStatus(int shipmentId, String status, String notes) {
        String sql = "UPDATE shipments SET status = ?, shipping_notes = COALESCE(?, shipping_notes), " +
                     "actual_delivery = (CASE WHEN ? = 'DELIVERED' THEN CURRENT_DATE ELSE actual_delivery END) " +
                     "WHERE shipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setString(3, status);
            ps.setInt(4, shipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating shipment status", e);
        }
    }

    @Override
    public String generateTrackingNumber(String carrier) {
        String prefix = "TRK";
        if (carrier != null) {
            String c = carrier.toUpperCase();
            if (c.contains("FEDEX")) prefix = "TRK-FDX";
            else if (c.contains("DHL")) prefix = "TRK-DHL";
            else if (c.contains("UPS")) prefix = "TRK-UPS";
            else if (c.contains("MAERSK")) prefix = "TRK-MSK";
            else prefix = "TRK-" + (carrier.length() >= 3 ? carrier.substring(0, 3).toUpperCase() : "GEN");
        }
        long rand = 10000000L + new Random().nextInt(90000000);
        return prefix + "-" + rand;
    }

    private Shipment mapResultSet(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setShipmentId(rs.getInt("shipment_id"));
        s.setSoId(rs.getInt("so_id"));
        s.setSoNumber(rs.getString("so_number"));
        s.setCustomerName(rs.getString("customer_name"));
        s.setTrackingNumber(rs.getString("tracking_number"));
        s.setCarrier(rs.getString("carrier"));
        s.setShipmentDate(rs.getDate("shipment_date").toLocalDate());
        s.setExpectedDelivery(rs.getDate("expected_delivery").toLocalDate());
        Date actDate = rs.getDate("actual_delivery");
        if (actDate != null) s.setActualDelivery(actDate.toLocalDate());
        s.setShippingNotes(rs.getString("shipping_notes"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
