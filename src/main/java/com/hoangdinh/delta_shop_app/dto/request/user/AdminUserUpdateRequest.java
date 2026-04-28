package com.hoangdinh.delta_shop_app.dto.request.user;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
}