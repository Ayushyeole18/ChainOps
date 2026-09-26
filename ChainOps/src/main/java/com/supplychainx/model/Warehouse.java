package com.supplychainx.model;

import java.sql.Timestamp;

public class Warehouse {
    private int warehouseId;
    private String warehouseName;
    private String code;
    private String location;
    private String managerName;
    private int capacity;
    private int currentUtilization;
    private String status;
    private Timestamp createdAt;

    public Warehouse() {
        this.capacity = 10000;
        this.status = "ACTIVE";
    }

    public Warehouse(int warehouseId, String warehouseName, String code, String location, String managerName, int capacity, int currentUtilization, String status) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.code = code;
        this.location = location;
        this.managerName = managerName;
        this.capacity = capacity;
        this.currentUtilization = currentUtilization;
        this.status = status;
    }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentUtilization() { return currentUtilization; }
    public void setCurrentUtilization(int currentUtilization) { this.currentUtilization = currentUtilization; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double getUtilizationPercentage() {
        if (capacity <= 0) return 0.0;
        return (double) currentUtilization / capacity * 100.0;
    }

    @Override
    public String toString() {
        return warehouseName + " [" + code + "]";
    }
}
