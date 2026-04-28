package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.dto.response.brand.BrandResponse;
import com.hoangdinh.delta_shop_app.dto.response.category.CategoryResponse;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String sku;
    private CategoryResponse category;
    private BrandResponse brand;
    private String shortDescription;
    private String description;
    private ProductStatus status;
    private BigDecimal basePrice;
    private BigDecimal comparePrice;
    private BigDecimal discountPercentage;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer totalSold;
    private Integer totalViews;
    private boolean featured;
    private boolean newArrival;
    private boolean bestSeller;
    private List<String> tags;
    private List<String> sportTypes;
    private List<ProductImageResponse> images;
    private Integer stockQuantity;
    private BigDecimal costPrice;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private LocalDateTime createdAt;
    private List<SizeGuideResponse> sizeGuides;
    private Map<String, Object> specifications;
    public static ProductResponse from(Product product) {
        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .category(CategoryResponse.from(product.getCategory()))
                .brand(BrandResponse.from(product.getBrand()))
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .status(product.getStatus())
                .basePrice(product.getBasePrice())
                .comparePrice(product.getComparePrice())
                .discountPercentage(calculateDiscountPercentage(product))
                .averageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO)
                .reviewCount(product.getReviewCount() != null ? product.getReviewCount() : 0)
                .totalSold(product.getTotalSold() != null ? product.getTotalSold() : 0)
                .totalViews(product.getTotalViews() != null ? product.getTotalViews() : 0)
                .featured(product.isFeatured())
                .newArrival(product.isNewArrival())
                .bestSeller(product.isBestSeller())
                .tags(product.getTags())
                .sportTypes(product.getSportTypes())
                .images(product.getImages() != null ?
                        product.getImages().stream()
                                .map(ProductImageResponse::from)
                                .collect(Collectors.toList()) : null)
                .stockQuantity(product.getVariants() != null ?
                        product.getVariants().stream()
                                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                                .sum() : 0)
                .costPrice(product.getCostPrice())
                .weight(product.getWeight())
                .length(product.getLength())
                .width(product.getWidth())
                .height(product.getHeight())
                .createdAt(product.getCreatedAt())
                .sizeGuides(product.getSizeGuides() != null ?
                        product.getSizeGuides().stream()
                                .map(SizeGuideResponse::from)
                                .collect(Collectors.toList()) : null)
                .specifications(product.getSpecifications())
                .build();
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
}