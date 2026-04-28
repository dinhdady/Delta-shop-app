package com.hoangdinh.delta_shop_app.dto.response.review;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ReviewStatsResponse {
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
    private Map<Integer, Integer> ratingDistribution;
    private Integer verifiedPurchaseCount;
    private Integer withImagesCount;
}