package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {

    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId AND pu.user.id = :userId")
    long countByPromotionAndUser(@Param("promotionId") UUID promotionId, @Param("userId") UUID userId);

    boolean existsByPromotionIdAndOrderId(UUID promotionId, UUID orderId);

    List<PromotionUsage> findByPromotionId(UUID promotionId);

    List<PromotionUsage> findByUserId(UUID userId);

    Optional<PromotionUsage> findByPromotionIdAndUserId(UUID promotionId, UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
    void deleteByPromotionId(@Param("promotionId") UUID promotionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PromotionUsage pu WHERE pu.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Query("SELECT pu.promotion.id, COUNT(pu) FROM PromotionUsage pu GROUP BY pu.promotion.id")
    List<Object[]> getUsageCountByPromotion();

    @Query("SELECT pu.user.id, COUNT(pu) FROM PromotionUsage pu GROUP BY pu.user.id ORDER BY COUNT(pu) DESC")
    List<Object[]> getTopUsersByPromotionUsage();
}