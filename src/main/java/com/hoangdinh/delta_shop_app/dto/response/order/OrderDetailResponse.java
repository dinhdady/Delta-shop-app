package com.hoangdinh.delta_shop_app.dto.response.order;

import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.OrderItem;
import com.hoangdinh.delta_shop_app.entity.OrderStatusHistory;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.PaymentMethod;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderDetailResponse {
    private UUID id;
    private String orderNumber;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    // Customer info
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Shipping info
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Integer loyaltyPointsUsed;
    private Integer loyaltyPointsEarned;

    // Promotion
    private String promotionCode;
    private BigDecimal promotionDiscount;

    // Tracking
    private String trackingNumber;
    private LocalDate estimatedDelivery;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String notes;
    private String cancelReason;

    // Items
    private List<OrderItemResponse> items;

    // History
    private List<OrderStatusHistoryResponse> statusHistory;

    public static OrderDetailResponse from(Order order) {
        if (order == null) return null;

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getUser() != null ? order.getUser().getFullName() : null)
                .customerEmail(order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail())
                .customerPhone(order.getUser() != null ? order.getUser().getPhone() : order.getShippingPhone())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingProvince(order.getShippingProvince())
                .shippingDistrict(order.getShippingDistrict())
                .shippingWard(order.getShippingWard())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .loyaltyPointsUsed(order.getLoyaltyPointsUsed())
                .loyaltyPointsEarned(order.getLoyaltyPointsEarned())
                .promotionCode(order.getPromotionCode())
                .trackingNumber(order.getTrackingNumber())
                .estimatedDelivery(order.getEstimatedDelivery())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .notes(order.getNotes())
                .cancelReason(order.getCancelReason())
                .items(order.getItems() != null ?
                        order.getItems().stream()
                                .map(OrderItemResponse::from)
                                .collect(Collectors.toList()) : null)
                .statusHistory(order.getStatusHistory() != null ?
                        order.getStatusHistory().stream()
                                .map(OrderStatusHistoryResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}