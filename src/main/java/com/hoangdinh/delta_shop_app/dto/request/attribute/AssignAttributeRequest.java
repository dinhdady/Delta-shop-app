package com.hoangdinh.delta_shop_app.dto.request.attribute;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignAttributeRequest {
    @NotBlank(message = "Mã thuộc tính không được để trống")
    private String attributeCode;

    @NotBlank(message = "Giá trị không được để trống")
    private String value;

    private String displayValue;
    private String colorCode;
    private Integer sortOrder;
}