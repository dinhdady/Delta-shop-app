package com.hoangdinh.delta_shop_app.dto.response.order;

import com.hoangdinh.delta_shop_app.entity.OrderStatusHistory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryResponse {
    private String fromStatus;
    private String toStatus;
    private String note;
    private LocalDateTime createdAt;

    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {
        if (history == null) return null;

        return OrderStatusHistoryResponse.builder()
                .fromStatus(history.getFromStatus() != null ? history.getFromStatus().name() : null)
                .toStatus(history.getToStatus() != null ? history.getToStatus().name() : null)
                .note(history.getNote())
                .createdAt(history.getCreatedAt())
                .build();
    }
}