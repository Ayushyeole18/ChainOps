package com.supplychainx.dao;

import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.exception.DatabaseException;
import com.supplychainx.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDaoImpl implements SupplierDao {

    @Override
    public Optional<Supplier> findById(int supplierId) {
        String sql = "SELECT supplier_id, supplier_name, contact_person, email, phone, address, city, country, status, created_at " +
                     "FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding supplier by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT supplier_id, supplier_name, contact_person, email, phone, address, city, country, status, created_at " +
                     "FROM suppliers ORDER BY supplier_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching suppliers", e);
        }
        return list;
    }

    @Override
    public List<Supplier> searchAndFilter(String query, String status) {
        List<Supplier> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT supplier_id, supplier_name, contact_person, email, phone, address, city, country, status, created_at ")
                .append("FROM suppliers WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(supplier_name) LIKE ? OR LOWER(contact_person) LIKE ? OR LOWER(city) LIKE ? OR LOWER(country) LIKE ?) ");
            String pat = "%" + query.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND status = ? ");
            params.add(status.toUpperCase());
        }

        sql.append("ORDER BY supplier_name ASC");

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
            throw new DatabaseException("Error searching suppliers", e);
        }
        return list;
    }

    @Override
    public int insert(Supplier supplier) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, email, phone, address, city, country, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getSupplierName().trim());
            ps.setString(2, supplier.getContactPerson().trim());
            ps.setString(3, supplier.getEmail().trim());
            ps.setString(4, supplier.getPhone().trim());
            ps.setString(5, supplier.getAddress().trim());
            ps.setString(6, supplier.getCity().trim());
            ps.setString(7, supplier.getCountry().trim());
            ps.setString(8, supplier.getStatus() != null ? supplier.getStatus() : "ACTIVE");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    supplier.setSupplierId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating supplier: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE suppliers SET supplier_name = ?, contact_person = ?, email = ?, phone = ?, " +
                     "address = ?, city = ?, country = ?, status = ? WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getSupplierName().trim());
            ps.setString(2, supplier.getContactPerson().trim());
            ps.setString(3, supplier.getEmail().trim());
            ps.setString(4, supplier.getPhone().trim());
            ps.setString(5, supplier.getAddress().trim());
            ps.setString(6, supplier.getCity().trim());
            ps.setString(7, supplier.getCountry().trim());
            ps.setString(8, supplier.getStatus());
            ps.setInt(9, supplier.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete supplier: Existing purchase orders reference this supplier.", e);
        }
    }

    @Override
    public int countPurchaseOrdersBySupplierId(int supplierId) {
        String sql = "SELECT COUNT(*) FROM purchase_orders WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting POs for supplier", e);
        }
        return 0;
    }

    private Supplier mapResultSet(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setCity(rs.getString("city"));
        s.setCountry(rs.getString("country"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
