package com.hoangdinh.delta_shop_app.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;
}