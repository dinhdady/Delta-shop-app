package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.review.ReviewCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.review.ReviewUpdateRequest;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.OrderItem;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.Review;
import com.hoangdinh.delta_shop_app.entity.ReviewVote;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.repository.OrderItemRepository;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.ReviewRepository;
import com.hoangdinh.delta_shop_app.repository.ReviewVoteRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewVoteRepository reviewVoteRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CloudinaryService cloudinaryService;

    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(
                reviewRepository, reviewVoteRepository, productRepository, userRepository, orderItemRepository, cloudinaryService);
    }

    @Test
    void createReviewMarksDeliveredPurchasedItemAsReviewed() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        User user = User.builder().id(userId).firstName("Test").lastName("Customer").build();
        Product product = Product.builder().id(productId).name("Product").build();
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();
        OrderItem item = OrderItem.builder().id(orderItemId).order(order).product(product).isReviewed(false).build();
        ReviewCreateRequest request = request(productId, orderItemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(item));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(UUID.randomUUID());
            return review;
        });
        when(reviewRepository.findByProductIdAndStatus(productId, com.hoangdinh.delta_shop_app.enums.ReviewStatus.APPROVED))
                .thenReturn(java.util.List.of(Review.builder()
                        .rating(5)
                        .isVerifiedPurchase(true)
                        .build()));

        var response = service.createReview(userId, request);

        assertThat(response.isVerifiedPurchase()).isTrue();
        assertThat(response.getStatus()).isEqualTo(com.hoangdinh.delta_shop_app.enums.ReviewStatus.APPROVED);
        assertThat(item.isReviewed()).isTrue();
        verify(orderItemRepository).save(item);
        verify(productRepository).updateRating(productId, new java.math.BigDecimal("5.00"), 1);
    }

    @Test
    void createReviewRejectsItemFromOrderThatIsNotPaid() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        Product product = Product.builder().id(productId).build();
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.SHIPPED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        OrderItem item = OrderItem.builder().id(orderItemId).order(order).product(product).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.createReview(userId, request(productId, orderItemId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã được thanh toán");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewRejectsItemOwnedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User anotherUser = User.builder().id(UUID.randomUUID()).build();
        Product product = Product.builder().id(productId).build();
        Order order = Order.builder()
                .user(anotherUser)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();
        OrderItem item = OrderItem.builder().id(orderItemId).order(order).product(product).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.createReview(userId, request(productId, orderItemId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đơn hàng của mình");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewRejectsPaidButCancelledOrder() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        Product product = Product.builder().id(productId).build();
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CANCELLED)
                .paymentStatus(PaymentStatus.PAID)
                .build();
        OrderItem item = OrderItem.builder().id(orderItemId).order(order).product(product).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.createReview(userId, request(productId, orderItemId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã hủy hoặc hoàn tiền");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateApprovedReviewOwnedByUserRecalculatesProductRating() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        User user = User.builder().id(userId).firstName("Test").lastName("Customer").build();
        Product product = Product.builder().id(productId).name("Product").build();
        Review review = Review.builder()
                .id(reviewId)
                .user(user)
                .product(product)
                .rating(5)
                .body("Đánh giá cũ")
                .status(com.hoangdinh.delta_shop_app.enums.ReviewStatus.APPROVED)
                .build();
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        request.setRating(3);
        request.setBody("Đánh giá đã sửa");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewRepository.findByProductIdAndStatus(productId, com.hoangdinh.delta_shop_app.enums.ReviewStatus.APPROVED))
                .thenReturn(java.util.List.of(review));

        var response = service.updateReview(reviewId, userId, request);

        assertThat(response.getRating()).isEqualTo(3);
        assertThat(response.getBody()).isEqualTo("Đánh giá đã sửa");
        verify(productRepository).updateRating(productId, new java.math.BigDecimal("3.00"), 1);
    }

    @Test
    void voteHelpfulPersistsVoteAndRejectsSecondVote() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        User author = User.builder().id(UUID.randomUUID()).build();
        User voter = User.builder().id(userId).build();
        Review review = Review.builder()
                .id(reviewId)
                .user(author)
                .helpfulCount(0)
                .unhelpfulCount(0)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findById(userId)).thenReturn(Optional.of(voter));
        when(reviewVoteRepository.existsByReviewIdAndUserId(reviewId, userId)).thenReturn(false, true);

        service.voteHelpful(reviewId, userId, true);

        assertThat(review.getHelpfulCount()).isEqualTo(1);
        verify(reviewVoteRepository).save(any(ReviewVote.class));

        assertThatThrownBy(() -> service.voteHelpful(reviewId, userId, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã đánh giá hữu ích");
    }

    @Test
    void getReviewEligibilityReturnsBackendDecisionForPaidItem() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        Product product = Product.builder().id(productId).build();
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();
        OrderItem item = OrderItem.builder().id(orderItemId).order(order).product(product).isReviewed(false).build();

        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(item));
        when(reviewRepository.findByOrderItemId(orderItemId)).thenReturn(Optional.empty());

        var eligibility = service.getReviewEligibility(userId, orderItemId);

        assertThat(eligibility.isCanReview()).isTrue();
        assertThat(eligibility.isReviewed()).isFalse();
        assertThat(eligibility.getProductId()).isEqualTo(productId);
    }

    private ReviewCreateRequest request(UUID productId, UUID orderItemId) {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setProductId(productId);
        request.setOrderItemId(orderItemId);
        request.setRating(5);
        request.setBody("Sản phẩm tốt");
        return request;
    }
}
