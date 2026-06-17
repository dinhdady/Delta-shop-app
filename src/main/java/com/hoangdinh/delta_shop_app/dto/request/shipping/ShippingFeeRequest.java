package com.hoangdinh.delta_shop_app.dto.request.shipping;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingFeeRequest {
    private String province;
    private BigDecimal orderAmount;
    private BigDecimal weight;
}
