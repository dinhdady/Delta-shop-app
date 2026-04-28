package com.hoangdinh.delta_shop_app.dto.request.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewModerationRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // APPROVED, REJECTED

    private String adminReply;
}