package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Review;
import com.hoangdinh.delta_shop_app.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Basic queries
    Page<Review> findByProductIdAndStatus(UUID productId, ReviewStatus status, Pageable pageable);

    List<Review> findByProductIdAndStatus(UUID productId, ReviewStatus status);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    Optional<Review> findByUserIdAndProductId(UUID userId, UUID productId);

    // Existence checks
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r " +
            "WHERE r.id = :reviewId AND r.user.id = :userId")
    boolean existsByIdAndUserId(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);

    // Rating calculations
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingByProduct(@Param("productId") UUID productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    long getReviewCountByProduct(@Param("productId") UUID productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED' GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("productId") UUID productId);

    // Vote queries
    @Query("SELECT CASE WHEN COUNT(rv) > 0 THEN true ELSE false END FROM ReviewVote rv " +
            "WHERE rv.review.id = :reviewId AND rv.user.id = :userId")
    boolean hasUserVoted(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);

    // Purchase verification
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi " +
            "WHERE oi.product.id = :productId AND oi.order.user.id = :userId AND oi.order.status = 'DELIVERED'")
    boolean canUserReviewProduct(@Param("userId") UUID userId, @Param("productId") UUID productId);

    // Delete operations
    @Modifying
    @Transactional
    void deleteByProductId(UUID productId);

    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);
}