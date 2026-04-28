package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.payment.PaymentRequest;
import com.hoangdinh.delta_shop_app.dto.request.payment.RefundRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentUrlResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {

    // Create payment
    PaymentUrlResponse createPayment(PaymentRequest request);
    PaymentResponse processPayment(UUID paymentId, Map<String, String> callbackParams);

    // Query payments
    PaymentResponse getPaymentById(UUID paymentId);
    PaymentResponse getPaymentByTransactionNo(String transactionNo);
    PageResponse<PaymentResponse> getOrderPayments(UUID orderId, int page, int size);
    PageResponse<PaymentResponse> getAllPayments(int page, int size, String status, String gateway);

    // Refund
    PaymentResponse refundPayment(RefundRequest request);
    PaymentResponse partialRefund(UUID paymentId, BigDecimal amount, String reason);

    // Gateway specific
    PaymentUrlResponse createVNPayPayment(PaymentRequest request);
    PaymentResponse handleVNPayCallback(Map<String, String> params);

    PaymentUrlResponse createMoMoPayment(PaymentRequest request);
    PaymentResponse handleMoMoCallback(Map<String, Object> callbackData);

    PaymentUrlResponse createBankTransferPayment(PaymentRequest request);

    // Validation
    boolean verifyPaymentSignature(Map<String, String> params, String gateway);
    void validatePaymentStatus(UUID orderId);

    // Statistics
    BigDecimal getTotalRevenueBetween(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getPaymentStatistics();
}