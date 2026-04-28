package com.hoangdinh.delta_shop_app.dto.response.promotion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PromotionValidationResponse {
    private boolean valid;
    private String code;
    private String name;
    private String message;
    private BigDecimal discountAmount;
    private String discountType;
    private BigDecimal minOrderAmount;
    private Integer remainingUses;
    private boolean isStackable;
}