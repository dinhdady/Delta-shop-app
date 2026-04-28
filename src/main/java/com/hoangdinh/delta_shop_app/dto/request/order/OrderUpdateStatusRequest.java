package com.hoangdinh.delta_shop_app.dto.request.order;

import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderUpdateStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private OrderStatus status;
    private String note;
}