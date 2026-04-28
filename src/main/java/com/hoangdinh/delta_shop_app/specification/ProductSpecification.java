package com.hoangdinh.delta_shop_app.specification;

import com.hoangdinh.delta_shop_app.dto.request.product.ProductFilterRequest;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> build(ProductFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Soft delete
            predicates.add(cb.isNull(root.get("deletedAt")));

            // Status
            if (filter.isPublicOnly()) {
                predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
            } else if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Category
            if (filter.getCategoryId() != null) {
                Join<Object, Object> category = root.join("category", JoinType.INNER);
                if (Boolean.TRUE.equals(filter.getIncludeSubcategories())) {
                    predicates.add(
                            cb.or(
                                    cb.equal(category.get("id"), filter.getCategoryId()),
                                    cb.like(category.get("path"), "%" + filter.getCategoryId() + "%")
                            )
                    );
                } else {
                    predicates.add(cb.equal(category.get("id"), filter.getCategoryId()));
                }
            }

            // Brand
            if (filter.getBrandId() != null) {
                Join<Object, Object> brand = root.join("brand", JoinType.LEFT);
                predicates.add(cb.equal(brand.get("id"), filter.getBrandId()));
            }

            // Multiple brands
            if (filter.getBrandIds() != null && !filter.getBrandIds().isEmpty()) {
                Join<Object, Object> brand = root.join("brand", JoinType.LEFT);
                predicates.add(brand.get("id").in(filter.getBrandIds()));
            }

            // Price range
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("basePrice"), BigDecimal.valueOf(filter.getMinPrice())));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("basePrice"), BigDecimal.valueOf(filter.getMaxPrice())));
            }

            // Rating filter
            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("averageRating"), BigDecimal.valueOf(filter.getMinRating())));
            }

            // Sport types
            if (filter.getSportType() != null && !filter.getSportType().isEmpty()) {
                // Sửa cách kiểm tra sportTypes
                predicates.add(cb.isTrue(cb.literal(true)));
            }

            // Featured / New / Best seller flags
            if (Boolean.TRUE.equals(filter.getFeatured())) {
                predicates.add(cb.isTrue(root.get("featured")));
            }
            if (Boolean.TRUE.equals(filter.getNewArrival())) {
                predicates.add(cb.isTrue(root.get("newArrival")));
            }
            if (Boolean.TRUE.equals(filter.getBestSeller())) {
                predicates.add(cb.isTrue(root.get("bestSeller")));
            }

            // In stock only - SỬA LẠI PHẦN NÀY
            if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                // Cách 1: Kiểm tra qua variants
                Join<Product, ProductVariant> variants = root.join("variants", JoinType.LEFT);
                predicates.add(cb.greaterThan(variants.get("stockQuantity"), 0));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String buildTsQuery(String keyword) {
        // Handle multi-word search
        String[] words = keyword.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(" & ");
            sb.append(words[i]).append(":*");
        }
        return sb.toString();
    }
}
