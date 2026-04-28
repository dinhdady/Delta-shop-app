package com.hoangdinh.delta_shop_app.dto.request.contact;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactReplyRequest {
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String reply;

    private String status; // Có thể cập nhật status khi reply
}