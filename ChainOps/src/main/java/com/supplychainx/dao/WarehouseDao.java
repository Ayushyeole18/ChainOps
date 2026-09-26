package com.supplychainx.dao;

import com.supplychainx.model.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseDao {
    Optional<Warehouse> findById(int warehouseId);
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findAll();
    List<Warehouse> search(String query);
    int insert(Warehouse warehouse);
    boolean update(Warehouse warehouse);
    boolean delete(int warehouseId);
    void updateUtilization(int warehouseId);
}
