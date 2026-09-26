package com.supplychainx.ai.feature;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesFeatureExtractor {

    public static class ProductDemandFeatures {
        public int productId;
        public List<Double> dailyQuantities;
        public double meanDaily;
        public double stdDev;
        public double recent7dVelocity;
        public double trendGradient;

        public ProductDemandFeatures(int productId, List<Double> dailyQuantities,
                                     double meanDaily, double stdDev,
                                     double recent7dVelocity, double trendGradient) {
            this.productId = productId;
            this.dailyQuantities = dailyQuantities;
            this.meanDaily = meanDaily;
            this.stdDev = stdDev;
            this.recent7dVelocity = recent7dVelocity;
            this.trendGradient = trendGradient;
        }
    }

    /**
     * Extracts statistical time-series features from raw order history for a product.
     */
    public static ProductDemandFeatures extract(int productId, List<Double> historicalSeries) {
        if (historicalSeries == null || historicalSeries.isEmpty()) {
            return new ProductDemandFeatures(productId, new ArrayList<>(), 0.0, 0.0, 0.0, 0.0);
        }

        int n = historicalSeries.size();
        double sum = 0;
        for (double val : historicalSeries) sum += val;
        double mean = sum / n;

        // Calculate variance & std deviation
        double varianceSum = 0;
        for (double val : historicalSeries) {
            varianceSum += Math.pow(val - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / n);

        // Recent 7-period velocity (or last available window)
        int recentCount = Math.min(7, n);
        double recentSum = 0;
        for (int i = n - recentCount; i < n; i++) {
            recentSum += historicalSeries.get(i);
        }
        double recentVelocity = recentSum / recentCount;

        // Trend gradient: difference between second half mean and first half mean
        double trendGradient = 0.0;
        if (n >= 4) {
            int mid = n / 2;
            double firstHalfSum = 0;
            for (int i = 0; i < mid; i++) firstHalfSum += historicalSeries.get(i);
            double secondHalfSum = 0;
            for (int i = mid; i < n; i++) secondHalfSum += historicalSeries.get(i);
            trendGradient = (secondHalfSum / (n - mid)) - (firstHalfSum / mid);
        }

        return new ProductDemandFeatures(productId, historicalSeries, mean, stdDev, recentVelocity, trendGradient);
    }
}
