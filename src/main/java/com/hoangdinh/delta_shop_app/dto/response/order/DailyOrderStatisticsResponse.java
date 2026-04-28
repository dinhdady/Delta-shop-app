package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailyOrderStatisticsResponse {
    private LocalDate date;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private String mostOrderedProduct;
    private int mostOrderedProductQuantity;
}