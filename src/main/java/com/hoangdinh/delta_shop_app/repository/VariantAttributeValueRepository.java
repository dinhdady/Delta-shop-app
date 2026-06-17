package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Attribute;
import com.hoangdinh.delta_shop_app.entity.VariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, UUID> {

    List<VariantAttributeValue> findByVariantId(UUID variantId);

    List<VariantAttributeValue> findByAttributeId(UUID attributeId);

    @Query("SELECT vav FROM VariantAttributeValue vav WHERE vav.variant.id = :variantId ORDER BY vav.sortOrder ASC")
    List<VariantAttributeValue> findByVariantIdOrdered(@Param("variantId") UUID variantId);

    @Query("SELECT vav FROM VariantAttributeValue vav WHERE vav.variant.product.id = :productId")
    List<VariantAttributeValue> findByProductId(@Param("productId") UUID productId);

    @Modifying
    @Transactional
    void deleteByVariantId(UUID variantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VariantAttributeValue vav WHERE vav.variant.id = :variantId AND vav.attribute.id = :attributeId")
    void deleteByVariantIdAndAttributeId(@Param("variantId") UUID variantId, @Param("attributeId") UUID attributeId);

    @Query("SELECT vav.attribute.code, vav.value FROM VariantAttributeValue vav WHERE vav.variant.id = :variantId")
    List<Object[]> getAttributeMapByVariantId(@Param("variantId") UUID variantId);

    @Query("SELECT DISTINCT vav.attribute FROM VariantAttributeValue vav WHERE vav.variant.product.id = :productId")
    List<Attribute> getAttributesUsedInProduct(@Param("productId") UUID productId);

    @Query("SELECT DISTINCT vav.value FROM VariantAttributeValue vav WHERE vav.attribute.code = :attributeCode")
    List<String> getDistinctValuesByAttributeCode(@Param("attributeCode") String attributeCode);

    @Query("SELECT DISTINCT vav.variant.id FROM VariantAttributeValue vav " +
            "WHERE LOWER(vav.attribute.code) = LOWER(:attributeCode) AND LOWER(vav.value) = LOWER(:value)")
    List<UUID> findVariantIdsByAttributeCodeAndValue(@Param("attributeCode") String attributeCode,
                                                     @Param("value") String value);
}
