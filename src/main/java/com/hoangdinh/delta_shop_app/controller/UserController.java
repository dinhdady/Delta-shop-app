package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.auth.ChangePasswordRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.*;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserAddressResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserProfileResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserStatisticsResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserSummaryResponse;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import com.hoangdinh.delta_shop_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "APIs for user management")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    // ========== USER PROFILE ENDPOINTS ==========

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update current user profile")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PostMapping("/me/change-password")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change user password")
    public ResponseEntity<?> changePassword(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody ChangePasswordRequest request,
            BindingResult bindingResult) {

        log.info("Received change password request for user: {}", userId);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            userService.changePassword(userId, request);
            log.info("Password changed successfully for user: {}", userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully",
                    "success", true
            ));
        } catch (Exception e) {
            log.error("Failed to change password: ", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "success", false
            ));
        }
    }

    @PostMapping("/me/avatar")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload user avatar")
    public ResponseEntity<String> uploadAvatar(
            @RequestAttribute("userId") UUID userId,
            @RequestParam("file") MultipartFile file) {
        var uploadResult = cloudinaryService.uploadUserAvatar(file, userId.toString());
        String avatarUrl = (String) uploadResult.get("secure_url");
        userService.uploadAvatar(userId, avatarUrl);
        return ResponseEntity.ok(avatarUrl);
    }

    @DeleteMapping("/me/avatar")
//    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete user avatar")
    public ResponseEntity<Void> deleteAvatar(@RequestAttribute("userId") UUID userId) {
        userService.deleteAvatar(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/statistics")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getUserStatistics(userId));
    }

    @GetMapping("/me/loyalty-points")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user loyalty points")
    public ResponseEntity<Integer> getLoyaltyPoints(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getUserLoyaltyPoints(userId));
    }

    // ========== ADDRESS ENDPOINTS ==========

    @GetMapping("/me/addresses")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user addresses")
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getUserAddresses(userId));
    }

    @GetMapping("/me/addresses/default")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get default address")
    public ResponseEntity<UserAddressResponse> getDefaultAddress(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(userService.getDefaultAddress(userId));
    }

    @PostMapping("/me/addresses")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Add new address")
    public ResponseEntity<UserAddressResponse> addAddress(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody AddAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(userId, request));
    }

    @PutMapping("/me/addresses/{addressId}")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update address")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(userService.updateAddress(addressId, userId, request));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete address")
    public ResponseEntity<Void> deleteAddress(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID addressId) {
        userService.deleteAddress(addressId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Set default address")
    public ResponseEntity<Void> setDefaultAddress(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID addressId) {
        userService.setDefaultAddress(addressId, userId);
        return ResponseEntity.ok().build();
    }

    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<PageResponse<UserSummaryResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, role, status, keyword));
    }

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/admin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update user (Admin only)")
    public ResponseEntity<UserProfileResponse> updateUserByAdmin(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserByAdmin(userId, request));
    }

    @PatchMapping("/admin/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update user status (Admin only)")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam String status,
            @RequestAttribute("userId") UUID adminId) {
        userService.updateUserStatus(userId, status, adminId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Assign user role (Super Admin only)")
    public ResponseEntity<Void> assignUserRole(
            @PathVariable UUID userId,
            @RequestParam String role,
            @RequestAttribute("userId") UUID adminId) {
        userService.assignUserRole(userId, role, adminId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @RequestAttribute("userId") UUID adminId) {
        userService.deleteUser(userId, adminId);
        return ResponseEntity.noContent().build();
    }
}
