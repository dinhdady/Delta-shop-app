package com.hoangdinh.delta_shop_app.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    @NotBlank(message = "Image URL không được để trống")
    private String url;
    private String publicId;
    private String altText;
    private Integer sortOrder;
    private Boolean primary;
}
