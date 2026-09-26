package com.supplychainx;

import com.supplychainx.dao.ProductDao;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.Product;
import com.supplychainx.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ProductValidationTest {

    private ProductService productService;
    private Product testProduct;

    @BeforeEach
    public void setup() {
        // Mock product dao for unit test
        ProductDao mockDao = new ProductDao() {
            @Override public Optional<Product> findById(int productId) { return Optional.empty(); }
            @Override public Optional<Product> findBySku(String sku) { return Optional.empty(); }
            @Override public java.util.List<Product> findAll() { return java.util.Collections.emptyList(); }
            @Override public java.util.List<Product> searchAndFilter(String query, Integer categoryId, String status) { return java.util.Collections.emptyList(); }
            @Override public int insert(Product product) { return 101; }
            @Override public boolean update(Product product) { return true; }
            @Override public boolean delete(int productId) { return true; }
            @Override public boolean isSkuUnique(String sku, Integer excludeProductId) {
                // "SKU-EXISTS" simulates an already taken SKU
                return !"SKU-EXISTS".equalsIgnoreCase(sku);
            }
        };

        productService = new ProductService(mockDao);

        testProduct = new Product();
        testProduct.setSku("SKU-TEST-001");
        testProduct.setProductName("High Strength Industrial Fastener");
        testProduct.setCategoryId(1);
        testProduct.setUnitPrice(new BigDecimal("12.50"));
        testProduct.setReorderLevel(25);
        testProduct.setUnit("Boxes");
        testProduct.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Valid product passes validation and inserts successfully")
    public void testValidProduct() {
        assertDoesNotThrow(() -> productService.validateProduct(testProduct, true));
        int id = productService.createProduct(testProduct);
        assertEquals(101, id);
    }

    @Test
    @DisplayName("Empty product name throws ValidationException")
    public void testEmptyProductName() {
        testProduct.setProductName("   ");
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.validateProduct(testProduct, true));
        assertTrue(ex.getMessage().contains("Product Name"));
    }

    @Test
    @DisplayName("Invalid SKU format throws ValidationException")
    public void testInvalidSkuFormat() {
        testProduct.setSku("AB"); // Too short
        assertThrows(ValidationException.class, () -> productService.validateProduct(testProduct, true));

        testProduct.setSku("INVALID SKU WITH SPACES!");
        assertThrows(ValidationException.class, () -> productService.validateProduct(testProduct, true));
    }

    @Test
    @DisplayName("Duplicate SKU throws ValidationException")
    public void testDuplicateSku() {
        testProduct.setSku("SKU-EXISTS");
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.validateProduct(testProduct, true));
        assertTrue(ex.getMessage().contains("already assigned"));
    }

    @Test
    @DisplayName("Negative unit price throws ValidationException")
    public void testNegativePrice() {
        testProduct.setUnitPrice(new BigDecimal("-5.00"));
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.validateProduct(testProduct, true));
        assertTrue(ex.getMessage().contains("negative"));
    }

    @Test
    @DisplayName("Negative reorder level throws ValidationException")
    public void testNegativeReorderLevel() {
        testProduct.setReorderLevel(-1);
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.validateProduct(testProduct, true));
        assertTrue(ex.getMessage().contains("negative"));
    }
}
