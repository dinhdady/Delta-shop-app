package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.entity.SizeGuide;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SizeGuideResponse {
    private String size;
    private String label;
    private String description;
    private String measurement;
    private Integer stockQuantity;
    private BigDecimal priceModifier;
    private String sku;
    private boolean inStock;

    public static SizeGuideResponse from(SizeGuide guide) {
        if (guide == null) return null;

        return SizeGuideResponse.builder()
                .size(guide.getSize())
                .label(guide.getLabel())
                .description(guide.getDescription())
                .measurement(guide.getMeasurement())
                .stockQuantity(guide.getStockQuantity())
                .priceModifier(guide.getPriceModifier())
                .sku(guide.getSku())
                .inStock(guide.getStockQuantity() != null && guide.getStockQuantity() > 0)
                .build();
    }
}
