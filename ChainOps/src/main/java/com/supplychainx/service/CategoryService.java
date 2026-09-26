package com.supplychainx.service;

import com.supplychainx.dao.CategoryDao;
import com.supplychainx.dao.CategoryDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.Category;
import com.supplychainx.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService() {
        this.categoryDao = new CategoryDaoImpl();
    }

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public void validateCategory(Category category, boolean isNew) {
        ValidationUtil.requireNonEmpty(category.getCategoryName(), "Category Name");
        ValidationUtil.requireNonEmpty(category.getCode(), "Category Code");

        if (isNew) {
            if (categoryDao.findByName(category.getCategoryName()).isPresent()) {
                throw new ValidationException("Category with name '" + category.getCategoryName() + "' already exists.");
            }
            if (categoryDao.findByCode(category.getCode()).isPresent()) {
                throw new ValidationException("Category with code '" + category.getCode() + "' already exists.");
            }
        }
    }

    public int createCategory(Category category) {
        validateCategory(category, true);
        return categoryDao.insert(category);
    }

    public boolean updateCategory(Category category) {
        validateCategory(category, false);
        return categoryDao.update(category);
    }

    public boolean deleteCategory(int categoryId) {
        int linkedProducts = categoryDao.countProductsByCategoryId(categoryId);
        if (linkedProducts > 0) {
            throw new BusinessRuleException("Cannot delete category: " + linkedProducts +
                    " products are currently assigned to it. Reassign or remove products first.");
        }
        return categoryDao.delete(categoryId);
    }

    public Optional<Category> getCategoryById(int categoryId) {
        return categoryDao.findById(categoryId);
    }

    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    public List<Category> searchCategories(String query) {
        return categoryDao.search(query);
    }
}
