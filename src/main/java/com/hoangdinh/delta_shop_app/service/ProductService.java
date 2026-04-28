package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.product.*;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductDetailResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductStatisticsResponse;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    // ========== QUERY METHODS ==========

    /**
     * Get product by slug
     * @param slug product slug
     * @return product detail response
     */
    ProductDetailResponse getBySlug(String slug);

    /**
     * Get product by ID
     * @param id product ID
     * @return product detail response
     */
    ProductDetailResponse getById(UUID id);

    /**
     * Search products with filters
     * @param filter filter criteria
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> search(ProductFilterRequest filter);

    /**
     * Get featured products
     * @param limit number of products to return
     * @return list of featured products
     */
    List<ProductSummaryResponse> getFeatured(int limit);

    /**
     * Get new arrival products
     * @param limit number of products to return
     * @return list of new arrival products
     */
    List<ProductSummaryResponse> getNewArrivals(int limit);

    /**
     * Get best seller products
     * @param limit number of products to return
     * @return list of best seller products
     */
    List<ProductSummaryResponse> getBestSellers(int limit);

    /**
     * Get related products
     * @param productId product ID
     * @param limit number of products to return
     * @return list of related products
     */
    List<ProductSummaryResponse> getRelated(UUID productId, int limit);

    /**
     * Get products by category
     * @param categoryId category ID
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> getProductsByCategory(UUID categoryId, int page, int size);

    /**
     * Get products by brand
     * @param brandId brand ID
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> getProductsByBrand(UUID brandId, int page, int size);

    /**
     * Get products by status
     * @param status product status
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> getProductsByStatus(ProductStatus status, int page, int size);

    /**
     * Search products by keyword
     * @param keyword search keyword
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> searchByKeyword(String keyword, int page, int size);

    /**
     * Get products on sale (have compare price > base price)
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> getProductsOnSale(int page, int size);

    /**
     * Get out of stock products
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> getOutOfStockProducts(int page, int size);

    /**
     * Get low stock products (stock < min_stock_alert)
     * @param page page number
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ProductSummaryResponse> getLowStockProducts(int page, int size);

    /**
     * Get product statistics
     * @return product statistics
     */
    ProductStatisticsResponse getProductStatistics();

    /**
     * Check if product exists by SKU
     * @param sku product SKU
     * @return true if exists
     */
    boolean existsBySku(String sku);

    /**
     * Check if product exists by slug
     * @param slug product slug
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    /**
     * Get total product count
     * @return total count
     */
    long getTotalProductCount();

    /**
     * Get product count by status
     * @param status product status
     * @return count
     */
    long getProductCountByStatus(ProductStatus status);

    // ========== ADMIN METHODS ==========

    /**
     * Create new product
     * @param request product creation request
     * @return created product detail
     */
    ProductDetailResponse create(ProductCreateRequest request);

    /**
     * Update existing product
     * @param id product ID
     * @param request product update request
     * @return updated product detail
     */
    ProductDetailResponse update(UUID id, ProductUpdateRequest request);

    /**
     * Delete product (soft delete)
     * @param id product ID
     */
    void delete(UUID id);

    /**
     * Permanently delete product (hard delete)
     * @param id product ID
     */
    void hardDelete(UUID id);

    /**
     * Restore soft-deleted product
     * @param id product ID
     * @return restored product detail
     */
    ProductDetailResponse restore(UUID id);

    /**
     * Update product status
     * @param id product ID
     * @param status new status
     * @return updated product detail
     */
    ProductDetailResponse updateStatus(UUID id, ProductStatus status);

    /**
     * Bulk update product status
     * @param productIds list of product IDs
     * @param status new status
     */
    void bulkUpdateStatus(List<UUID> productIds, ProductStatus status);

    /**
     * Bulk delete products
     * @param productIds list of product IDs
     */
    void bulkDelete(List<UUID> productIds);

    /**
     * Bulk update products
     * @param request bulk update request
     */
    void bulkUpdate(ProductBulkUpdateRequest request);

    /**
     * Update product featured status
     * @param id product ID
     * @param featured featured status
     * @return updated product detail
     */
    ProductDetailResponse updateFeatured(UUID id, boolean featured);

    /**
     * Update product new arrival status
     * @param id product ID
     * @param newArrival new arrival status
     * @return updated product detail
     */
    ProductDetailResponse updateNewArrival(UUID id, boolean newArrival);

    /**
     * Update product best seller status
     * @param id product ID
     * @param bestSeller best seller status
     * @return updated product detail
     */
    ProductDetailResponse updateBestSeller(UUID id, boolean bestSeller);

    /**
     * Update product inventory
     * @param id product ID
     * @param quantity quantity to add/subtract
     * @param type type of adjustment (ADD, SUBTRACT, SET)
     */
    void updateInventory(UUID id, int quantity, String type);

    /**
     * Reindex product for search
     * @param id product ID
     */
    void reindexProduct(UUID id);

    /**
     * Reindex all products for search
     */
    void reindexAllProducts();

    // ========== VARIANT METHODS ==========

    /**
     * Add variant to product
     * @param productId product ID
     * @param variantRequest variant creation request
     * @return updated product detail
     */
    ProductDetailResponse addVariant(UUID productId, VariantRequest variantRequest);

    /**
     * Update product variant
     * @param variantId variant ID
     * @param variantRequest variant update request
     * @return updated product detail
     */
    ProductDetailResponse updateVariant(UUID variantId, VariantRequest variantRequest);

    /**
     * Delete product variant
     * @param variantId variant ID
     * @return updated product detail
     */
    ProductDetailResponse deleteVariant(UUID variantId);

    /**
     * Update variant stock
     * @param variantId variant ID
     * @param quantity new quantity
     */
    void updateVariantStock(UUID variantId, int quantity);

    // ========== IMAGE METHODS ==========

    /**
     * Add image to product
     * @param productId product ID
     * @param imageUrl image URL
     * @param isPrimary whether this is primary image
     * @return updated product detail
     */
    ProductDetailResponse addImage(UUID productId, String imageUrl, boolean isPrimary);

    /**
     * Delete product image
     * @param imageId image ID
     * @return updated product detail
     */
    ProductDetailResponse deleteImage(UUID imageId);

    /**
     * Set primary image for product
     * @param imageId image ID
     * @return updated product detail
     */
    ProductDetailResponse setPrimaryImage(UUID imageId);

    /**
     * Reorder product images
     * @param productId product ID
     * @param imageIds ordered list of image IDs
     */
    void reorderImages(UUID productId, List<UUID> imageIds);
}