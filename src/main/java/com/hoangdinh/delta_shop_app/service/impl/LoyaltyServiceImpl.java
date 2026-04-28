package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.loyalty.LoyaltyTransactionResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoyaltyServiceImpl implements LoyaltyService {

    private final UserRepository userRepository;

    private static final int POINTS_PER_AMOUNT = 1000; // 1 điểm = 1000 VND
    private static final int MAX_POINTS_PER_ORDER = 10000; // Tối đa 10000 điểm/đơn

    @Override
    public void addPoints(User user, int points, UUID orderId, String description) {
        if (user == null || points <= 0) {
            return;
        }

        user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
        userRepository.save(user);

        log.info("Added {} points to user {} for order {}: {}",
                points, user.getEmail(), orderId, description);
    }

    @Override
    public void deductPoints(User user, int points, UUID orderId, String description) {
        if (user == null || points <= 0) {
            return;
        }

        int newPoints = Math.max(0, user.getLoyaltyPoints() - points);
        user.setLoyaltyPoints(newPoints);
        userRepository.save(user);

        log.info("Deducted {} points from user {} for order {}: {}",
                points, user.getEmail(), orderId, description);
    }

    @Override
    public int calculatePointsFromOrder(BigDecimal orderAmount) {
        return 0;
    }

    @Override
    public int calculatePointsFromOrder(Order order) {
        if (order == null || order.getTotalAmount() == null) {
            return 0;
        }

        // 1 điểm cho mỗi 1000 VND
        int points = order.getTotalAmount()
                .divide(BigDecimal.valueOf(POINTS_PER_AMOUNT), 0, RoundingMode.FLOOR)
                .intValue();

        // Giới hạn điểm tối đa cho mỗi đơn hàng
        return Math.min(points, MAX_POINTS_PER_ORDER);
    }

    @Override
    public int getUserPoints(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getLoyaltyPoints)
                .orElse(0);
    }

    @Override
    public PageResponse<LoyaltyTransactionResponse> getUserPointTransactions(UUID userId, int page, int size) {
        return null;
    }

    @Override
    public int getPointsEarnedInDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return 0;
    }

    @Override
    public boolean canRedeemPoints(UUID userId, int points) {
        return false;
    }

    @Override
    public int redeemPoints(UUID userId, int points, String redemptionType) {
        return 0;
    }

    @Override
    public BigDecimal getPointsValue(int points) {
        return null;
    }

    @Override
    public String getUserTier(UUID userId) {
        return "";
    }

    @Override
    public int getPointsNeededForNextTier(UUID userId) {
        return 0;
    }

    @Override
    public void updateUserTier(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            int points = user.getLoyaltyPoints();
            String tier = getTierByPoints(points);
            log.info("User {} has tier: {} with {} points", user.getEmail(), tier, points);
            // Có thể lưu tier vào database nếu cần
        });
    }

    @Override
    public void updateAllUserTiers() {

    }

    @Override
    public void expireOldPoints() {

    }

    @Override
    public void notifyPointsExpiration() {

    }

    @Override
    public long getTotalPointsEarned() {
        return 0;
    }

    @Override
    public long getTotalPointsRedeemed() {
        return 0;
    }

    private String getTierByPoints(int points) {
        if (points >= 10000) {
            return "DIAMOND";
        } else if (points >= 5000) {
            return "PLATINUM";
        } else if (points >= 2000) {
            return "GOLD";
        } else if (points >= 500) {
            return "SILVER";
        } else {
            return "BRONZE";
        }
    }
}