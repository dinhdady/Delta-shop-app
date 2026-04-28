package com.hoangdinh.delta_shop_app.dto.request.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendNotificationRequest {
    @NotBlank(message = "Title không được để trống")
    private String title;

    @NotBlank(message = "Body không được để trống")
    private String body;

    private String type;
    private String data;
}