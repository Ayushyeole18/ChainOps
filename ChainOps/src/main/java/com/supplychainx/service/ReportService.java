package com.supplychainx.service;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.util.CsvExportUtil;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportService {

    public enum ReportType {
        INVENTORY("Inventory Valuation & Stock Levels Report"),
        LOW_STOCK("Critical Low-Stock & Reorder Report"),
        SUPPLIERS("Supplier Performance & Vendor Roster"),
        PURCHASE_ORDERS("Purchase Orders & Inbound Procurements"),
        SALES_ORDERS("Sales Orders & Fulfillment Revenue"),
        SHIPMENTS("Logistics & Dispatch Shipments Report"),
        STOCK_TRANSACTIONS("Inventory Audit & Stock Movements Log"),
        WAREHOUSE_UTILIZATION("Warehouse Capacity & Storage Utilization");

        private final String title;
        ReportType(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    public static class ReportResult {
        private final List<String> headers;
        private final List<List<String>> rows;

        public ReportResult(List<String> headers, List<List<String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }

        public List<String> getHeaders() { return headers; }
        public List<List<String>> getRows() { return rows; }
    }

    public ReportResult generateReport(ReportType type, LocalDate fromDate, LocalDate toDate, Integer warehouseId) {
        switch (type) {
            case INVENTORY:
                return generateInventoryReport(warehouseId);
            case LOW_STOCK:
                return generateLowStockReport(warehouseId);
            case SUPPLIERS:
                return generateSupplierReport();
            case PURCHASE_ORDERS:
                return generatePurchaseOrderReport(fromDate, toDate);
            case SALES_ORDERS:
                return generateSalesOrderReport(fromDate, toDate);
            case SHIPMENTS:
                return generateShipmentReport(fromDate, toDate);
            case STOCK_TRANSACTIONS:
                return generateStockTransactionsReport(fromDate, toDate, warehouseId);
            case WAREHOUSE_UTILIZATION:
                return generateWarehouseReport();
            default:
                return new ReportResult(List.of("Message"), List.of(List.of("No report selected")));
        }
    }

    public void exportReportToCsv(ReportType type, LocalDate fromDate, LocalDate toDate, Integer warehouseId, File targetFile) {
        ReportResult result = generateReport(type, fromDate, toDate, warehouseId);
        CsvExportUtil.exportToCsv(targetFile, result.getHeaders(), result.getRows());
    }

    private ReportResult generateInventoryReport(Integer warehouseId) {
        List<String> headers = Arrays.asList("SKU", "Product Name", "Category", "Warehouse", "Available Qty", "Reserved Qty", "Total Qty", "Unit Price ($)", "Total Value ($)", "Stock Status");
        List<List<String>> rows = new ArrayList<>();

        String sql = "SELECT p.sku, p.product_name, c.category_name, w.warehouse_name, " +
                     "i.quantity_available, i.quantity_reserved, (i.quantity_available + i.quantity_reserved) as total_qty, " +
                     "p.unit_price, ((i.quantity_available + i.quantity_reserved) * p.unit_price) as total_val, " +
                     "CASE WHEN i.quantity_available = 0 THEN 'OUT OF STOCK' " +
                     "     WHEN i.quantity_available <= p.reorder_level THEN 'LOW STOCK' " +
                     "     ELSE 'IN STOCK' END as status " +
                     "FROM inventory i " +
                     "JOIN products p ON i.product_id = p.product_id " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN warehouses w ON i.warehouse_id = w.warehouse_id " +
                     (warehouseId != null && warehouseId > 0 ? "WHERE i.warehouse_id = ? " : "") +
                     "ORDER BY w.warehouse_name, p.product_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (warehouseId != null && warehouseId > 0) {
                ps.setInt(1, warehouseId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(Arrays.asList(
                            rs.getString("sku"),
                            rs.getString("product_name"),
                            rs.getString("category_name"),
                            rs.getString("warehouse_name"),
                            String.valueOf(rs.getInt("quantity_available")),
                            String.valueOf(rs.getInt("quantity_reserved")),
                            String.valueOf(rs.getInt("total_qty")),
                            String.format("%.2f", rs.getDouble("unit_price")),
                            String.format("%.2f", rs.getDouble("total_val")),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate inventory report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generateLowStockReport(Integer warehouseId) {
        List<String> headers = Arrays.asList("SKU", "Product Name", "Warehouse", "Available Qty", "Reorder Level", "Deficit", "Unit Price ($)", "Status");
        List<List<String>> rows = new ArrayList<>();

        String sql = "SELECT p.sku, p.product_name, w.warehouse_name, i.quantity_available, p.reorder_level, " +
                     "(p.reorder_level - i.quantity_available) as deficit, p.unit_price, " +
                     "CASE WHEN i.quantity_available = 0 THEN 'CRITICAL (OUT OF STOCK)' ELSE 'WARNING (LOW STOCK)' END as status " +
                     "FROM inventory i " +
                     "JOIN products p ON i.product_id = p.product_id " +
                     "JOIN warehouses w ON i.warehouse_id = w.warehouse_id " +
                     "WHERE i.quantity_available <= p.reorder_level " +
                     (warehouseId != null && warehouseId > 0 ? "AND i.warehouse_id = ? " : "") +
                     "ORDER BY deficit DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (warehouseId != null && warehouseId > 0) {
                ps.setInt(1, warehouseId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(Arrays.asList(
                            rs.getString("sku"),
                            rs.getString("product_name"),
                            rs.getString("warehouse_name"),
                            String.valueOf(rs.getInt("quantity_available")),
                            String.valueOf(rs.getInt("reorder_level")),
                            String.valueOf(rs.getInt("deficit")),
                            String.format("%.2f", rs.getDouble("unit_price")),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate low-stock report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generateSupplierReport() {
        List<String> headers = Arrays.asList("Supplier Name", "Contact Person", "Email", "Phone", "City", "Country", "Total Orders Placed", "Total Spend ($)", "Status");
        List<List<String>> rows = new ArrayList<>();

        String sql = "SELECT s.supplier_name, s.contact_person, s.email, s.phone, s.city, s.country, s.status, " +
                     "COUNT(po.po_id) as total_pos, COALESCE(SUM(po.total_amount), 0) as total_spend " +
                     "FROM suppliers s " +
                     "LEFT JOIN purchase_orders po ON s.supplier_id = po.supplier_id " +
                     "GROUP BY s.supplier_id ORDER BY total_spend DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(Arrays.asList(
                        rs.getString("supplier_name"),
                        rs.getString("contact_person"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("city"),
                        rs.getString("country"),
                        String.valueOf(rs.getInt("total_pos")),
                        String.format("%.2f", rs.getDouble("total_spend")),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate supplier report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generatePurchaseOrderReport(LocalDate fromDate, LocalDate toDate) {
        List<String> headers = Arrays.asList("PO Number", "Supplier", "Destination Warehouse", "Order Date", "Expected Delivery", "Total Amount ($)", "Status", "Created By");
        List<List<String>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT po.po_number, s.supplier_name, w.warehouse_name, po.order_date, po.expected_delivery_date, " +
                "po.total_amount, po.status, u.full_name as created_by " +
                "FROM purchase_orders po " +
                "JOIN suppliers s ON po.supplier_id = s.supplier_id " +
                "JOIN warehouses w ON po.warehouse_id = w.warehouse_id " +
                "LEFT JOIN users u ON po.created_by_user_id = u.user_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND po.order_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND po.order_date <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY po.order_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(Arrays.asList(
                            rs.getString("po_number"),
                            rs.getString("supplier_name"),
                            rs.getString("warehouse_name"),
                            String.valueOf(rs.getDate("order_date")),
                            String.valueOf(rs.getDate("expected_delivery_date")),
                            String.format("%.2f", rs.getDouble("total_amount")),
                            rs.getString("status"),
                            rs.getString("created_by") != null ? rs.getString("created_by") : "System"
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate purchase order report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generateSalesOrderReport(LocalDate fromDate, LocalDate toDate) {
        List<String> headers = Arrays.asList("SO Number", "Customer Name", "Customer Email", "Fulfillment Warehouse", "Order Date", "Total ($)", "Status", "Created By");
        List<List<String>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT so.so_number, so.customer_name, so.customer_email, w.warehouse_name, " +
                "so.order_date, so.total_amount, so.status, u.full_name as created_by " +
                "FROM sales_orders so " +
                "JOIN warehouses w ON so.warehouse_id = w.warehouse_id " +
                "LEFT JOIN users u ON so.created_by_user_id = u.user_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND so.order_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND so.order_date <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY so.order_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(Arrays.asList(
                            rs.getString("so_number"),
                            rs.getString("customer_name"),
                            rs.getString("customer_email"),
                            rs.getString("warehouse_name"),
                            String.valueOf(rs.getDate("order_date")),
                            String.format("%.2f", rs.getDouble("total_amount")),
                            rs.getString("status"),
                            rs.getString("created_by") != null ? rs.getString("created_by") : "System"
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate sales order report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generateShipmentReport(LocalDate fromDate, LocalDate toDate) {
        List<String> headers = Arrays.asList("Tracking #", "SO Number", "Customer", "Carrier", "Ship Date", "Expected Delivery", "Actual Delivery", "Status");
        List<List<String>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT s.tracking_number, so.so_number, so.customer_name, s.carrier, " +
                "s.shipment_date, s.expected_delivery, s.actual_delivery, s.status " +
                "FROM shipments s " +
                "JOIN sales_orders so ON s.so_id = so.so_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND s.shipment_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND s.shipment_date <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY s.shipment_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(Arrays.asList(
                            rs.getString("tracking_number"),
                            rs.getString("so_number"),
                            rs.getString("customer_name"),
                            rs.getString("carrier"),
                            String.valueOf(rs.getDate("shipment_date")),
                            String.valueOf(rs.getDate("expected_delivery")),
                            rs.getDate("actual_delivery") != null ? String.valueOf(rs.getDate("actual_delivery")) : "In Transit",
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate shipments report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generateStockTransactionsReport(LocalDate fromDate, LocalDate toDate, Integer warehouseId) {
        List<String> headers = Arrays.asList("Date/Time", "SKU", "Product", "Warehouse", "Movement Type", "Quantity", "Ref #", "Performed By", "Notes");
        List<List<String>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT t.created_at, p.sku, p.product_name, w.warehouse_name, t.transaction_type, " +
                "t.quantity, t.reference_number, u.full_name as performed_by, t.notes " +
                "FROM stock_transactions t " +
                "JOIN products p ON t.product_id = p.product_id " +
                "JOIN warehouses w ON t.warehouse_id = w.warehouse_id " +
                "LEFT JOIN users u ON t.performed_by_user_id = u.user_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (warehouseId != null && warehouseId > 0) {
            sql.append("AND t.warehouse_id = ? ");
            params.add(warehouseId);
        }
        if (fromDate != null) {
            sql.append("AND t.created_at >= ? ");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            sql.append("AND t.created_at <= ? ");
            params.add(Timestamp.valueOf(toDate.atTime(23, 59, 59)));
        }
        sql.append("ORDER BY t.created_at DESC LIMIT 1000");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(Arrays.asList(
                            String.valueOf(rs.getTimestamp("created_at")),
                            rs.getString("sku"),
                            rs.getString("product_name"),
                            rs.getString("warehouse_name"),
                            rs.getString("transaction_type"),
                            String.valueOf(rs.getInt("quantity")),
                            rs.getString("reference_number") != null ? rs.getString("reference_number") : "-",
                            rs.getString("performed_by") != null ? rs.getString("performed_by") : "System",
                            rs.getString("notes") != null ? rs.getString("notes") : ""
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate stock transactions report", e);
        }
        return new ReportResult(headers, rows);
    }

    private ReportResult generateWarehouseReport() {
        List<String> headers = Arrays.asList("Warehouse Code", "Warehouse Name", "Location", "Facility Manager", "Max Capacity (Units)", "Current Stock Units", "Utilization (%)", "Operational Status");
        List<List<String>> rows = new ArrayList<>();

        String sql = "SELECT code, warehouse_name, location, manager_name, capacity, current_utilization, status FROM warehouses ORDER BY code ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int cap = rs.getInt("capacity");
                int util = rs.getInt("current_utilization");
                double pct = cap > 0 ? (double) util / cap * 100.0 : 0.0;
                rows.add(Arrays.asList(
                        rs.getString("code"),
                        rs.getString("warehouse_name"),
                        rs.getString("location"),
                        rs.getString("manager_name"),
                        String.valueOf(cap),
                        String.valueOf(util),
                        String.format("%.1f%%", pct),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate warehouse utilization report", e);
        }
        return new ReportResult(headers, rows);
    }
}
