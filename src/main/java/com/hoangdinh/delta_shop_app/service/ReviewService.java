package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.review.ReviewCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewModerationRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewStatsResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewEligibilityResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    // User operations
    ReviewResponse createReview(UUID userId, ReviewCreateRequest request);
    ReviewResponse updateReview(UUID reviewId, UUID userId, ReviewUpdateRequest request);
    void deleteReview(UUID reviewId, UUID userId);
    void voteHelpful(UUID reviewId, UUID userId, boolean helpful);

    // Query operations
    PageResponse<ReviewResponse> getProductReviews(UUID productId, UUID userId, int page, int size, String sortBy);
    PageResponse<ReviewResponse> getUserReviews(UUID userId, int page, int size);
    ReviewResponse getReviewById(UUID reviewId);
    ReviewStatsResponse getProductReviewStats(UUID productId);
    ReviewEligibilityResponse getReviewEligibility(UUID userId, UUID orderItemId);
    ReviewEligibilityResponse getProductReviewEligibility(UUID userId, UUID productId);

    // Admin operations
    PageResponse<ReviewResponse> getPendingReviews(int page, int size);
    ReviewResponse moderateReview(UUID reviewId, ReviewModerationRequest request, UUID adminId);
    void batchModerateReviews(List<UUID> reviewIds, String status, UUID adminId);

    // Helper methods
    boolean canUserReviewProduct(UUID userId, UUID productId);
    void checkAndUpdateProductRating(UUID productId);
}
