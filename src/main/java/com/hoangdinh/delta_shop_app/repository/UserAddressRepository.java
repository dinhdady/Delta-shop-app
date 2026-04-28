package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUserIdOrderByIsDefaultDesc(UUID userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrue(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void removeDefaultFlag(@Param("userId") UUID userId);

    @Query("SELECT a FROM UserAddress a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<UserAddress> findDefaultAddress(@Param("userId") UUID userId);

    void deleteByUserId(UUID userId);
}