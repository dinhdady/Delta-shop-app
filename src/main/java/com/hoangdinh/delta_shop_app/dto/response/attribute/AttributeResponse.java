package com.hoangdinh.delta_shop_app.dto.response.attribute;

import com.hoangdinh.delta_shop_app.entity.Attribute;
import com.hoangdinh.delta_shop_app.entity.AttributeOption;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class AttributeResponse {
    private UUID id;
    private String name;
    private String code;
    private String type;
    private String unit;
    private boolean filterable;
    private boolean required;
    private Integer sortOrder;
    private List<AttributeOptionResponse> options;

    public static AttributeResponse from(Attribute attribute) {
        if (attribute == null) return null;

        return AttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .code(attribute.getCode())
                .type(attribute.getType())
                .unit(attribute.getUnit())
                .filterable(attribute.isFilterable())
                .required(attribute.isRequired())
                .sortOrder(attribute.getSortOrder())
                .options(attribute.getOptions() != null ?
                        attribute.getOptions().stream()
                                .map(AttributeOptionResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}