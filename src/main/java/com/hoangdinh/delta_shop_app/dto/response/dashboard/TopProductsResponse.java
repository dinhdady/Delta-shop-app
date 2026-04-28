package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResponse {
    private List<TopProductItem> products;
    private String period;
    private int limit;
    private long totalItems;
    private BigDecimal totalRevenue;
    private int totalQuantitySold;
}

