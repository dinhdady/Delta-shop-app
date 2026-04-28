package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.dto.response.dashboard.TopProductStatsDto;
import com.hoangdinh.delta_shop_app.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    List<OrderItem> findByProductId(UUID productId);

    List<OrderItem> findByVariantId(UUID variantId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId AND oi.isReviewed = false")
    List<OrderItem> findUnreviewedItemsByUser(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderItem oi SET oi.isReviewed = true WHERE oi.id = :id")
    void markAsReviewed(@Param("id") UUID id);

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.status = 'DELIVERED'")
    long countSoldItemsByProduct(@Param("productId") UUID productId);

//    // QUERY ĐÚNG - Sửa đường dẫn relationship
//    @Query("""
//        SELECT new com.hoangdinh.delta_shop_app.dto.response.dashboard.TopProductStatsDto(
//            p.id,
//            p.name,
//            p.slug,
//            (SELECT pi.url FROM ProductImage pi WHERE pi.product = p AND pi.isPrimary = true),
//            c.name,
//            b.name,
//            SUM(oi.quantity),
//            SUM(oi.unitPrice * oi.quantity),
//            AVG(oi.unitPrice)
//        )
//        FROM OrderItem oi
//        JOIN oi.variant pv
//        JOIN pv.product p
//        LEFT JOIN p.category c
//        LEFT JOIN p.brand b
//        JOIN oi.order o
//        WHERE o.status = 'DELIVERED'
//        GROUP BY p.id, p.name, c.name, b.name
//        ORDER BY SUM(oi.quantity) DESC
//    """)
//    List<TopProductStatsDto> findTopSellingProducts(Pageable pageable);
//
//    // QUERY ĐÚNG - Có filter theo ngày
//    @Query("""
//        SELECT new com.hoangdinh.delta_shop_app.dto.response.dashboard.TopProductStatsDto(
//            p.id,
//            p.name,
//            p.slug,
//            (SELECT pi.url FROM ProductImage pi WHERE pi.product = p AND pi.isPrimary = true),
//            c.name,
//            b.name,
//            SUM(oi.quantity),
//            SUM(oi.unitPrice * oi.quantity),
//            AVG(oi.unitPrice)
//        )
//        FROM OrderItem oi
//        JOIN oi.variant pv
//        JOIN pv.product p
//        LEFT JOIN p.category c
//        LEFT JOIN p.brand b
//        JOIN oi.order o
//        WHERE o.status = 'DELIVERED'
//        AND o.createdAt BETWEEN :startDate AND :endDate
//        GROUP BY p.id, p.name, c.name, b.name
//        ORDER BY SUM(oi.quantity) DESC
//    """)
//    List<TopProductStatsDto> findTopSellingProductsByDateRange(
//            @Param("startDate") LocalDateTime startDate,
//            @Param("endDate") LocalDateTime endDate,
//            Pageable pageable
//    );
}