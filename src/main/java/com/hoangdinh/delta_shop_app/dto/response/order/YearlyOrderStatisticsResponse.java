package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class YearlyOrderStatisticsResponse {
    private int year;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private List<MonthlyOrderStatisticsResponse> monthlyStats;
}