package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType {
    PERCENTAGE("Phần trăm (%)"),
    FIXED_AMOUNT("Số tiền cố định"),
    FREE_SHIPPING("Miễn phí vận chuyển"),
    BUY_X_GET_Y("Mua X tặng Y");

    private final String description;
}