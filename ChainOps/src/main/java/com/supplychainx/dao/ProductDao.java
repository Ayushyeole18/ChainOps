package com.supplychainx.dao;

import com.supplychainx.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Optional<Product> findById(int productId);
    Optional<Product> findBySku(String sku);
    List<Product> findAll();
    List<Product> searchAndFilter(String query, Integer categoryId, String status);
    int insert(Product product);
    boolean update(Product product);
    boolean delete(int productId);
    boolean isSkuUnique(String sku, Integer excludeProductId);
}
