package com.hoangdinh.delta_shop_app.dto.response.shipping;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShippingFeeResponse {
    private String method;
    private String description;
    private BigDecimal fee;
    private int estimatedDays;
}
