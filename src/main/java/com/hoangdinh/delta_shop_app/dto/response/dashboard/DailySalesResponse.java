package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailySalesResponse {
    private LocalDate date;
    private long totalOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
}
