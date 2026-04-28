package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.attribute.AssignAttributeRequest;
import com.hoangdinh.delta_shop_app.dto.response.attribute.VariantAttributeValueResponse;
import com.hoangdinh.delta_shop_app.entity.Attribute;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.entity.VariantAttributeValue;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.AttributeRepository;
import com.hoangdinh.delta_shop_app.repository.ProductVariantRepository;
import com.hoangdinh.delta_shop_app.repository.VariantAttributeValueRepository;
import com.hoangdinh.delta_shop_app.service.VariantAttributeValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VariantAttributeValueServiceImpl implements VariantAttributeValueService {

    private final VariantAttributeValueRepository variantAttributeValueRepository;
    private final ProductVariantRepository variantRepository;
    private final AttributeRepository attributeRepository;

    @Override
    @Transactional
    public VariantAttributeValueResponse assignAttributeToVariant(UUID variantId, AssignAttributeRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));

        Attribute attribute = attributeRepository.findByCode(request.getAttributeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Attribute", "code", request.getAttributeCode()));

        // Check if already exists
        variantAttributeValueRepository.deleteByVariantIdAndAttributeId(variantId, attribute.getId());

        VariantAttributeValue vav = VariantAttributeValue.builder()
                .variant(variant)
                .attribute(attribute)
                .value(request.getValue())
                .displayValue(request.getDisplayValue())
                .colorCode(request.getColorCode())
                .sortOrder(request.getSortOrder())
                .build();

        VariantAttributeValue saved = variantAttributeValueRepository.save(vav);
        log.info("Attribute {} assigned to variant {}", attribute.getCode(), variantId);

        return VariantAttributeValueResponse.from(saved);
    }

    @Override
    @Transactional
    public void removeAttributeFromVariant(UUID variantId, UUID attributeId) {
        variantAttributeValueRepository.deleteByVariantIdAndAttributeId(variantId, attributeId);
        log.info("Attribute {} removed from variant {}", attributeId, variantId);
    }

    @Override
    @Transactional
    public void removeAllAttributesFromVariant(UUID variantId) {
        variantAttributeValueRepository.deleteByVariantId(variantId);
        log.info("All attributes removed from variant {}", variantId);
    }

    @Override
    public List<VariantAttributeValueResponse> getAttributesByVariant(UUID variantId) {
        return variantAttributeValueRepository.findByVariantIdOrdered(variantId)
                .stream()
                .map(VariantAttributeValueResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getAttributeMapByVariant(UUID variantId) {
        List<Object[]> results = variantAttributeValueRepository.getAttributeMapByVariantId(variantId);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (String) row[1],
                        (v1, v2) -> v1
                ));
    }

    @Override
    @Transactional
    public void bulkAssignAttributes(UUID variantId, Map<String, String> attributes) {
        // Remove existing
        variantAttributeValueRepository.deleteByVariantId(variantId);

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Attribute attribute = attributeRepository.findByCode(entry.getKey())
                    .orElse(null);

            if (attribute != null) {
                VariantAttributeValue vav = VariantAttributeValue.builder()
                        .variant(variant)
                        .attribute(attribute)
                        .value(entry.getValue())
                        .build();
                variantAttributeValueRepository.save(vav);
            }
        }

        log.info("Bulk assigned {} attributes to variant {}", attributes.size(), variantId);
    }

    @Override
    public List<UUID> findVariantsByAttribute(String attributeCode, String value) {
        // This would require a custom query in repository
        return List.of();
    }
}