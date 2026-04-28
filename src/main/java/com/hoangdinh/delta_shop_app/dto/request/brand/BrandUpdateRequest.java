package com.hoangdinh.delta_shop_app.dto.request.brand;

import lombok.Data;

@Data
public class BrandUpdateRequest {
    private String name;
    private String logoUrl;
    private String websiteUrl;
    private String description;
    private String countryOfOrigin;
    private Boolean isFeatured;
    private Boolean isActive;
    private Integer sortOrder;
}