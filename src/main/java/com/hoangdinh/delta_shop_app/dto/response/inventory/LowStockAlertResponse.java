package com.hoangdinh.delta_shop_app.dto.response.inventory;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LowStockAlertResponse {
    private UUID variantId;
    private UUID productId;
    private String productName;
    private String variantName;
    private String sku;
    private Integer currentStock;
    private Integer minStockAlert;
    private Integer reorderQuantity;
}