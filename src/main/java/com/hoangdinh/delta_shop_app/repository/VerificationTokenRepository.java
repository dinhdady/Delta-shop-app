package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    // ========== BASIC QUERIES ==========

    /**
     * Find verification token by token string and type
     */
    Optional<VerificationToken> findByTokenAndType(String token, String type);

    /**
     * Find verification token by token string
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Check if token exists and is valid (not expired, not used)
     */
    @Query("SELECT CASE WHEN COUNT(vt) > 0 THEN true ELSE false END FROM VerificationToken vt " +
            "WHERE vt.token = :token AND vt.type = :type AND vt.used = false AND vt.expiryDate > CURRENT_TIMESTAMP")
    boolean existsValidToken(@Param("token") String token, @Param("type") String type);

    // ========== USER BASED QUERIES ==========

    /**
     * Find all verification tokens by user ID
     */
    List<VerificationToken> findByUserId(UUID userId);

    /**
     * Find verification token by user ID and type
     */
    Optional<VerificationToken> findByUserIdAndType(UUID userId, String type);

    /**
     * Find latest verification token by user ID and type
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.user.id = :userId AND vt.type = :type ORDER BY vt.createdAt DESC")
    Optional<VerificationToken> findLatestByUserIdAndType(@Param("userId") UUID userId, @Param("type") String type);

    // ========== EXPIRATION QUERIES ==========

    /**
     * Find all expired and unused verification tokens
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.expiryDate < :now AND vt.used = false")
    List<VerificationToken> findAllExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all expired verification tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a user
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Delete all tokens of specific type for a user
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.user.id = :userId AND vt.type = :type")
    void deleteByUserIdAndType(@Param("userId") UUID userId, @Param("type") String type);

    // ========== UPDATE OPERATIONS ==========

    /**
     * Mark token as used
     */
    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.token = :token")
    void markAsUsed(@Param("token") String token);

    /**
     * Invalidate all tokens for a user
     */
    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user.id = :userId AND vt.used = false")
    void invalidateAllUserTokens(@Param("userId") UUID userId);

    // ========== COUNT QUERIES ==========

    /**
     * Count unused tokens by user and type
     */
    @Query("SELECT COUNT(vt) FROM VerificationToken vt WHERE vt.user.id = :userId AND vt.type = :type AND vt.used = false")
    long countUnusedTokensByUserAndType(@Param("userId") UUID userId, @Param("type") String type);

    /**
     * Check if user has any valid token of specific type
     */
    @Query("SELECT CASE WHEN COUNT(vt) > 0 THEN true ELSE false END FROM VerificationToken vt " +
            "WHERE vt.user.id = :userId AND vt.type = :type AND vt.used = false AND vt.expiryDate > CURRENT_TIMESTAMP")
    boolean hasValidToken(@Param("userId") UUID userId, @Param("type") String type);

    // ========== CLEANUP QUERIES ==========

    /**
     * Delete old used tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.used = true AND vt.createdAt < :date")
    int deleteOldUsedTokens(@Param("date") LocalDateTime date);

    /**
     * Delete by user entity
     */
    void deleteByUser(User user);

    /**
     * Delete expired tokens (simple version)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    /**
     * Find token that is not used and not expired
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.token = :token AND vt.type = :type AND vt.used = false AND vt.expiryDate > :now")
    Optional<VerificationToken> findValidToken(@Param("token") String token, @Param("type") String type, @Param("now") LocalDateTime now);

    /**
     * Delete expired tokens by user and type
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user AND vt.type = :type AND vt.expiryDate < :now")
    void deleteExpiredTokensByUserAndType(@Param("user") User user, @Param("type") String type, @Param("now") LocalDateTime now);

    /**
     * Delete expired and used tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < :now OR vt.used = true")
    void deleteExpiredAndUsedTokens(@Param("now") LocalDateTime now);

    /**
     * Delete by user and type
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user AND vt.type = :type")
    void deleteByUserAndType(@Param("user") User user, @Param("type") String type);

    /**
     * Check if token exists by user and type
     */
    boolean existsByUserAndType(User user, String type);
}