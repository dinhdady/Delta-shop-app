package com.hoangdinh.delta_shop_app.dto.response.payment;

import com.hoangdinh.delta_shop_app.entity.Payment;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private String transactionNo;
    private String gatewayTxnId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String gateway;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String failureReason;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .transactionNo(payment.getTransactionNo())
                .gatewayTxnId(payment.getGatewayTxnId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .gateway(payment.getGateway())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .build();
    }
}