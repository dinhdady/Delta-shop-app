package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.wishlist.WishlistResponse;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;
import com.hoangdinh.delta_shop_app.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "APIs for user wishlist")
@SecurityRequirement(name = "Bearer Authentication")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get current user wishlist")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getWishlist(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(wishlistService.getUserWishlist(userId, page, size));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get wishlist summary")
    public ResponseEntity<WishlistResponse> getSummary(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(wishlistService.getWishlistSummary(userId));
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "Check product in wishlist")
    public ResponseEntity<Map<String, Boolean>> checkProduct(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(Map.of("inWishlist", wishlistService.isProductInWishlist(userId, productId)));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(userId, productId));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<Void> removeFromWishlist(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear wishlist")
    public ResponseEntity<Void> clearWishlist(@RequestAttribute("userId") UUID userId) {
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/move-to-cart")
    @Operation(summary = "Move a wishlist product to cart")
    public ResponseEntity<CartResponse> moveToCart(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.moveWishlistItemToCart(userId, productId));
    }

    @PostMapping("/move-all-to-cart")
    @Operation(summary = "Move all available wishlist products to cart")
    public ResponseEntity<CartResponse> moveAllToCart(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(wishlistService.moveAllWishlistToCart(userId));
    }
}
