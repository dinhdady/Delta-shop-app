package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.promotion.PromotionCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.promotion.PromotionUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.promotion.PromotionResponse;
import com.hoangdinh.delta_shop_app.dto.response.promotion.PromotionValidationResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.Promotion;
import com.hoangdinh.delta_shop_app.entity.PromotionUsage;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.OrderRepository;
import com.hoangdinh.delta_shop_app.repository.PromotionRepository;
import com.hoangdinh.delta_shop_app.repository.PromotionUsageRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionCreateRequest request, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));

        if (!admin.isAdmin()) {
            throw new BusinessException("Chỉ admin mới có quyền tạo khuyến mãi");
        }

        if (request.getCode() != null && promotionRepository.findByCodeAndIsActiveTrue(request.getCode()).isPresent()) {
            throw new BusinessException("Mã khuyến mãi đã tồn tại");
        }

        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .type(request.getType())
                .value(request.getValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usagePerUser(request.getUsagePerUser() != null ? request.getUsagePerUser() : 1)
                .appliesTo(request.getAppliesTo())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .isActive(true)
                .isStackable(request.isStackable())
                .createdBy(admin)
                .build();

        Promotion saved = promotionRepository.save(promotion);
        log.info("Promotion created: {} ({})", saved.getName(), saved.getId());

        return PromotionResponse.from(saved);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(UUID id, PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        if (request.getName() != null) promotion.setName(request.getName());
        if (request.getDescription() != null) promotion.setDescription(request.getDescription());
        if (request.getValue() != null) promotion.setValue(request.getValue());
        if (request.getMinOrderAmount() != null) promotion.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getMaxDiscountAmount() != null) promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getUsageLimit() != null) promotion.setUsageLimit(request.getUsageLimit());
        if (request.getStartsAt() != null) promotion.setStartsAt(request.getStartsAt());
        if (request.getEndsAt() != null) promotion.setEndsAt(request.getEndsAt());

        Promotion saved = promotionRepository.save(promotion);
        log.info("Promotion updated: {}", id);

        return PromotionResponse.from(saved);
    }

    @Override
    public PromotionResponse getPromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        return PromotionResponse.from(promotion);
    }

    @Override
    public PromotionResponse getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "code", code));
        return PromotionResponse.from(promotion);
    }

    @Override
    public PageResponse<PromotionResponse> getAllPromotions(int page, int size, String status) {
        return null;
    }

    @Override
    public List<PromotionResponse> getActivePromotions() {
        return List.of();
    }

    @Override
    public List<PromotionResponse> getPromotionsApplicableToOrder(BigDecimal subtotal, UUID userId) {
        return List.of();
    }

    @Override
    public PageResponse<PromotionResponse> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Promotion> promotions = promotionRepository.findAll(pageable);
        return PageResponse.of(promotions.map(PromotionResponse::from));
    }

    @Override
    @Transactional
    public void deletePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        promotion.setActive(false);
        promotionRepository.save(promotion);
        log.info("Promotion deleted: {}", id);
    }

    @Override
    @Transactional
    public void activatePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        promotion.setActive(true);
        promotionRepository.save(promotion);
        log.info("Promotion activated: {}", id);
    }

    @Override
    @Transactional
    public void deactivatePromotion(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        promotion.setActive(false);
        promotionRepository.save(promotion);
        log.info("Promotion deactivated: {}", id);
    }

    @Override
    public PromotionResponse duplicatePromotion(UUID id, UUID adminId) {
        return null;
    }

    @Override
    public BigDecimal calculateDiscount(String code, BigDecimal subtotal, UUID userId) {
        PromotionValidationResponse validation = validatePromotion(code, subtotal, userId);

        if (!validation.isValid()) {
            throw new BusinessException(validation.getMessage());
        }

        return validation.getDiscountAmount();
    }

    @Override
    public BigDecimal calculateBestDiscount(BigDecimal subtotal, UUID userId, List<String> applicableCodes) {
        return null;
    }

    @Override
    public PromotionValidationResponse validatePromotion(String code, BigDecimal subtotal, UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        Promotion promotion = promotionRepository.findValidByCode(code, subtotal, now)
                .orElse(null);

        if (promotion == null) {
            return PromotionValidationResponse.builder()
                    .valid(false)
                    .code(code)
                    .message("Mã khuyến mãi không hợp lệ hoặc đã hết hạn")
                    .build();
        }

        // Check usage per user
        long userUsedCount = promotionUsageRepository.countByPromotionAndUser(promotion.getId(), userId);
        if (!promotion.canUserUse(userId, userUsedCount)) {
            return PromotionValidationResponse.builder()
                    .valid(false)
                    .code(code)
                    .name(promotion.getName())
                    .message("Bạn đã sử dụng hết số lần cho mã khuyến mãi này")
                    .remainingUses(0)
                    .build();
        }

        BigDecimal discountAmount = promotion.calculateDiscount(subtotal);

        return PromotionValidationResponse.builder()
                .valid(true)
                .code(code)
                .name(promotion.getName())
                .message("Áp dụng thành công")
                .discountAmount(discountAmount)
                .discountType(promotion.getType().name())
                .minOrderAmount(promotion.getMinOrderAmount())
                .remainingUses(promotion.getUsageLimit() != null ?
                        promotion.getUsageLimit() - promotion.getUsedCount() : null)
                .isStackable(promotion.isStackable())
                .build();
    }

    @Override
    @Transactional
    public void recordPromotionUsage(UUID promotionId, UUID userId, UUID orderId, BigDecimal discountAmount) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId).orElse(null);
        }

        PromotionUsage usage = PromotionUsage.builder()
                .promotion(promotion)
                .user(user)
                .order(order)  // SỬA: dùng order thay vì orderId
                .discountAmount(discountAmount)
                .build();

        promotionUsageRepository.save(usage);

        // Increment used count
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);

        log.info("Promotion usage recorded: promotion {}, user {}, order {}", promotionId, userId, orderId);
    }


    @Override
    public long getUserPromotionUsageCount(UUID promotionId, UUID userId) {
        return promotionUsageRepository.countByPromotionAndUser(promotionId, userId);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    public void deactivateExpiredPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Promotion promotion : promotions) {
            if (promotion.isActive() && promotion.getEndsAt() != null && promotion.getEndsAt().isBefore(now)) {
                promotion.setActive(false);
                promotionRepository.save(promotion);
                log.info("Auto-deactivated expired promotion: {}", promotion.getCode());
            }
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 10 * * *") // Run daily at 10 AM
    public void sendPromotionExpirationNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        List<Promotion> expiringPromotions = promotionRepository.findAll().stream()
                .filter(p -> p.isActive() && p.getEndsAt() != null &&
                        p.getEndsAt().isAfter(now) && p.getEndsAt().isBefore(tomorrow))
                .collect(Collectors.toList());

        for (Promotion promotion : expiringPromotions) {
            log.info("Promotion {} will expire tomorrow: {}", promotion.getCode(), promotion.getEndsAt());
            // TODO: Send notifications to users
        }
    }
}