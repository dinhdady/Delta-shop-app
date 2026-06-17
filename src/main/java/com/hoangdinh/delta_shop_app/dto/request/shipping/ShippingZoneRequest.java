package com.hoangdinh.delta_shop_app.dto.request.shipping;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShippingZoneRequest {
    private String name;
    private List<String> provinces;
    private BigDecimal baseFee;
    private boolean active = true;
}
