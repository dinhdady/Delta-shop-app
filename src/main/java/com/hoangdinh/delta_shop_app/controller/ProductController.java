package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.product.ProductCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.ProductFilterRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.ProductUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.StockAdjustmentRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductDetailResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.entity.StockMovement;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import com.hoangdinh.delta_shop_app.enums.StockMovementType;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.ProductVariantRepository;
import com.hoangdinh.delta_shop_app.repository.StockMovementRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product", description = "APIs for product management")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    // ========== PUBLIC ENDPOINTS ==========

    @GetMapping
    @Operation(summary = "Get all products with pagination and filters")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStockOnly) {

        if ("null".equalsIgnoreCase(keyword) || "undefined".equalsIgnoreCase(keyword)) {
            keyword = null;
        }

        log.info("Product search request: keyword='{}', page={}, size={}, sortBy={}, sortDir={}, categoryId={}, brandId={}",
                keyword, page, size, sortBy, sortDir, categoryId, brandId);

        ProductFilterRequest filter = ProductFilterRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .keyword(keyword)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .featured(featured)
                .inStockOnly(inStockOnly)
                .publicOnly(true)
                .build();

        return ResponseEntity.ok(productService.search(filter));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStockOnly) {

        if ("null".equalsIgnoreCase(keyword) || "undefined".equalsIgnoreCase(keyword)) {
            keyword = null;
        }

        log.info("Product SEARCH API called: keyword='{}', page={}, size={}, sortBy={}, sortDir={}, categoryId={}, brandId={}",
                keyword, page, size, sortBy, sortDir, categoryId, brandId);

        ProductFilterRequest filter = ProductFilterRequest.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .inStockOnly(inStockOnly)
                .publicOnly(true)
                .build();

        return ResponseEntity.ok(productService.search(filter));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<List<ProductSummaryResponse>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.getFeatured(limit));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrival products")
    public ResponseEntity<List<ProductSummaryResponse>> getNewArrivals(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.getNewArrivals(limit));
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Get best seller products")
    public ResponseEntity<List<ProductSummaryResponse>> getBestSellers(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.getBestSellers(limit));
    }

    @GetMapping("/on-sale")
    @Operation(summary = "Get products on sale")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getProductsOnSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getProductsOnSale(page, size));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    @GetMapping("/brand/{brandId}")
    @Operation(summary = "Get products by brand")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getProductsByBrand(
            @PathVariable UUID brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getProductsByBrand(brandId, page, size));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/{productId}/related")
    @Operation(summary = "Get related products")
    public ResponseEntity<List<ProductSummaryResponse>> getRelatedProducts(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getRelated(productId, limit));
    }

    // ========== ADMIN ENDPOINTS ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create new product (Admin only)")
    public ResponseEntity<ProductDetailResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update product (Admin only)")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete product (soft delete)")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Permanently delete product (Super Admin only)")
    public ResponseEntity<Void> hardDeleteProduct(@PathVariable UUID id) {
        productService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Restore soft-deleted product")
    public ResponseEntity<ProductDetailResponse> restoreProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.restore(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update product status")
    public ResponseEntity<ProductDetailResponse> updateProductStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(productService.updateStatus(id, ProductStatus.valueOf(status)));
    }

    @PatchMapping("/{id}/featured")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update product featured status")
    public ResponseEntity<ProductDetailResponse> updateProductFeatured(
            @PathVariable UUID id,
            @RequestParam boolean featured) {
        return ResponseEntity.ok(productService.updateFeatured(id, featured));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Adjust product stock (Admin only)")
    @Transactional
    public ResponseEntity<ProductSummaryResponse> adjustStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustmentRequest request,
            @RequestAttribute("userId") UUID adminId) {
        if (request.getStockQuantity() < 0) {
            throw new BusinessException("Số lượng tồn kho không được âm");
        }

        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        ProductVariant variant;
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", request.getVariantId()));
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new BusinessException("Biến thể không thuộc sản phẩm này");
            }
        } else {
            variant = product.getVariants().stream()
                    .findFirst()
                    .orElseGet(() -> {
                        ProductVariant defaultVariant = ProductVariant.builder()
                                .product(product)
                                .sku(product.getSku() != null ? product.getSku() : "DEF-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Default")
                                .stockQuantity(0)
                                .isActive(true)
                                .build();
                        product.getVariants().add(defaultVariant);
                        return defaultVariant;
                    });
        }

        int quantityBefore = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
        variant.setStockQuantity(request.getStockQuantity());
        ProductVariant savedVariant = variantRepository.save(variant);

        User admin = userRepository.findById(adminId).orElse(null);
        stockMovementRepository.save(StockMovement.builder()
                .variant(savedVariant)
                .type(StockMovementType.ADJUSTMENT)
                .quantity(request.getStockQuantity() - quantityBefore)
                .quantityBefore(quantityBefore)
                .quantityAfter(request.getStockQuantity())
                .note(request.getNote() != null && !request.getNote().isBlank()
                        ? request.getNote()
                        : "Điều chỉnh tồn kho từ trang quản trị")
                .createdBy(admin)
                .build());

        return ResponseEntity.ok(ProductSummaryResponse.from(product));
    }

    @PostMapping("/bulk/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Bulk update product status")
    public ResponseEntity<Void> bulkUpdateStatus(
            @RequestBody List<UUID> productIds,
            @RequestParam String status) {
        productService.bulkUpdateStatus(productIds, ProductStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Bulk delete products")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<UUID> productIds) {
        productService.bulkDelete(productIds);
        return ResponseEntity.ok().build();
    }
}
