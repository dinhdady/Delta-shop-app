package com.hoangdinh.delta_shop_app.dto.request.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VariantUpdateRequest {
    private UUID variantId;
    private String sku;
    private String name;
    private BigDecimal priceModifier;
    private Integer stockQuantity;
    private String barcode;
    private String imageUrl;
    private Integer minStockAlert;
    private Boolean isActive;
}