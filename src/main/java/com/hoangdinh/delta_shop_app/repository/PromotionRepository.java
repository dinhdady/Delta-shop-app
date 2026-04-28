package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    Optional<Promotion> findByCodeAndIsActiveTrue(String code);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
            "AND p.startsAt <= :now AND (p.endsAt IS NULL OR p.endsAt >= :now) " +
            "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    List<Promotion> findAllValid(@Param("now") ZonedDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
            "AND p.code = :code " +
            "AND p.startsAt <= :now AND (p.endsAt IS NULL OR p.endsAt >= :now) " +
            "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit) " +
            "AND (p.minOrderAmount IS NULL OR :subtotal >= p.minOrderAmount)")
    Optional<Promotion> findValidByCode(@Param("code") String code,
                                        @Param("subtotal") BigDecimal subtotal,
                                        @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
            "AND p.startsAt <= :now AND (p.endsAt IS NULL OR p.endsAt >= :now) " +
            "AND p.appliesTo = 'ALL'")
    List<Promotion> findAllApplicablePromotions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId AND pu.user.id = :userId")
    long countUserUsage(@Param("promotionId") UUID promotionId, @Param("userId") UUID userId);

    Page<Promotion> findByIsActiveTrue(Pageable pageable);
}