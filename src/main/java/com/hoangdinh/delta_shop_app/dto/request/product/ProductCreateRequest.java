package com.hoangdinh.delta_shop_app.dto.request.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoangdinh.delta_shop_app.entity.SizeGuide;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "Danh mục không được để trống")
    @JsonProperty("categoryId")
    private UUID categoryId;

    @JsonProperty("brandId")
    private UUID brandId;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("shortDescription")
    private String shortDescription;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    @JsonProperty("basePrice")
    private BigDecimal basePrice;

    @JsonProperty("comparePrice")
    private BigDecimal comparePrice;

    @JsonProperty("costPrice")
    private BigDecimal costPrice;

    @JsonProperty("weight")
    private BigDecimal weight;

    @JsonProperty("length")
    private BigDecimal length;

    @JsonProperty("width")
    private BigDecimal width;

    @JsonProperty("height")
    private BigDecimal height;

    @JsonProperty("featured")
    private Boolean featured;

    @JsonProperty("newArrival")
    private Boolean newArrival;

    @JsonProperty("bestSeller")
    private Boolean bestSeller;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("sportTypes")
    private List<String> sportTypes;

    @JsonProperty("status")
    private ProductStatus status;

    @JsonProperty("stockQuantity")
    private Integer stockQuantity;

    @JsonProperty("variants")
    private List<VariantRequest> variants;

    @JsonProperty("images")
    private List<ProductImageRequest> images;
    @JsonProperty("sizeGuides")
    private List<SizeGuide> sizeGuides;

    @JsonProperty("specifications")
    private Map<String, Object> specifications;
}