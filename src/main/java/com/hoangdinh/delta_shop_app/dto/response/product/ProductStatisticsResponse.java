package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ProductStatisticsResponse {
    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long outOfStockProducts;
    private long lowStockProducts;
    private long discontinuedProducts;
    private long productsOnSale;
    private long featuredProducts;
    private long newArrivals;
    private long bestSellers;
    private BigDecimal averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Map<String, Long> productsByCategory;
    private Map<String, Long> productsByBrand;
    private Map<ProductStatus, Long> productsByStatus;
}