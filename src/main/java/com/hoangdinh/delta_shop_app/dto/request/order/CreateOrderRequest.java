package com.hoangdinh.delta_shop_app.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 sản phẩm")
    private List<OrderItemRequest> items;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String shippingName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String shippingPhone;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String shippingProvince;

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String shippingDistrict;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String shippingWard;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String shippingAddress;

    private String paymentMethod;
    private String promotionCode;
    private Integer loyaltyPointsToUse;
    private String notes;
}