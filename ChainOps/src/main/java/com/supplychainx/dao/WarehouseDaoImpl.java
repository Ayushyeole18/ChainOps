package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.Warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WarehouseDaoImpl implements WarehouseDao {

    @Override
    public Optional<Warehouse> findById(int warehouseId) {
        String sql = "SELECT warehouse_id, warehouse_name, code, location, manager_name, capacity, current_utilization, status, created_at " +
                     "FROM warehouses WHERE warehouse_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding warehouse by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Warehouse> findByCode(String code) {
        String sql = "SELECT warehouse_id, warehouse_name, code, location, manager_name, capacity, current_utilization, status, created_at " +
                     "FROM warehouses WHERE LOWER(code) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding warehouse by code", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Warehouse> findAll() {
        List<Warehouse> list = new ArrayList<>();
        String sql = "SELECT warehouse_id, warehouse_name, code, location, manager_name, capacity, current_utilization, status, created_at " +
                     "FROM warehouses ORDER BY warehouse_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching warehouses", e);
        }
        return list;
    }

    @Override
    public List<Warehouse> search(String query) {
        List<Warehouse> list = new ArrayList<>();
        String sql = "SELECT warehouse_id, warehouse_name, code, location, manager_name, capacity, current_utilization, status, created_at " +
                     "FROM warehouses WHERE LOWER(warehouse_name) LIKE ? OR LOWER(code) LIKE ? OR LOWER(location) LIKE ? OR LOWER(manager_name) LIKE ? " +
                     "ORDER BY warehouse_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pat = "%" + query.trim().toLowerCase() + "%";
            ps.setString(1, pat);
            ps.setString(2, pat);
            ps.setString(3, pat);
            ps.setString(4, pat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching warehouses", e);
        }
        return list;
    }

    @Override
    public int insert(Warehouse warehouse) {
        String sql = "INSERT INTO warehouses (warehouse_name, code, location, manager_name, capacity, current_utilization, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, warehouse.getWarehouseName().trim());
            ps.setString(2, warehouse.getCode().toUpperCase().trim());
            ps.setString(3, warehouse.getLocation().trim());
            ps.setString(4, warehouse.getManagerName().trim());
            ps.setInt(5, warehouse.getCapacity());
            ps.setInt(6, warehouse.getCurrentUtilization());
            ps.setString(7, warehouse.getStatus() != null ? warehouse.getStatus() : "ACTIVE");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    warehouse.setWarehouseId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating warehouse: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public boolean update(Warehouse warehouse) {
        String sql = "UPDATE warehouses SET warehouse_name = ?, code = ?, location = ?, manager_name = ?, " +
                     "capacity = ?, current_utilization = ?, status = ? WHERE warehouse_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, warehouse.getWarehouseName().trim());
            ps.setString(2, warehouse.getCode().toUpperCase().trim());
            ps.setString(3, warehouse.getLocation().trim());
            ps.setString(4, warehouse.getManagerName().trim());
            ps.setInt(5, warehouse.getCapacity());
            ps.setInt(6, warehouse.getCurrentUtilization());
            ps.setString(7, warehouse.getStatus());
            ps.setInt(8, warehouse.getWarehouseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating warehouse: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int warehouseId) {
        String sql = "DELETE FROM warehouses WHERE warehouse_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete warehouse: Existing inventory or orders reference this warehouse.", e);
        }
    }

    @Override
    public void updateUtilization(int warehouseId) {
        String sql = "UPDATE warehouses w SET current_utilization = " +
                     "COALESCE((SELECT SUM(i.quantity_available + i.quantity_reserved) FROM inventory i WHERE i.warehouse_id = w.warehouse_id), 0) " +
                     "WHERE w.warehouse_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error updating warehouse utilization", e);
        }
    }

    private Warehouse mapResultSet(ResultSet rs) throws SQLException {
        Warehouse w = new Warehouse();
        w.setWarehouseId(rs.getInt("warehouse_id"));
        w.setWarehouseName(rs.getString("warehouse_name"));
        w.setCode(rs.getString("code"));
        w.setLocation(rs.getString("location"));
        w.setManagerName(rs.getString("manager_name"));
        w.setCapacity(rs.getInt("capacity"));
        w.setCurrentUtilization(rs.getInt("current_utilization"));
        w.setStatus(rs.getString("status"));
        w.setCreatedAt(rs.getTimestamp("created_at"));
        return w;
    }
}
