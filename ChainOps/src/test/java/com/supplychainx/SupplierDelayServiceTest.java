package com.supplychainx;

import com.supplychainx.ai.util.ProbabilityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SupplierDelayServiceTest {

    @Test
    @DisplayName("Bayesian delay probability correctly incorporates late shipment ratio and prior")
    void testBayesianDelayProbability() {
        int delayedOrders = 5;
        int totalOrders = 15;
        double priorRate = 0.15;
        double priorWeight = 4.0;

        double prob = ProbabilityUtil.calculateDelayProbability(delayedOrders, totalOrders, priorRate, priorWeight);

        assertTrue(prob > 25.0 && prob < 35.0, "Probability should be around 29.5%");

        String category = ProbabilityUtil.classifyDelayRisk(prob);
        assertEquals("CRITICAL", category, "A 30% delay probability must be categorized as CRITICAL");
    }

    @Test
    @DisplayName("Reliable supplier with zero late orders gets LOW risk category")
    void testZeroDelaySupplier() {
        int delayedOrders = 0;
        int totalOrders = 20;

        double prob = ProbabilityUtil.calculateDelayProbability(delayedOrders, totalOrders, 0.15, 4.0);
        assertTrue(prob < 10.0, "Zero delayed orders out of 20 should produce low probability (< 10%)");

        String category = ProbabilityUtil.classifyDelayRisk(prob);
        assertEquals("LOW", category);

        double reliability = ProbabilityUtil.computeReliabilityScore(prob, 5.0);
        assertTrue(reliability > 90.0, "Reliability score should exceed 90 for on-time supplier");
    }

    @Test
    @DisplayName("Sigmoid utility maps values correctly between 0.0 and 1.0")
    void testSigmoid() {
        assertEquals(0.5, ProbabilityUtil.sigmoid(0.0), 0.001);
        assertTrue(ProbabilityUtil.sigmoid(5.0) > 0.99);
        assertTrue(ProbabilityUtil.sigmoid(-5.0) < 0.01);
    }
}
