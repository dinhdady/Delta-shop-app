package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Wishlist;
import com.hoangdinh.delta_shop_app.entity.Wishlist.WishlistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    // SỬA: dùng id.userId thay vì idUserId
    Page<Wishlist> findByIdUserId(UUID userId, Pageable pageable);

    // SỬA: tìm theo userId và productId
    Optional<Wishlist> findByIdUserIdAndIdProductId(UUID userId, UUID productId);

    // SỬA: kiểm tra tồn tại
    boolean existsByIdUserIdAndIdProductId(UUID userId, UUID productId);

    // SỬA: xóa theo userId và productId
    @Modifying
    @Transactional
    void deleteByIdUserIdAndIdProductId(UUID userId, UUID productId);

    // SỬA: xóa tất cả wishlist của user
    @Modifying
    @Transactional
    void deleteByIdUserId(UUID userId);

    // SỬA: đếm số lượng wishlist của user
    long countByIdUserId(UUID userId);

    // Lấy danh sách product IDs từ wishlist của user
    @Query("SELECT w.id.productId FROM Wishlist w WHERE w.id.userId = :userId")
    List<UUID> findProductIdsByUserId(@Param("userId") UUID userId);
}