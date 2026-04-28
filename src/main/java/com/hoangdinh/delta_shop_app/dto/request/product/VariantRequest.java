package com.hoangdinh.delta_shop_app.dto.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariantRequest {
    private String sku;
    private String name;
    private BigDecimal priceModifier;
    private Integer stockQuantity;
    private String barcode;
    private String imageUrl;
    private Integer minStockAlert;
    private Boolean isActive;
}