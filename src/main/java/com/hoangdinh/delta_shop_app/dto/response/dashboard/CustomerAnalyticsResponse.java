package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CustomerAnalyticsResponse {
    private long totalCustomers;
    private long activeCustomers;
    private long newCustomersToday;
    private long newCustomersThisMonth;
    private BigDecimal averageCustomerSpend;
}
