package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Payment;
import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Query by order
    List<Payment> findByOrderId(UUID orderId);
    // THÊM METHOD NÀY - hỗ trợ phân trang
    Page<Payment> findByOrderId(UUID orderId, Pageable pageable);
    // Query by gateway
    Optional<Payment> findByGatewayTxnId(String gatewayTxnId);
    Optional<Payment> findByGatewayRefId(String gatewayRefId);

    // Query by transaction number
    Optional<Payment> findByTransactionNo(String transactionNo);

    // Query by status
    List<Payment> findByStatus(PaymentStatus status);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    // Query by date range
    List<Payment> findByCreatedAtBetween(ZonedDateTime startDate, ZonedDateTime endDate);
    List<Payment> findByPaidAtBetween(ZonedDateTime startDate, ZonedDateTime endDate);

    // Statistics
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaidAmountBetween(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate")
    long getTotalSuccessfulPaymentsBetween(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.refundAmount), 0) FROM Payment p WHERE p.status IN ('REFUNDED', 'PARTIALLY_REFUNDED') AND p.refundedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRefundedAmountBetween(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);

    // Update operations
    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id = :id")
    void updatePaymentStatus(@Param("id") UUID id, @Param("status") PaymentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.status = 'FAILED', p.failureReason = :reason, p.failedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.status = 'PENDING'")
    void markAsFailed(@Param("id") UUID id, @Param("reason") String reason);

    // Cleanup
    @Modifying
    @Transactional
    @Query("DELETE FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :timeout")
    void deleteExpiredPendingPayments(@Param("timeout") ZonedDateTime timeout);

    // Dashboard statistics
    @Query("SELECT DATE(p.paidAt), COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paidAt >= :startDate GROUP BY DATE(p.paidAt) ORDER BY DATE(p.paidAt)")
    List<Object[]> getDailyPaymentStats(@Param("startDate") ZonedDateTime startDate);

    @Query("SELECT p.gateway, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' GROUP BY p.gateway")
    List<Object[]> getPaymentMethodStats();
}