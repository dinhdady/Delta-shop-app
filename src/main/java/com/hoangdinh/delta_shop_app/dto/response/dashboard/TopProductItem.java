package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductItem {
    private UUID productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private String categoryName;
    private String brandName;
    private int totalSold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
    private double percentageOfTotalSales;
}
