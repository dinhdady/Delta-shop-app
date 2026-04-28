package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TopProductResponse {
    private UUID productId;
    private String productName;
    private String productImage;
    private int totalQuantitySold;
    private BigDecimal totalRevenue;
    private int orderCount;
}