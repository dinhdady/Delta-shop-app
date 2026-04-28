package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOverviewResponse {
    private BigDecimal totalRevenue;
    private BigDecimal previousPeriodRevenue;
    private double growthPercentage;
    private long totalOrders;
    private long previousPeriodOrders;
    private double orderGrowthPercentage;
    private BigDecimal averageOrderValue;
    private BigDecimal previousAverageOrderValue;
    private double averageOrderGrowth;
    private Map<LocalDate, BigDecimal> dailyRevenue;
    private Map<LocalDate, Long> dailyOrders;
    private Map<String, BigDecimal> revenueByPaymentMethod;
    private Map<String, Long> ordersByStatus;
}