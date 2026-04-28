package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.inventory.StockAdjustmentRequest;
import com.hoangdinh.delta_shop_app.dto.response.inventory.InventoryResponse;
import com.hoangdinh.delta_shop_app.dto.response.inventory.StockMovementResponse;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.inventory.LowStockAlertResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InventoryService {

    // Stock management
    InventoryResponse getVariantInventory(UUID variantId);
    List<InventoryResponse> getProductInventory(UUID productId);
    void adjustStock(StockAdjustmentRequest request, UUID userId);
    void reserveStock(UUID variantId, int quantity);
    void releaseReservedStock(UUID variantId, int quantity);
    void confirmStockDeduction(UUID variantId, int quantity);

    // Stock movements
    PageResponse<StockMovementResponse> getStockMovements(UUID variantId, int page, int size);
    PageResponse<StockMovementResponse> getAllStockMovements(int page, int size, String type, String dateRange);

    // Alerts
    List<LowStockAlertResponse> getLowStockAlerts();
    List<LowStockAlertResponse> getOutOfStockAlerts();
    void sendLowStockNotifications();

    // Bulk operations
    void bulkUpdateStock(Map<UUID, Integer> stockUpdates, UUID userId);
    void bulkImportStock(String filePath, UUID userId);

    // Reports
    byte[] generateInventoryReport(String format);
    byte[] generateStockMovementReport(java.time.LocalDate startDate, java.time.LocalDate endDate);
}