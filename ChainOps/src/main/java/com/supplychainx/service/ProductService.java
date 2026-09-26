package com.supplychainx.service;

import com.supplychainx.dao.ProductDao;
import com.supplychainx.dao.ProductDaoImpl;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.Product;
import com.supplychainx.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductDao productDao;

    public ProductService() {
        this.productDao = new ProductDaoImpl();
    }

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public void validateProduct(Product product, boolean isNew) {
        ValidationUtil.requireNonEmpty(product.getProductName(), "Product Name");
        ValidationUtil.validateSku(product.getSku());
        ValidationUtil.validatePrice(product.getUnitPrice(), "Unit Price");
        ValidationUtil.validateNonNegativeQuantity(product.getReorderLevel(), "Reorder Level");
        if (product.getCategoryId() <= 0) {
            throw new ValidationException("A valid Category must be selected.");
        }

        // Validate uniqueness of SKU
        Integer excludeId = isNew ? null : product.getProductId();
        if (!productDao.isSkuUnique(product.getSku(), excludeId)) {
            throw new ValidationException("SKU '" + product.getSku() + "' is already assigned to another product.");
        }
    }

    public int createProduct(Product product) {
        validateProduct(product, true);
        return productDao.insert(product);
    }

    public boolean updateProduct(Product product) {
        validateProduct(product, false);
        return productDao.update(product);
    }

    public boolean deleteProduct(int productId) {
        return productDao.delete(productId);
    }

    public Optional<Product> getProductById(int productId) {
        return productDao.findById(productId);
    }

    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    public List<Product> searchAndFilter(String query, Integer categoryId, String status) {
        return productDao.searchAndFilter(query, categoryId, status);
    }
}
