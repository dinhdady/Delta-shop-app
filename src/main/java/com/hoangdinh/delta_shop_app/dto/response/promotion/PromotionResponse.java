package com.hoangdinh.delta_shop_app.dto.response.promotion;

import com.hoangdinh.delta_shop_app.entity.Promotion;
import com.hoangdinh.delta_shop_app.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PromotionResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private DiscountType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usagePerUser;
    private Integer usedCount;
    private String appliesTo;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean active;
    private boolean stackable;
    private LocalDateTime createdAt;

    public static PromotionResponse from(Promotion promotion) {
        if (promotion == null) return null;

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .code(promotion.getCode())
                .description(promotion.getDescription())
                .type(promotion.getType())
                .value(promotion.getValue())
                .minOrderAmount(promotion.getMinOrderAmount())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .usageLimit(promotion.getUsageLimit())
                .usagePerUser(promotion.getUsagePerUser())
                .usedCount(promotion.getUsedCount())
                .appliesTo(promotion.getAppliesTo())
                .startsAt(promotion.getStartsAt())
                .endsAt(promotion.getEndsAt())
                .active(promotion.isActive())
                .stackable(promotion.isStackable())
                .createdAt(promotion.getCreatedAt())
                .build();
    }
}