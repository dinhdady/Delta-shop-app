package com.hoangdinh.delta_shop_app.dto.response.loyalty;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoyaltyPointsResponse {
    private Integer currentPoints;
    private BigDecimal pointsValue;
    private String currentTier;
    private Integer pointsToNextTier;
    private Integer totalPointsEarned;
    private Integer totalPointsRedeemed;
}