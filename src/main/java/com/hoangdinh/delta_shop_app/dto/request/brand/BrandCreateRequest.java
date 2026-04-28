package com.hoangdinh.delta_shop_app.dto.request.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandCreateRequest {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    private String logoUrl;
    private String websiteUrl;
    private String description;
    private String countryOfOrigin;
    private Boolean isFeatured;
    private Integer sortOrder;
}