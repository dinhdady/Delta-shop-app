package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.enums.UserRole;
import com.hoangdinh.delta_shop_app.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    Optional<User> findByPhone(String phone);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.phone = :phone AND u.deletedAt IS NULL")
    boolean existsByPhone(@Param("phone") String phone);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalSpent = u.totalSpent + :amount WHERE u.id = :userId")
    void incrementTotalSpent(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.loyaltyPoints = u.loyaltyPoints + :points WHERE u.id = :userId")
    void addLoyaltyPoints(@Param("userId") UUID userId, @Param("points") Integer points);

    long countByStatus(UserStatus status);
    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Page<User> findByRoleAndDeletedAtIsNull(UserRole role, Pageable pageable);

    Page<User> findByStatusAndDeletedAtIsNull(UserStatus status, Pageable pageable);

    Page<User> findByRoleAndStatusAndDeletedAtIsNull(UserRole role, UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

}
