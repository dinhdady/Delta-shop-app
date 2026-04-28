package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.search.AdvancedSearchRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.AutoCompleteResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.FacetResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.FilterOptionResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.SearchSuggestionResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SearchService {

    // Product search
    PageResponse<ProductSummaryResponse> searchProducts(String keyword, int page, int size);
    PageResponse<ProductSummaryResponse> advancedSearch(AdvancedSearchRequest request);

    // Auto-complete and suggestions
    List<AutoCompleteResponse> getAutoCompleteSuggestions(String prefix, int limit);
    SearchSuggestionResponse getSearchSuggestions(String keyword);
    List<String> getPopularSearchKeywords(int limit);

    // Filter options
    Map<String, List<FilterOptionResponse>> getFilterOptions(String categorySlug);
    List<FacetResponse> getSearchFacets(String keyword);

    // Index management
    void indexProduct(UUID productId);
    void reindexAllProducts();
    void removeFromIndex(UUID productId);

    // Search analytics
    void logSearchQuery(String keyword, UUID userId);
    List<String> getTrendingSearches(int limit);
    Map<String, Long> getSearchAnalytics(java.time.LocalDate startDate, java.time.LocalDate endDate);
}