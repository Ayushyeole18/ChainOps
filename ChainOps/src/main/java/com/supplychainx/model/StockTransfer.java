package com.supplychainx.model;

import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StockTransfer {
    private int transferId;
    private String transferNumber;
    private int sourceWarehouseId;
    private String sourceWarehouseName;
    private int destinationWarehouseId;
    private String destinationWarehouseName;
    private LocalDate transferDate;
    private String status; // PENDING, IN_TRANSIT, COMPLETED, CANCELLED
    private Integer initiatedByUserId;
    private String initiatedByName;
    private String notes;
    private Timestamp createdAt;
    private List<StockTransferItem> items = new ArrayList<>();

    public StockTransfer() {
        this.transferDate = LocalDate.now();
        this.status = "PENDING";
    }

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public String getTransferNumber() { return transferNumber; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }

    public int getSourceWarehouseId() { return sourceWarehouseId; }
    public void setSourceWarehouseId(int sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }

    public String getSourceWarehouseName() { return sourceWarehouseName; }
    public void setSourceWarehouseName(String sourceWarehouseName) { this.sourceWarehouseName = sourceWarehouseName; }

    public int getDestinationWarehouseId() { return destinationWarehouseId; }
    public void setDestinationWarehouseId(int destinationWarehouseId) { this.destinationWarehouseId = destinationWarehouseId; }

    public String getDestinationWarehouseName() { return destinationWarehouseName; }
    public void setDestinationWarehouseName(String destinationWarehouseName) { this.destinationWarehouseName = destinationWarehouseName; }

    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getInitiatedByUserId() { return initiatedByUserId; }
    public void setInitiatedByUserId(Integer initiatedByUserId) { this.initiatedByUserId = initiatedByUserId; }

    public String getInitiatedByName() { return initiatedByName; }
    public void setInitiatedByName(String initiatedByName) { this.initiatedByName = initiatedByName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<StockTransferItem> getItems() { return items; }
    public void setItems(List<StockTransferItem> items) { this.items = items; }
}
