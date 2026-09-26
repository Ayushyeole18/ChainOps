package com.supplychainx.service;

import com.supplychainx.dao.SupplierDao;
import com.supplychainx.dao.SupplierDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.Supplier;
import com.supplychainx.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class SupplierService {

    private final SupplierDao supplierDao;

    public SupplierService() {
        this.supplierDao = new SupplierDaoImpl();
    }

    public SupplierService(SupplierDao supplierDao) {
        this.supplierDao = supplierDao;
    }

    public void validateSupplier(Supplier supplier) {
        ValidationUtil.requireNonEmpty(supplier.getSupplierName(), "Supplier Name");
        ValidationUtil.requireNonEmpty(supplier.getContactPerson(), "Contact Person");
        ValidationUtil.validateEmail(supplier.getEmail());
        ValidationUtil.validatePhone(supplier.getPhone());
        ValidationUtil.requireNonEmpty(supplier.getAddress(), "Address");
        ValidationUtil.requireNonEmpty(supplier.getCity(), "City");
        ValidationUtil.requireNonEmpty(supplier.getCountry(), "Country");
    }

    public int createSupplier(Supplier supplier) {
        validateSupplier(supplier);
        return supplierDao.insert(supplier);
    }

    public boolean updateSupplier(Supplier supplier) {
        validateSupplier(supplier);
        return supplierDao.update(supplier);
    }

    public boolean deleteSupplier(int supplierId) {
        int orderCount = supplierDao.countPurchaseOrdersBySupplierId(supplierId);
        if (orderCount > 0) {
            throw new BusinessRuleException("Cannot delete supplier: " + orderCount +
                    " purchase orders are linked to this supplier. Mark as INACTIVE instead.");
        }
        return supplierDao.delete(supplierId);
    }

    public Optional<Supplier> getSupplierById(int supplierId) {
        return supplierDao.findById(supplierId);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierDao.findAll();
    }

    public List<Supplier> searchAndFilter(String query, String status) {
        return supplierDao.searchAndFilter(query, status);
    }
}
