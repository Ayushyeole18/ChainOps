package com.supplychainx.ai.service;

import com.supplychainx.ai.dao.MlDao;
import com.supplychainx.ai.dao.MlDaoImpl;
import com.supplychainx.ai.feature.SupplierFeatureExtractor;
import com.supplychainx.ai.model.SupplierDelayPrediction;
import com.supplychainx.ai.util.ProbabilityUtil;
import com.supplychainx.dao.SupplierDao;
import com.supplychainx.dao.SupplierDaoImpl;
import com.supplychainx.model.Supplier;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SupplierDelayService {

    private final MlDao mlDao;
    private final SupplierDao supplierDao;

    public SupplierDelayService() {
        this.mlDao = new MlDaoImpl();
        this.supplierDao = new SupplierDaoImpl();
    }

    public SupplierDelayService(MlDao mlDao, SupplierDao supplierDao) {
        this.mlDao = mlDao;
        this.supplierDao = supplierDao;
    }

    public SupplierDelayPrediction evaluateSupplierDelay(int supplierId) {
        Supplier supplier = supplierDao.findById(supplierId)
            .orElseThrow(() -> new IllegalArgumentException("Supplier ID " + supplierId + " not found."));

        List<Integer> leadTimes = mlDao.getSupplierLeadTimes(supplierId);
        int delayedOrders = mlDao.getSupplierDelayedOrderCount(supplierId);

        if (leadTimes.isEmpty()) {
            // Baseline standard lead times if vendor has no completed orders yet
            leadTimes = List.of(7, 8, 6, 9);
        }

        SupplierFeatureExtractor.SupplierPerformanceFeatures features =
            SupplierFeatureExtractor.extract(supplierId, leadTimes, delayedOrders);

        // Bayesian estimation of delay probability (prior: 15% average delay rate across manufacturing suppliers)
        double delayProb = ProbabilityUtil.calculateDelayProbability(
            features.delayedOrders,
            features.totalOrders,
            0.15,
            4.0
        );

        String riskCategory = ProbabilityUtil.classifyDelayRisk(delayProb);
        double reliabilityScore = ProbabilityUtil.computeReliabilityScore(delayProb, features.avgLeadTimeDays);

        SupplierDelayPrediction prediction = new SupplierDelayPrediction();
        prediction.setSupplierId(supplierId);
        prediction.setSupplierName(supplier.getSupplierName());
        prediction.setContactPerson(supplier.getContactPerson());
        prediction.setEmail(supplier.getEmail());
        prediction.setAverageLeadTimeDays(Math.round(features.avgLeadTimeDays * 10.0) / 10.0);
        prediction.setLateDeliveryCount(delayedOrders);
        prediction.setTotalOrdersEvaluated(features.totalOrders);
        prediction.setDelayProbability(Math.round(delayProb * 100.0) / 100.0);
        prediction.setRiskCategory(riskCategory);
        prediction.setReliabilityScore(reliabilityScore);
        prediction.setEvaluatedAt(new Timestamp(System.currentTimeMillis()));

        mlDao.saveSupplierDelayPrediction(prediction);
        return prediction;
    }

    public List<SupplierDelayPrediction> evaluateAllSuppliers() {
        List<Supplier> suppliers = supplierDao.findAll();
        List<SupplierDelayPrediction> list = new ArrayList<>();
        for (Supplier s : suppliers) {
            list.add(evaluateSupplierDelay(s.getSupplierId()));
        }
        return list;
    }

    public List<SupplierDelayPrediction> getCachedSupplierPredictions() {
        List<SupplierDelayPrediction> list = mlDao.getAllSupplierDelayPredictions();
        if (list.isEmpty()) {
            return evaluateAllSuppliers();
        }
        return list;
    }
}
