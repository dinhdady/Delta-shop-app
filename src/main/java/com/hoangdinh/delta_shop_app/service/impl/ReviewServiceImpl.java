package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.review.ReviewCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewModerationRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewResponse;
import com.hoangdinh.delta_shop_app.dto.response.review.ReviewStatsResponse;
import com.hoangdinh.delta_shop_app.entity.OrderItem;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.Review;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.enums.ReviewStatus;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.OrderItemRepository;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.ReviewRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import com.hoangdinh.delta_shop_app.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID userId, ReviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", request.getOrderItemId()));

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new BusinessException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Check if user purchased this product
        boolean isVerifiedPurchase = orderItem.getOrder().getUser() != null &&
                orderItem.getOrder().getUser().getId().equals(userId);

        Review review = Review.builder()
                .user(user)
                .product(product)
                .orderItem(orderItem)
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .status(ReviewStatus.PENDING)
                .isVerifiedPurchase(isVerifiedPurchase)
                .build();

        Review saved = reviewRepository.save(review);

        // Upload images if any
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Handle image upload logic
            log.info("Uploading {} images for review {}", request.getImages().size(), saved.getId());
        }

        log.info("Review created: user {}, product {}", userId, request.getProductId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UUID userId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền sửa đánh giá này");
        }

        if (review.getStatus() == ReviewStatus.APPROVED) {
            throw new BusinessException("Không thể sửa đánh giá đã được duyệt");
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            review.setBody(request.getBody());
        }

        Review saved = reviewRepository.save(review);
        log.info("Review updated: {}", reviewId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền xóa đánh giá này");
        }

        // Delete images from Cloudinary
        review.getImages().forEach(image -> {
            if (image.getPublicId() != null) {
                cloudinaryService.deleteFile(image.getPublicId());
            }
        });

        reviewRepository.delete(review);
        log.info("Review deleted: {}", reviewId);

        // Update product rating
        updateProductRating(review.getProduct().getId());
    }

    @Override
    @Transactional
    public void voteHelpful(UUID reviewId, UUID userId, boolean helpful) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Check if user already voted
        if (reviewRepository.hasUserVoted(reviewId, userId)) {
            throw new BusinessException("Bạn đã đánh giá hữu ích cho đánh giá này rồi");
        }

        if (helpful) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        } else {
            review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        }

        reviewRepository.save(review);
        log.info("User {} voted {} for review {}", userId, helpful ? "helpful" : "unhelpful", reviewId);
    }

    @Override
    public PageResponse<ReviewResponse> getProductReviews(UUID productId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable);

        return PageResponse.of(reviews.map(this::mapToResponse));
    }

    @Override
    public PageResponse<ReviewResponse> getUserReviews(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);

        return PageResponse.of(reviews.map(this::mapToResponse));
    }

    @Override
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return mapToResponse(review);
    }

    @Override
    public ReviewStatsResponse getProductReviewStats(UUID productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED);

        if (reviews.isEmpty()) {
            return ReviewStatsResponse.builder()
                    .averageRating(BigDecimal.ZERO)
                    .totalReviews(0)
                    .fiveStarCount(0)
                    .fourStarCount(0)
                    .threeStarCount(0)
                    .twoStarCount(0)
                    .oneStarCount(0)
                    .verifiedPurchaseCount(0)
                    .withImagesCount(0)
                    .build();
        }

        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;
        int verifiedCount = 0;
        int withImagesCount = 0;
        BigDecimal totalRating = BigDecimal.ZERO;

        for (Review review : reviews) {
            totalRating = totalRating.add(BigDecimal.valueOf(review.getRating()));
            switch (review.getRating()) {
                case 5 -> fiveStar++;
                case 4 -> fourStar++;
                case 3 -> threeStar++;
                case 2 -> twoStar++;
                case 1 -> oneStar++;
            }
            if (review.isVerifiedPurchase()) verifiedCount++;
            if (review.getImages() != null && !review.getImages().isEmpty()) withImagesCount++;
        }

        BigDecimal averageRating = totalRating.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

        return ReviewStatsResponse.builder()
                .averageRating(averageRating)
                .totalReviews(reviews.size())
                .fiveStarCount(fiveStar)
                .fourStarCount(fourStar)
                .threeStarCount(threeStar)
                .twoStarCount(twoStar)
                .oneStarCount(oneStar)
                .verifiedPurchaseCount(verifiedCount)
                .withImagesCount(withImagesCount)
                .build();
    }

    @Override
    public PageResponse<ReviewResponse> getPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Review> reviews = reviewRepository.findByStatus(ReviewStatus.PENDING, pageable);

        return PageResponse.of(reviews.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public ReviewResponse moderateReview(UUID reviewId, ReviewModerationRequest request, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));

        if (!admin.isAdmin()) {
            throw new BusinessException("Chỉ admin mới có quyền duyệt đánh giá");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        ReviewStatus newStatus = ReviewStatus.valueOf(request.getStatus());
        review.setStatus(newStatus);

        if (request.getAdminReply() != null && !request.getAdminReply().isEmpty()) {
            review.setAdminReply(request.getAdminReply());
            review.setAdminReplyAt(LocalDateTime.now());
        }

        Review saved = reviewRepository.save(review);
        log.info("Review moderated: {} -> {} by admin {}", reviewId, newStatus, adminId);

        // Update product rating if approved
        if (newStatus == ReviewStatus.APPROVED) {
            updateProductRating(review.getProduct().getId());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void batchModerateReviews(List<UUID> reviewIds, String status, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));

        if (!admin.isAdmin()) {
            throw new BusinessException("Chỉ admin mới có quyền duyệt đánh giá");
        }

        ReviewStatus newStatus = ReviewStatus.valueOf(status);
        List<Review> reviews = reviewRepository.findAllById(reviewIds);

        for (Review review : reviews) {
            review.setStatus(newStatus);
            reviewRepository.save(review);

            if (newStatus == ReviewStatus.APPROVED) {
                updateProductRating(review.getProduct().getId());
            }
        }

        log.info("Batch moderated {} reviews to {} by admin {}", reviewIds.size(), newStatus, adminId);
    }

    @Override
    public boolean canUserReviewProduct(UUID userId, UUID productId) {
        return reviewRepository.canUserReviewProduct(userId, productId);
    }

    @Override
    @Transactional
    public void checkAndUpdateProductRating(UUID productId) {
        updateProductRating(productId);
    }

    private void updateProductRating(UUID productId) {
        ReviewStatsResponse stats = getProductReviewStats(productId);

        productRepository.updateRating(
                productId,
                stats.getAverageRating(),
                stats.getTotalReviews()
        );
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .userAvatar(review.getUser().getAvatarUrl())
                .rating(review.getRating())
                .title(review.getTitle())
                .body(review.getBody())
                .images(review.getImages() != null ?
                        review.getImages().stream()
                                .map(img -> img.getUrl())
                                .collect(Collectors.toList()) : null)
                .verifiedPurchase(review.isVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .adminReply(review.getAdminReply())
                .adminReplyAt(review.getAdminReplyAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}