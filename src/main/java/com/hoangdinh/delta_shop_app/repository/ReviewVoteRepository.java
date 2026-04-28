package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, UUID> {

    Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    @Query("SELECT CASE WHEN COUNT(rv) > 0 THEN true ELSE false END FROM ReviewVote rv " +
            "WHERE rv.review.id = :reviewId AND rv.user.id = :userId")
    boolean existsByReviewIdAndUserId(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);

    @Modifying
    @Transactional
    void deleteByReviewIdAndUserId(UUID reviewId, UUID userId);

    @Modifying
    @Transactional
    void deleteByReviewId(UUID reviewId);
}