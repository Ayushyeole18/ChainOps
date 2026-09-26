package com.supplychainx.service;

import com.supplychainx.dao.WarehouseDao;
import com.supplychainx.dao.WarehouseDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.Warehouse;
import com.supplychainx.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class WarehouseService {

    private final WarehouseDao warehouseDao;

    public WarehouseService() {
        this.warehouseDao = new WarehouseDaoImpl();
    }

    public WarehouseService(WarehouseDao warehouseDao) {
        this.warehouseDao = warehouseDao;
    }

    public void validateWarehouse(Warehouse warehouse, boolean isNew) {
        ValidationUtil.requireNonEmpty(warehouse.getWarehouseName(), "Warehouse Name");
        ValidationUtil.requireNonEmpty(warehouse.getCode(), "Warehouse Code");
        ValidationUtil.requireNonEmpty(warehouse.getLocation(), "Location");
        ValidationUtil.requireNonEmpty(warehouse.getManagerName(), "Manager Name");
        ValidationUtil.validatePositiveQuantity(warehouse.getCapacity(), "Capacity");

        if (isNew && warehouseDao.findByCode(warehouse.getCode()).isPresent()) {
            throw new ValidationException("Warehouse code '" + warehouse.getCode() + "' is already in use.");
        }
    }

    public int createWarehouse(Warehouse warehouse) {
        validateWarehouse(warehouse, true);
        return warehouseDao.insert(warehouse);
    }

    public boolean updateWarehouse(Warehouse warehouse) {
        validateWarehouse(warehouse, false);
        return warehouseDao.update(warehouse);
    }

    public boolean deleteWarehouse(int warehouseId) {
        return warehouseDao.delete(warehouseId);
    }

    public Optional<Warehouse> getWarehouseById(int warehouseId) {
        return warehouseDao.findById(warehouseId);
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseDao.findAll();
    }

    public List<Warehouse> searchWarehouses(String query) {
        return warehouseDao.search(query);
    }
}
