package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.auth.*;
import com.hoangdinh.delta_shop_app.dto.request.auth.ChangePasswordRequest;
import com.hoangdinh.delta_shop_app.dto.response.auth.AuthResponse;
import com.hoangdinh.delta_shop_app.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface AuthService {

    /**
     * Register new user
     * @param request registration request
     * @return authentication response with tokens
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Login user
     * @param request login request
     * @param ipAddress client IP address
     * @param userAgent client user agent
     * @return authentication response with tokens
     */
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Refresh access token using refresh token
     * @param request refresh token request
     * @return new authentication response
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user by revoking refresh token
     * @param refreshToken refresh token to revoke
     */
    void logout(String refreshToken);

    /**
     * Verify user email with token
     * @param token verification token
     */
    void verifyEmail(VerifyEmailRequest request);

    /**
     * Request password reset email
     * @param request forgot password request
     */
    void requestPasswordReset(ForgotPasswordRequest request);

    /**
     * Reset password with token
     * @param request reset password request
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Resend verification email
     * @param email user email
     */
    void resendVerificationEmail(String email);

    /**
     * Change password for authenticated user
     * @param userId user ID
     * @param request change password request
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * Verify if user is authenticated
     * @param token access token
     * @return user details
     */
    UserDetails verifyToken(String token);

    /**
     * Get current user from token
     * @param token access token
     * @return user entity
     */
    User getCurrentUser(String token);

    /**
     * Validate refresh token
     * @param refreshToken refresh token
     * @return true if valid
     */
    boolean validateRefreshToken(String refreshToken);

    /**
     * Revoke all user sessions
     * @param userId user ID
     */
    void revokeAllUserSessions(UUID userId);
}