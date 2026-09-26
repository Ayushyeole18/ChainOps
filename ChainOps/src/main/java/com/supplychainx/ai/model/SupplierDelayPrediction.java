package com.supplychainx.ai.model;

import java.sql.Timestamp;

public class SupplierDelayPrediction {
    private int delayId;
    private int supplierId;
    private String supplierName;
    private String contactPerson;
    private String email;
    private double averageLeadTimeDays;
    private int lateDeliveryCount;
    private int totalOrdersEvaluated;
    private double delayProbability; // 0 to 100%
    private String riskCategory; // LOW, MODERATE, CRITICAL
    private double reliabilityScore; // 0 to 100%
    private Timestamp evaluatedAt;

    public SupplierDelayPrediction() {}

    public SupplierDelayPrediction(int delayId, int supplierId, String supplierName,
                                   String contactPerson, String email, double averageLeadTimeDays,
                                   int lateDeliveryCount, int totalOrdersEvaluated,
                                   double delayProbability, String riskCategory,
                                   double reliabilityScore, Timestamp evaluatedAt) {
        this.delayId = delayId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.averageLeadTimeDays = averageLeadTimeDays;
        this.lateDeliveryCount = lateDeliveryCount;
        this.totalOrdersEvaluated = totalOrdersEvaluated;
        this.delayProbability = delayProbability;
        this.riskCategory = riskCategory;
        this.reliabilityScore = reliabilityScore;
        this.evaluatedAt = evaluatedAt;
    }

    public int getDelayId() { return delayId; }
    public void setDelayId(int delayId) { this.delayId = delayId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getAverageLeadTimeDays() { return averageLeadTimeDays; }
    public void setAverageLeadTimeDays(double averageLeadTimeDays) { this.averageLeadTimeDays = averageLeadTimeDays; }

    public int getLateDeliveryCount() { return lateDeliveryCount; }
    public void setLateDeliveryCount(int lateDeliveryCount) { this.lateDeliveryCount = lateDeliveryCount; }

    public int getTotalOrdersEvaluated() { return totalOrdersEvaluated; }
    public void setTotalOrdersEvaluated(int totalOrdersEvaluated) { this.totalOrdersEvaluated = totalOrdersEvaluated; }

    public double getDelayProbability() { return delayProbability; }
    public void setDelayProbability(double delayProbability) { this.delayProbability = delayProbability; }

    public String getRiskCategory() { return riskCategory; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }

    public double getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(double reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public Timestamp getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Timestamp evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
