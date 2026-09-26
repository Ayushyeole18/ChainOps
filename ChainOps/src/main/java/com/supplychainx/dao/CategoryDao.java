package com.supplychainx.dao;

import com.supplychainx.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    Optional<Category> findById(int categoryId);
    Optional<Category> findByName(String categoryName);
    Optional<Category> findByCode(String code);
    List<Category> findAll();
    List<Category> search(String query);
    int insert(Category category);
    boolean update(Category category);
    boolean delete(int categoryId);
    int countProductsByCategoryId(int categoryId);
}
