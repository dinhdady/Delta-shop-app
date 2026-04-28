package com.hoangdinh.delta_shop_app.dto.response.product;

import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductImage;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ProductDetailResponse {
    private ProductResponse product;
    private List<VariantResponse> variants;
    private List<ProductImageResponse> galleryImages;
    private List<ProductSummaryResponse> relatedProducts;
    public static ProductDetailResponse from(Product product) {
        if (product == null) return null;

        return ProductDetailResponse.builder()
                .product(ProductResponse.from(product))
                .variants(product.getVariants() != null ?
                        product.getVariants().stream()
                                .map(VariantResponse::from)
                                .collect(Collectors.toList()) : null)
                .galleryImages(product.getImages() != null ?
                        product.getImages().stream()
                                .map(ProductImageResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public static ProductDetailResponse from(Product product, List<ProductSummaryResponse> relatedProducts) {
        ProductDetailResponse response = from(product);
        if (response != null) {
            response.setRelatedProducts(relatedProducts);
        }
        return response;
    }
}