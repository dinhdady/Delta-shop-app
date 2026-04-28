package com.hoangdinh.delta_shop_app.enums;

public enum ContactStatus {
    PENDING("Chờ xử lý"),
    PROCESSING("Đang xử lý"),
    REPLIED("Đã phản hồi"),
    RESOLVED("Đã giải quyết"),
    CLOSED("Đã đóng");

    private final String description;

    ContactStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}