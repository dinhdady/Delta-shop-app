package com.hoangdinh.delta_shop_app.dto.response.loyalty;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class LoyaltyTransactionResponse {
    private UUID id;
    private Integer points;
    private Integer balanceAfter;
    private String description;
    private String type; // EARN, REDEEM, EXPIRE
    private UUID orderId;
    private ZonedDateTime createdAt;
    private ZonedDateTime expiresAt;
}