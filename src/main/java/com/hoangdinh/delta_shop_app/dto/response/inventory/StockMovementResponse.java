package com.hoangdinh.delta_shop_app.dto.response.inventory;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class StockMovementResponse {
    private UUID id;
    private UUID variantId;
    private String productName;
    private String variantName;
    private String type;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String note;
    private String createdBy;
    private ZonedDateTime createdAt;
}