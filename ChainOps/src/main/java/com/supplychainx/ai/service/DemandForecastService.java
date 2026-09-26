package com.supplychainx.ai.service;

import com.supplychainx.ai.dao.MlDao;
import com.supplychainx.ai.dao.MlDaoImpl;
import com.supplychainx.ai.feature.TimeSeriesFeatureExtractor;
import com.supplychainx.ai.model.DemandForecast;
import com.supplychainx.ai.util.RegressionUtil;
import com.supplychainx.dao.ProductDao;
import com.supplychainx.dao.ProductDaoImpl;
import com.supplychainx.model.Product;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemandForecastService {

    private final MlDao mlDao;
    private final ProductDao productDao;

    public DemandForecastService() {
        this.mlDao = new MlDaoImpl();
        this.productDao = new ProductDaoImpl();
    }

    public DemandForecastService(MlDao mlDao, ProductDao productDao) {
        this.mlDao = mlDao;
        this.productDao = productDao;
    }

    /**
     * Generates a 30-day demand forecast for a single product using OLS regression & time-series analysis.
     */
    public DemandForecast generateForecast(int productId) {
        Optional<Product> prodOpt = productDao.findById(productId);
        if (prodOpt.isEmpty()) {
            throw new IllegalArgumentException("Product ID " + productId + " not found.");
        }
        Product product = prodOpt.get();

        List<Double> historicalSeries = mlDao.getHistoricalSalesSeries(productId);
        if (historicalSeries.isEmpty()) {
            // Seed reasonable baseline based on product reorder level if no historical orders exist yet
            historicalSeries = List.of(
                (double) product.getReorderLevel() * 0.15,
                (double) product.getReorderLevel() * 0.18,
                (double) product.getReorderLevel() * 0.22,
                (double) product.getReorderLevel() * 0.20,
                (double) product.getReorderLevel() * 0.25
            );
        }

        TimeSeriesFeatureExtractor.ProductDemandFeatures features =
            TimeSeriesFeatureExtractor.extract(productId, historicalSeries);

        // Prepare (x, y) vectors for linear trend regression
        List<Double> xVals = new ArrayList<>();
        List<Double> yVals = new ArrayList<>();
        for (int i = 0; i < historicalSeries.size(); i++) {
            xVals.add((double) (i + 1));
            yVals.add(historicalSeries.get(i));
        }

        RegressionUtil.RegressionResult reg = RegressionUtil.fitLinearModel(xVals, yVals);

        // Blend OLS forecast with weighted moving average for robust projection
        double wmaVelocity = RegressionUtil.weightedMovingAverage(historicalSeries);
        double olsVelocity = Math.max(0.1, reg.predict(historicalSeries.size() + 15));
        double blendedDailyVelocity = (wmaVelocity * 0.4) + (olsVelocity * 0.6);

        int periodDays = 30;
        int predictedQuantity = (int) Math.round(blendedDailyVelocity * periodDays);

        // Confidence interval (+/- 1.96 std dev or based on MAE)
        double errorMargin = Math.max(3.0, (reg.mae > 0 ? reg.mae * periodDays * 0.4 : features.stdDev * Math.sqrt(periodDays)));
        int confidenceLower = (int) Math.max(0, Math.round(predictedQuantity - errorMargin));
        int confidenceUpper = (int) Math.round(predictedQuantity + errorMargin);

        // Determine trend direction
        String trendDirection = "STABLE";
        if (reg.slope > 0.05) {
            trendDirection = "UP";
        } else if (reg.slope < -0.05) {
            trendDirection = "DOWN";
        }

        DemandForecast forecast = new DemandForecast();
        forecast.setProductId(productId);
        forecast.setSku(product.getSku());
        forecast.setProductName(product.getProductName());
        forecast.setForecastPeriodDays(periodDays);
        forecast.setHistoricalDailyAvg(Math.round(features.meanDaily * 100.0) / 100.0);
        forecast.setPredictedQuantity(predictedQuantity);
        forecast.setConfidenceLower(confidenceLower);
        forecast.setConfidenceUpper(confidenceUpper);
        forecast.setTrendDirection(trendDirection);
        forecast.setTrendSlope(Math.round(reg.slope * 10000.0) / 10000.0);
        forecast.setMae(Math.round(reg.mae * 100.0) / 100.0);
        forecast.setRmse(Math.round(reg.rmse * 100.0) / 100.0);
        forecast.setCalculatedAt(new Timestamp(System.currentTimeMillis()));

        // Persist to database
        mlDao.saveDemandForecast(forecast);
        return forecast;
    }

    /**
     * Retrains and generates forecasts for all active products in the catalog.
     */
    public List<DemandForecast> trainAndForecastAll() {
        List<Product> products = productDao.findAll();
        List<DemandForecast> forecasts = new ArrayList<>();
        for (Product p : products) {
            forecasts.add(generateForecast(p.getProductId()));
        }
        return forecasts;
    }

    public List<DemandForecast> getCachedForecasts() {
        List<DemandForecast> list = mlDao.getAllDemandForecasts();
        if (list.isEmpty()) {
            return trainAndForecastAll();
        }
        return list;
    }

    public Optional<DemandForecast> getForecastForProduct(int productId) {
        Optional<DemandForecast> opt = mlDao.getDemandForecastByProductId(productId);
        if (opt.isPresent()) return opt;
        return Optional.of(generateForecast(productId));
    }
}
