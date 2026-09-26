package com.supplychainx.ai.dao;

import com.supplychainx.ai.model.DemandForecast;
import com.supplychainx.ai.model.ModelTrainingMetadata;
import com.supplychainx.ai.model.StockRiskPrediction;
import com.supplychainx.ai.model.SupplierDelayPrediction;

import java.util.List;
import java.util.Optional;

public interface MlDao {
    void saveDemandForecast(DemandForecast forecast);
    List<DemandForecast> getAllDemandForecasts();
    Optional<DemandForecast> getDemandForecastByProductId(int productId);

    void saveStockRiskPrediction(StockRiskPrediction risk);
    List<StockRiskPrediction> getAllStockRiskPredictions();

    void saveSupplierDelayPrediction(SupplierDelayPrediction delay);
    List<SupplierDelayPrediction> getAllSupplierDelayPredictions();

    void saveModelMetadata(ModelTrainingMetadata metadata);
    List<ModelTrainingMetadata> getAllModelMetadata();

    List<Double> getHistoricalSalesSeries(int productId);
    List<Integer> getSupplierLeadTimes(int supplierId);
    int getSupplierDelayedOrderCount(int supplierId);
    int getProductTotalStock(int productId);
}
