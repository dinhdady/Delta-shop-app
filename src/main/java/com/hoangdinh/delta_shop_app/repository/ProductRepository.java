package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    // ========== BASIC QUERIES ==========

    Optional<Product> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySku(String sku);

    // ========== CATEGORY QUERIES ==========

    // THÊM METHOD NÀY
    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    // THÊM METHOD NÀY
    long countByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    Page<Product> findByCategoryIdAndDeletedAtIsNull(UUID categoryId, Pageable pageable);

    // ========== BRAND QUERIES ==========

    Page<Product> findByBrandIdAndDeletedAtIsNull(UUID brandId, Pageable pageable);

    boolean existsByBrandIdAndDeletedAtIsNull(UUID brandId);

    long countByBrandIdAndDeletedAtIsNull(UUID brandId);

    // ========== STATUS BASED QUERIES ==========

    Page<Product> findByStatusAndDeletedAtIsNull(ProductStatus status, Pageable pageable);

    long countByStatusAndDeletedAtIsNull(ProductStatus status);

    // ========== FEATURE FLAG QUERIES ==========

    Page<Product> findByFeaturedTrueAndStatusAndDeletedAtIsNull(ProductStatus status, Pageable pageable);

    Page<Product> findByNewArrivalTrueAndStatusAndDeletedAtIsNull(ProductStatus status, Pageable pageable);

    Page<Product> findByBestSellerTrueAndStatusAndDeletedAtIsNull(ProductStatus status, Pageable pageable);

    long countByFeaturedTrueAndDeletedAtIsNull();

    long countByNewArrivalTrueAndDeletedAtIsNull();

    long countByBestSellerTrueAndDeletedAtIsNull();

    // ========== RELATED PRODUCTS ==========

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :productId AND p.status = :status AND p.deletedAt IS NULL")
    List<Product> findRelated(@Param("categoryId") UUID categoryId,
                              @Param("productId") UUID productId,
                              @Param("status") ProductStatus status,
                              Pageable pageable);

    // ========== SALE & DISCOUNT QUERIES ==========

    @Query("SELECT p FROM Product p WHERE p.comparePrice IS NOT NULL AND p.comparePrice > p.basePrice AND p.deletedAt IS NULL AND p.status = 'ACTIVE'")
    Page<Product> findProductsOnSale(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.comparePrice IS NOT NULL AND p.comparePrice > p.basePrice AND p.deletedAt IS NULL")
    long countProductsOnSale();

    // ========== INVENTORY QUERIES ==========

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE' AND " +
            "NOT EXISTS (SELECT v FROM ProductVariant v WHERE v.product = p AND v.stockQuantity > 0)")
    Page<Product> findOutOfStockProducts(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL AND " +
            "NOT EXISTS (SELECT v FROM ProductVariant v WHERE v.product = p AND v.stockQuantity > 0)")
    long countOutOfStock();

    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE p.deletedAt IS NULL AND " +
            "v.stockQuantity > 0 AND v.stockQuantity <= v.minStockAlert")
    Page<Product> findLowStockProducts(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p) FROM Product p JOIN p.variants v WHERE p.deletedAt IS NULL AND " +
            "v.stockQuantity > 0 AND v.stockQuantity <= v.minStockAlert")
    long countLowStock();

    // ========== PRICE QUERIES ==========

    @Query("SELECT AVG(p.basePrice) FROM Product p WHERE p.deletedAt IS NULL")
    BigDecimal getAveragePrice();

    @Query("SELECT MIN(p.basePrice) FROM Product p WHERE p.deletedAt IS NULL")
    BigDecimal getMinPrice();

    @Query("SELECT MAX(p.basePrice) FROM Product p WHERE p.deletedAt IS NULL")
    BigDecimal getMaxPrice();

    // ========== SEARCH QUERIES ==========

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.basePrice <= :maxPrice) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "p.deletedAt IS NULL")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") UUID categoryId,
                                 @Param("brandId") UUID brandId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("status") ProductStatus status,
                                 Pageable pageable);

    // ========== UPDATE OPERATIONS ==========

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.totalViews = p.totalViews + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.totalSold = p.totalSold + :quantity WHERE p.id = :id")
    void incrementTotalSold(@Param("id") UUID id, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.averageRating = :avgRating, p.reviewCount = :reviewCount WHERE p.id = :id")
    void updateRating(@Param("id") UUID id,
                      @Param("avgRating") BigDecimal avgRating,
                      @Param("reviewCount") Integer reviewCount);

    // ========== BULK OPERATIONS ==========

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.status = :status WHERE p.id IN :ids AND p.deletedAt IS NULL")
    void bulkUpdateStatus(@Param("ids") List<UUID> ids, @Param("status") ProductStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.featured = :featured WHERE p.id IN :ids")
    void bulkUpdateFeatured(@Param("ids") List<UUID> ids, @Param("featured") Boolean featured);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id IN :ids")
    void bulkSoftDelete(@Param("ids") List<UUID> ids);

    // ========== COUNT QUERIES ==========

    long countByDeletedAtIsNull();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE'")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'INACTIVE'")
    long countInactiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'DISCONTINUED'")
    long countDiscontinuedProducts();

    // ========== DATE RANGE QUERIES ==========

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt >= :startDate AND p.deletedAt IS NULL")
    long countProductsCreatedAfter(@Param("startDate") java.time.ZonedDateTime startDate);

    @Query("SELECT p FROM Product p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.deletedAt IS NULL")
    Page<Product> findProductsByDateRange(@Param("startDate") java.time.ZonedDateTime startDate,
                                          @Param("endDate") java.time.ZonedDateTime endDate,
                                          Pageable pageable);

    // ========== TOP PRODUCTS QUERIES ==========

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE' ORDER BY p.totalSold DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE' ORDER BY p.totalViews DESC")
    Page<Product> findMostViewedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE' ORDER BY p.averageRating DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);

    // ========== TAG & SPORT TYPE QUERIES ==========

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE' AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :tag, '%'))")
    Page<Product> findProductsByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.status = 'ACTIVE' AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :sportType, '%'))")
    Page<Product> findProductsBySportType(@Param("sportType") String sportType, Pageable pageable);

    // ========== STATISTICS ==========

    @Query("SELECT p.category.id, COUNT(p) FROM Product p WHERE p.deletedAt IS NULL GROUP BY p.category.id")
    List<Object[]> countProductsByCategory();

    @Query("SELECT p.brand.id, COUNT(p) FROM Product p WHERE p.brand IS NOT NULL AND p.deletedAt IS NULL GROUP BY p.brand.id")
    List<Object[]> countProductsByBrand();

    @Query("SELECT p.status, COUNT(p) FROM Product p WHERE p.deletedAt IS NULL GROUP BY p.status")
    List<Object[]> countProductsByStatus();

    // ========== PRICE RANGE QUERIES ==========

//    @Query("SELECT p FROM Product p WHERE p.basePrice BETWEEN :minPrice AND :maxPrice AND p.deletedAt IS NULL AND p.status = 'ACTIVE'")
//    Page<Product> findProductsByPriceRange(@Param("minPrice") BigDecimal minPrice,
//                                           @Param("maxPrice") BigDecimal maxPrice,
//                                           Pageable pageable);
//    @Modifying
//    @Query("""
//    UPDATE Product p
//    SET p.totalSold = (
//        SELECT COALESCE(SUM(oi.quantity), 0)
//        FROM OrderItem oi
//        JOIN oi.productVariant pv
//        WHERE pv.product = p
//        AND oi.order.status = 'DELIVERED'
//    )
//""")
//    void updateAllProductsTotalSold();
}