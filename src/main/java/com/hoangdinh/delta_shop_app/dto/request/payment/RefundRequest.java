package com.hoangdinh.delta_shop_app.dto.request.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RefundRequest {
    @NotNull(message = "Payment ID không được để trống")
    private UUID paymentId;

    @NotNull(message = "Số tiền hoàn không được để trống")
    @Positive(message = "Số tiền hoàn phải lớn hơn 0")
    private BigDecimal amount;

    private String reason;
}