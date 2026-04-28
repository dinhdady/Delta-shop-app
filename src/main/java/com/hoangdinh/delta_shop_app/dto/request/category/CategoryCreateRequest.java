package com.hoangdinh.delta_shop_app.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryCreateRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private UUID parentId;
    private String description;
    private String imageUrl;
    private String iconClass;
    private Integer sortOrder;
    private String metaTitle;
    private String metaDescription;
}