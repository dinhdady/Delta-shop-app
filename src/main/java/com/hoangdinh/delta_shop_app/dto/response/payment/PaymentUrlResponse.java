package com.hoangdinh.delta_shop_app.dto.response.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentUrlResponse {
    private String paymentUrl;
    private String paymentId;
    private UUID orderId;
    private String orderNumber;
    private String gateway;
    private BigDecimal amount;
    private String transactionNo;
}