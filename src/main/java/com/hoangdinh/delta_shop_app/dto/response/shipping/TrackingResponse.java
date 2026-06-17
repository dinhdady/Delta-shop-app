package com.hoangdinh.delta_shop_app.dto.response.shipping;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TrackingResponse {
    private UUID orderId;
    private String orderNumber;
    private String trackingNumber;
    private String status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
