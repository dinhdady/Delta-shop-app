package com.hoangdinh.delta_shop_app.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddToCartRequest {
    @NotNull(message = "Variant ID không được để trống")
    private UUID variantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    // THÊM SIZE FIELDS
    private String selectedSize;
    private String selectedSizeLabel;
    private String selectedSizeMeasurement;
}