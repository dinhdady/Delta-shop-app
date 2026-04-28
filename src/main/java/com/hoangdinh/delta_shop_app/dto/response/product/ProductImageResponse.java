package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private String url;
    private String publicId;
    private String altText;
    private Integer sortOrder;
    private Boolean primary;

    public static ProductImageResponse from(ProductImage image) {
        if (image == null) return null;
        return ProductImageResponse.builder()
                .url(image.getUrl())
                .publicId(image.getPublicId())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .primary(image.isPrimary())
                .build();
    }
}
