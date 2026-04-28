package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductImage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Data
@Builder
public class ProductSummaryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String primaryImage;
    private BigDecimal basePrice;
    private BigDecimal comparePrice;
    private BigDecimal discountPercentage;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer totalSold;
    private Integer stockQuantity;
    private String sku;
    private String status;
    private boolean featured;
    private String categoryName;
    private String brandName;
    private boolean inStock;

    public static ProductSummaryResponse from(Product product) {
        if (product == null) return null;

        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .primaryImage(getPrimaryImage(product))
                .basePrice(product.getBasePrice())
                .comparePrice(product.getComparePrice())
                .discountPercentage(calculateDiscountPercentage(product))
                .averageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO)
                .reviewCount(product.getReviewCount() != null ? product.getReviewCount() : 0)
                .totalSold(product.getTotalSold() != null ? product.getTotalSold() : 0)
                .stockQuantity(calculateTotalStock(product))
                .sku(product.getSku())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .featured(product.isFeatured())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .inStock(isInStock(product))
                .build();
    }

    private static Integer calculateTotalStock(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return 0;
        }
        return product.getVariants().stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
    }

    private static String getPrimaryImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(product.getImages().get(0).getUrl());
    }

    private static BigDecimal calculateDiscountPercentage(Product product) {
        if (product.getComparePrice() == null ||
                product.getComparePrice().compareTo(BigDecimal.ZERO) == 0 ||
                product.getBasePrice() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = product.getComparePrice().subtract(product.getBasePrice());
        BigDecimal percentage = discount.multiply(BigDecimal.valueOf(100))
                .divide(product.getComparePrice(), 2, RoundingMode.HALF_UP);

        return percentage.compareTo(BigDecimal.ZERO) > 0 ? percentage : BigDecimal.ZERO;
    }

    private static boolean isInStock(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return false;
        }
        return product.getVariants().stream()
                .anyMatch(variant -> variant.getStockQuantity() != null && variant.getStockQuantity() > 0);
    }
}