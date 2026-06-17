package com.hoangdinh.delta_shop_app.dto.response.shipping;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ShippingZoneResponse {
    private UUID id;
    private String name;
    private List<String> provinces;
    private BigDecimal baseFee;
    private boolean active;
}
