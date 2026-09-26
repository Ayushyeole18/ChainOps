package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.StockTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDaoImpl implements StockTransactionDao {

    private static final String SELECT_BASE =
            "SELECT t.transaction_id, t.product_id, p.sku as product_sku, p.product_name, " +
            "t.warehouse_id, w.warehouse_name, t.transaction_type, t.quantity, " +
            "t.reference_number, t.notes, t.performed_by_user_id, u.full_name as performed_by_name, t.created_at " +
            "FROM stock_transactions t " +
            "JOIN products p ON t.product_id = p.product_id " +
            "JOIN warehouses w ON t.warehouse_id = w.warehouse_id " +
            "LEFT JOIN users u ON t.performed_by_user_id = u.user_id ";

    @Override
    public int insert(Connection conn, StockTransaction tx) {
        String sql = "INSERT INTO stock_transactions (product_id, warehouse_id, transaction_type, quantity, reference_number, notes, performed_by_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getProductId());
            ps.setInt(2, tx.getWarehouseId());
            ps.setString(3, tx.getTransactionType());
            ps.setInt(4, tx.getQuantity());
            ps.setString(5, tx.getReferenceNumber());
            ps.setString(6, tx.getNotes());
            if (tx.getPerformedByUserId() != null) {
                ps.setInt(7, tx.getPerformedByUserId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    tx.setTransactionId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error recording stock transaction: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public int insert(StockTransaction tx) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insert(conn, tx);
        } catch (SQLException e) {
            throw new DatabaseException("Error recording stock transaction", e);
        }
    }

    @Override
    public List<StockTransaction> findAll() {
        List<StockTransaction> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY t.created_at DESC LIMIT 500";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving stock transactions", e);
        }
        return list;
    }

    @Override
    public List<StockTransaction> searchAndFilter(String query, Integer warehouseId, String transactionType) {
        List<StockTransaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(p.product_name) LIKE ? OR LOWER(p.sku) LIKE ? OR LOWER(t.reference_number) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (warehouseId != null && warehouseId > 0) {
            sql.append("AND t.warehouse_id = ? ");
            params.add(warehouseId);
        }

        if (transactionType != null && !transactionType.equalsIgnoreCase("ALL")) {
            sql.append("AND t.transaction_type = ? ");
            params.add(transactionType.toUpperCase());
        }

        sql.append("ORDER BY t.created_at DESC LIMIT 500");

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
            throw new DatabaseException("Error searching stock transactions", e);
        }
        return list;
    }

    @Override
    public List<StockTransaction> findByProduct(int productId) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE t.product_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving stock transactions by product", e);
        }
        return list;
    }

    private StockTransaction mapResultSet(ResultSet rs) throws SQLException {
        StockTransaction tx = new StockTransaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setProductId(rs.getInt("product_id"));
        tx.setProductSku(rs.getString("product_sku"));
        tx.setProductName(rs.getString("product_name"));
        tx.setWarehouseId(rs.getInt("warehouse_id"));
        tx.setWarehouseName(rs.getString("warehouse_name"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setQuantity(rs.getInt("quantity"));
        tx.setReferenceNumber(rs.getString("reference_number"));
        tx.setNotes(rs.getString("notes"));
        int userId = rs.getInt("performed_by_user_id");
        if (!rs.wasNull()) {
            tx.setPerformedByUserId(userId);
        }
        tx.setPerformedByName(rs.getString("performed_by_name"));
        tx.setCreatedAt(rs.getTimestamp("created_at"));
        return tx;
    }
}
