package com.hoangdinh.delta_shop_app.dto.response.review;

import com.hoangdinh.delta_shop_app.entity.Review;
import com.hoangdinh.delta_shop_app.entity.ReviewImage;
import com.hoangdinh.delta_shop_app.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private ReviewStatus status;
    private String title;
    private String body;
    private List<String> images;
    private boolean verifiedPurchase;
    private Integer helpfulCount;
    private boolean votedHelpful;
    private String adminReply;
    private LocalDateTime adminReplyAt;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        if (review == null) return null;

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getFullName() : null)
                .userAvatar(review.getUser() != null ? review.getUser().getAvatarUrl() : null)
                .rating(review.getRating())
                .status(review.getStatus())
                .title(review.getTitle())
                .body(review.getBody())
                .images(review.getImages() != null ?
                        review.getImages().stream()
                                .map(ReviewImage::getUrl)
                                .collect(Collectors.toList()) : null)
                .verifiedPurchase(review.isVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .adminReply(review.getAdminReply())
                .adminReplyAt(review.getAdminReplyAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
