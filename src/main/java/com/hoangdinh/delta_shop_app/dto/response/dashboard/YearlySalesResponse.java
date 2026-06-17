package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class YearlySalesResponse {
    private int year;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private List<MonthlySalesResponse> monthlySales;
}
