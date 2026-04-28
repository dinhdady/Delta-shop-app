package com.hoangdinh.delta_shop_app.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartSummaryResponse {
    private Integer totalItems;
    private Integer uniqueItems;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String discountCode;
    private Integer loyaltyPointsToUse;
    private BigDecimal loyaltyDiscount;
}