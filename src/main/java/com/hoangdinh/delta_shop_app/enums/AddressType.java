package com.hoangdinh.delta_shop_app.enums;

public enum AddressType {
    HOME("HOME"),
    OFFICE("OFFICE"),
    OTHER("OTHER");

    private final String value;

    AddressType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}