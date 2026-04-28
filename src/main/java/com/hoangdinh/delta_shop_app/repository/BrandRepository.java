package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findBySlug(String slug);

    List<Brand> findByActiveTrueOrderBySortOrderAsc();

    // SỬA: đổi isFeatured thành featured
    List<Brand> findByFeaturedTrueAndActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT b FROM Brand b WHERE b.active = true ORDER BY b.sortOrder ASC")
    List<Brand> findAllActive();

    @Query("SELECT b FROM Brand b WHERE b.featured = true AND b.active = true ORDER BY b.sortOrder ASC")
    List<Brand> findAllFeatured();

    Page<Brand> findByActive(boolean active, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId AND p.deletedAt IS NULL")
    long countProductsByBrandId(@Param("brandId") UUID brandId);
}   