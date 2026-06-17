package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.loyalty.LoyaltyTransactionResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.repository.LoyaltyTransactionRepository;
import com.hoangdinh.delta_shop_app.repository.OrderRepository;
import com.hoangdinh.delta_shop_app.entity.LoyaltyTransaction;
import com.hoangdinh.delta_shop_app.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoyaltyServiceImpl implements LoyaltyService {

    private final UserRepository userRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final OrderRepository orderRepository;

    private static final int POINTS_PER_AMOUNT = 1000; // 1 điểm = 1000 VND
    private static final int MAX_POINTS_PER_ORDER = 10000; // Tối đa 10000 điểm/đơn

    @Override
    public void addPoints(User user, int points, UUID orderId, String description) {
        if (user == null || points <= 0) {
            return;
        }

        user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
        userRepository.save(user);
        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .user(user)
                .order(orderId != null ? orderRepository.findById(orderId).orElse(null) : null)
                .points(points)
                .balanceAfter(user.getLoyaltyPoints())
                .description(description)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build());

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
        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .user(user)
                .order(orderId != null ? orderRepository.findById(orderId).orElse(null) : null)
                .points(-points)
                .balanceAfter(newPoints)
                .description(description)
                .build());

        log.info("Deducted {} points from user {} for order {}: {}",
                points, user.getEmail(), orderId, description);
    }

    @Override
    public int calculatePointsFromOrder(BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.signum() <= 0) return 0;
        return Math.min(orderAmount.divide(BigDecimal.valueOf(POINTS_PER_AMOUNT), 0, RoundingMode.FLOOR).intValue(),
                MAX_POINTS_PER_ORDER);
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
        Page<LoyaltyTransactionResponse> transactions = loyaltyTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapTransaction);
        return PageResponse.of(transactions);
    }

    @Override
    public int getPointsEarnedInDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return loyaltyTransactionRepository.findByUserIdAndDateRange(
                        userId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()).stream()
                .mapToInt(LoyaltyTransaction::getPoints)
                .filter(points -> points > 0)
                .sum();
    }

    @Override
    public boolean canRedeemPoints(UUID userId, int points) {
        return points > 0 && getUserPoints(userId) >= points;
    }

    @Override
    public int redeemPoints(UUID userId, int points, String redemptionType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException("User", "id", userId));
        if (!canRedeemPoints(userId, points)) {
            throw new com.hoangdinh.delta_shop_app.exception.BusinessException("Số điểm không đủ để đổi");
        }
        deductPoints(user, points, null, "Đổi điểm: " + redemptionType);
        return user.getLoyaltyPoints();
    }

    @Override
    public BigDecimal getPointsValue(int points) {
        return BigDecimal.valueOf(points).multiply(BigDecimal.valueOf(POINTS_PER_AMOUNT));
    }

    @Override
    public String getUserTier(UUID userId) {
        return getTierByPoints(getUserPoints(userId));
    }

    @Override
    public int getPointsNeededForNextTier(UUID userId) {
        int points = getUserPoints(userId);
        if (points < 500) return 500 - points;
        if (points < 2000) return 2000 - points;
        if (points < 5000) return 5000 - points;
        if (points < 10000) return 10000 - points;
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
        userRepository.findAll().forEach(user -> updateUserTier(user.getId()));
    }

    @Override
    public void expireOldPoints() {
        loyaltyTransactionRepository.findExpiredPoints(LocalDateTime.now()).forEach(transaction -> {
            User user = transaction.getUser();
            int pointsToExpire = Math.min(transaction.getPoints(), user.getLoyaltyPoints());
            if (pointsToExpire > 0) deductPoints(user, pointsToExpire, null, "Điểm hết hạn");
            transaction.setExpiresAt(null);
            loyaltyTransactionRepository.save(transaction);
        });
    }

    @Override
    public void notifyPointsExpiration() {

    }

    @Override
    public long getTotalPointsEarned() {
        return loyaltyTransactionRepository.getTotalPointsEarnedAll();
    }

    @Override
    public long getTotalPointsRedeemed() {
        return Math.abs(loyaltyTransactionRepository.getTotalPointsRedeemedAll());
    }

    private LoyaltyTransactionResponse mapTransaction(LoyaltyTransaction transaction) {
        return LoyaltyTransactionResponse.builder()
                .id(transaction.getId())
                .points(transaction.getPoints())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .type(transaction.getPoints() >= 0 ? "EARN" : "REDEEM")
                .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
                .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().atZone(ZoneId.systemDefault()) : null)
                .expiresAt(transaction.getExpiresAt() != null ? transaction.getExpiresAt().atZone(ZoneId.systemDefault()) : null)
                .build();
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
