package com.supplychainx;

import com.supplychainx.model.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryValidationTest {

    @Test
    @DisplayName("Stock status is correctly evaluated as OUT OF STOCK when available quantity is zero")
    public void testOutOfStockCalculation() {
        InventoryItem item = new InventoryItem();
        item.setQuantityAvailable(0);
        item.setReorderLevel(15);
        assertEquals("OUT OF STOCK", item.getStockStatus());
    }

    @Test
    @DisplayName("Stock status is correctly evaluated as LOW STOCK when available is less than or equal to reorder level")
    public void testLowStockCalculation() {
        InventoryItem item = new InventoryItem();
        item.setReorderLevel(20);

        item.setQuantityAvailable(20);
        assertEquals("LOW STOCK", item.getStockStatus());

        item.setQuantityAvailable(5);
        assertEquals("LOW STOCK", item.getStockStatus());
    }

    @Test
    @DisplayName("Stock status is correctly evaluated as IN STOCK when available exceeds reorder level")
    public void testInStockCalculation() {
        InventoryItem item = new InventoryItem();
        item.setReorderLevel(20);
        item.setQuantityAvailable(21);
        assertEquals("IN STOCK", item.getStockStatus());

        item.setQuantityAvailable(500);
        assertEquals("IN STOCK", item.getStockStatus());
    }

    @Test
    @DisplayName("Total physical stock correctly aggregates available and reserved units")
    public void testTotalPhysicalQuantity() {
        InventoryItem item = new InventoryItem();
        item.setQuantityAvailable(150);
        item.setQuantityReserved(35);
        assertEquals(185, item.getTotalPhysicalQuantity());
    }
}
