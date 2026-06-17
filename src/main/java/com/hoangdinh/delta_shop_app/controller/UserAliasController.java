package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.user.AddAddressRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.UpdateAddressRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.UpdateProfileRequest;
import com.hoangdinh.delta_shop_app.dto.response.user.UserAddressResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserProfileResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAliasController {

    private final UserController userController;

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestAttribute("userId") UUID userId) {
        return userController.getProfile(userId);
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return userController.updateProfile(userId, request);
    }

    @GetMapping("/me/addresses")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user addresses")
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses(@RequestAttribute("userId") UUID userId) {
        return userController.getUserAddresses(userId);
    }

    @PostMapping("/me/addresses")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add new address")
    public ResponseEntity<UserAddressResponse> addAddress(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody AddAddressRequest request) {
        return userController.addAddress(userId, request);
    }

    @PutMapping("/me/addresses/{addressId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update address")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        return userController.updateAddress(addressId, userId, request);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete address")
    public ResponseEntity<Void> deleteAddress(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID addressId) {
        userController.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/statistics")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@RequestAttribute("userId") UUID userId) {
        return userController.getUserStatistics(userId);
    }

    @PostMapping("/me/avatar")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload user avatar")
    public ResponseEntity<UserProfileResponse> uploadAvatar(
            @RequestAttribute("userId") UUID userId,
            @RequestParam("file") MultipartFile file) {
        return userController.uploadAvatar(userId, file);
    }

    @DeleteMapping("/me/avatar")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete user avatar")
    public ResponseEntity<Void> deleteAvatar(@RequestAttribute("userId") UUID userId) {
        return userController.deleteAvatar(userId);
    }
}
