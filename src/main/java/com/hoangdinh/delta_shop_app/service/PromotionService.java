package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.promotion.PromotionCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.promotion.PromotionUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.request.promotion.ApplyPromotionRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.promotion.PromotionResponse;
import com.hoangdinh.delta_shop_app.dto.response.promotion.PromotionValidationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PromotionService {

    // Admin operations
    PromotionResponse createPromotion(PromotionCreateRequest request, UUID adminId);
    PromotionResponse updatePromotion(UUID id, PromotionUpdateRequest request);

    PageResponse<PromotionResponse> getAllPromotions(int page, int size);

    void deletePromotion(UUID id);
    void activatePromotion(UUID id);
    void deactivatePromotion(UUID id);
    PromotionResponse duplicatePromotion(UUID id, UUID adminId);

    // Query operations
    PromotionResponse getPromotion(UUID id);
    PromotionResponse getPromotionByCode(String code);
    PageResponse<PromotionResponse> getAllPromotions(int page, int size, String status);
    List<PromotionResponse> getActivePromotions();
    List<PromotionResponse> getPromotionsApplicableToOrder(BigDecimal subtotal, UUID userId);

    // Validation and calculation
    PromotionValidationResponse validatePromotion(String code, BigDecimal subtotal, UUID userId);
    BigDecimal calculateDiscount(String code, BigDecimal subtotal, UUID userId);
    BigDecimal calculateBestDiscount(BigDecimal subtotal, UUID userId, List<String> applicableCodes);

    // Usage tracking
    void recordPromotionUsage(UUID promotionId, UUID userId, UUID orderId, BigDecimal discountAmount);
    long getUserPromotionUsageCount(UUID promotionId, UUID userId);

    // Auto-expiration
    void deactivateExpiredPromotions();
    void sendPromotionExpirationNotifications();
}