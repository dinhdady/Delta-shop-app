package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    Page<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT SUM(lt.points) FROM LoyaltyTransaction lt WHERE lt.user.id = :userId AND lt.points > 0")
    Integer getTotalPointsEarned(@Param("userId") UUID userId);

    @Query("SELECT SUM(lt.points) FROM LoyaltyTransaction lt WHERE lt.user.id = :userId AND lt.points < 0")
    Integer getTotalPointsRedeemed(@Param("userId") UUID userId);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.expiresAt IS NOT NULL AND lt.expiresAt < :now AND lt.points > 0")
    List<LoyaltyTransaction> findExpiredPoints(@Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.user.id = :userId")
    int getCurrentBalance(@Param("userId") UUID userId);

    List<LoyaltyTransaction> findByOrderId(UUID orderId);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.user.id = :userId AND lt.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyTransaction> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.points > 0")
    long getTotalPointsEarnedAll();

    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.points < 0")
    long getTotalPointsRedeemedAll();
}
