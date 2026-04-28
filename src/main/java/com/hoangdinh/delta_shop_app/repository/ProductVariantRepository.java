package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findBySku(String sku);
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") UUID productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true ORDER BY v.sortOrder ASC")
    List<ProductVariant> findActiveByProductId(@Param("productId") UUID productId);

    boolean existsBySku(String sku);
}
