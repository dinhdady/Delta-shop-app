package com.hoangdinh.delta_shop_app.dto.request.user;

import com.hoangdinh.delta_shop_app.enums.AddressType;
import lombok.Data;

@Data
public class UpdateAddressRequest {
    private AddressType type;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private String postalCode;
    private boolean isDefault;
}