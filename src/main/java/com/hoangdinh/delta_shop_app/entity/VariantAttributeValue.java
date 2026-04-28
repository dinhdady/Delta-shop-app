package com.hoangdinh.delta_shop_app.entity;

import com.hoangdinh.delta_shop_app.entity.Attribute;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "variant_attribute_values")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(nullable = false, length = 200)
    private String value;

    @Column(name = "display_value", length = 200)
    private String displayValue;

    @Column(name = "color_code", length = 10)
    private String colorCode; // For color attributes, store hex code

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}