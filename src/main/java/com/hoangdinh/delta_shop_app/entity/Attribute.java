package com.hoangdinh.delta_shop_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attributes")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String type = "TEXT"; // TEXT, COLOR, SIZE, NUMBER

    @Column(length = 20)
    private String unit; // For numeric attributes (kg, cm, etc.)

    @Column(name = "is_filterable", nullable = false)
    @Builder.Default
    private boolean filterable = true;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private boolean required = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VariantAttributeValue> attributeValues = new ArrayList<>();

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AttributeOption> options = new ArrayList<>();
}