package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.DashboardMetrics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DashboardDaoImpl implements DashboardDao {

    @Override
    public DashboardMetrics fetchDashboardMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Total Products
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'")) {
                if (rs.next()) metrics.setTotalProducts(rs.getInt(1));
            }

            // 2. Total Inventory Units
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(quantity_available), 0) FROM inventory")) {
                if (rs.next()) metrics.setTotalInventoryUnits(rs.getInt(1));
            }

            // 3. Low Stock Items Count
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM inventory i JOIN products p ON i.product_id = p.product_id WHERE i.quantity_available <= p.reorder_level")) {
                if (rs.next()) metrics.setLowStockItemsCount(rs.getInt(1));
            }

            // 4. Active Suppliers
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM suppliers WHERE status = 'ACTIVE'")) {
                if (rs.next()) metrics.setActiveSuppliersCount(rs.getInt(1));
            }

            // 5. Pending Purchase Orders
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM purchase_orders WHERE status IN ('DRAFT', 'PENDING', 'APPROVED')")) {
                if (rs.next()) metrics.setPendingPurchaseOrdersCount(rs.getInt(1));
            }

            // 6. Pending Sales Orders
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sales_orders WHERE status IN ('PENDING', 'CONFIRMED', 'PROCESSING')")) {
                if (rs.next()) metrics.setPendingSalesOrdersCount(rs.getInt(1));
            }

            // 7. Active Shipments
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shipments WHERE status IN ('READY', 'IN_TRANSIT', 'OUT_FOR_DELIVERY')")) {
                if (rs.next()) metrics.setActiveShipmentsCount(rs.getInt(1));
            }

            // 8. Total Warehouses
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM warehouses WHERE status = 'ACTIVE'")) {
                if (rs.next()) metrics.setTotalWarehousesCount(rs.getInt(1));
            }

            // Chart 1: Inventory by Category
            Map<String, Integer> invByCat = new HashMap<>();
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT c.category_name, COALESCE(SUM(i.quantity_available), 0) as total_qty " +
                    "FROM categories c " +
                    "LEFT JOIN products p ON c.category_id = p.category_id " +
                    "LEFT JOIN inventory i ON p.product_id = i.product_id " +
                    "GROUP BY c.category_id, c.category_name ORDER BY total_qty DESC")) {
                while (rs.next()) {
                    invByCat.put(rs.getString("category_name"), rs.getInt("total_qty"));
                }
            }
            metrics.setInventoryByCategory(invByCat);

            // Chart 2: Order Status Distribution
            Map<String, Integer> orderDist = new HashMap<>();
            try (ResultSet rs = stmt.executeQuery("SELECT status, COUNT(*) as cnt FROM sales_orders GROUP BY status")) {
                while (rs.next()) {
                    orderDist.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
            metrics.setOrderStatusDistribution(orderDist);

            // Chart 3: Monthly Sales
            Map<String, Double> monthlySales = new HashMap<>();
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT DATE_FORMAT(order_date, '%b %Y') as m_month, COALESCE(SUM(total_amount), 0) as total_rev " +
                    "FROM sales_orders WHERE status != 'CANCELLED' " +
                    "GROUP BY m_month, YEAR(order_date), MONTH(order_date) " +
                    "ORDER BY YEAR(order_date) ASC, MONTH(order_date) ASC LIMIT 6")) {
                while (rs.next()) {
                    monthlySales.put(rs.getString("m_month"), rs.getDouble("total_rev"));
                }
            }
            metrics.setMonthlySales(monthlySales);

            // Chart 4: Warehouse Inventory
            Map<String, Integer> whInv = new HashMap<>();
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT w.warehouse_name, COALESCE(SUM(i.quantity_available), 0) as total_qty " +
                    "FROM warehouses w " +
                    "LEFT JOIN inventory i ON w.warehouse_id = i.warehouse_id " +
                    "GROUP BY w.warehouse_id, w.warehouse_name")) {
                while (rs.next()) {
                    whInv.put(rs.getString("warehouse_name"), rs.getInt("total_qty"));
                }
            }
            metrics.setWarehouseInventory(whInv);

        } catch (SQLException e) {
            throw new DatabaseException("Error gathering dashboard metrics", e);
        }

        return metrics;
    }
}
