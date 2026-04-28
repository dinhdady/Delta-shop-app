package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class VariantResponse {
    private UUID id;
    private String sku;
    private String name;
    private BigDecimal priceModifier;
    private BigDecimal finalPrice;
    private Integer stockQuantity;
    private Integer availableQuantity;
    private String imageUrl;
    private boolean inStock;

    public static VariantResponse from(ProductVariant variant) {
        if (variant == null) return null;

        return VariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .name(variant.getName())
                .priceModifier(variant.getPriceModifier() != null ? variant.getPriceModifier() : BigDecimal.ZERO)
                .finalPrice(variant.getFinalPrice())
                .stockQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0)
                .availableQuantity(variant.getAvailableQuantity())
                .imageUrl(variant.getImageUrl())
                .inStock(variant.isInStock())
                .build();
    }
}