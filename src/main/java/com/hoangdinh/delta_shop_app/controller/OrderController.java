package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.order.CreateOrderRequest;
import com.hoangdinh.delta_shop_app.dto.request.order.OrderUpdateStatusRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.order.OrderDetailResponse;
import com.hoangdinh.delta_shop_app.dto.response.order.OrderResponse;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import com.hoangdinh.delta_shop_app.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "APIs for order management")
public class OrderController {

    private final OrderService orderService;

    // ========== USER ENDPOINTS ==========

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create new order")
    public ResponseEntity<OrderDetailResponse> createOrder(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(userId, request));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<PageResponse<OrderResponse>> getUserOrders(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, page, size));
    }

    @GetMapping("/me/{orderId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get order detail for current user")
    public ResponseEntity<OrderDetailResponse> getUserOrderDetail(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId, userId));
    }

    @PostMapping("/{orderId}/cancel")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel order")
    public ResponseEntity<OrderDetailResponse> cancelOrder(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID orderId,
            @RequestParam String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId, reason));
    }

    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all orders (Admin only)")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, status));
    }

    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get order detail (Admin only)")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update order status (Admin only)")
    public ResponseEntity<OrderDetailResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateStatusRequest request,
            @RequestAttribute("userId") UUID adminId) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request, adminId));
    }

    @PatchMapping("/admin/{orderId}/payment-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update payment status (Admin only)")
    public ResponseEntity<OrderDetailResponse> updatePaymentStatus(
            @PathVariable UUID orderId,
            @RequestParam String paymentStatus,
            @RequestAttribute("userId") UUID adminId) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, PaymentStatus.valueOf(paymentStatus), adminId));
    }

    @PostMapping("/admin/{orderId}/tracking")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add tracking number (Admin only)")
    public ResponseEntity<OrderDetailResponse> addTrackingNumber(
            @PathVariable UUID orderId,
            @RequestParam String trackingNumber,
            @RequestAttribute("userId") UUID adminId) {
        return ResponseEntity.ok(orderService.addTrackingNumber(orderId, trackingNumber, adminId));
    }

    @DeleteMapping("/admin/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete order (Admin only)")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable UUID orderId,
            @RequestAttribute("userId") UUID adminId) {
        orderService.deleteOrder(orderId, adminId);
        return ResponseEntity.noContent().build();
    }
}
