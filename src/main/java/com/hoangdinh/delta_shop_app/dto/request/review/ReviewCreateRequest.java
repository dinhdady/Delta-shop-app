package com.hoangdinh.delta_shop_app.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReviewCreateRequest {
    @NotNull(message = "Product ID không được để trống")
    private UUID productId;

    @NotNull(message = "Order item ID không được để trống")
    private UUID orderItemId;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1-5")
    @Max(value = 5, message = "Rating phải từ 1-5")
    private Integer rating;

    private String title;
    private String body;
    private List<String> images;
}