package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIdAndActiveTrue(UUID parentId);

    List<Category> findByActiveTrueOrderBySortOrderAsc();

    @Query("SELECT c FROM Category c WHERE c.path LIKE CONCAT('%', :categoryId, '%') AND c.active = true")
    List<Category> findAllSubcategories(@Param("categoryId") UUID categoryId);

    boolean existsBySlug(String slug);
    List<Category> findAllByOrderBySortOrderAsc();
    List<Category> findByParentIsNullOrderBySortOrderAsc();
    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
}