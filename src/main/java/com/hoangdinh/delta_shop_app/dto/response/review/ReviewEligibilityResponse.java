package com.hoangdinh.delta_shop_app.dto.response.review;

import com.hoangdinh.delta_shop_app.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReviewEligibilityResponse {
    private UUID orderItemId;
    private UUID productId;
    private boolean canReview;
    private boolean reviewed;
    private String reason;
    private UUID reviewId;
    private ReviewStatus reviewStatus;
}
