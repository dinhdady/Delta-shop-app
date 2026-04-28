package com.hoangdinh.delta_shop_app.dto.response.user;

import com.hoangdinh.delta_shop_app.enums.UserRole;
import com.hoangdinh.delta_shop_app.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private UserRole role;
    private UserStatus status;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Integer loyaltyPoints;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}