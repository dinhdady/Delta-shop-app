package com.hoangdinh.delta_shop_app.entity;

import com.hoangdinh.delta_shop_app.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DiscountType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_per_user", nullable = false)
    @Builder.Default
    private Integer usagePerUser = 1;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "applies_to", nullable = false, length = 20)
    @Builder.Default
    private String appliesTo = "ALL";  // ALL, CATEGORY, PRODUCT, BRAND

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_stackable", nullable = false)
    @Builder.Default
    private boolean isStackable = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromotionItem> promotionItems = new ArrayList<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PromotionUsage> usages = new ArrayList<>();

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                startsAt.isBefore(now) &&
                (endsAt == null || endsAt.isAfter(now)) &&
                (usageLimit == null || usedCount < usageLimit);
    }

    public boolean isApplicableToAll() {
        return "ALL".equalsIgnoreCase(appliesTo);
    }

    public boolean isApplicableToCategory() {
        return "CATEGORY".equalsIgnoreCase(appliesTo);
    }

    public boolean isApplicableToProduct() {
        return "PRODUCT".equalsIgnoreCase(appliesTo);
    }

    public boolean isApplicableToBrand() {
        return "BRAND".equalsIgnoreCase(appliesTo);
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (minOrderAmount != null && subtotal.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = switch (type) {
            case PERCENTAGE -> subtotal.multiply(value).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> value.min(subtotal);
            case FREE_SHIPPING -> BigDecimal.ZERO;
            case BUY_X_GET_Y -> BigDecimal.ZERO; // Complex logic handled separately
        };

        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        return discount;
    }

    public boolean canUserUse(UUID userId, long userUsedCount) {
        if (usagePerUser == null) return true;
        return userUsedCount < usagePerUser;
    }
}