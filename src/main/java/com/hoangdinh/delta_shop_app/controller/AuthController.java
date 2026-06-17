package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.auth.*;
import com.hoangdinh.delta_shop_app.dto.response.auth.AuthResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserResponse;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.service.AuthService;
import com.hoangdinh.delta_shop_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for authentication and authorization")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        log.info("=== REGISTER REQUEST ===");
        log.info("Email: {}", request.getEmail());
        log.info("FirstName: {}", request.getFirstName());
        log.info("LastName: {}", request.getLastName());
        log.info("Phone: {}", request.getPhone());
        log.info("Password length: {}", request.getPassword() != null ? request.getPassword().length() : 0);
        log.info("ConfirmPassword length: {}", request.getConfirmPassword() != null ? request.getConfirmPassword().length() : 0);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            log.error("Validation errors: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            log.error("Business error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        try {
            return ResponseEntity.ok(authService.login(request, ipAddress, userAgent));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String refreshToken = authorization.replace("Bearer ", "");
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<Void> changePassword(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestAttribute("userId") UUID userId) {
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .email(userService.getProfile(userId).getEmail())
                .phone(userService.getProfile(userId).getPhone())
                .firstName(userService.getProfile(userId).getFirstName())
                .lastName(userService.getProfile(userId).getLastName())
                .fullName(userService.getProfile(userId).getFullName())
                .avatarUrl(userService.getProfile(userId).getAvatarUrl())
                .dateOfBirth(userService.getProfile(userId).getDateOfBirth())
                .role(userService.getProfile(userId).getRole())
                .status(userService.getProfile(userId).getStatus())
                .emailVerified(userService.getProfile(userId).isEmailVerified())
                .phoneVerified(userService.getProfile(userId).isPhoneVerified())
                .loyaltyPoints(userService.getProfile(userId).getLoyaltyPoints())
                .lastLoginAt(userService.getProfile(userId).getLastLoginAt())
                .createdAt(userService.getProfile(userId).getCreatedAt())
                .build();
        return ResponseEntity.ok(userResponse);
    }
}
