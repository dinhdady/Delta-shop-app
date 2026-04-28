package com.hoangdinh.delta_shop_app.dto.request.attribute;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AttributeCreateRequest {
    @NotBlank(message = "Tên thuộc tính không được để trống")
    private String name;

    @NotBlank(message = "Code thuộc tính không được để trống")
    private String code;

    private String type;
    private String unit;
    private Boolean filterable;
    private Boolean required;
    private Integer sortOrder;
    private List<AttributeOptionRequest> options;
}

@Data
class AttributeOptionRequest {
    private String value;
    private String displayValue;
    private String colorCode;
    private Integer sortOrder;
}