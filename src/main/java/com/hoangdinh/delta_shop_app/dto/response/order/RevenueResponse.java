package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class RevenueResponse {
    private BigDecimal totalRevenue;
    private BigDecimal previousPeriodRevenue;
    private double growthPercentage;
    private Map<String, BigDecimal> dailyRevenue;
    private Map<String, BigDecimal> revenueByPaymentMethod;
}