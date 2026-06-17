package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.auth.ChangePasswordRequest;
import com.hoangdinh.delta_shop_app.dto.request.user.*;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserAddressResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserProfileResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserStatisticsResponse;
import com.hoangdinh.delta_shop_app.dto.response.user.UserSummaryResponse;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.entity.UserAddress;
import com.hoangdinh.delta_shop_app.enums.OrderStatus;
import com.hoangdinh.delta_shop_app.enums.UserRole;
import com.hoangdinh.delta_shop_app.enums.UserStatus;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.OrderRepository;
import com.hoangdinh.delta_shop_app.repository.UserAddressRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import com.hoangdinh.delta_shop_app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Override
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToProfileResponse(user);
    }
    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("Số điện thoại đã được sử dụng");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        User saved = userRepository.save(user);
        log.info("User profile updated: {}", userId);

        return mapToProfileResponse(saved);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("=== START changePassword for user: {} ===", userId);

        // 1. Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.error("Password confirmation mismatch");
            try {
                throw new BadRequestException("Mật khẩu xác nhận không khớp");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        // 2. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found",null,null));

        log.info("User found: {}", user.getEmail());

        // 3. Verify current password
        boolean matches = passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash());
        log.info("Current password matches: {}", matches);

        if (!matches) {
            log.error("Current password incorrect for user: {}", userId);
            try {
                throw new BadRequestException("Mật khẩu hiện tại không đúng");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        // 4. Check if new password is same as old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            log.error("New password is same as old password");
            try {
                throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        // 5. Validate password strength
        if (!isValidPassword(request.getNewPassword())) {
            try {
                throw new BadRequestException("Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        // 6. Encode and set new password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());

        // 7. Save to database
        userRepository.saveAndFlush(user);

        log.info("=== END changePassword - SUCCESS for user: {} ===", userId);
    }
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // Check for at least one digit, one lowercase, one uppercase
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$");
    }
    @Override
    @Transactional
    public void uploadAvatar(UUID userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Cloudinary overwrites the fixed avatar public ID. Deleting the old URL here
        // would delete the newly uploaded asset as well.
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        log.info("Avatar uploaded for user: {}", userId);
    }

    @Override
    @Transactional
    public void deleteAvatar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getAvatarUrl() != null && user.getAvatarUrl().contains("cloudinary")) {
            String publicId = extractPublicId(user.getAvatarUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId);
            }
        }

        user.setAvatarUrl(null);
        userRepository.save(user);
        log.info("Avatar deleted for user: {}", userId);
    }

    @Override
    @Transactional
    public UserAddressResponse addAddress(UUID userId, AddAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserAddress address = UserAddress.builder()
                .user(user)
                .type(request.getType())
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .streetAddress(request.getStreetAddress())
                .postalCode(request.getPostalCode())
                .isDefault(request.isDefault())
                .build();

        // If this is default, remove default from other addresses
        if (request.isDefault()) {
            addressRepository.removeDefaultFlag(userId);
        }

        UserAddress saved = addressRepository.save(address);
        log.info("Address added for user: {}", userId);

        return mapToAddressResponse(saved);
    }

    @Override
    @Transactional
    public UserAddressResponse updateAddress(UUID addressId, UUID userId, UpdateAddressRequest request) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền sửa địa chỉ này");
        }

        if (request.getRecipientName() != null) address.setRecipientName(request.getRecipientName());
        if (request.getPhone() != null) address.setPhone(request.getPhone());
        if (request.getProvince() != null) address.setProvince(request.getProvince());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getWard() != null) address.setWard(request.getWard());
        if (request.getStreetAddress() != null) address.setStreetAddress(request.getStreetAddress());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getType() != null) address.setType(request.getType());

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.removeDefaultFlag(userId);
            address.setDefault(true);
        } else if (!request.isDefault() && address.isDefault()) {
            address.setDefault(false);
        }

        UserAddress saved = addressRepository.save(address);
        log.info("Address updated for user: {}", userId);

        return mapToAddressResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressId, UUID userId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền xóa địa chỉ này");
        }

        addressRepository.delete(address);
        log.info("Address deleted for user: {}", userId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(UUID addressId, UUID userId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền đặt địa chỉ mặc định này");
        }

        addressRepository.removeDefaultFlag(userId);
        address.setDefault(true);
        addressRepository.save(address);
        log.info("Default address set for user: {}", userId);
    }

    @Override
    public List<UserAddressResponse> getUserAddresses(UUID userId) {
        List<UserAddress> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        return addresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressResponse getDefaultAddress(UUID userId) {
        UserAddress address = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElse(null);
        return address != null ? mapToAddressResponse(address) : null;
    }

    @Override
    public int getUserLoyaltyPoints(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return user.getLoyaltyPoints();
    }

    @Override
    @Transactional
    public void addLoyaltyPoints(UUID userId, int points, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
        userRepository.save(user);
        log.info("Added {} loyalty points to user {}: {}", points, userId, reason);
    }

    @Override
    @Transactional
    public void deductLoyaltyPoints(UUID userId, int points, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        int newPoints = Math.max(0, user.getLoyaltyPoints() - points);
        user.setLoyaltyPoints(newPoints);
        userRepository.save(user);
        log.info("Deducted {} loyalty points from user {}: {}", points, userId, reason);
    }

    @Override
    public PageResponse<UserSummaryResponse> getAllUsers(int page, int size, String role, String status, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users;

        if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.searchUsers(keyword, pageable);
        } else if (role != null && status != null) {
            users = userRepository.findByRoleAndStatusAndDeletedAtIsNull(UserRole.valueOf(role), UserStatus.valueOf(status), pageable);
        } else if (role != null) {
            users = userRepository.findByRoleAndDeletedAtIsNull(UserRole.valueOf(role), pageable);
        } else if (status != null) {
            users = userRepository.findByStatusAndDeletedAtIsNull(UserStatus.valueOf(status), pageable);
        } else {
            users = userRepository.findByDeletedAtIsNull(pageable);
        }

        return PageResponse.of(users.map(this::mapToSummaryResponse));
    }

    @Override
    public UserProfileResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserByAdmin(UUID userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getRole() != null) user.setRole(UserRole.valueOf(request.getRole()));

        User saved = userRepository.save(user);
        log.info("User updated by admin: {}", userId);

        return mapToProfileResponse(saved);
    }

    @Override
    @Transactional
    public void updateUserStatus(UUID userId, String status, UUID adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setStatus(UserStatus.valueOf(status));
        userRepository.save(user);
        log.info("User status updated: {} -> {} by admin {}", userId, status, adminId);
    }

    @Override
    @Transactional
    public void assignUserRole(UUID userId, String role, UUID adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(UserRole.valueOf(role));
        userRepository.save(user);
        log.info("User role assigned: {} -> {} by admin {}", userId, role, adminId);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId, UUID adminId) {
        if (userId.equals(adminId)) {
            throw new BusinessException("Không thể xóa tài khoản đang đăng nhập");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        LocalDateTime deletedAt = LocalDateTime.now();
        user.setDeletedAt(deletedAt);
        user.setStatus(UserStatus.INACTIVE);
        user.setEmail("deleted-" + user.getId() + "@deleted.local");
        user.setPhone(null);
        userRepository.save(user);
        log.info("User soft-deleted: {} by admin {}", userId, adminId);
    }

    @Override
    public UserStatisticsResponse getUserStatistics(UUID userId) {
        Double totalSpent = orderRepository.getTotalSpentByUser(userId);
        Long orderCount = orderRepository.getOrderCountByUser(userId);
        Long completedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED);

        return UserStatisticsResponse.builder()
                .totalOrders(orderCount != null ? orderCount.intValue() : 0)
                .completedOrders(completedOrders != null ? completedOrders.intValue() : 0)
                .cancelledOrders(cancelledOrders != null ? cancelledOrders.intValue() : 0)
                .totalSpent(totalSpent != null ? BigDecimal.valueOf(totalSpent) : BigDecimal.ZERO)
                .averageOrderValue(totalSpent != null && orderCount != null && orderCount > 0 ?
                        BigDecimal.valueOf(totalSpent / orderCount) : BigDecimal.ZERO)
                .loyaltyPoints(getUserLoyaltyPoints(userId))
                .build();
    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }


    @Override
    public long getNewUserCountByDateRange(LocalDate startDate, LocalDate endDate) {
        ZonedDateTime start = startDate.atStartOfDay(ZonedDateTime.now().getZone());
        ZonedDateTime end = endDate.plusDays(1).atStartOfDay(ZonedDateTime.now().getZone());
        return userRepository.countByCreatedAtBetween(start.toLocalDateTime(), end.toLocalDateTime());
    }

    @Override
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean isPhoneExist(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void validateUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.isActive()) {
            throw new BusinessException("Tài khoản đã bị khóa hoặc không hoạt động");
        }
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .loyaltyPoints(user.getLoyaltyPoints())
                .totalSpent(user.getTotalSpent())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserSummaryResponse mapToSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .loyaltyPoints(user.getLoyaltyPoints())
                .totalSpent(user.getTotalSpent())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserAddressResponse mapToAddressResponse(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .type(address.getType())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .streetAddress(address.getStreetAddress())
                .postalCode(address.getPostalCode())
                .isDefault(address.isDefault())
                .fullAddress(address.getFullAddress())
                .build();
    }

    private String extractPublicId(String url) {
        if (url == null) return null;
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex < 0) return null;

        String publicId = url.substring(uploadIndex + "/upload/".length());
        publicId = publicId.replaceFirst("^v\\d+/", "");
        int extensionIndex = publicId.lastIndexOf('.');
        return extensionIndex > 0 ? publicId.substring(0, extensionIndex) : publicId;
    }
}
