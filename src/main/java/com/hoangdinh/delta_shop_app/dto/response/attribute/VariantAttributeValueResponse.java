package com.hoangdinh.delta_shop_app.dto.response.attribute;

import com.hoangdinh.delta_shop_app.entity.VariantAttributeValue;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VariantAttributeValueResponse {
    private UUID id;
    private UUID attributeId;
    private String attributeName;
    private String attributeCode;
    private String attributeType;
    private String value;
    private String displayValue;
    private String colorCode;

    public static VariantAttributeValueResponse from(VariantAttributeValue vav) {
        if (vav == null) return null;

        return VariantAttributeValueResponse.builder()
                .id(vav.getId())
                .attributeId(vav.getAttribute() != null ? vav.getAttribute().getId() : null)
                .attributeName(vav.getAttribute() != null ? vav.getAttribute().getName() : null)
                .attributeCode(vav.getAttribute() != null ? vav.getAttribute().getCode() : null)
                .attributeType(vav.getAttribute() != null ? vav.getAttribute().getType() : null)
                .value(vav.getValue())
                .displayValue(vav.getDisplayValue())
                .colorCode(vav.getColorCode())
                .build();
    }
}