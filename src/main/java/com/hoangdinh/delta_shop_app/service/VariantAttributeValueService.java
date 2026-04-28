package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.attribute.AssignAttributeRequest;
import com.hoangdinh.delta_shop_app.dto.response.attribute.VariantAttributeValueResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VariantAttributeValueService {

    VariantAttributeValueResponse assignAttributeToVariant(UUID variantId, AssignAttributeRequest request);

    void removeAttributeFromVariant(UUID variantId, UUID attributeId);

    void removeAllAttributesFromVariant(UUID variantId);

    List<VariantAttributeValueResponse> getAttributesByVariant(UUID variantId);

    Map<String, String> getAttributeMapByVariant(UUID variantId);

    void bulkAssignAttributes(UUID variantId, Map<String, String> attributes);

    List<UUID> findVariantsByAttribute(String attributeCode, String value);
}