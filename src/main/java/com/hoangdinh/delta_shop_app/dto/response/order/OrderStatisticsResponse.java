package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class OrderStatisticsResponse {
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long processingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long refundedOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal thisWeekRevenue;
    private BigDecimal thisMonthRevenue;
    private BigDecimal averageOrderValue;
    private double orderGrowthRate;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByStatus;
}