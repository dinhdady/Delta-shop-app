package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    ORDER_PLACED("Đơn hàng mới"),
    ORDER_CONFIRMED("Đơn hàng đã xác nhận"),
    ORDER_SHIPPED("Đơn hàng đang giao"),
    ORDER_DELIVERED("Đơn hàng đã giao"),
    ORDER_CANCELLED("Đơn hàng đã hủy"),
    PAYMENT_SUCCESS("Thanh toán thành công"),
    PAYMENT_FAILED("Thanh toán thất bại"),
    REVIEW_APPROVED("Đánh giá được duyệt"),
    PROMOTION("Khuyến mãi mới"),
    SYSTEM("Thông báo hệ thống");

    private final String description;
}
