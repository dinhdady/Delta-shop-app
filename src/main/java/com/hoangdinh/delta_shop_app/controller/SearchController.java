package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.search.AdvancedSearchRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.AutoCompleteResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.FacetResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.FilterOptionResponse;
import com.hoangdinh.delta_shop_app.dto.response.search.SearchSuggestionResponse;
import com.hoangdinh.delta_shop_app.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "APIs for advanced search and suggestions")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/autocomplete")
    @Operation(summary = "Get autocomplete search suggestions")
    public ResponseEntity<List<AutoCompleteResponse>> getAutoComplete(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(searchService.getAutoCompleteSuggestions(prefix, limit));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions and popular keywords")
    public ResponseEntity<SearchSuggestionResponse> getSuggestions(@RequestParam String keyword) {
        return ResponseEntity.ok(searchService.getSearchSuggestions(keyword));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular search keywords")
    public ResponseEntity<List<String>> getPopularKeywords(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(searchService.getPopularSearchKeywords(limit));
    }

    @GetMapping("/facets")
    @Operation(summary = "Get search facets (categories/brands counters)")
    public ResponseEntity<List<FacetResponse>> getSearchFacets(@RequestParam String keyword) {
        return ResponseEntity.ok(searchService.getSearchFacets(keyword));
    }

    @GetMapping("/filter-options")
    @Operation(summary = "Get category filter options")
    public ResponseEntity<Map<String, List<FilterOptionResponse>>> getFilterOptions(
            @RequestParam(required = false) String categorySlug) {
        return ResponseEntity.ok(searchService.getFilterOptions(categorySlug));
    }

    @PostMapping("/advanced")
    @Operation(summary = "Perform advanced product search")
    public ResponseEntity<PageResponse<ProductSummaryResponse>> advancedSearch(
            @RequestBody AdvancedSearchRequest request) {
        return ResponseEntity.ok(searchService.advancedSearch(request));
    }
}
