package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.product.ProductBulkUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.ProductCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.ProductFilterRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.ProductImageRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.ProductUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.request.product.VariantRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductDetailResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductStatisticsResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.entity.Brand;
import com.hoangdinh.delta_shop_app.entity.Category;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductImage;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.BrandRepository;
import com.hoangdinh.delta_shop_app.repository.CategoryRepository;
import com.hoangdinh.delta_shop_app.repository.ProductImageRepository;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.ProductVariantRepository;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import com.hoangdinh.delta_shop_app.service.ProductService;
import com.hoangdinh.delta_shop_app.specification.ProductSpecification;
import com.hoangdinh.delta_shop_app.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    // ========== QUERY METHODS ==========

    @Override
    @Transactional
    @Cacheable(value = "products", key = "#slug")
    public ProductDetailResponse getBySlug(String slug) {
        Product product = productRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        productRepository.incrementViewCount(product.getId());
        return mapToDetail(product);
    }

    @Override
    public ProductDetailResponse getById(UUID id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDetail(product);
    }

    @Override
    public PageResponse<ProductSummaryResponse> search(ProductFilterRequest filter) {
        Specification<Product> spec = ProductSpecification.build(filter);
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDir());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Product> page = productRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::mapToSummary));
    }

    @Override
    @Cacheable(value = "featured-products")
    public List<ProductSummaryResponse> getFeatured(int limit) {
        return productRepository
                .findByFeaturedTrueAndStatusAndDeletedAtIsNull(ProductStatus.ACTIVE,
                        PageRequest.of(0, limit))
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    @Cacheable(value = "new-arrivals")
    public List<ProductSummaryResponse> getNewArrivals(int limit) {
        return productRepository
                .findByNewArrivalTrueAndStatusAndDeletedAtIsNull(ProductStatus.ACTIVE,
                        PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    @Cacheable(value = "best-sellers")
    public List<ProductSummaryResponse> getBestSellers(int limit) {
        return productRepository
                .findByBestSellerTrueAndStatusAndDeletedAtIsNull(ProductStatus.ACTIVE,
                        PageRequest.of(0, limit, Sort.by("totalSold").descending()))
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    @Cacheable(value = "related-products", key = "#productId + '-' + #limit")
    public List<ProductSummaryResponse> getRelated(UUID productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return productRepository.findRelated(
                product.getCategory().getId(),
                productId,
                ProductStatus.ACTIVE,
                PageRequest.of(0, limit)
        ).stream().map(this::mapToSummary).toList();
    }

    @Override
    public PageResponse<ProductSummaryResponse> getProductsByCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findByCategoryIdAndDeletedAtIsNull(categoryId, pageable);
        return PageResponse.of(products.map(this::mapToSummary));
    }

    @Override
    public PageResponse<ProductSummaryResponse> getProductsByBrand(UUID brandId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findByBrandIdAndDeletedAtIsNull(brandId, pageable);
        return PageResponse.of(products.map(this::mapToSummary));
    }

    @Override
    public PageResponse<ProductSummaryResponse> getProductsByStatus(ProductStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        return PageResponse.of(products.map(this::mapToSummary));
    }

    @Override
    public PageResponse<ProductSummaryResponse> searchByKeyword(String keyword, int page, int size) {
        ProductFilterRequest filter = ProductFilterRequest.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .publicOnly(true)
                .build();
        return search(filter);
    }
    @Override
    public PageResponse<ProductSummaryResponse> getProductsOnSale(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findProductsOnSale(pageable);
        return PageResponse.of(products.map(this::mapToSummary));
    }

    @Override
    public PageResponse<ProductSummaryResponse> getOutOfStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findOutOfStockProducts(pageable);
        return PageResponse.of(products.map(this::mapToSummary));
    }

    @Override
    public PageResponse<ProductSummaryResponse> getLowStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findLowStockProducts(pageable);
        return PageResponse.of(products.map(this::mapToSummary));
    }

    @Override
    public ProductStatisticsResponse getProductStatistics() {
        return ProductStatisticsResponse.builder()
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.ACTIVE))
                .inactiveProducts(productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.INACTIVE))
                .outOfStockProducts(productRepository.countOutOfStock())
                .lowStockProducts(productRepository.countLowStock())
                .discontinuedProducts(productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.DISCONTINUED))
                .productsOnSale(productRepository.countProductsOnSale())
                .featuredProducts(productRepository.countByFeaturedTrueAndDeletedAtIsNull())
                .newArrivals(productRepository.countByNewArrivalTrueAndDeletedAtIsNull())
                .bestSellers(productRepository.countByBestSellerTrueAndDeletedAtIsNull())
                .averagePrice(productRepository.getAveragePrice())
                .minPrice(productRepository.getMinPrice())
                .maxPrice(productRepository.getMaxPrice())
                .build();
    }

    @Override
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return productRepository.existsBySlugAndDeletedAtIsNull(slug);
    }

    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }

    @Override
    public long getProductCountByStatus(ProductStatus status) {
        return productRepository.countByStatusAndDeletedAtIsNull(status);
    }

    // ========== ADMIN METHODS ==========

    @Override
    @Transactional
    @CacheEvict(value = {"products", "featured-products", "new-arrivals", "best-sellers"}, allEntries = true)
    public ProductDetailResponse create(ProductCreateRequest request) {
        String slug = SlugUtils.generate(request.getName());
        if (productRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Brand brand = request.getBrandId() != null
                ? brandRepository.findById(request.getBrandId()).orElse(null)
                : null;
        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .sku(request.getSku())
                .category(category)
                .brand(brand)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .comparePrice(request.getComparePrice())
                .costPrice(request.getCostPrice())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .featured(Boolean.TRUE.equals(request.getFeatured()))
                .newArrival(Boolean.TRUE.equals(request.getNewArrival()))
                .bestSeller(Boolean.TRUE.equals(request.getBestSeller()))
                .tags(request.getTags())
                .sportTypes(request.getSportTypes())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
                .build();

        // Handle stockQuantity for simple products (no variants provided)
        if ((request.getVariants() == null || request.getVariants().isEmpty()) && request.getStockQuantity() != null) {
            ProductVariant defaultVariant = ProductVariant.builder()
                    .product(product)
                    .sku(request.getSku() != null ? request.getSku() : "DEF-" + UUID.randomUUID().toString().substring(0, 8))
                    .name("Default")
                    .stockQuantity(request.getStockQuantity())
                    .isActive(true)
                    .build();
            product.getVariants().add(defaultVariant);
        }

        if (request.getVariants() != null) {
            request.getVariants().forEach(varReq -> {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .sku(varReq.getSku())
                        .name(varReq.getName())
                        .priceModifier(varReq.getPriceModifier() != null ? varReq.getPriceModifier() : BigDecimal.ZERO)
                        .stockQuantity(varReq.getStockQuantity() != null ? varReq.getStockQuantity() : 0)
                        .barcode(varReq.getBarcode())
                        .imageUrl(varReq.getImageUrl())
                        .build();
                product.getVariants().add(variant);
            });
        }
        if (request.getSizeGuides() != null && !request.getSizeGuides().isEmpty()) {
            product.setSizeGuides(request.getSizeGuides());
        }

        // Thêm specifications
        if (request.getSpecifications() != null) {
            product.setSpecifications(request.getSpecifications());
        }
        applyProductImages(product, request.getImages());

        Product saved = productRepository.save(product);
        log.info("Product created: {} ({})", saved.getName(), saved.getId());
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "featured-products", "new-arrivals", "best-sellers"}, allEntries = true)
    public ProductDetailResponse update(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null && !request.getName().isBlank() && !request.getName().equals(product.getName())) {
            product.setName(request.getName());
            product.setSlug(com.hoangdinh.delta_shop_app.util.SlugUtils.generate(request.getName()));
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId()).orElse(null);
            product.setBrand(brand);
        }

        // Map standard fields
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getComparePrice() != null) product.setComparePrice(request.getComparePrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getSku() != null) product.setSku(request.getSku());
        
        // Map dimensions and weight
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getLength() != null) product.setLength(request.getLength());
        if (request.getWidth() != null) product.setWidth(request.getWidth());
        if (request.getHeight() != null) product.setHeight(request.getHeight());

        // Map flags
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        if (request.getNewArrival() != null) product.setNewArrival(request.getNewArrival());
        if (request.getBestSeller() != null) product.setBestSeller(request.getBestSeller());

        // Map collections
        if (request.getTags() != null) {
            product.getTags().clear();
            product.getTags().addAll(request.getTags());
        }
        if (request.getSportTypes() != null) {
            product.getSportTypes().clear();
            product.getSportTypes().addAll(request.getSportTypes());
        }

        // Handle stockQuantity update for simple products
        if (request.getStockQuantity() != null) {
            if (product.getVariants().isEmpty()) {
                ProductVariant defaultVariant = ProductVariant.builder()
                        .product(product)
                        .sku(product.getSku() != null ? product.getSku() : "DEF-" + UUID.randomUUID().toString().substring(0, 8))
                        .name("Default")
                        .stockQuantity(request.getStockQuantity())
                        .isActive(true)
                        .build();
                product.getVariants().add(defaultVariant);
            } else if (product.getVariants().size() == 1) {
                product.getVariants().get(0).setStockQuantity(request.getStockQuantity());
            }
        }

        if (request.getImages() != null) {
            applyProductImages(product, request.getImages());
        }
        if (request.getSizeGuides() != null) {
            product.setSizeGuides(request.getSizeGuides());
        }

        if (request.getSpecifications() != null) {
            product.setSpecifications(request.getSpecifications());
        }
        Product saved = productRepository.save(product);
        log.info("Product updated: {} ({})", saved.getName(), id);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "featured-products", "new-arrivals", "best-sellers"}, allEntries = true)
    public void delete(UUID id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.getImages().forEach(image -> {
            if (image.getPublicId() != null) {
                cloudinaryService.deleteFile(image.getPublicId());
            }
        });

        productRepository.delete(product);
        log.info("Product hard-deleted: {}", id);
        evictProductCache();
    }

    @Override
    @Transactional
    public ProductDetailResponse restore(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setDeletedAt(null);
        Product restored = productRepository.save(product);
        log.info("Product restored: {}", id);
        return mapToDetail(restored);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateStatus(UUID id, ProductStatus status) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setStatus(status);
        Product saved = productRepository.save(product);
        log.info("Product status updated: {} -> {}", id, status);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(List<UUID> productIds, ProductStatus status) {
        List<Product> products = productRepository.findAllById(productIds);
        products.forEach(product -> product.setStatus(status));
        productRepository.saveAll(products);
        log.info("Bulk status update: {} products set to {}", productIds.size(), status);
        evictProductCache();
    }

    @Override
    @Transactional
    public void bulkDelete(List<UUID> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        products.forEach(product -> product.setDeletedAt(LocalDateTime.now()));
        productRepository.saveAll(products);
        log.info("Bulk soft-delete: {} products", productIds.size());
        evictProductCache();
    }

    @Override
    @Transactional
    public void bulkUpdate(ProductBulkUpdateRequest request) {
        List<Product> products = productRepository.findAllById(request.getProductIds());

        for (Product product : products) {
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(UUID.fromString(request.getCategoryId()))
                        .orElseThrow();
                product.setCategory(category);
            }
            if (request.getBrandId() != null) {
                Brand brand = brandRepository.findById(UUID.fromString(request.getBrandId()))
                        .orElse(null);
                product.setBrand(brand);
            }
            if (request.getDiscountPercentage() != null) {
                // SỬA: Chuyển đổi discount percentage từ int sang BigDecimal
                BigDecimal discountPercent = BigDecimal.valueOf(request.getDiscountPercentage().intValue());
                BigDecimal hundred = BigDecimal.valueOf(100);
                BigDecimal multiplier = hundred.subtract(discountPercent);

                // Tính compare price mới
                BigDecimal comparePrice = product.getBasePrice()
                        .multiply(hundred)
                        .divide(multiplier, 2, RoundingMode.HALF_UP);
                product.setComparePrice(comparePrice);
            }
            if (request.getStatus() != null) {
                product.setStatus(request.getStatus());
            }
            if (request.getFeatured() != null) {
                product.setFeatured(request.getFeatured());
            }
            if (request.getNewArrival() != null) {
                product.setNewArrival(request.getNewArrival());
            }
            if (request.getBestSeller() != null) {
                product.setBestSeller(request.getBestSeller());
            }
        }

        productRepository.saveAll(products);
        log.info("Bulk update: {} products updated", request.getProductIds().size());
        evictProductCache();
    }

    @Override
    @Transactional
    public ProductDetailResponse updateFeatured(UUID id, boolean featured) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setFeatured(featured);
        Product saved = productRepository.save(product);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateNewArrival(UUID id, boolean newArrival) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setNewArrival(newArrival);
        Product saved = productRepository.save(product);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateBestSeller(UUID id, boolean bestSeller) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setBestSeller(bestSeller);
        Product saved = productRepository.save(product);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public void updateInventory(UUID id, int quantity, String type) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        for (ProductVariant variant : product.getVariants()) {
            int currentStock = variant.getStockQuantity();
            int newStock = switch (type.toUpperCase()) {
                case "ADD" -> currentStock + quantity;
                case "SUBTRACT" -> Math.max(0, currentStock - quantity);
                case "SET" -> Math.max(0, quantity);
                default -> currentStock;
            };
            variant.setStockQuantity(newStock);
        }

        productRepository.save(product);
        log.info("Inventory updated for product: {}, type: {}, quantity: {}", id, type, quantity);
    }

    @Override
    @Transactional
    public void reindexProduct(UUID id) {
        productRepository.findById(id).ifPresent(productRepository::save);
    }

    @Override
    @Transactional
    public void reindexAllProducts() {
        List<Product> products = productRepository.findAll();
        productRepository.saveAll(products);
        log.info("Reindexed all {} products", products.size());
    }

    // ========== VARIANT METHODS ==========

    @Override
    @Transactional
    public ProductDetailResponse addVariant(UUID productId, VariantRequest variantRequest) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (variantRepository.existsBySku(variantRequest.getSku())) {
            throw new RuntimeException("SKU already exists: " + variantRequest.getSku());
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(variantRequest.getSku())
                .name(variantRequest.getName())
                .priceModifier(variantRequest.getPriceModifier() != null ? variantRequest.getPriceModifier() : BigDecimal.ZERO)
                .stockQuantity(variantRequest.getStockQuantity() != null ? variantRequest.getStockQuantity() : 0)
                .barcode(variantRequest.getBarcode())
                .imageUrl(variantRequest.getImageUrl())
                .minStockAlert(variantRequest.getMinStockAlert() != null ? variantRequest.getMinStockAlert() : 5)
                .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                .build();

        product.getVariants().add(variant);
        Product saved = productRepository.save(product);
        log.info("Variant added to product: {}", productId);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateVariant(UUID variantId, VariantRequest variantRequest) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));

        if (variantRequest.getSku() != null && !variantRequest.getSku().equals(variant.getSku())) {
            if (variantRepository.existsBySku(variantRequest.getSku())) {
                throw new RuntimeException("SKU already exists: " + variantRequest.getSku());
            }
            variant.setSku(variantRequest.getSku());
        }
        if (variantRequest.getName() != null) variant.setName(variantRequest.getName());
        if (variantRequest.getPriceModifier() != null) variant.setPriceModifier(variantRequest.getPriceModifier());
        if (variantRequest.getStockQuantity() != null) variant.setStockQuantity(variantRequest.getStockQuantity());
        if (variantRequest.getBarcode() != null) variant.setBarcode(variantRequest.getBarcode());
        if (variantRequest.getImageUrl() != null) variant.setImageUrl(variantRequest.getImageUrl());
        if (variantRequest.getMinStockAlert() != null) variant.setMinStockAlert(variantRequest.getMinStockAlert());
        if (variantRequest.getIsActive() != null) variant.setActive(variantRequest.getIsActive());

        variantRepository.save(variant);
        log.info("Variant updated: {}", variantId);
        return mapToDetail(variant.getProduct());
    }

    @Override
    @Transactional
    public ProductDetailResponse deleteVariant(UUID variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));

        Product product = variant.getProduct();
        variant.setActive(false);
        variantRepository.save(variant);
        log.info("Variant deactivated: {}", variantId);
        return mapToDetail(product);
    }

    @Override
    @Transactional
    public void updateVariantStock(UUID variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));
        variant.setStockQuantity(quantity);
        variantRepository.save(variant);
        log.info("Variant stock updated: {} -> {}", variantId, quantity);
    }

    // ========== IMAGE METHODS ==========

    @Override
    @Transactional
    public ProductDetailResponse addImage(UUID productId, String imageUrl, boolean isPrimary) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(imageUrl)
                .isPrimary(isPrimary)
                .sortOrder(product.getImages().size())
                .build();

        if (isPrimary) {
            product.getImages().forEach(img -> img.setPrimary(false));
        }

        product.getImages().add(image);
        Product saved = productRepository.save(product);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse deleteImage(UUID imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        Product product = image.getProduct();

        if (image.getPublicId() != null) {
            cloudinaryService.deleteFile(image.getPublicId());
        }

        product.getImages().remove(image);

        if (image.isPrimary() && !product.getImages().isEmpty()) {
            product.getImages().get(0).setPrimary(true);
        }

        Product saved = productRepository.save(product);
        return mapToDetail(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse setPrimaryImage(UUID imageId) {
        ProductImage newPrimary = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        Product product = newPrimary.getProduct();
        product.getImages().forEach(img -> img.setPrimary(false));
        newPrimary.setPrimary(true);

        productRepository.save(product);
        return mapToDetail(product);
    }

    @Override
    @Transactional
    public void reorderImages(UUID productId, List<UUID> imageIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        for (int i = 0; i < imageIds.size(); i++) {
            UUID imageId = imageIds.get(i);
            final int order = i;
            product.getImages().stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .ifPresent(img -> img.setSortOrder(order));
        }

        productRepository.save(product);
    }

    // ========== PRIVATE METHODS ==========

    private Sort buildSort(String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return switch (sortBy != null ? sortBy : "createdAt") {
            case "price" -> Sort.by(direction, "basePrice");
            case "rating" -> Sort.by(direction, "averageRating");
            case "sold" -> Sort.by(direction, "totalSold");
            case "name" -> Sort.by(direction, "name");
            case "createdAt" -> Sort.by(direction, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private ProductDetailResponse mapToDetail(Product product) {
        List<ProductSummaryResponse> relatedProducts = getRelated(product.getId(), 10);
        return ProductDetailResponse.from(product, relatedProducts);
    }

    private ProductSummaryResponse mapToSummary(Product product) {
        return ProductSummaryResponse.from(product);
    }

    private void applyProductImages(Product product, List<ProductImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }

        List<ProductImage> newImages = new ArrayList<>();
        boolean hasPrimary = false;

        for (int i = 0; i < imageRequests.size(); i++) {
            ProductImageRequest imageRequest = imageRequests.get(i);
            if (imageRequest == null || imageRequest.getUrl() == null || imageRequest.getUrl().isBlank()) {
                continue;
            }

            boolean isPrimary = Boolean.TRUE.equals(imageRequest.getPrimary());
            if (isPrimary) {
                hasPrimary = true;
            }

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(imageRequest.getUrl())
                    .publicId(imageRequest.getPublicId())
                    .altText(imageRequest.getAltText())
                    .sortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : i)
                    .isPrimary(isPrimary)
                    .build();
            newImages.add(image);
        }

        if (newImages.isEmpty()) {
            return;
        }

        if (!hasPrimary) {
            newImages.get(0).setPrimary(true);
        }

        product.getImages().clear();
        product.getImages().addAll(newImages);
    }

    private void evictProductCache() {
        log.debug("Evicting product caches");
    }
}
