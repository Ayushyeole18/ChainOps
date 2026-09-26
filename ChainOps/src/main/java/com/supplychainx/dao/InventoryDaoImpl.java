package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.InventoryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryDaoImpl implements InventoryDao {

    private static final String SELECT_BASE =
            "SELECT i.inventory_id, i.product_id, p.sku, p.product_name, c.category_name, p.unit_price, " +
            "i.warehouse_id, w.warehouse_name, w.code as warehouse_code, " +
            "i.quantity_available, i.quantity_reserved, p.reorder_level, i.last_counted_at " +
            "FROM inventory i " +
            "JOIN products p ON i.product_id = p.product_id " +
            "JOIN categories c ON p.category_id = c.category_id " +
            "JOIN warehouses w ON i.warehouse_id = w.warehouse_id ";

    @Override
    public Optional<InventoryItem> findByProductAndWarehouse(int productId, int warehouseId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findByProductAndWarehouse(conn, productId, warehouseId);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding inventory item", e);
        }
    }

    @Override
    public Optional<InventoryItem> findByProductAndWarehouse(Connection conn, int productId, int warehouseId) {
        String sql = SELECT_BASE + "WHERE i.product_id = ? AND i.warehouse_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding inventory item for product " + productId + " and warehouse " + warehouseId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<InventoryItem> findAll() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY w.warehouse_name ASC, p.product_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching inventory", e);
        }
        return list;
    }

    @Override
    public List<InventoryItem> searchAndFilter(String query, Integer warehouseId, String stockStatus) {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(p.product_name) LIKE ? OR LOWER(p.sku) LIKE ? OR LOWER(c.category_name) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (warehouseId != null && warehouseId > 0) {
            sql.append("AND i.warehouse_id = ? ");
            params.add(warehouseId);
        }

        if (stockStatus != null && !stockStatus.equalsIgnoreCase("ALL")) {
            if ("OUT_OF_STOCK".equalsIgnoreCase(stockStatus)) {
                sql.append("AND i.quantity_available = 0 ");
            } else if ("LOW_STOCK".equalsIgnoreCase(stockStatus)) {
                sql.append("AND i.quantity_available > 0 AND i.quantity_available <= p.reorder_level ");
            } else if ("IN_STOCK".equalsIgnoreCase(stockStatus)) {
                sql.append("AND i.quantity_available > p.reorder_level ");
            }
        }

        sql.append("ORDER BY w.warehouse_name ASC, p.product_name ASC");

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
            throw new DatabaseException("Error searching inventory", e);
        }
        return list;
    }

    @Override
    public List<InventoryItem> findLowStockItems() {
        return searchAndFilter(null, null, "LOW_STOCK");
    }

    @Override
    public boolean adjustStock(Connection conn, int productId, int warehouseId, int deltaAvailable, int deltaReserved) {
        // First check current inventory
        String checkSql = "SELECT inventory_id, quantity_available, quantity_reserved FROM inventory " +
                          "WHERE product_id = ? AND warehouse_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, productId);
            ps.setInt(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int currentAvailable = rs.getInt("quantity_available");
                    int currentReserved = rs.getInt("quantity_reserved");

                    int newAvailable = currentAvailable + deltaAvailable;
                    int newReserved = currentReserved + deltaReserved;

                    if (newAvailable < 0) {
                        throw new BusinessRuleException("Insufficient inventory available: Requested change " + deltaAvailable +
                                " exceeds current available stock (" + currentAvailable + ").");
                    }
                    if (newReserved < 0) {
                        throw new BusinessRuleException("Reserved inventory cannot be negative.");
                    }

                    String updateSql = "UPDATE inventory SET quantity_available = ?, quantity_reserved = ? " +
                                       "WHERE product_id = ? AND warehouse_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, newAvailable);
                        updatePs.setInt(2, newReserved);
                        updatePs.setInt(3, productId);
                        updatePs.setInt(4, warehouseId);
                        return updatePs.executeUpdate() > 0;
                    }
                } else {
                    // Record doesn't exist yet, insert if delta is positive
                    if (deltaAvailable < 0 || deltaReserved < 0) {
                        throw new BusinessRuleException("Cannot deduct stock: No inventory record exists for product in selected warehouse.");
                    }
                    String insertSql = "INSERT INTO inventory (product_id, warehouse_id, quantity_available, quantity_reserved) " +
                                       "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, productId);
                        insertPs.setInt(2, warehouseId);
                        insertPs.setInt(3, deltaAvailable);
                        insertPs.setInt(4, deltaReserved);
                        return insertPs.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error updating inventory stock", e);
        }
    }

    @Override
    public boolean upsertInitialStock(int productId, int warehouseId, int quantityAvailable) {
        String sql = "INSERT INTO inventory (product_id, warehouse_id, quantity_available, quantity_reserved) " +
                     "VALUES (?, ?, ?, 0) " +
                     "ON DUPLICATE KEY UPDATE quantity_available = VALUES(quantity_available)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, warehouseId);
            ps.setInt(3, quantityAvailable);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error upserting inventory record", e);
        }
    }

    private InventoryItem mapResultSet(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setInventoryId(rs.getInt("inventory_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setProductSku(rs.getString("sku"));
        item.setProductName(rs.getString("product_name"));
        item.setCategoryName(rs.getString("category_name"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setWarehouseId(rs.getInt("warehouse_id"));
        item.setWarehouseName(rs.getString("warehouse_name"));
        item.setWarehouseCode(rs.getString("warehouse_code"));
        item.setQuantityAvailable(rs.getInt("quantity_available"));
        item.setQuantityReserved(rs.getInt("quantity_reserved"));
        item.setReorderLevel(rs.getInt("reorder_level"));
        item.setLastCountedAt(rs.getTimestamp("last_counted_at"));
        return item;
    }
}
