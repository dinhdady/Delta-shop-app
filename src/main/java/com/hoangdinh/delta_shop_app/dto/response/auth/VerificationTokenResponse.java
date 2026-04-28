package com.hoangdinh.delta_shop_app.dto.response.auth;

import com.hoangdinh.delta_shop_app.entity.VerificationToken;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VerificationTokenResponse {
    private UUID id;
    private UUID userId;
    private String type;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean isValid;

    public static VerificationTokenResponse from(VerificationToken token) {
        if (token == null) return null;

        return VerificationTokenResponse.builder()
                .id(token.getId())
                .userId(token.getUser() != null ? token.getUser().getId() : null)
                .type(token.getType())
                .expiresAt(token.getExpiresAt())
                .createdAt(token.getCreatedAt())
                .isValid(token.isValid())
                .build();
    }
}