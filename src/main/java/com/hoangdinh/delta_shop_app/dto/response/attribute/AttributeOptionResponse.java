package com.hoangdinh.delta_shop_app.dto.response.attribute;

import com.hoangdinh.delta_shop_app.entity.AttributeOption;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AttributeOptionResponse {
    private UUID id;
    private String value;
    private String displayValue;
    private String colorCode;
    private Integer sortOrder;

    public static AttributeOptionResponse from(AttributeOption option) {
        if (option == null) return null;

        return AttributeOptionResponse.builder()
                .id(option.getId())
                .value(option.getValue())
                .displayValue(option.getDisplayValue())
                .colorCode(option.getColorCode())
                .sortOrder(option.getSortOrder())
                .build();
    }
}