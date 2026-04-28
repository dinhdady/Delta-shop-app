package com.hoangdinh.delta_shop_app.dto.request.payment;

import lombok.Data;

import java.util.Map;

@Data
public class PaymentCallbackRequest {
    private String gateway;
    private Map<String, String> params;
    private String rawResponse;
}