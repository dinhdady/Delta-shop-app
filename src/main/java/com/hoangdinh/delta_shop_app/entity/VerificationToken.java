package com.hoangdinh.delta_shop_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false)
    private String type; // EMAIL_VERIFICATION, PASSWORD_RESET

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "used")
    private boolean used; // Đánh dấu token đã được sử dụng chưa

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        used = false;
    }

    // Check if token is valid (not expired and not used)
    public boolean isValid() {
        return expiryDate != null && expiryDate.isAfter(LocalDateTime.now()) && !used;
    }

    // Check if token has expired
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    // Check if token has been used
    public boolean isUsed() {
        return used;
    }

    // Mark token as used
    public void markAsUsed() {
        this.used = true;
    }

    // Getter for expiresAt (alias for expiryDate)
    public LocalDateTime getExpiresAt() {
        return expiryDate;
    }
}