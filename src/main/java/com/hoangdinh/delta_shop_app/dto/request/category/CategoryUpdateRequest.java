package com.hoangdinh.delta_shop_app.dto.request.category;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryUpdateRequest {
    private String name;
    private UUID parentId;
    private String description;
    private String imageUrl;
    private String iconClass;
    private Integer sortOrder;
    private Boolean active;
    private String metaTitle;
    private String metaDescription;
}