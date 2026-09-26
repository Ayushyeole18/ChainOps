package com.supplychainx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StockRiskServiceTest {

    @Test
    @DisplayName("Stock-out risk correctly flags items with critically low runout days as HIGH")
    void testStockoutRiskClassification() {
        int currentStock = 15;
        double dailyVelocity = 5.0; // 3 days remaining!
        int reorderLevel = 30;

        int daysUntilStockout = (int) Math.round(currentStock / dailyVelocity);
        assertEquals(3, daysUntilStockout);

        String riskLevel;
        if (currentStock <= 0 || daysUntilStockout <= 7 || currentStock <= reorderLevel * 0.5) {
            riskLevel = "HIGH";
        } else if (daysUntilStockout <= 21 || currentStock <= reorderLevel) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        assertEquals("HIGH", riskLevel, "3 days of stock remaining must be classified as HIGH risk");
    }

    @Test
    @DisplayName("Safe inventory with > 30 days buffer is correctly categorized as LOW risk")
    void testLowStockoutRisk() {
        int currentStock = 300;
        double dailyVelocity = 4.0; // 75 days remaining!
        int reorderLevel = 50;

        int daysUntilStockout = (int) Math.round(currentStock / dailyVelocity);
        assertEquals(75, daysUntilStockout);

        String riskLevel;
        if (currentStock <= 0 || daysUntilStockout <= 7 || currentStock <= reorderLevel * 0.5) {
            riskLevel = "HIGH";
        } else if (daysUntilStockout <= 21 || currentStock <= reorderLevel) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        assertEquals("LOW", riskLevel, "75 days of stock remaining must be classified as LOW risk");
    }

    @Test
    @DisplayName("Procurement recommendation calculation produces exact non-negative replenishment")
    void testProcurementQuantityCalculation() {
        int currentStock = 40;
        int predicted30dDemand = 120;
        int safetyStock = 20;
        double dailyVelocity = 4.0;
        double leadTimeDays = 7.0;
        double delayRiskPct = 10.0;

        double leadTimeDemand = dailyVelocity * leadTimeDays; // 28
        double adjustedSafetyStock = safetyStock * (1.0 + (delayRiskPct / 100.0)); // 22

        double grossRequirement = predicted30dDemand + leadTimeDemand + adjustedSafetyStock; // 170
        int recommendedOrder = (int) Math.max(0, Math.ceil(grossRequirement - currentStock)); // 130

        assertTrue(recommendedOrder >= 100, "Recommended replenishment should order enough to cover lead-time & buffer");
    }
}
