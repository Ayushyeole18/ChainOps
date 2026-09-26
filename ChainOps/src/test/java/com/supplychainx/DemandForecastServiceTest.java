package com.supplychainx;

import com.supplychainx.ai.feature.TimeSeriesFeatureExtractor;
import com.supplychainx.ai.util.RegressionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DemandForecastServiceTest {

    @Test
    @DisplayName("OLS Linear Regression accurately calculates positive trend slope and intercept")
    void testLinearRegressionTrend() {
        // Linear sequence: y = 2x + 10 with slight noise
        List<Double> xVals = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);
        List<Double> yVals = List.of(12.0, 14.1, 15.9, 18.0, 20.2, 22.0, 24.1);

        RegressionUtil.RegressionResult result = RegressionUtil.fitLinearModel(xVals, yVals);

        assertTrue(result.slope > 1.8 && result.slope < 2.2, "Slope should be approximately 2.0");
        assertTrue(result.intercept > 9.5 && result.intercept < 10.5, "Intercept should be approximately 10.0");
        assertTrue(result.rSquared > 0.95, "R-squared should indicate high linearity (> 0.95)");
        assertTrue(result.mae >= 0.0 && result.mae < 1.0, "MAE should be less than 1.0");

        // Forecast future period (e.g. period 14)
        double futurePrediction = result.predict(14.0);
        assertTrue(futurePrediction > 35.0 && futurePrediction < 40.0, "Day 14 prediction should be near 38 units");
    }

    @Test
    @DisplayName("Feature extractor extracts mean, velocity, and gradient accurately")
    void testTimeSeriesFeatureExtraction() {
        List<Double> historicalSeries = List.of(10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 22.0, 24.0);

        TimeSeriesFeatureExtractor.ProductDemandFeatures features =
            TimeSeriesFeatureExtractor.extract(1, historicalSeries);

        assertEquals(1, features.productId);
        assertEquals(17.0, features.meanDaily, 0.01, "Mean should equal 17.0");
        assertTrue(features.recent7dVelocity > 17.0, "Recent 7-day velocity should capture upward climb");
        assertTrue(features.trendGradient > 0, "Trend gradient must be positive for growing demand");
    }

    @Test
    @DisplayName("Weighted moving average prioritizes recent demand observations")
    void testWeightedMovingAverage() {
        // Sudden spike in recent periods: 5, 5, 5, 20
        List<Double> series = List.of(5.0, 5.0, 5.0, 20.0);
        double simpleAvg = (5.0 + 5.0 + 5.0 + 20.0) / 4.0; // 8.75
        double wma = RegressionUtil.weightedMovingAverage(series);

        assertTrue(wma > simpleAvg, "WMA must be higher than simple mean when recent sales spike");
    }
}
