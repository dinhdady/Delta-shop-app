package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.wishlist.WishlistResponse;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;

import java.util.List;
import java.util.UUID;

public interface WishlistService {

    // User operations
    WishlistResponse addToWishlist(UUID userId, UUID productId);
    void removeFromWishlist(UUID userId, UUID productId);
    void clearWishlist(UUID userId);

    // Query operations
    PageResponse<ProductSummaryResponse> getUserWishlist(UUID userId, int page, int size);
    WishlistResponse getWishlistSummary(UUID userId);
    boolean isProductInWishlist(UUID userId, UUID productId);
    int getWishlistCount(UUID userId);

    // Bulk operations
    void addMultipleToWishlist(UUID userId, List<UUID> productIds);
    void removeMultipleFromWishlist(UUID userId, List<UUID> productIds);

    // Move to cart
    CartResponse moveWishlistItemToCart(UUID userId, UUID productId);
    CartResponse moveAllWishlistToCart(UUID userId);
}
