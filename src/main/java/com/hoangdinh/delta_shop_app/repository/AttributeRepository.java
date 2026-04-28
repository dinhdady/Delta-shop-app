package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributeRepository extends JpaRepository<Attribute, UUID> {

    Optional<Attribute> findByCode(String code);

    List<Attribute> findByFilterableTrueOrderBySortOrderAsc();

    List<Attribute> findByType(String type);

    @Query("SELECT a FROM Attribute a WHERE a.id IN :ids")
    List<Attribute> findByIds(@Param("ids") List<UUID> ids);

    boolean existsByCode(String code);
}