package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.cart.AddToCartRequest;
import com.hoangdinh.delta_shop_app.dto.request.cart.UpdateCartItemRequest;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartSummaryResponse;

import java.util.UUID;

public interface CartService {

    // User cart operations
    CartResponse getCart(UUID userId);
    CartResponse addToCart(UUID userId, AddToCartRequest request);
    CartResponse updateCartItem(UUID userId, UpdateCartItemRequest request);
    CartResponse removeCartItem(UUID userId, UUID cartItemId);
    void clearCart(UUID userId);
    CartSummaryResponse getCartSummary(UUID userId);

    // Guest cart operations
    CartResponse getGuestCart(String sessionId);
    CartResponse addToGuestCart(String sessionId, AddToCartRequest request);
    CartResponse mergeGuestCart(UUID userId, String sessionId);

    // Validation
    void validateCartItems(UUID userId);
    void validateStock(UUID variantId, int quantity);

    // Checkout preparation
    CartResponse prepareCartForCheckout(UUID userId);
    void lockCartForCheckout(UUID userId);
}