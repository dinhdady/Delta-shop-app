package com.hoangdinh.delta_shop_app.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TopProductStatsDto {
    private UUID productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private String categoryName;
    private String brandName;
    private int totalSold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;

    // Constructor với đúng thứ tự 9 parameters
    public TopProductStatsDto(
            UUID productId,
            String productName,
            String productSlug,
            String productImage,
            String categoryName,
            String brandName,
            Long totalSold,
            BigDecimal totalRevenue,
            BigDecimal averagePrice
    ) {
        this.productId = productId;
        this.productName = productName;
        this.productSlug = productSlug;
        this.productImage = productImage;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.totalSold = totalSold != null ? totalSold.intValue() : 0;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.averagePrice = averagePrice != null ? averagePrice : BigDecimal.ZERO;
    }
}