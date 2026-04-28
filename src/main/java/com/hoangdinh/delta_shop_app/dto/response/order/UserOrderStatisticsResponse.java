package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserOrderStatisticsResponse {
    private int totalOrders;
    private int completedOrders;
    private int cancelledOrders;
    private int pendingOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private String lastOrderDate;
    private int loyaltyPointsEarned;
    private int loyaltyPointsUsed;
}