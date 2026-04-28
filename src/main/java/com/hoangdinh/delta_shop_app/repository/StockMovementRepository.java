package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.StockMovement;
import com.hoangdinh.delta_shop_app.enums.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByVariantIdOrderByCreatedAtDesc(UUID variantId, Pageable pageable);

    List<StockMovement> findByVariantIdAndType(UUID variantId, StockMovementType type);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :startDate AND :endDate")
    Page<StockMovement> findByDateRange(@Param("startDate") ZonedDateTime startDate,
                                        @Param("endDate") ZonedDateTime endDate,
                                        Pageable pageable);

    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.variant.id = :variantId AND sm.type IN :types")
    Integer getTotalMovementByTypes(@Param("variantId") UUID variantId,
                                    @Param("types") List<StockMovementType> types);
}