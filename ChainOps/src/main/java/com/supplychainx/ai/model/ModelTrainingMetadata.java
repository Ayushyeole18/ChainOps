package com.supplychainx.ai.model;

import java.sql.Timestamp;

public class ModelTrainingMetadata {
    private int modelId;
    private String modelName;
    private String modelType;
    private String algorithm;
    private int trainingSampleSize;
    private Double mae;
    private Double rmse;
    private Double rSquared;
    private Double accuracyScore;
    private Timestamp lastTrainedAt;
    private String status; // TRAINED, OUTDATED, TRAINING_FAILED
    private String notes;

    public ModelTrainingMetadata() {}

    public ModelTrainingMetadata(int modelId, String modelName, String modelType, String algorithm,
                                 int trainingSampleSize, Double mae, Double rmse, Double rSquared,
                                 Double accuracyScore, Timestamp lastTrainedAt, String status, String notes) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelType = modelType;
        this.algorithm = algorithm;
        this.trainingSampleSize = trainingSampleSize;
        this.mae = mae;
        this.rmse = rmse;
        this.rSquared = rSquared;
        this.accuracyScore = accuracyScore;
        this.lastTrainedAt = lastTrainedAt;
        this.status = status;
        this.notes = notes;
    }

    public int getModelId() { return modelId; }
    public void setModelId(int modelId) { this.modelId = modelId; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public int getTrainingSampleSize() { return trainingSampleSize; }
    public void setTrainingSampleSize(int trainingSampleSize) { this.trainingSampleSize = trainingSampleSize; }

    public Double getMae() { return mae; }
    public void setMae(Double mae) { this.mae = mae; }

    public Double getRmse() { return rmse; }
    public void setRmse(Double rmse) { this.rmse = rmse; }

    public Double getRSquared() { return rSquared; }
    public void setRSquared(Double rSquared) { this.rSquared = rSquared; }

    public Double getAccuracyScore() { return accuracyScore; }
    public void setAccuracyScore(Double accuracyScore) { this.accuracyScore = accuracyScore; }

    public Timestamp getLastTrainedAt() { return lastTrainedAt; }
    public void setLastTrainedAt(Timestamp lastTrainedAt) { this.lastTrainedAt = lastTrainedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
