package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    EMAIL_VERIFY("Xác thực email"),
    PASSWORD_RESET("Đặt lại mật khẩu"),
    PHONE_OTP("Xác thực số điện thoại");

    private final String description;

    public static TokenType fromString(String value) {
        if (value == null) return null;
        try {
            return TokenType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}