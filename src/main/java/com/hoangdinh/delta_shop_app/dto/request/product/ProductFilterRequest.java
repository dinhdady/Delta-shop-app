package com.hoangdinh.delta_shop_app.dto.request.product;

import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {
    private String keyword;
    private UUID categoryId;
    private Boolean includeSubcategories;
    private UUID brandId;
    private List<UUID> brandIds;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private String sportType;
    private Boolean featured;
    private Boolean newArrival;
    private Boolean bestSeller;
    private Boolean inStockOnly;
    private String sortBy;
    private String sortDir;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 12;
    @Builder.Default
    private boolean publicOnly = true;
    private ProductStatus status;
}
