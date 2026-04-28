package com.hoangdinh.delta_shop_app.entity;

import com.hoangdinh.delta_shop_app.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 50)
    private String gateway; // VNPAY, MOMO, BANK_TRANSFER, CREDIT_CARD

    @Column(name = "gateway_txn_id", length = 255)
    private String gatewayTxnId; // External transaction ID from gateway

    @Column(name = "gateway_ref_id", length = 255)
    private String gatewayRefId; // Reference ID from gateway

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_response", columnDefinition = "JSON")
    private String gatewayResponse; // Raw response from payment gateway

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "payment_method_detail", length = 100)
    private String paymentMethodDetail; // e.g., "Bank: VCB", "Card: ****1234"

    @Column(name = "bank_code", length = 50)
    private String bankCode; // Bank code for bank transfer

    @Column(name = "card_type", length = 50)
    private String cardType; // VISA, MASTERCARD, JCB, etc.

    @Column(name = "transaction_no", length = 100)
    private String transactionNo; // Internal transaction number

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isSuccessful() {
        return status == PaymentStatus.PAID;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }

    public boolean isPartiallyRefunded() {
        return status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    public boolean canRefund() {
        return status == PaymentStatus.PAID && (refundAmount == null || refundAmount.compareTo(amount) < 0);
    }

    public BigDecimal getRemainingRefundableAmount() {
        if (refundAmount == null) return amount;
        return amount.subtract(refundAmount);
    }

    public void markAsPaid(String gatewayTxnId, String gatewayResponse) {
        this.status = PaymentStatus.PAID;
        this.gatewayTxnId = gatewayTxnId;
        this.gatewayResponse = gatewayResponse;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason, String gatewayResponse) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.gatewayResponse = gatewayResponse;
        this.failedAt = LocalDateTime.now();
    }


    public void markAsRefunded(BigDecimal refundAmount, String reason) {
        if (refundAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.refundedAt = LocalDateTime.now();
    }
    // THÊM METHOD NÀY
    public boolean isFullyRefunded() {
        return status == PaymentStatus.REFUNDED;
    }
}