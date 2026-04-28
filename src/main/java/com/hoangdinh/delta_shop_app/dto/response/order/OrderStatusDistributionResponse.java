package com.hoangdinh.delta_shop_app.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OrderStatusDistributionResponse {
    private Map<String, Long> distribution;
    private Map<String, Double> percentage;
    private long total;
}