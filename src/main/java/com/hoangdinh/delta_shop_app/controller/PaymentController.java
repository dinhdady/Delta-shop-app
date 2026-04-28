package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.payment.PaymentRequest;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentUrlResponse;
import com.hoangdinh.delta_shop_app.service.PaymentService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing")
public class PaymentController {

    private final PaymentService paymentService;
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
