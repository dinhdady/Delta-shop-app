package com.hoangdinh.delta_shop_app.dto.request.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionUpdateRequest {
    private String name;
    private String description;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}