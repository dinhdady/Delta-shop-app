package com.hoangdinh.delta_shop_app.dto.request.promotion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApplyPromotionRequest {
    @NotBlank(message = "Mã khuyến mãi không được để trống")
    private String code;

    private BigDecimal subtotal;
    private UUID userId;
}