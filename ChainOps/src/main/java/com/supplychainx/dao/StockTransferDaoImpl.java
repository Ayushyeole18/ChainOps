package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.InventoryItem;
import com.supplychainx.model.StockTransaction;
import com.supplychainx.model.StockTransfer;
import com.supplychainx.model.StockTransferItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockTransferDaoImpl implements StockTransferDao {

    private final InventoryDao inventoryDao = new InventoryDaoImpl();
    private final StockTransactionDao stockTransactionDao = new StockTransactionDaoImpl();
    private final WarehouseDao warehouseDao = new WarehouseDaoImpl();

    private static final String SELECT_BASE =
            "SELECT st.transfer_id, st.transfer_number, st.source_warehouse_id, w1.warehouse_name as source_warehouse_name, " +
            "st.destination_warehouse_id, w2.warehouse_name as destination_warehouse_name, st.transfer_date, " +
            "st.status, st.initiated_by_user_id, u.full_name as initiated_by_name, st.notes, st.created_at " +
            "FROM stock_transfers st " +
            "JOIN warehouses w1 ON st.source_warehouse_id = w1.warehouse_id " +
            "JOIN warehouses w2 ON st.destination_warehouse_id = w2.warehouse_id " +
            "LEFT JOIN users u ON st.initiated_by_user_id = u.user_id ";

    @Override
    public Optional<StockTransfer> findById(int transferId) {
        String sql = SELECT_BASE + "WHERE st.transfer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockTransfer st = mapResultSet(rs);
                    st.setItems(loadItems(conn, st.getTransferId()));
                    return Optional.of(st);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding stock transfer by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<StockTransfer> findAll() {
        List<StockTransfer> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY st.transfer_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching stock transfers", e);
        }
        return list;
    }

    @Override
    public List<StockTransfer> searchAndFilter(String query, String status) {
        List<StockTransfer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(st.transfer_number) LIKE ? OR LOWER(w1.warehouse_name) LIKE ? OR LOWER(w2.warehouse_name) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND st.status = ? ");
            params.add(status.toUpperCase());
        }

        sql.append("ORDER BY st.transfer_id DESC");

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
            throw new DatabaseException("Error searching stock transfers", e);
        }
        return list;
    }

    @Override
    public int createAndExecuteTransfer(StockTransfer transfer, int performedByUserId) {
        if (transfer.getSourceWarehouseId() == transfer.getDestinationWarehouseId()) {
            throw new BusinessRuleException("Source warehouse and destination warehouse cannot be the same.");
        }
        if (transfer.getItems() == null || transfer.getItems().isEmpty()) {
            throw new BusinessRuleException("At least one product item is required for stock transfer.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Verify that source has adequate available stock for all items
            for (StockTransferItem item : transfer.getItems()) {
                Optional<InventoryItem> invOpt = inventoryDao.findByProductAndWarehouse(conn, item.getProductId(), transfer.getSourceWarehouseId());
                if (invOpt.isEmpty()) {
                    throw new BusinessRuleException("Source warehouse has no inventory for selected product ID: " + item.getProductId());
                }
                InventoryItem inv = invOpt.get();
                if (inv.getQuantityAvailable() < item.getQuantity()) {
                    throw new BusinessRuleException("Insufficient inventory in source warehouse for product '" +
                            inv.getProductName() + "': Available: " + inv.getQuantityAvailable() + ", Requested transfer: " + item.getQuantity());
                }
            }

            // 2. Insert transfer header
            String insertTransferSql = "INSERT INTO stock_transfers (transfer_number, source_warehouse_id, destination_warehouse_id, " +
                                       "transfer_date, status, initiated_by_user_id, notes) " +
                                       "VALUES (?, ?, ?, ?, 'COMPLETED', ?, ?)";
            int transferId;
            try (PreparedStatement ps = conn.prepareStatement(insertTransferSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, transfer.getTransferNumber());
                ps.setInt(2, transfer.getSourceWarehouseId());
                ps.setInt(3, transfer.getDestinationWarehouseId());
                ps.setDate(4, Date.valueOf(transfer.getTransferDate()));
                ps.setInt(5, performedByUserId);
                ps.setString(6, transfer.getNotes());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        transferId = rs.getInt(1);
                        transfer.setTransferId(transferId);
                    } else {
                        throw new SQLException("Failed to obtain ID for stock transfer");
                    }
                }
            }

            // 3. Insert items, perform atomic stock deduction at source and increment at destination
            String insertItemSql = "INSERT INTO stock_transfer_items (transfer_id, product_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (StockTransferItem item : transfer.getItems()) {
                    psItem.setInt(1, transferId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.addBatch();

                    // Deduct from source
                    inventoryDao.adjustStock(conn, item.getProductId(), transfer.getSourceWarehouseId(), -item.getQuantity(), 0);
                    // Add to destination
                    inventoryDao.adjustStock(conn, item.getProductId(), transfer.getDestinationWarehouseId(), item.getQuantity(), 0);

                    // Record source transaction
                    StockTransaction txOut = new StockTransaction(
                            item.getProductId(),
                            transfer.getSourceWarehouseId(),
                            "TRANSFER_OUT",
                            -item.getQuantity(),
                            transfer.getTransferNumber(),
                            "Transfer out to destination warehouse #" + transfer.getDestinationWarehouseId(),
                            performedByUserId
                    );
                    stockTransactionDao.insert(conn, txOut);

                    // Record destination transaction
                    StockTransaction txIn = new StockTransaction(
                            item.getProductId(),
                            transfer.getDestinationWarehouseId(),
                            "TRANSFER_IN",
                            item.getQuantity(),
                            transfer.getTransferNumber(),
                            "Transfer in from source warehouse #" + transfer.getSourceWarehouseId(),
                            performedByUserId
                    );
                    stockTransactionDao.insert(conn, txIn);
                }
                psItem.executeBatch();
            }

            conn.commit();

            // Refresh warehouse utilization
            warehouseDao.updateUtilization(transfer.getSourceWarehouseId());
            warehouseDao.updateUtilization(transfer.getDestinationWarehouseId());

            return transferId;
        } catch (Exception e) {
            DatabaseConnection.rollbackQuietly(conn);
            if (e instanceof BusinessRuleException) {
                throw (BusinessRuleException) e;
            }
            throw new DatabaseException("Failed to complete stock transfer: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public String generateNextTransferNumber() {
        String sql = "SELECT COUNT(*) FROM stock_transfers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = rs.next() ? rs.getInt(1) : 0;
            return String.format("TR-2026-%03d", count + 501);
        } catch (SQLException e) {
            return "TR-2026-" + System.currentTimeMillis() % 10000;
        }
    }

    private List<StockTransferItem> loadItems(Connection conn, int transferId) throws SQLException {
        List<StockTransferItem> items = new ArrayList<>();
        String sql = "SELECT sti.item_id, sti.transfer_id, sti.product_id, p.sku as product_sku, p.product_name, sti.quantity " +
                     "FROM stock_transfer_items sti " +
                     "JOIN products p ON sti.product_id = p.product_id " +
                     "WHERE sti.transfer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockTransferItem item = new StockTransferItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setTransferId(rs.getInt("transfer_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductSku(rs.getString("product_sku"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private StockTransfer mapResultSet(ResultSet rs) throws SQLException {
        StockTransfer st = new StockTransfer();
        st.setTransferId(rs.getInt("transfer_id"));
        st.setTransferNumber(rs.getString("transfer_number"));
        st.setSourceWarehouseId(rs.getInt("source_warehouse_id"));
        st.setSourceWarehouseName(rs.getString("source_warehouse_name"));
        st.setDestinationWarehouseId(rs.getInt("destination_warehouse_id"));
        st.setDestinationWarehouseName(rs.getString("destination_warehouse_name"));
        st.setTransferDate(rs.getDate("transfer_date").toLocalDate());
        st.setStatus(rs.getString("status"));
        int uid = rs.getInt("initiated_by_user_id");
        if (!rs.wasNull()) st.setInitiatedByUserId(uid);
        st.setInitiatedByName(rs.getString("initiated_by_name"));
        st.setNotes(rs.getString("notes"));
        st.setCreatedAt(rs.getTimestamp("created_at"));
        return st;
    }
}
