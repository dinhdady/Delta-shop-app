package com.hoangdinh.delta_shop_app.dto.request.product;

import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
public class ProductBulkUpdateRequest {
    private List<UUID> productIds;
    private String categoryId;
    private String brandId;
    private BigDecimal discountPercentage;
    private ProductStatus status;
    private Boolean featured;
    private Boolean newArrival;
    private Boolean bestSeller;
}