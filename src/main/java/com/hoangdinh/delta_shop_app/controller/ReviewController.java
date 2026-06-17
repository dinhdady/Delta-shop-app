package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.review.BatchModerateRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewModerationRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewStatsResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewEligibilityResponse;
import com.hoangdinh.delta_shop_app.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "APIs for product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // ========== PUBLIC ENDPOINTS ==========

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get product reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getProductReviews(
            @PathVariable UUID productId,
            @RequestAttribute(value = "userId", required = false) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, userId, page, size, sortBy));
    }

    @GetMapping("/products/{productId}/stats")
    @Operation(summary = "Get product review statistics")
    public ResponseEntity<ReviewStatsResponse> getProductReviewStats(@PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.getProductReviewStats(productId));
    }

    // ========== USER ENDPOINTS ==========

    @GetMapping("/order-items/{orderItemId}/eligibility")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Check whether current user can review an order item")
    public ResponseEntity<ReviewEligibilityResponse> getReviewEligibility(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID orderItemId) {
        return ResponseEntity.ok(reviewService.getReviewEligibility(userId, orderItemId));
    }

    @GetMapping("/eligibility/products/{productId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Check whether current user can review a product")
    public ResponseEntity<ReviewEligibilityResponse> getProductReviewEligibility(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.getProductReviewEligibility(userId, productId));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create review")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(userId, request));
    }

    @PutMapping("/{reviewId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update review")
    public ResponseEntity<ReviewResponse> updateReview(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
    }

    @DeleteMapping("/{reviewId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete review")
    public ResponseEntity<Void> deleteReview(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reviewId}/helpful")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Vote helpful for review")
    public ResponseEntity<Void> voteHelpful(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID reviewId,
            @RequestParam boolean helpful) {
        reviewService.voteHelpful(reviewId, userId, helpful);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getMyReviews(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId, page, size));
    }

    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/admin/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get pending reviews (Admin only)")
    public ResponseEntity<PageResponse<ReviewResponse>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getPendingReviews(page, size));
    }

    @PostMapping("/admin/{reviewId}/moderate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Moderate review (Admin only)")
    public ResponseEntity<ReviewResponse> moderateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewModerationRequest request,
            @RequestAttribute("userId") UUID adminId) {
        return ResponseEntity.ok(reviewService.moderateReview(reviewId, request, adminId));
    }

    @PostMapping("/admin/bulk-moderate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Batch moderate reviews (Admin only)")
    public ResponseEntity<Void> batchModerateReviews(
            @RequestBody BatchModerateRequest request,
            @RequestAttribute("userId") UUID adminId) {
        reviewService.batchModerateReviews(request.getReviewIds(), request.getStatus(), adminId);
        return ResponseEntity.ok().build();
    }
}
