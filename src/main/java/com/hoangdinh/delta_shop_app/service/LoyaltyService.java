package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.response.loyalty.LoyaltyPointsResponse;
import com.hoangdinh.delta_shop_app.dto.response.loyalty.LoyaltyTransactionResponse;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.User;

import java.math.BigDecimal;
import java.util.UUID;

public interface LoyaltyService {

    // Points management
    void addPoints(User user, int points, UUID orderId, String description);
    void deductPoints(User user, int points, UUID orderId, String description);
    int calculatePointsFromOrder(BigDecimal orderAmount);
    int calculatePointsFromOrder(Order order);

    // User queries
    int getUserPoints(UUID userId);
    PageResponse<LoyaltyTransactionResponse> getUserPointTransactions(UUID userId, int page, int size);
    int getPointsEarnedInDateRange(UUID userId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    // Redemption
    boolean canRedeemPoints(UUID userId, int points);
    int redeemPoints(UUID userId, int points, String redemptionType);
    BigDecimal getPointsValue(int points);

    // Tier management
    String getUserTier(UUID userId);
    int getPointsNeededForNextTier(UUID userId);
    void updateUserTier(UUID userId);
    void updateAllUserTiers();

    // Expiration
    void expireOldPoints();
    void notifyPointsExpiration();

    // Statistics
    long getTotalPointsEarned();
    long getTotalPointsRedeemed();
}