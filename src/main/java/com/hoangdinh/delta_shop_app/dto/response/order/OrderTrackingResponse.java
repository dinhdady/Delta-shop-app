package com.hoangdinh.delta_shop_app.dto.response.order;

import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class OrderTrackingResponse {
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;
    private LocalDate estimatedDelivery;
    private ZonedDateTime shippedAt;
    private ZonedDateTime deliveredAt;
    private String trackingNumber;
    private String carrier;
    private String cancelReason;
    private List<TrackingEventResponse> trackingEvents;
}

@Data
@Builder
class TrackingEventResponse {
    private String status;
    private String location;
    private String description;
    private ZonedDateTime timestamp;
}