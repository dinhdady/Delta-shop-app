package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByTrackingNumber(String trackingNumber);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND " +
           "(:query IS NULL OR :query = '' OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Order> searchOrdersByUser(@Param("userId") UUID userId,
                                   @Param("query") String query,
                                   Pageable pageable);

    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED'")
    Double getTotalSpentByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED'")
    Long getOrderCountByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')
            AND o.paymentStatus = 'PAID'
            """)
    BigDecimal getTotalRevenue();

    @Query("""
            SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o
            WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')
            AND o.paymentStatus = 'PAID'
            """)
    BigDecimal getAverageOrderValue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.createdAt BETWEEN :startDate AND :endDate
            AND o.status NOT IN ('CANCELLED', 'REFUNDED')
            AND o.paymentStatus = 'PAID'
            """)
    BigDecimal getTotalRevenueForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o
            WHERE o.createdAt BETWEEN :startDate AND :endDate
            AND o.status NOT IN ('CANCELLED', 'REFUNDED')
            AND o.paymentStatus = 'PAID'
            """)
    BigDecimal getAverageOrderValueForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(o.shippingName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Order> searchOrdersAdmin(@Param("status") OrderStatus status,
                                  @Param("query") String query,
                                  Pageable pageable);
}
