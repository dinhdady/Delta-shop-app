package com.hoangdinh.delta_shop_app.dto.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID không được để trống")
    private UUID orderId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // VNPAY, MOMO, BANK_TRANSFER, CREDIT_CARD

    private String returnUrl;
    private String cancelUrl;
    private String bankCode; // For VNPAY
}