package com.hoangdinh.delta_shop_app.dto.response.inventory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class InventoryResponse {
    private UUID variantId;
    private String productName;
    private String variantName;
    private String sku;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer minStockAlert;
    private String status; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    private BigDecimal price;
}