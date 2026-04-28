package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    VNPAY("VNPay"),
    MOMO("Ví MoMo"),
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    CREDIT_CARD("Thẻ tín dụng/ghi nợ");

    private final String description;

    public boolean isOnline() {
        return this != COD;
    }

    public boolean isCOD() {
        return this == COD;
    }

    public boolean isBankTransfer() {
        return this == BANK_TRANSFER;
    }

    public boolean isWallet() {
        return this == MOMO;
    }

    public static PaymentMethod fromString(String value) {
        if (value == null) return null;
        try {
            return PaymentMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}