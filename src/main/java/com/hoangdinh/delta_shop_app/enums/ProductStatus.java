package com.hoangdinh.delta_shop_app.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ACTIVE("Đang bán"),
    INACTIVE("Ngừng bán"),
    OUT_OF_STOCK("Hết hàng"),
    DISCONTINUED("Ngừng sản xuất");

    private final String description;
}