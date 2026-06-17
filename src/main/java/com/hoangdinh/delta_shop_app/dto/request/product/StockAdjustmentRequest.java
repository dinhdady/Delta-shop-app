package com.hoangdinh.delta_shop_app.dto.request.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StockAdjustmentRequest {
    private UUID variantId;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    private Integer stockQuantity;

    private String note;
}
