package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl implements CategoryDao {

    @Override
    public Optional<Category> findById(int categoryId) {
        String sql = "SELECT c.category_id, c.category_name, c.code, c.description, c.status, c.created_at, " +
                     "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.category_id) as prod_count " +
                     "FROM categories c WHERE c.category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding category by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String categoryName) {
        String sql = "SELECT c.category_id, c.category_name, c.code, c.description, c.status, c.created_at, " +
                     "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.category_id) as prod_count " +
                     "FROM categories c WHERE LOWER(c.category_name) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding category by name", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByCode(String code) {
        String sql = "SELECT c.category_id, c.category_name, c.code, c.description, c.status, c.created_at, " +
                     "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.category_id) as prod_count " +
                     "FROM categories c WHERE LOWER(c.code) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding category by code", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT c.category_id, c.category_name, c.code, c.description, c.status, c.created_at, " +
                     "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.category_id) as prod_count " +
                     "FROM categories c ORDER BY c.category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching categories", e);
        }
        return list;
    }

    @Override
    public List<Category> search(String query) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT c.category_id, c.category_name, c.code, c.description, c.status, c.created_at, " +
                     "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.category_id) as prod_count " +
                     "FROM categories c WHERE LOWER(c.category_name) LIKE ? OR LOWER(c.code) LIKE ? ORDER BY c.category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim().toLowerCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching categories", e);
        }
        return list;
    }

    @Override
    public int insert(Category category) {
        String sql = "INSERT INTO categories (category_name, code, description, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getCode().toUpperCase());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getStatus() != null ? category.getStatus() : "ACTIVE");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    category.setCategoryId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating category: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public boolean update(Category category) {
        String sql = "UPDATE categories SET category_name = ?, code = ?, description = ?, status = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getCode().toUpperCase());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getStatus());
            ps.setInt(5, category.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating category: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting category: " + e.getMessage(), e);
        }
    }

    @Override
    public int countProductsByCategoryId(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting products for category", e);
        }
        return 0;
    }

    private Category mapResultSet(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setProductCount(rs.getInt("prod_count"));
        return c;
    }
}
