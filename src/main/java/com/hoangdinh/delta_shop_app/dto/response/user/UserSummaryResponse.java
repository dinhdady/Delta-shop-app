package com.hoangdinh.delta_shop_app.dto.response.user;

import com.hoangdinh.delta_shop_app.enums.UserRole;
import com.hoangdinh.delta_shop_app.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private UserRole role;
    private UserStatus status;
    private Integer loyaltyPoints;
    private BigDecimal totalSpent;
    private LocalDateTime createdAt;
}