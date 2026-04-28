package com.hoangdinh.delta_shop_app.dto.response.brand;

import com.hoangdinh.delta_shop_app.entity.Brand;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BrandResponse {
    private UUID id;
    private String name;
    private String slug;
    private String logoUrl;
    private String websiteUrl;
    private String description;
    private String countryOfOrigin;
    private boolean featured;
    private boolean active;
    private Integer productCount;

    public static BrandResponse from(Brand brand) {
        if (brand == null) return null;

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logoUrl(brand.getLogoUrl())
                .websiteUrl(brand.getWebsiteUrl())
                .description(brand.getDescription())
                .countryOfOrigin(brand.getCountryOfOrigin())
                .featured(brand.isFeatured())
                .active(brand.isActive())
                .build();
    }
}