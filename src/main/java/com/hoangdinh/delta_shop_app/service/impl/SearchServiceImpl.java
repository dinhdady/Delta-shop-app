package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.search.AdvancedSearchRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.AutoCompleteResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.FacetResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.FilterOptionResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.SearchSuggestionResponse;
import com.hoangdinh.delta_shop_app.entity.Brand;
import com.hoangdinh.delta_shop_app.entity.Category;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductImage;
import com.hoangdinh.delta_shop_app.enums.ProductStatus;
import com.hoangdinh.delta_shop_app.repository.BrandRepository;
import com.hoangdinh.delta_shop_app.repository.CategoryRepository;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    private static final List<String> POPULAR_KEYWORDS = List.of(
            "giày chạy bộ", "áo thể thao", "tạ tay", "thảm yoga", 
            "vợt cầu lông", "quần đùi nam", "áo polo thể thao", "bóng đá"
    );

    @Override
    public PageResponse<ProductSummaryResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalSold").descending());
        Page<Product> products = productRepository.searchProducts(
                keyword, null, null, null, null, ProductStatus.ACTIVE, pageable
        );
        return PageResponse.of(products.map(ProductSummaryResponse::from));
    }

    @Override
    public PageResponse<ProductSummaryResponse> advancedSearch(AdvancedSearchRequest request) {
        Sort.Direction direction = Sort.Direction.fromString(
                request.getSortDir() != null ? request.getSortDir() : "desc"
        );
        String sortByField = request.getSortBy() != null && !request.getSortBy().isEmpty() 
                ? request.getSortBy() : "totalSold";
                
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, sortByField)
        );

        BigDecimal minPrice = request.getMinPrice() != null ? BigDecimal.valueOf(request.getMinPrice()) : null;
        BigDecimal maxPrice = request.getMaxPrice() != null ? BigDecimal.valueOf(request.getMaxPrice()) : null;

        Page<Product> products = productRepository.searchProducts(
                request.getKeyword(),
                request.getCategoryId(),
                request.getBrandId(),
                minPrice,
                maxPrice,
                ProductStatus.ACTIVE,
                pageable
        );

        return PageResponse.of(products.map(ProductSummaryResponse::from));
    }

    @Override
    public List<AutoCompleteResponse> getAutoCompleteSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.trim().length() < 2) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by("totalViews").descending());
        Page<Product> products = productRepository.searchProducts(
                prefix.trim(), null, null, null, null, ProductStatus.ACTIVE, pageable
        );

        return products.stream()
                .map(this::mapToAutoCompleteResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SearchSuggestionResponse getSearchSuggestions(String keyword) {
        List<AutoCompleteResponse> suggestions = getAutoCompleteSuggestions(keyword, 5);
        List<String> popular = getPopularSearchKeywords(5);
        return SearchSuggestionResponse.builder()
                .keyword(keyword)
                .suggestions(suggestions)
                .popularKeywords(popular)
                .build();
    }

    @Override
    public List<String> getPopularSearchKeywords(int limit) {
        return POPULAR_KEYWORDS.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<FilterOptionResponse>> getFilterOptions(String categorySlug) {
        Map<String, List<FilterOptionResponse>> filterOptions = new HashMap<>();

        // Get brand options
        List<Brand> brands = brandRepository.findAll();
        List<FilterOptionResponse> brandOptions = brands.stream()
                .map(b -> FilterOptionResponse.builder()
                        .value(b.getId().toString())
                        .label(b.getName())
                        .count(productRepository.countByBrandIdAndDeletedAtIsNull(b.getId()))
                        .build())
                .collect(Collectors.toList());

        // Get category options
        List<Category> categories = categoryRepository.findAll();
        List<FilterOptionResponse> categoryOptions = categories.stream()
                .map(c -> FilterOptionResponse.builder()
                        .value(c.getSlug())
                        .label(c.getName())
                        .count(productRepository.countByCategoryIdAndDeletedAtIsNull(c.getId()))
                        .build())
                .collect(Collectors.toList());

        filterOptions.put("brands", brandOptions);
        filterOptions.put("categories", categoryOptions);

        return filterOptions;
    }

    @Override
    public List<FacetResponse> getSearchFacets(String keyword) {
        List<Product> products = productRepository.searchProducts(
                keyword, null, null, null, null, ProductStatus.ACTIVE, Pageable.unpaged()
        ).getContent();

        // Facets for categories
        Map<Category, Long> categoryCounts = products.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        List<FilterOptionResponse> categoryFacets = categoryCounts.entrySet().stream()
                .map(e -> FilterOptionResponse.builder()
                        .value(e.getKey().getId().toString())
                        .label(e.getKey().getName())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        // Facets for brands
        Map<Brand, Long> brandCounts = products.stream()
                .filter(p -> p.getBrand() != null)
                .collect(Collectors.groupingBy(Product::getBrand, Collectors.counting()));

        List<FilterOptionResponse> brandFacets = brandCounts.entrySet().stream()
                .map(e -> FilterOptionResponse.builder()
                        .value(e.getKey().getId().toString())
                        .label(e.getKey().getName())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        List<FacetResponse> facets = new ArrayList<>();
        facets.add(FacetResponse.builder().name("Categories").options(categoryFacets).build());
        facets.add(FacetResponse.builder().name("Brands").options(brandFacets).build());

        return facets;
    }

    @Override
    @Transactional
    public void indexProduct(UUID productId) {
        // DB-based search does not require active reindexing
        log.info("Indexed product in database search: {}", productId);
    }

    @Override
    @Transactional
    public void reindexAllProducts() {
        log.info("Reindexed all products in database search");
    }

    @Override
    @Transactional
    public void removeFromIndex(UUID productId) {
        log.info("Removed product from database search index: {}", productId);
    }

    @Override
    @Transactional
    public void logSearchQuery(String keyword, UUID userId) {
        log.info("User {} searched for keyword: {}", userId, keyword);
    }

    @Override
    public List<String> getTrendingSearches(int limit) {
        return getPopularSearchKeywords(limit);
    }

    @Override
    public Map<String, Long> getSearchAnalytics(LocalDate startDate, LocalDate endDate) {
        return Collections.emptyMap();
    }

    private AutoCompleteResponse mapToAutoCompleteResponse(Product product) {
        String primaryImageUrl = "assets/images/placeholder.jpg";
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            primaryImageUrl = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .map(ProductImage::getUrl)
                    .findFirst()
                    .orElse(product.getImages().get(0).getUrl());
        }

        return AutoCompleteResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .primaryImage(primaryImageUrl)
                .basePrice(product.getBasePrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }
}
