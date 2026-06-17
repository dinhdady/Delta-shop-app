package com.hoangdinh.delta_shop_app.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 300, message = "Tiêu đề đánh giá không được vượt quá 300 ký tự")
    private String title;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String body;

    @Size(max = 5, message = "Mỗi đánh giá chỉ được tối đa 5 hình ảnh")
    private List<String> images;
}
