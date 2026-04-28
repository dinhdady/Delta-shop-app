package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockMovementType {

    PURCHASE("Nhập hàng", "in"),
    SALE("Bán hàng", "out"),
    RETURN("Trả hàng", "in"),
    ADJUSTMENT("Điều chỉnh", "both"),
    DAMAGED("Hàng hỏng", "out"),
    RESERVATION("Đặt chỗ", "reserve"),
    RELEASE("Giải phóng", "release");

    private final String description;
    private final String direction; // in, out, both, reserve, release

    /**
     * Kiểm tra có phải nhập kho không
     */
    public boolean isInbound() {
        return this == PURCHASE || this == RETURN;
    }

    /**
     * Kiểm tra có phải xuất kho không
     */
    public boolean isOutbound() {
        return this == SALE || this == DAMAGED;
    }

    /**
     * Kiểm tra có phải đặt chỗ không
     */
    public boolean isReservation() {
        return this == RESERVATION;
    }

    /**
     * Kiểm tra có phải giải phóng không
     */
    public boolean isRelease() {
        return this == RELEASE;
    }

    /**
     * Kiểm tra có phải điều chỉnh không
     */
    public boolean isAdjustment() {
        return this == ADJUSTMENT;
    }

    /**
     * Lấy dấu hiệu (+/-) cho số lượng
     */
    public int getQuantitySign() {
        return switch (this) {
            case PURCHASE, RETURN, RELEASE -> 1;      // Tăng kho
            case SALE, DAMAGED -> -1;                  // Giảm kho
            case RESERVATION -> 0;                     // Không thay đổi kho thực tế
            case ADJUSTMENT -> 0;                      // Có thể tăng hoặc giảm
        };
    }

    public static StockMovementType fromString(String value) {
        if (value == null) return null;
        try {
            return StockMovementType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}