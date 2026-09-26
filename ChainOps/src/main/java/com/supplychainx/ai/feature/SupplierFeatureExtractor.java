package com.supplychainx.ai.feature;

import java.util.List;

public class SupplierFeatureExtractor {

    public static class SupplierPerformanceFeatures {
        public int supplierId;
        public int totalOrders;
        public int delayedOrders;
        public double avgLeadTimeDays;
        public double maxLeadTimeDays;
        public double leadTimeStdDev;
        public double delayRatio;

        public SupplierPerformanceFeatures(int supplierId, int totalOrders, int delayedOrders,
                                           double avgLeadTimeDays, double maxLeadTimeDays,
                                           double leadTimeStdDev, double delayRatio) {
            this.supplierId = supplierId;
            this.totalOrders = totalOrders;
            this.delayedOrders = delayedOrders;
            this.avgLeadTimeDays = avgLeadTimeDays;
            this.maxLeadTimeDays = maxLeadTimeDays;
            this.leadTimeStdDev = leadTimeStdDev;
            this.delayRatio = delayRatio;
        }
    }

    public static SupplierPerformanceFeatures extract(int supplierId, List<Integer> leadTimeDaysList, int delayedCount) {
        if (leadTimeDaysList == null || leadTimeDaysList.isEmpty()) {
            return new SupplierPerformanceFeatures(supplierId, 0, 0, 7.0, 7.0, 0.0, 0.0);
        }

        int totalOrders = leadTimeDaysList.size();
        double sum = 0;
        double max = 0;

        for (int days : leadTimeDaysList) {
            sum += days;
            if (days > max) max = days;
        }

        double avgLeadTime = sum / totalOrders;

        double varianceSum = 0;
        for (int days : leadTimeDaysList) {
            varianceSum += Math.pow(days - avgLeadTime, 2);
        }
        double stdDev = Math.sqrt(varianceSum / totalOrders);
        double delayRatio = (totalOrders > 0) ? ((double) delayedCount / totalOrders) : 0.0;

        return new SupplierPerformanceFeatures(supplierId, totalOrders, delayedCount, avgLeadTime, max, stdDev, delayRatio);
    }
}
