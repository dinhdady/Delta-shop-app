package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.response.auth.VerificationTokenResponse;
import com.hoangdinh.delta_shop_app.entity.User;

import java.util.UUID;

public interface VerificationTokenService {

    /**
     * Create verification token for user
     * @param user user entity
     * @param type token type
     * @param expirationHours hours until expiration
     * @return created token string
     */
    String createToken(User user, String type, long expirationHours);

    /**
     * Validate token
     * @param token token string
     * @param type token type
     * @return true if valid
     */
    boolean validateToken(String token, String type);

    /**
     * Get user by valid token
     * @param token token string
     * @param type token type
     * @return user entity
     */
    User getUserByValidToken(String token, String type);

    /**
     * Mark token as used
     * @param token token string
     */
    void markTokenAsUsed(String token);

    /**
     * Delete expired tokens
     * @return number of deleted tokens
     */
    int deleteExpiredTokens();

    /**
     * Get token info
     * @param token token string
     * @return token response
     */
    VerificationTokenResponse getTokenInfo(String token);

    /**
     * Invalidate all user tokens
     * @param userId user ID
     */
    void invalidateUserTokens(UUID userId);
}