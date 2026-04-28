package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE("Hoạt động"),
    INACTIVE("Chưa kích hoạt"),
    BANNED("Đã bị khóa"),
    PENDING_VERIFICATION("Chờ xác thực email");

    private final String description;

    // Kiểm tra tài khoản có thể đăng nhập không
    public boolean isLoginAllowed() {
        return this == ACTIVE;
    }

    // Kiểm tra tài khoản có bị khóa không
    public boolean isLocked() {
        return this == BANNED;
    }

    // Kiểm tra tài khoản chờ xác thực không
    public boolean isPending() {
        return this == PENDING_VERIFICATION;
    }

    // Chuyển từ String sang enum an toàn (không throw exception)
    public static UserStatus fromString(String value) {
        if (value == null) return null;
        try {
            return UserStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}