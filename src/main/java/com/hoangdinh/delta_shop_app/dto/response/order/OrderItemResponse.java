package com.hoangdinh.delta_shop_app.dto.response.order;

import com.hoangdinh.delta_shop_app.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private String productName;
    private String variantName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private boolean reviewed;

    public static OrderItemResponse from(OrderItem item) {
        if (item == null) return null;

        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalPrice(item.getTotalPrice())
                .reviewed(item.isReviewed())
                .build();
    }
}