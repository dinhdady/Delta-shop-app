package com.hoangdinh.delta_shop_app.dto.response.notification;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String body;
    private String type;
    private String data;
    private boolean isRead;
    private ZonedDateTime createdAt;
    private ZonedDateTime readAt;
}