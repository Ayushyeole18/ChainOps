package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {

    private static final String SELECT_BASE =
            "SELECT p.product_id, p.sku, p.product_name, p.category_id, c.category_name, p.description, " +
            "p.unit_price, p.reorder_level, p.unit, p.status, p.created_at, " +
            "COALESCE((SELECT SUM(i.quantity_available) FROM inventory i WHERE i.product_id = p.product_id), 0) AS total_stock " +
            "FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.category_id ";

    @Override
    public Optional<Product> findById(int productId) {
        String sql = SELECT_BASE + "WHERE p.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        String sql = SELECT_BASE + "WHERE LOWER(p.sku) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by SKU", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY p.product_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching products", e);
        }
        return list;
    }

    @Override
    public List<Product> searchAndFilter(String query, Integer categoryId, String status) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(p.product_name) LIKE ? OR LOWER(p.sku) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND p.status = ? ");
            params.add(status.toUpperCase());
        }

        sql.append("ORDER BY p.product_name ASC");

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
            throw new DatabaseException("Error searching and filtering products", e);
        }
        return list;
    }

    @Override
    public int insert(Product product) {
        String sql = "INSERT INTO products (sku, product_name, category_id, description, unit_price, reorder_level, unit, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getSku().toUpperCase().trim());
            ps.setString(2, product.getProductName().trim());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getUnitPrice());
            ps.setInt(6, product.getReorderLevel());
            ps.setString(7, product.getUnit());
            ps.setString(8, product.getStatus() != null ? product.getStatus() : "ACTIVE");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    product.setProductId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating product: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET sku = ?, product_name = ?, category_id = ?, description = ?, " +
                     "unit_price = ?, reorder_level = ?, unit = ?, status = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getSku().toUpperCase().trim());
            ps.setString(2, product.getProductName().trim());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setBigDecimal(5, product.getUnitPrice());
            ps.setInt(6, product.getReorderLevel());
            ps.setString(7, product.getUnit());
            ps.setString(8, product.getStatus());
            ps.setInt(9, product.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete product: It is referenced by existing inventory or orders.", e);
        }
    }

    @Override
    public boolean isSkuUnique(String sku, Integer excludeProductId) {
        String sql = "SELECT COUNT(*) FROM products WHERE LOWER(sku) = LOWER(?) " +
                     (excludeProductId != null ? "AND product_id != ?" : "");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku.trim());
            if (excludeProductId != null) {
                ps.setInt(2, excludeProductId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error verifying SKU uniqueness", e);
        }
        return true;
    }

    private Product mapResultSet(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setSku(rs.getString("sku"));
        p.setProductName(rs.getString("product_name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setDescription(rs.getString("description"));
        p.setUnitPrice(rs.getBigDecimal("unit_price"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setUnit(rs.getString("unit"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setTotalStock(rs.getInt("total_stock"));
        return p;
    }
}
