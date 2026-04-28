package com.hoangdinh.delta_shop_app.dto.response.cart;

import com.hoangdinh.delta_shop_app.entity.Cart;
import com.hoangdinh.delta_shop_app.entity.CartItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;

    public static CartResponse from(Cart cart) {
        if (cart == null) return null;

        List<CartItemResponse> itemResponses = cart.getItems() != null ?
                cart.getItems().stream()
                        .map(CartItemResponse::from)
                        .collect(Collectors.toList()) : null;

        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(cart.getItems() != null ? cart.getItems().size() : 0)
                .subtotal(subtotal)
                .discount(discount)
                .total(total)
                .build();
    }

    private static BigDecimal calculateSubtotal(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}