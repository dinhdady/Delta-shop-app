package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.payment.PaymentRequest;
import com.hoangdinh.delta_shop_app.dto.request.payment.RefundRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentUrlResponse;
import com.hoangdinh.delta_shop_app.service.PaymentService;
import com.hoangdinh.delta_shop_app.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.vnpay.return-url:http://localhost:8080/api/vnpay/return}")
    private String backendReturnUrl;

    @PostMapping("/payments/vnpay")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create VNPay payment URL")
    public ResponseEntity<PaymentUrlResponse> createVNPayPayment(@Valid @RequestBody PaymentRequest request) {
        if (request.getReturnUrl() == null || request.getReturnUrl().isBlank()) {
            request.setReturnUrl(backendReturnUrl);
        }
        return ResponseEntity.ok(paymentService.createVNPayPayment(request));
    }

    @PostMapping("/payments")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create payment")
    public ResponseEntity<PaymentUrlResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/payments/momo")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PaymentUrlResponse> createMoMoPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createMoMoPayment(request));
    }

    @PostMapping("/payments/bank-transfer")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PaymentUrlResponse> createBankTransferPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createBankTransferPayment(request));
    }

    @GetMapping("/payments/order/{orderId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<PageResponse<PaymentResponse>> getOrderPayments(
            @PathVariable UUID orderId,
            @org.springframework.web.bind.annotation.RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        orderService.getOrderDetail(orderId, userId);
        return ResponseEntity.ok(paymentService.getOrderPayments(orderId, page, size));
    }

    @GetMapping("/payments/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PageResponse<PaymentResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gateway) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, size, status, gateway));
    }

    @PostMapping("/payments/admin/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentResponse> refund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(request));
    }

    @GetMapping("/payments/admin/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> statistics() {
        return ResponseEntity.ok(paymentService.getPaymentStatistics());
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<PaymentResponse> handleMoMoCallback(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(paymentService.handleMoMoCallback(params));
    }

    @GetMapping("/vnpay/return")
    public RedirectView handleVNPayReturn(@RequestParam Map<String, String> params,
                                          HttpServletRequest request) {
        // Log tất cả params nhận được
        log.info("========== VNPAY CALLBACK RECEIVED ==========");
        log.info("All params: {}", params);
        log.info("Query string: {}", request.getQueryString());
        log.info("=============================================");

        try {
            PaymentResponse payment = paymentService.handleVNPayCallback(params);

            log.info("Payment processed successfully: status={}, orderId={}",
                    payment.getStatus(), payment.getOrderId());

            String redirectUrl = UriComponentsBuilder
                    .fromHttpUrl(frontendUrl)
                    .path("/payment-result")
                    .queryParam("status", payment.getStatus())
                    .queryParam("orderId", payment.getOrderId())
                    .queryParam("orderNumber", payment.getOrderNumber())
                    .queryParam("transactionNo", payment.getTransactionNo())
                    .queryParam("gatewayTxnId", payment.getGatewayTxnId())
                    .build()
                    .toUriString();

            log.info("Redirecting to: {}", redirectUrl);
            return new RedirectView(redirectUrl);

        } catch (Exception ex) {
            log.error("Error processing VNPay callback", ex);

            // Lấy orderId từ session hoặc params nếu có
            String orderId = params.get("vnp_TxnRef");

            String redirectUrl = UriComponentsBuilder
                    .fromHttpUrl(frontendUrl)
                    .path("/payment-result")
                    .queryParam("status", "FAILED")
                    .queryParam("message", ex.getMessage())
                    .queryParam("orderId", orderId)
                    .build()
                    .toUriString();

            return new RedirectView(redirectUrl);
        }
    }
}
