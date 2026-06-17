package com.hoangdinh.delta_shop_app.dto.request.search;

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
public class AdvancedSearchRequest {
    private String keyword;
    private UUID categoryId;
    private UUID brandId;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private List<String> tags;
    private Boolean inStockOnly;
    
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 12;
    private String sortBy;
    @Builder.Default
    private String sortDir = "desc";
}
