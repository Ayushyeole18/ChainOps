package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.PurchaseOrder;
import com.supplychainx.model.PurchaseOrderItem;
import com.supplychainx.model.StockTransaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderDaoImpl implements PurchaseOrderDao {

    private final InventoryDao inventoryDao = new InventoryDaoImpl();
    private final StockTransactionDao stockTransactionDao = new StockTransactionDaoImpl();
    private final WarehouseDao warehouseDao = new WarehouseDaoImpl();

    private static final String SELECT_BASE =
            "SELECT po.po_id, po.po_number, po.supplier_id, s.supplier_name, " +
            "po.warehouse_id, w.warehouse_name, po.order_date, po.expected_delivery_date, " +
            "po.actual_delivery_date, po.total_amount, po.status, po.created_by_user_id, " +
            "u1.full_name as created_by_name, po.approved_by_user_id, u2.full_name as approved_by_name, " +
            "po.notes, po.created_at " +
            "FROM purchase_orders po " +
            "JOIN suppliers s ON po.supplier_id = s.supplier_id " +
            "JOIN warehouses w ON po.warehouse_id = w.warehouse_id " +
            "LEFT JOIN users u1 ON po.created_by_user_id = u1.user_id " +
            "LEFT JOIN users u2 ON po.approved_by_user_id = u2.user_id ";

    @Override
    public Optional<PurchaseOrder> findById(int poId) {
        String sql = SELECT_BASE + "WHERE po.po_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PurchaseOrder po = mapResultSet(rs);
                    po.setItems(loadItems(conn, po.getPoId()));
                    return Optional.of(po);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding purchase order by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PurchaseOrder> findByNumber(String poNumber) {
        String sql = SELECT_BASE + "WHERE LOWER(po.po_number) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, poNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PurchaseOrder po = mapResultSet(rs);
                    po.setItems(loadItems(conn, po.getPoId()));
                    return Optional.of(po);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding purchase order by number", e);
        }
        return Optional.empty();
    }

    @Override
    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY po.po_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching purchase orders", e);
        }
        return list;
    }

    @Override
    public List<PurchaseOrder> searchAndFilter(String query, String status, Integer supplierId, LocalDate fromDate, LocalDate toDate) {
        List<PurchaseOrder> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(po.po_number) LIKE ? OR LOWER(s.supplier_name) LIKE ? OR LOWER(w.warehouse_name) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND po.status = ? ");
            params.add(status.toUpperCase());
        }

        if (supplierId != null && supplierId > 0) {
            sql.append("AND po.supplier_id = ? ");
            params.add(supplierId);
        }

        if (fromDate != null) {
            sql.append("AND po.order_date >= ? ");
            params.add(java.sql.Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append("AND po.order_date <= ? ");
            params.add(java.sql.Date.valueOf(toDate));
        }

        sql.append("ORDER BY po.po_id DESC");

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
            throw new DatabaseException("Error searching purchase orders", e);
        }
        return list;
    }

    @Override
    public int insert(PurchaseOrder po) {
        String insertPoSql = "INSERT INTO purchase_orders (po_number, supplier_id, warehouse_id, order_date, " +
                             "expected_delivery_date, total_amount, status, created_by_user_id, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO purchase_order_items (po_id, product_id, quantity_ordered, quantity_received, unit_cost, subtotal) " +
                              "VALUES (?, ?, ?, 0, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int poId;
            try (PreparedStatement ps = conn.prepareStatement(insertPoSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, po.getPoNumber());
                ps.setInt(2, po.getSupplierId());
                ps.setInt(3, po.getWarehouseId());
                ps.setDate(4, Date.valueOf(po.getOrderDate()));
                if (po.getExpectedDeliveryDate() != null) {
                    ps.setDate(5, Date.valueOf(po.getExpectedDeliveryDate()));
                } else {
                    ps.setNull(5, Types.DATE);
                }
                ps.setBigDecimal(6, po.getTotalAmount());
                ps.setString(7, po.getStatus() != null ? po.getStatus() : "PENDING");
                if (po.getCreatedByUserId() != null) {
                    ps.setInt(8, po.getCreatedByUserId());
                } else {
                    ps.setNull(8, Types.INTEGER);
                }
                ps.setString(9, po.getNotes());

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        poId = rs.getInt(1);
                        po.setPoId(poId);
                    } else {
                        throw new SQLException("Failed to obtain generated ID for purchase order");
                    }
                }
            }

            // Insert Items
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (PurchaseOrderItem item : po.getItems()) {
                    psItem.setInt(1, poId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantityOrdered());
                    psItem.setBigDecimal(4, item.getUnitCost());
                    psItem.setBigDecimal(5, item.getSubtotal());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();
            return poId;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            throw new DatabaseException("Failed to save purchase order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean updateStatus(int poId, String newStatus, Integer approvedByUserId) {
        String sql = "UPDATE purchase_orders SET status = ?, approved_by_user_id = COALESCE(?, approved_by_user_id) WHERE po_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            if (approvedByUserId != null) {
                ps.setInt(2, approvedByUserId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, poId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating purchase order status", e);
        }
    }

    @Override
    public boolean receivePurchaseOrder(int poId, int receivedByUserId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Lock and check PO
            String checkSql = "SELECT po_number, warehouse_id, status FROM purchase_orders WHERE po_id = ? FOR UPDATE";
            String poNumber;
            int warehouseId;
            String currentStatus;

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, poId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new BusinessRuleException("Purchase order not found with ID: " + poId);
                    }
                    poNumber = rs.getString("po_number");
                    warehouseId = rs.getInt("warehouse_id");
                    currentStatus = rs.getString("status");
                }
            }

            if ("RECEIVED".equalsIgnoreCase(currentStatus)) {
                throw new BusinessRuleException("This purchase order has already been received! Duplicate receiving prevented.");
            }
            if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                throw new BusinessRuleException("Cannot receive a cancelled purchase order.");
            }

            // 2. Fetch items to receive
            List<PurchaseOrderItem> items = loadItems(conn, poId);
            if (items.isEmpty()) {
                throw new BusinessRuleException("Cannot receive purchase order with no items.");
            }

            // 3. Increment stock & create stock transaction for each item
            for (PurchaseOrderItem item : items) {
                inventoryDao.adjustStock(conn, item.getProductId(), warehouseId, item.getQuantityOrdered(), 0);

                StockTransaction tx = new StockTransaction(
                        item.getProductId(),
                        warehouseId,
                        "INBOUND_PO",
                        item.getQuantityOrdered(),
                        poNumber,
                        "Received items for " + poNumber,
                        receivedByUserId
                );
                stockTransactionDao.insert(conn, tx);

                // Update quantity_received in PO item
                String updateItemSql = "UPDATE purchase_order_items SET quantity_received = quantity_ordered WHERE item_id = ?";
                try (PreparedStatement itemPs = conn.prepareStatement(updateItemSql)) {
                    itemPs.setInt(1, item.getItemId());
                    itemPs.executeUpdate();
                }
            }

            // 4. Update PO status to RECEIVED
            String updatePoSql = "UPDATE purchase_orders SET status = 'RECEIVED', actual_delivery_date = ? WHERE po_id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updatePoSql)) {
                updatePs.setDate(1, Date.valueOf(LocalDate.now()));
                updatePs.setInt(2, poId);
                updatePs.executeUpdate();
            }

            conn.commit();

            // Update warehouse utilization metric
            warehouseDao.updateUtilization(warehouseId);

            return true;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof BusinessRuleException) {
                throw (BusinessRuleException) e;
            }
            throw new DatabaseException("Error during purchase order receipt: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean cancelPurchaseOrder(int poId) {
        String checkSql = "SELECT status FROM purchase_orders WHERE po_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setInt(1, poId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("RECEIVED".equalsIgnoreCase(status)) {
                        throw new BusinessRuleException("Cannot cancel an already received purchase order.");
                    }
                }
            }
            String updateSql = "UPDATE purchase_orders SET status = 'CANCELLED' WHERE po_id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, poId);
                return psUpdate.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error cancelling purchase order", e);
        }
    }

    @Override
    public String generateNextPoNumber() {
        String sql = "SELECT COUNT(*) FROM purchase_orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = rs.next() ? rs.getInt(1) : 0;
            return String.format("PO-2026-%03d", count + 1);
        } catch (SQLException e) {
            return "PO-2026-" + System.currentTimeMillis() % 10000;
        }
    }

    private List<PurchaseOrderItem> loadItems(Connection conn, int poId) throws SQLException {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String sql = "SELECT poi.item_id, poi.po_id, poi.product_id, p.sku as product_sku, p.product_name, " +
                     "poi.quantity_ordered, poi.quantity_received, poi.unit_cost, poi.subtotal " +
                     "FROM purchase_order_items poi " +
                     "JOIN products p ON poi.product_id = p.product_id " +
                     "WHERE poi.po_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderItem item = new PurchaseOrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setPoId(rs.getInt("po_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductSku(rs.getString("product_sku"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantityOrdered(rs.getInt("quantity_ordered"));
                    item.setQuantityReceived(rs.getInt("quantity_received"));
                    item.setUnitCost(rs.getBigDecimal("unit_cost"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private PurchaseOrder mapResultSet(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoId(rs.getInt("po_id"));
        po.setPoNumber(rs.getString("po_number"));
        po.setSupplierId(rs.getInt("supplier_id"));
        po.setSupplierName(rs.getString("supplier_name"));
        po.setWarehouseId(rs.getInt("warehouse_id"));
        po.setWarehouseName(rs.getString("warehouse_name"));
        po.setOrderDate(rs.getDate("order_date").toLocalDate());
        Date expDate = rs.getDate("expected_delivery_date");
        if (expDate != null) po.setExpectedDeliveryDate(expDate.toLocalDate());
        Date actDate = rs.getDate("actual_delivery_date");
        if (actDate != null) po.setActualDeliveryDate(actDate.toLocalDate());
        po.setTotalAmount(rs.getBigDecimal("total_amount"));
        po.setStatus(rs.getString("status"));
        int u1 = rs.getInt("created_by_user_id");
        if (!rs.wasNull()) po.setCreatedByUserId(u1);
        po.setCreatedByName(rs.getString("created_by_name"));
        int u2 = rs.getInt("approved_by_user_id");
        if (!rs.wasNull()) po.setApprovedByUserId(u2);
        po.setApprovedByName(rs.getString("approved_by_name"));
        po.setNotes(rs.getString("notes"));
        po.setCreatedAt(rs.getTimestamp("created_at"));
        return po;
    }
}
