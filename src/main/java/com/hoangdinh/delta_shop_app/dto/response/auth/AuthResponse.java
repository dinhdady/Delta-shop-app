package com.hoangdinh.delta_shop_app.dto.response.auth;

import com.hoangdinh.delta_shop_app.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UUID userId;
    private String email;
    private String fullName;
    private String role;
    private boolean emailVerified;

    // Thêm các trường cho response đăng ký
    private boolean success;
    private String message;
    private boolean requiresVerification;

    public static AuthResponse from(User user, String accessToken, String refreshToken, Long expiresIn) {
        if (user == null) return null;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .emailVerified(user.isEmailVerified())
                .success(true)
                .build();
    }

    // Factory method for registration response (no tokens)
    public static AuthResponse forRegistration(User user, String message, boolean requiresVerification) {
        if (user == null) return null;

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .emailVerified(user.isEmailVerified())
                .success(true)
                .message(message)
                .requiresVerification(requiresVerification)
                .build();
    }

    // Factory method for error response
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}