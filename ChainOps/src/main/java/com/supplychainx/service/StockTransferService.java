package com.supplychainx.service;

import com.supplychainx.dao.StockTransferDao;
import com.supplychainx.dao.StockTransferDaoImpl;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.StockTransfer;
import com.supplychainx.model.StockTransferItem;
import com.supplychainx.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class StockTransferService {

    private final StockTransferDao transferDao;

    public StockTransferService() {
        this.transferDao = new StockTransferDaoImpl();
    }

    public StockTransferService(StockTransferDao transferDao) {
        this.transferDao = transferDao;
    }

    public void validateTransfer(StockTransfer transfer) {
        if (transfer.getSourceWarehouseId() <= 0) {
            throw new ValidationException("Source Warehouse must be selected.");
        }
        if (transfer.getDestinationWarehouseId() <= 0) {
            throw new ValidationException("Destination Warehouse must be selected.");
        }
        if (transfer.getSourceWarehouseId() == transfer.getDestinationWarehouseId()) {
            throw new BusinessRuleException("Source and Destination warehouses must be different.");
        }
        if (transfer.getItems() == null || transfer.getItems().isEmpty()) {
            throw new ValidationException("At least one product item must be specified for stock transfer.");
        }
        for (StockTransferItem item : transfer.getItems()) {
            ValidationUtil.validatePositiveQuantity(item.getQuantity(), "Transfer quantity for " + item.getProductName());
        }
    }

    public int executeStockTransfer(StockTransfer transfer, int performedByUserId) {
        if (transfer.getTransferNumber() == null || transfer.getTransferNumber().trim().isEmpty()) {
            transfer.setTransferNumber(transferDao.generateNextTransferNumber());
        }
        validateTransfer(transfer);
        return transferDao.createAndExecuteTransfer(transfer, performedByUserId);
    }

    public Optional<StockTransfer> getTransferById(int transferId) {
        return transferDao.findById(transferId);
    }

    public List<StockTransfer> getAllTransfers() {
        return transferDao.findAll();
    }

    public List<StockTransfer> searchAndFilter(String query, String status) {
        return transferDao.searchAndFilter(query, status);
    }

    public String generateNextTransferNumber() {
        return transferDao.generateNextTransferNumber();
    }
}
