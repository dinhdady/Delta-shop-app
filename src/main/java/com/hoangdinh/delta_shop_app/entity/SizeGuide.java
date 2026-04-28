package com.hoangdinh.delta_shop_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeGuide {
    private String size;           // S, M, L, XL, 38, 39, 40, ...
    private String label;          // "Small", "Medium", hoặc "Size 38"
    private String description;    // "Cân nặng 40-50kg" hoặc "Chiều dài chân 25-26cm"
    private String measurement;    // "40-50kg" hoặc "25cm"
    private Integer stockQuantity; // Số lượng tồn cho size này (optional)
    private BigDecimal priceModifier; // Điều chỉnh giá theo size (optional)
    private String sku;            // SKU riêng cho size (optional)
}
