package com.supplychainx.ai.util;

import java.util.List;

public class RegressionUtil {

    public static class RegressionResult {
        public final double slope;
        public final double intercept;
        public final double rSquared;
        public final double mae;
        public final double rmse;

        public RegressionResult(double slope, double intercept, double rSquared, double mae, double rmse) {
            this.slope = slope;
            this.intercept = intercept;
            this.rSquared = rSquared;
            this.mae = mae;
            this.rmse = rmse;
        }

        public double predict(double x) {
            return Math.max(0.0, intercept + slope * x);
        }
    }

    /**
     * Fits an Ordinary Least Squares (OLS) linear model on paired (x, y) coordinates.
     */
    public static RegressionResult fitLinearModel(List<Double> xVals, List<Double> yVals) {
        if (xVals == null || yVals == null || xVals.size() != yVals.size() || xVals.size() < 2) {
            // Fallback for minimal data points
            double defaultMean = (yVals != null && !yVals.isEmpty()) ? yVals.get(0) : 0.0;
            return new RegressionResult(0.0, defaultMean, 0.0, 0.0, 0.0);
        }

        int n = xVals.size();
        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += xVals.get(i);
            sumY += yVals.get(i);
        }
        double meanX = sumX / n;
        double meanY = sumY / n;

        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < n; i++) {
            double dx = xVals.get(i) - meanX;
            double dy = yVals.get(i) - meanY;
            numerator += dx * dy;
            denominator += dx * dx;
        }

        double slope = (denominator != 0) ? (numerator / denominator) : 0.0;
        double intercept = meanY - slope * meanX;

        // Calculate evaluation metrics: MAE, RMSE, R-squared
        double ssRes = 0;
        double ssTot = 0;
        double absErrorSum = 0;

        for (int i = 0; i < n; i++) {
            double actual = yVals.get(i);
            double predicted = intercept + slope * xVals.get(i);
            double error = actual - predicted;

            absErrorSum += Math.abs(error);
            ssRes += error * error;
            ssTot += Math.pow(actual - meanY, 2);
        }

        double mae = absErrorSum / n;
        double rmse = Math.sqrt(ssRes / n);
        double rSquared = (ssTot > 0) ? Math.max(0.0, 1.0 - (ssRes / ssTot)) : 1.0;

        return new RegressionResult(slope, intercept, rSquared, mae, rmse);
    }

    /**
     * Calculates Exponential Smoothing forecast for time-series demand.
     */
    public static double exponentialSmoothing(List<Double> series, double alpha) {
        if (series == null || series.isEmpty()) return 0.0;
        double smoothed = series.get(0);
        for (int i = 1; i < series.size(); i++) {
            smoothed = alpha * series.get(i) + (1.0 - alpha) * smoothed;
        }
        return smoothed;
    }

    /**
     * Calculates Weighted Moving Average giving higher weights to recent observations.
     */
    public static double weightedMovingAverage(List<Double> series) {
        if (series == null || series.isEmpty()) return 0.0;
        int n = series.size();
        double weightSum = 0;
        double weightedVal = 0;

        for (int i = 0; i < n; i++) {
            double weight = i + 1; // 1, 2, 3... higher for recent
            weightedVal += series.get(i) * weight;
            weightSum += weight;
        }

        return (weightSum > 0) ? (weightedVal / weightSum) : 0.0;
    }
}
