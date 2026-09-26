package com.supplychainx;

import com.supplychainx.dao.StockTransferDao;
import com.supplychainx.exception.BusinessRuleException;
import com.supplychainx.exception.ValidationException;
import com.supplychainx.model.StockTransfer;
import com.supplychainx.model.StockTransferItem;
import com.supplychainx.service.StockTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class StockTransferValidationTest {

    private StockTransferService transferService;

    @BeforeEach
    public void setup() {
        StockTransferDao mockDao = new StockTransferDao() {
            @Override public Optional<StockTransfer> findById(int transferId) { return Optional.empty(); }
            @Override public List<StockTransfer> findAll() { return List.of(); }
            @Override public List<StockTransfer> searchAndFilter(String query, String status) { return List.of(); }
            @Override public int createAndExecuteTransfer(StockTransfer transfer, int performedByUserId) { return 501; }
            @Override public String generateNextTransferNumber() { return "TR-2026-999"; }
        };

        transferService = new StockTransferService(mockDao);
    }

    @Test
    @DisplayName("Stock transfer between same source and destination warehouse throws BusinessRuleException")
    public void testSameWarehouseTransferFails() {
        StockTransfer st = new StockTransfer();
        st.setSourceWarehouseId(1);
        st.setDestinationWarehouseId(1); // Same warehouse
        st.getItems().add(new StockTransferItem(1, "SKU-01", "Product 1", 10));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> transferService.validateTransfer(st));
        assertTrue(ex.getMessage().contains("must be different"));
    }

    @Test
    @DisplayName("Stock transfer with zero items throws ValidationException")
    public void testEmptyTransferItems() {
        StockTransfer st = new StockTransfer();
        st.setSourceWarehouseId(1);
        st.setDestinationWarehouseId(2);

        ValidationException ex = assertThrows(ValidationException.class, () -> transferService.validateTransfer(st));
        assertTrue(ex.getMessage().contains("At least one product item"));
    }

    @Test
    @DisplayName("Stock transfer with zero or negative quantity throws ValidationException")
    public void testZeroOrNegativeQuantity() {
        StockTransfer st = new StockTransfer();
        st.setSourceWarehouseId(1);
        st.setDestinationWarehouseId(2);
        st.getItems().add(new StockTransferItem(1, "SKU-01", "Product 1", 0));

        assertThrows(ValidationException.class, () -> transferService.validateTransfer(st));
    }

    @Test
    @DisplayName("Valid multi-warehouse stock transfer passes validation")
    public void testValidTransfer() {
        StockTransfer st = new StockTransfer();
        st.setSourceWarehouseId(1);
        st.setDestinationWarehouseId(2);
        st.getItems().add(new StockTransferItem(1, "SKU-01", "Product 1", 15));

        assertDoesNotThrow(() -> transferService.validateTransfer(st));
    }
}
