package com.hoangdinh.delta_shop_app.dto.request.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoangdinh.delta_shop_app.entity.SizeGuide;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductUpdateRequest {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("categoryId")
    private UUID categoryId;
    
    @JsonProperty("brandId")
    private UUID brandId;
    
    @JsonProperty("shortDescription")
    private String shortDescription;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("basePrice")
    private BigDecimal basePrice;
    
    @JsonProperty("comparePrice")
    private BigDecimal comparePrice;
    
    @JsonProperty("costPrice")
    private BigDecimal costPrice;
    
    @JsonProperty("status")
    private ProductStatus status;
    
    @JsonProperty("featured")
    private Boolean featured;
    
    @JsonProperty("newArrival")
    private Boolean newArrival;
    
    @JsonProperty("bestSeller")
    private Boolean bestSeller;
    
    @JsonProperty("weight")
    private BigDecimal weight;
    
    @JsonProperty("length")
    private BigDecimal length;
    
    @JsonProperty("width")
    private BigDecimal width;
    
    @JsonProperty("height")
    private BigDecimal height;
    
    @JsonProperty("sku")
    private String sku;
    
    @JsonProperty("stockQuantity")
    private Integer stockQuantity;
    
    @JsonProperty("tags")
    private List<String> tags;
    
    @JsonProperty("sportTypes")
    private List<String> sportTypes;
    
    @JsonProperty("images")
    private List<ProductImageRequest> images;
    @JsonProperty("sizeGuides")
    private List<SizeGuide> sizeGuides;

    @JsonProperty("specifications")
    private Map<String, Object> specifications;
}