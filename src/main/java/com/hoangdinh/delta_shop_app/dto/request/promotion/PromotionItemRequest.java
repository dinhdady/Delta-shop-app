package com.hoangdinh.delta_shop_app.dto.request.promotion;

import lombok.Data;

import java.util.UUID;

@Data
public class PromotionItemRequest {
    private UUID itemId;
    private String itemType;
}
