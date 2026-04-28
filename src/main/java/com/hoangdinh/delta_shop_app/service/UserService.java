package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.user.UpdateProfileRequest;
import com.hoangdinh.delta_shop_app.dto.request.auth.ChangePasswordRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.AddAddressRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.UpdateAddressRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.AdminUserUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserProfileResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserAddressResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserStatisticsResponse;
import org.apache.coyote.BadRequestException;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // User profile operations
    UserProfileResponse getProfile(UUID userId);
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
    void changePassword(UUID userId, ChangePasswordRequest request) throws BadRequestException;
    void uploadAvatar(UUID userId, String avatarUrl);
    void deleteAvatar(UUID userId);

    // Address management
    UserAddressResponse addAddress(UUID userId, AddAddressRequest request);
    UserAddressResponse updateAddress(UUID addressId, UUID userId, UpdateAddressRequest request);
    void deleteAddress(UUID addressId, UUID userId);
    void setDefaultAddress(UUID addressId, UUID userId);
    List<UserAddressResponse> getUserAddresses(UUID userId);
    UserAddressResponse getDefaultAddress(UUID userId);

    // Loyalty points
    int getUserLoyaltyPoints(UUID userId);
    void addLoyaltyPoints(UUID userId, int points, String reason);
    void deductLoyaltyPoints(UUID userId, int points, String reason);

    // Admin operations
    PageResponse<UserSummaryResponse> getAllUsers(int page, int size, String role, String status, String keyword);
    UserProfileResponse getUserById(UUID userId);
    UserProfileResponse updateUserByAdmin(UUID userId, AdminUserUpdateRequest request);
    void updateUserStatus(UUID userId, String status, UUID adminId);
    void assignUserRole(UUID userId, String role, UUID adminId);
    void deleteUser(UUID userId, UUID adminId);

    // Statistics
    UserStatisticsResponse getUserStatistics(UUID userId);
    long getTotalUserCount();
    long getNewUserCountByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate);

    // Helper methods
    boolean isEmailExist(String email);
    boolean isPhoneExist(String phone);
    void validateUserStatus(UUID userId);
}