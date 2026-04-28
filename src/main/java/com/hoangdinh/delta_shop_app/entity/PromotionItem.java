package com.hoangdinh.delta_shop_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "promotion_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PromotionItemId.class)
public class PromotionItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Id
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Id
    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;  // CATEGORY, PRODUCT, BRAND
}

// Composite key class
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class PromotionItemId implements Serializable {
    private UUID promotion;
    private UUID itemId;
    private String itemType;
}