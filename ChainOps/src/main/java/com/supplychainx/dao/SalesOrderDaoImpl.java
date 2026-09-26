package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.InventoryItem;
import com.supplychainx.model.SalesOrder;
import com.supplychainx.model.SalesOrderItem;
import com.supplychainx.model.StockTransaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderDaoImpl implements SalesOrderDao {

    private final InventoryDao inventoryDao = new InventoryDaoImpl();
    private final StockTransactionDao stockTransactionDao = new StockTransactionDaoImpl();
    private final WarehouseDao warehouseDao = new WarehouseDaoImpl();

    private static final String SELECT_BASE =
            "SELECT so.so_id, so.so_number, so.customer_name, so.customer_email, so.customer_phone, " +
            "so.shipping_address, so.warehouse_id, w.warehouse_name, so.order_date, so.total_amount, " +
            "so.status, so.created_by_user_id, u.full_name as created_by_name, so.notes, so.created_at " +
            "FROM sales_orders so " +
            "JOIN warehouses w ON so.warehouse_id = w.warehouse_id " +
            "LEFT JOIN users u ON so.created_by_user_id = u.user_id ";

    @Override
    public Optional<SalesOrder> findById(int soId) {
        String sql = SELECT_BASE + "WHERE so.so_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SalesOrder so = mapResultSet(rs);
                    so.setItems(loadItems(conn, so.getSoId()));
                    return Optional.of(so);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding sales order by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SalesOrder> findByNumber(String soNumber) {
        String sql = SELECT_BASE + "WHERE LOWER(so.so_number) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SalesOrder so = mapResultSet(rs);
                    so.setItems(loadItems(conn, so.getSoId()));
                    return Optional.of(so);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding sales order by number", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SalesOrder> findAll() {
        List<SalesOrder> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY so.so_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching sales orders", e);
        }
        return list;
    }

    @Override
    public List<SalesOrder> searchAndFilter(String query, String status, Integer warehouseId, LocalDate fromDate, LocalDate toDate) {
        List<SalesOrder> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(so.so_number) LIKE ? OR LOWER(so.customer_name) LIKE ? OR LOWER(so.customer_email) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND so.status = ? ");
            params.add(status.toUpperCase());
        }

        if (warehouseId != null && warehouseId > 0) {
            sql.append("AND so.warehouse_id = ? ");
            params.add(warehouseId);
        }

        if (fromDate != null) {
            sql.append("AND so.order_date >= ? ");
            params.add(java.sql.Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append("AND so.order_date <= ? ");
            params.add(java.sql.Date.valueOf(toDate));
        }

        sql.append("ORDER BY so.so_id DESC");

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
            throw new DatabaseException("Error searching sales orders", e);
        }
        return list;
    }

    @Override
    public int insert(SalesOrder so) {
        String insertSoSql = "INSERT INTO sales_orders (so_number, customer_name, customer_email, customer_phone, " +
                             "shipping_address, warehouse_id, order_date, total_amount, status, created_by_user_id, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO sales_order_items (so_id, product_id, quantity, unit_price, subtotal) " +
                              "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int soId;
            try (PreparedStatement ps = conn.prepareStatement(insertSoSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, so.getSoNumber());
                ps.setString(2, so.getCustomerName().trim());
                ps.setString(3, so.getCustomerEmail().trim());
                ps.setString(4, so.getCustomerPhone());
                ps.setString(5, so.getShippingAddress().trim());
                ps.setInt(6, so.getWarehouseId());
                ps.setDate(7, Date.valueOf(so.getOrderDate()));
                ps.setBigDecimal(8, so.getTotalAmount());
                ps.setString(9, so.getStatus() != null ? so.getStatus() : "PENDING");
                if (so.getCreatedByUserId() != null) {
                    ps.setInt(10, so.getCreatedByUserId());
                } else {
                    ps.setNull(10, Types.INTEGER);
                }
                ps.setString(11, so.getNotes());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        soId = rs.getInt(1);
                        so.setSoId(soId);
                    } else {
                        throw new SQLException("Failed to obtain generated ID for sales order");
                    }
                }
            }

            // Insert Items
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (SalesOrderItem item : so.getItems()) {
                    psItem.setInt(1, soId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getUnitPrice());
                    psItem.setBigDecimal(5, item.getSubtotal());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();
            return soId;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            throw new DatabaseException("Failed to save sales order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean confirmOrder(int soId, int confirmedByUserId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Lock and inspect sales order
            String checkSql = "SELECT so_number, warehouse_id, status FROM sales_orders WHERE so_id = ? FOR UPDATE";
            String soNumber;
            int warehouseId;
            String currentStatus;

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, soId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new BusinessRuleException("Sales order not found with ID: " + soId);
                    }
                    soNumber = rs.getString("so_number");
                    warehouseId = rs.getInt("warehouse_id");
                    currentStatus = rs.getString("status");
                }
            }

            if (!"PENDING".equalsIgnoreCase(currentStatus)) {
                throw new BusinessRuleException("Cannot confirm order in status '" + currentStatus + "'. Only PENDING orders can be confirmed.");
            }

            // 2. Fetch order items
            List<SalesOrderItem> items = loadItems(conn, soId);
            if (items.isEmpty()) {
                throw new BusinessRuleException("Cannot confirm sales order with no line items.");
            }

            // 3. Verify stock availability for each item
            for (SalesOrderItem item : items) {
                Optional<InventoryItem> invOpt = inventoryDao.findByProductAndWarehouse(conn, item.getProductId(), warehouseId);
                if (invOpt.isEmpty()) {
                    throw new BusinessRuleException("Insufficient inventory: Product '" + item.getProductName() +
                            "' (" + item.getProductSku() + ") is completely out of stock in selected warehouse.");
                }
                InventoryItem inv = invOpt.get();
                if (inv.getQuantityAvailable() < item.getQuantity()) {
                    throw new BusinessRuleException("Insufficient inventory for product '" + item.getProductName() +
                            "': Available: " + inv.getQuantityAvailable() + ", Requested: " + item.getQuantity() + ".");
                }
            }

            // 4. Stock deduction and transaction recording
            for (SalesOrderItem item : items) {
                // Deduct from available, add to reserved or directly reduce
                inventoryDao.adjustStock(conn, item.getProductId(), warehouseId, -item.getQuantity(), item.getQuantity());

                StockTransaction tx = new StockTransaction(
                        item.getProductId(),
                        warehouseId,
                        "OUTBOUND_SO",
                        -item.getQuantity(),
                        soNumber,
                        "Stock reserved for Sales Order " + soNumber,
                        confirmedByUserId
                );
                stockTransactionDao.insert(conn, tx);
            }

            // 5. Update SO status to CONFIRMED
            String updateSoSql = "UPDATE sales_orders SET status = 'CONFIRMED' WHERE so_id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSoSql)) {
                updatePs.setInt(1, soId);
                updatePs.executeUpdate();
            }

            conn.commit();

            // Update warehouse utilization
            warehouseDao.updateUtilization(warehouseId);

            return true;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof BusinessRuleException) {
                throw (BusinessRuleException) e;
            }
            throw new DatabaseException("Error confirming sales order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean updateStatus(int soId, String newStatus) {
        String sql = "UPDATE sales_orders SET status = ? WHERE so_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, soId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating sales order status", e);
        }
    }

    @Override
    public boolean cancelOrder(int soId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT warehouse_id, status FROM sales_orders WHERE so_id = ? FOR UPDATE";
            int warehouseId;
            String currentStatus;

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, soId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new BusinessRuleException("Sales order not found with ID: " + soId);
                    }
                    warehouseId = rs.getInt("warehouse_id");
                    currentStatus = rs.getString("status");
                }
            }

            if ("SHIPPED".equalsIgnoreCase(currentStatus) || "DELIVERED".equalsIgnoreCase(currentStatus)) {
                throw new BusinessRuleException("Cannot cancel an order that has already been shipped or delivered.");
            }

            // If it was confirmed/processing, release reserved stock
            if ("CONFIRMED".equalsIgnoreCase(currentStatus) || "PROCESSING".equalsIgnoreCase(currentStatus)) {
                List<SalesOrderItem> items = loadItems(conn, soId);
                for (SalesOrderItem item : items) {
                    inventoryDao.adjustStock(conn, item.getProductId(), warehouseId, item.getQuantity(), -item.getQuantity());
                }
            }

            String updateSql = "UPDATE sales_orders SET status = 'CANCELLED' WHERE so_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, soId);
                ps.executeUpdate();
            }

            conn.commit();
            warehouseDao.updateUtilization(warehouseId);
            return true;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof BusinessRuleException) {
                throw (BusinessRuleException) e;
            }
            throw new DatabaseException("Error cancelling sales order", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public String generateNextSoNumber() {
        String sql = "SELECT COUNT(*) FROM sales_orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = rs.next() ? rs.getInt(1) : 0;
            return String.format("SO-2026-%03d", count + 101);
        } catch (SQLException e) {
            return "SO-2026-" + System.currentTimeMillis() % 10000;
        }
    }

    private List<SalesOrderItem> loadItems(Connection conn, int soId) throws SQLException {
        List<SalesOrderItem> items = new ArrayList<>();
        String sql = "SELECT soi.item_id, soi.so_id, soi.product_id, p.sku as product_sku, p.product_name, " +
                     "soi.quantity, soi.unit_price, soi.subtotal " +
                     "FROM sales_order_items soi " +
                     "JOIN products p ON soi.product_id = p.product_id " +
                     "WHERE soi.so_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesOrderItem item = new SalesOrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setSoId(rs.getInt("so_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductSku(rs.getString("product_sku"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private SalesOrder mapResultSet(ResultSet rs) throws SQLException {
        SalesOrder so = new SalesOrder();
        so.setSoId(rs.getInt("so_id"));
        so.setSoNumber(rs.getString("so_number"));
        so.setCustomerName(rs.getString("customer_name"));
        so.setCustomerEmail(rs.getString("customer_email"));
        so.setCustomerPhone(rs.getString("customer_phone"));
        so.setShippingAddress(rs.getString("shipping_address"));
        so.setWarehouseId(rs.getInt("warehouse_id"));
        so.setWarehouseName(rs.getString("warehouse_name"));
        so.setOrderDate(rs.getDate("order_date").toLocalDate());
        so.setTotalAmount(rs.getBigDecimal("total_amount"));
        so.setStatus(rs.getString("status"));
        int uid = rs.getInt("created_by_user_id");
        if (!rs.wasNull()) so.setCreatedByUserId(uid);
        so.setCreatedByName(rs.getString("created_by_name"));
        so.setNotes(rs.getString("notes"));
        so.setCreatedAt(rs.getTimestamp("created_at"));
        return so;
    }
}
