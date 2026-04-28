package com.hoangdinh.delta_shop_app.dto.response.category;

import com.hoangdinh.delta_shop_app.entity.Category;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private String iconClass;
    private Integer sortOrder;
    private boolean active;
    private UUID parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private Integer productCount;

    public static CategoryResponse from(Category category) {
        if (category == null) return null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .iconClass(category.getIconClass())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(category.getChildren() != null ?
                        category.getChildren().stream()
                                .map(CategoryResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public static CategoryResponse fromWithCount(Category category, Integer productCount) {
        CategoryResponse response = from(category);
        if (response != null) {
            response.setProductCount(productCount);
        }
        return response;
    }
}