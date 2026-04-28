package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.AttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttributeOptionRepository extends JpaRepository<AttributeOption, UUID> {

    List<AttributeOption> findByAttributeIdOrderBySortOrderAsc(UUID attributeId);

    @Query("SELECT ao FROM AttributeOption ao WHERE ao.attribute.code = :attributeCode ORDER BY ao.sortOrder ASC")
    List<AttributeOption> findByAttributeCode(@Param("attributeCode") String attributeCode);

    void deleteByAttributeId(UUID attributeId);
}