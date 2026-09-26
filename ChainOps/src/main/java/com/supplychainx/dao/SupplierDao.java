package com.supplychainx.dao;

import com.supplychainx.model.Supplier;
import java.util.List;
import java.util.Optional;

public interface SupplierDao {
    Optional<Supplier> findById(int supplierId);
    List<Supplier> findAll();
    List<Supplier> searchAndFilter(String query, String status);
    int insert(Supplier supplier);
    boolean update(Supplier supplier);
    boolean delete(int supplierId);
    int countPurchaseOrdersBySupplierId(int supplierId);
}
