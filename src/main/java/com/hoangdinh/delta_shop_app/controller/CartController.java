package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.cart.AddToCartRequest;
import com.hoangdinh.delta_shop_app.dto.request.cart.UpdateCartItemRequest;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;
import com.hoangdinh.delta_shop_app.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "APIs for shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user cart")
    public ResponseEntity<CartResponse> getCart(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartResponse> addToCart(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(userId, request));
    }

    @PutMapping("/items")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeCartItem(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID cartItemId) {
        return ResponseEntity.ok(cartService.removeCartItem(userId, cartItemId));
    }

    @DeleteMapping("/clear")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Clear cart")
    public ResponseEntity<Void> clearCart(@RequestAttribute("userId") UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge guest cart with user cart")
    public ResponseEntity<CartResponse> mergeGuestCart(
            @RequestAttribute("userId") UUID userId,
            HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        return ResponseEntity.ok(cartService.mergeGuestCart(userId, sessionId));
    }

    @GetMapping("/guest")
    @Operation(summary = "Get guest cart")
    public ResponseEntity<CartResponse> getGuestCart(HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getGuestCart(request.getSession(true).getId()));
    }

    @PostMapping("/guest/items")
    @Operation(summary = "Add item to guest cart")
    public ResponseEntity<CartResponse> addToGuestCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToGuestCart(httpRequest.getSession(true).getId(), request));
    }

    @PutMapping("/guest/items")
    @Operation(summary = "Update guest cart item")
    public ResponseEntity<CartResponse> updateGuestCartItem(
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(cartService.updateGuestCartItem(httpRequest.getSession(true).getId(), request));
    }

    @DeleteMapping("/guest/items/{cartItemId}")
    @Operation(summary = "Remove guest cart item")
    public ResponseEntity<CartResponse> removeGuestCartItem(
            @PathVariable UUID cartItemId,
            HttpServletRequest request) {
        return ResponseEntity.ok(cartService.removeGuestCartItem(request.getSession(true).getId(), cartItemId));
    }

    @DeleteMapping("/guest/clear")
    @Operation(summary = "Clear guest cart")
    public ResponseEntity<Void> clearGuestCart(HttpServletRequest request) {
        cartService.clearGuestCart(request.getSession(true).getId());
        return ResponseEntity.noContent().build();
    }
}
