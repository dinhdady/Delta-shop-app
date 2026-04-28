package com.hoangdinh.delta_shop_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "product_variants")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 150)
    private String sku;

    @Column(length = 300)
    private String name;

    @Column(name = "price_modifier", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceModifier = BigDecimal.ZERO;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "min_stock_alert", nullable = false)
    @Builder.Default
    private Integer minStockAlert = 5;

    @Column(length = 100)
    private String barcode;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VariantAttributeValue> attributeValues = new ArrayList<>();

    public Integer getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }

    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }

    public BigDecimal getFinalPrice() {
        return product.getBasePrice().add(priceModifier);
    }
    public String getAttributeValue(String attributeCode) {
        return attributeValues.stream()
                .filter(vav -> vav.getAttribute() != null &&
                        attributeCode.equals(vav.getAttribute().getCode()))
                .findFirst()
                .map(VariantAttributeValue::getValue)
                .orElse(null);
    }
    public String getDisplayImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        // Nếu variant không có ảnh, lấy ảnh từ product
        if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
            ProductImage primaryImage = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .orElse(product.getImages().get(0));
            return primaryImage.getUrl();
        }
        return null;
    }
    public UUID getProductId() {
        return product != null ? product.getId() : null;
    }
    // Helper method to get all attributes as map
    public Map<String, String> getAttributesAsMap() {
        return attributeValues.stream()
                .filter(vav -> vav.getAttribute() != null)
                .collect(Collectors.toMap(
                        vav -> vav.getAttribute().getCode(),
                        VariantAttributeValue::getValue,
                        (v1, v2) -> v1
                ));
    }
}