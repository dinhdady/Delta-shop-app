package com.hoangdinh.delta_shop_app.dto.response.user;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserStatisticsResponse {
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private Integer loyaltyPoints;
    private Integer totalReviews;
    private Integer helpfulVotesReceived;
    private Integer wishlistCount;
}