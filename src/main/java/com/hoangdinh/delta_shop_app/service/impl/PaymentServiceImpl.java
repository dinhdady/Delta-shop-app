package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.payment.PaymentRequest;
import com.hoangdinh.delta_shop_app.dto.request.payment.RefundRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentResponse;
import com.hoangdinh.delta_shop_app.dto.response.payment.PaymentUrlResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.entity.Payment;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.OrderRepository;
import com.hoangdinh.delta_shop_app.repository.PaymentRepository;
import com.hoangdinh.delta_shop_app.service.PaymentService;
import com.hoangdinh.delta_shop_app.util.PaymentGatewayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayUtil paymentGatewayUtil;

    private static final AtomicInteger TRANSACTION_COUNTER = new AtomicInteger(0);
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting object to JSON", e);
            return "{}";
        }
    }

    @Override
    @Transactional
    public PaymentUrlResponse createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException("Đơn hàng đã được thanh toán");
        }

        Payment payment = Payment.builder()
                .order(order)
                .gateway(request.getPaymentMethod())
                .amount(order.getTotalAmount())
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .transactionNo(generateTransactionNo())
                .build();

        Payment saved = paymentRepository.save(payment);

        return switch (request.getPaymentMethod().toUpperCase()) {
            case "VNPAY" -> createVNPayPayment(request, saved);
            case "MOMO" -> createMoMoPayment(request, saved);
            case "BANK_TRANSFER" -> createBankTransferPayment(request, saved);
            default -> throw new BusinessException("Phương thức thanh toán không được hỗ trợ: " + request.getPaymentMethod());
        };
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(UUID paymentId, Map<String, String> callbackParams) {
        log.info("Processing payment: {}, params: {}", paymentId, callbackParams);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        boolean verified = paymentGatewayUtil.verifySignature(callbackParams, payment.getGateway());
        if (!verified) {
            payment.markAsFailed("Chữ ký không hợp lệ", toJson(callbackParams));
            paymentRepository.save(payment);
            throw new BusinessException("Chữ ký thanh toán không hợp lệ");
        }

        String responseCode = callbackParams.get("vnp_ResponseCode");
        log.info("VNPay response code: {}", responseCode);

        if ("00".equals(responseCode)) {
            String gatewayTxnId = callbackParams.get("vnp_TransactionNo");
            // SỬA: dùng markAsPaid thay vì set thủ công
            payment.markAsPaid(gatewayTxnId, toJson(callbackParams));

            // Update order
            Order order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.PAID);
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
            }
            orderRepository.save(order);

            log.info("Payment successful: {} for order: {}", payment.getTransactionNo(), order.getOrderNumber());
        } else {
            String failureReason = getFailureReason(responseCode);
            // SỬA: dùng markAsFailed
            payment.markAsFailed(failureReason, toJson(callbackParams));
            log.warn("Payment failed: {} for order: {}, reason: {}",
                    payment.getTransactionNo(), payment.getOrder().getOrderNumber(), failureReason);
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }
    @Override
    public PaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse getPaymentByTransactionNo(String transactionNo) {
        Payment payment = paymentRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionNo", transactionNo));
        return PaymentResponse.from(payment);
    }

    @Override
    public PageResponse<PaymentResponse> getOrderPayments(UUID orderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> payments = paymentRepository.findByOrderId(orderId, pageable);
        return PageResponse.of(payments.map(PaymentResponse::from));
    }

    @Override
    public PageResponse<PaymentResponse> getAllPayments(int page, int size, String status, String gateway) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> payments;

        if (status != null && !status.isEmpty()) {
            payments = paymentRepository.findByStatus(PaymentStatus.valueOf(status), pageable);
        } else {
            payments = paymentRepository.findAll(pageable);
        }

        return PageResponse.of(payments.map(PaymentResponse::from));
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId()));

        if (!payment.canRefund()) {
            throw new BusinessException("Không thể hoàn tiền cho thanh toán này. Trạng thái hiện tại: " + payment.getStatus());
        }

        BigDecimal remainingAmount = payment.getRemainingRefundableAmount();
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException("Số tiền hoàn vượt quá số tiền có thể hoàn. Có thể hoàn tối đa: " + remainingAmount);
        }

        // Call gateway refund API
        boolean refundSuccess = paymentGatewayUtil.processRefund(payment, request.getAmount(), request.getReason());

        if (refundSuccess) {
            // SỬA: dùng markAsRefunded
            payment.markAsRefunded(request.getAmount(), request.getReason());

            Order order = payment.getOrder();
            if (payment.isFullyRefunded()) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            } else {
                order.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }
            orderRepository.save(order);

            log.info("Refund processed: {} for payment: {}, amount: {}",
                    request.getReason(), payment.getTransactionNo(), request.getAmount());
        } else {
            throw new BusinessException("Hoàn tiền thất bại. Vui lòng thử lại sau.");
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }
    @Override
    @Transactional
    public PaymentResponse partialRefund(UUID paymentId, BigDecimal amount, String reason) {
        RefundRequest request = new RefundRequest();
        request.setPaymentId(paymentId);
        request.setAmount(amount);
        request.setReason(reason);
        return refundPayment(request);
    }

    @Override
    @Transactional
    public PaymentUrlResponse createVNPayPayment(PaymentRequest request) {
        Payment payment = createPendingPayment(request);
        return paymentGatewayUtil.generateVNPayUrl(payment, request.getReturnUrl(), request.getBankCode());
    }

    @Override
    @Transactional
    public PaymentResponse handleVNPayCallback(Map<String, String> params) {
        log.info("Processing VNPay callback with params: {}", params);

        // Lấy transaction reference từ params
        String transactionNo = params.get("vnp_TxnRef");
        if (transactionNo == null || transactionNo.isEmpty()) {
            log.error("Missing vnp_TxnRef in callback params");
            throw new BusinessException("Missing transaction reference");
        }

        log.info("Looking for payment with transactionNo: {}", transactionNo);

        Payment payment = paymentRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> {
                    log.error("Payment not found for transactionNo: {}", transactionNo);
                    return new ResourceNotFoundException("Payment", "transactionNo", transactionNo);
                });

        log.info("Found payment: id={}, status={}, amount={}",
                payment.getId(), payment.getStatus(), payment.getAmount());

        // Lấy response code
        String responseCode = params.get("vnp_ResponseCode");
        log.info("VNPay response code: {}", responseCode);

        if ("00".equals(responseCode)) {
            String gatewayTxnId = params.get("vnp_TransactionNo");
            String bankTxnNo = params.get("vnp_BankTranNo");

            payment.markAsPaid(gatewayTxnId, toJson(params));

            // Update order
            Order order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.PAID);
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
            }
            orderRepository.save(order);

            log.info("Payment SUCCESS: txn={}, gatewayTxnId={}", transactionNo, gatewayTxnId);
        } else {
            String failureReason = getFailureReason(responseCode);
            payment.markAsFailed(failureReason, toJson(params));
            log.warn("Payment FAILED: txn={}, reason={}", transactionNo, failureReason);
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional
    public PaymentUrlResponse createMoMoPayment(PaymentRequest request) {
        Payment payment = createPendingPayment(request);
        return paymentGatewayUtil.generateMoMoUrl(payment, request.getReturnUrl(), request.getCancelUrl());
    }

    @Override
    public PaymentResponse handleMoMoCallback(Map<String, Object> callbackData) {
        String transactionNo = (String) callbackData.get("orderId");
        Payment payment = paymentRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionNo", transactionNo));

        Map<String, String> params = convertMoMoResponse(callbackData);
        return processPayment(payment.getId(), params);
    }

    @Override
    @Transactional
    public PaymentUrlResponse createBankTransferPayment(PaymentRequest request) {
        Payment payment = createPendingPayment(request);

        return PaymentUrlResponse.builder()
                .paymentUrl("/api/payments/bank-transfer-instructions/" + payment.getId())
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .gateway("BANK_TRANSFER")
                .amount(payment.getAmount())
                .transactionNo(payment.getTransactionNo())
                .build();
    }

    @Override
    public boolean verifyPaymentSignature(Map<String, String> params, String gateway) {
        return paymentGatewayUtil.verifySignature(params, gateway);
    }

    @Override
    public void validatePaymentStatus(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getPaymentStatus() != PaymentStatus.PAID &&
                order.getPaymentMethod().isOnline()) {
            throw new BusinessException("Đơn hàng chưa được thanh toán");
        }
    }

    @Override
    public BigDecimal getTotalRevenueBetween(LocalDate startDate, LocalDate endDate) {
        ZonedDateTime start = startDate.atStartOfDay(ZonedDateTime.now().getZone());
        ZonedDateTime end = endDate.plusDays(1).atStartOfDay(ZonedDateTime.now().getZone());
        return paymentRepository.getTotalPaidAmountBetween(start, end);
    }

    @Override
    public Map<String, Object> getPaymentStatistics() {
        return Map.of(
                "totalRevenue", paymentRepository.getTotalPaidAmountBetween(
                        ZonedDateTime.now().minusDays(30), ZonedDateTime.now()),
                "successfulPayments", paymentRepository.getTotalSuccessfulPaymentsBetween(
                        ZonedDateTime.now().minusDays(30), ZonedDateTime.now()),
                "totalRefunded", paymentRepository.getTotalRefundedAmountBetween(
                        ZonedDateTime.now().minusDays(30), ZonedDateTime.now()),
                "paymentMethods", paymentRepository.getPaymentMethodStats()
        );
    }

    private Payment createPendingPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        Payment payment = Payment.builder()
                .order(order)
                .gateway(request.getPaymentMethod())
                .amount(order.getTotalAmount())
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .transactionNo(generateTransactionNo())
                .build();

        return paymentRepository.saveAndFlush(payment);
    }

    private PaymentUrlResponse createVNPayPayment(PaymentRequest request, Payment payment) {
        return paymentGatewayUtil.generateVNPayUrl(payment, request.getReturnUrl(), request.getBankCode());
    }

    private PaymentUrlResponse createMoMoPayment(PaymentRequest request, Payment payment) {
        return paymentGatewayUtil.generateMoMoUrl(payment, request.getReturnUrl(), request.getCancelUrl());
    }

    private PaymentUrlResponse createBankTransferPayment(PaymentRequest request, Payment payment) {
        return PaymentUrlResponse.builder()
                .paymentUrl("/api/payments/bank-transfer-instructions/" + payment.getId())
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .gateway("BANK_TRANSFER")
                .amount(payment.getAmount())
                .transactionNo(payment.getTransactionNo())
                .build();
    }

    private String generateTransactionNo() {
        String date = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = TRANSACTION_COUNTER.incrementAndGet() % 10000;
        return String.format("TXN%s%04d", date, seq);
    }

    private String getFailureReason(String responseCode) {
        return switch (responseCode) {
            case "01" -> "Giao dịch đã tồn tại";
            case "02" -> "Merchant không hợp lệ";
            case "04" -> "Giao dịch bị khóa";
            case "05" -> "Giao dịch không thành công";
            case "06" -> "Số tiền không hợp lệ";
            case "07" -> "Thông tin thẻ không chính xác";
            case "09" -> "Thẻ/Tài khoản không đủ số dư";
            default -> "Lỗi không xác định: " + responseCode;
        };
    }

    private Map<String, String> convertMoMoResponse(Map<String, Object> callbackData) {
        Map<String, String> result = new java.util.HashMap<>();
        result.put("vnp_ResponseCode", "00".equals(callbackData.get("resultCode")) ? "00" : "05");
        result.put("vnp_TransactionNo", (String) callbackData.get("transId"));
        result.put("vnp_TxnRef", (String) callbackData.get("orderId"));
        return result;
    }
}
