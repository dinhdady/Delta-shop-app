package com.hoangdinh.delta_shop_app.dto.response.user;

import com.hoangdinh.delta_shop_app.enums.AddressType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserAddressResponse {
    private UUID id;
    private AddressType type;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private String postalCode;
    private boolean isDefault;
    private String fullAddress;
}