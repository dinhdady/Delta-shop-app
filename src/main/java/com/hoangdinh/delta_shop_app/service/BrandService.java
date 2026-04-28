package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.brand.BrandCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.brand.BrandUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.brand.BrandResponse;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

public interface BrandService {

    // Query methods
    List<BrandResponse> getAllBrands();
    List<BrandResponse> getActiveBrands();
    List<BrandResponse> getFeaturedBrands();
    BrandResponse getBrandById(UUID id);
    BrandResponse getBrandBySlug(String slug);
    PageResponse<BrandResponse> getBrandsPaginated(int page, int size, String sortBy, String sortDir);

    // Admin methods
    BrandResponse createBrand(BrandCreateRequest request, UUID adminId);
    BrandResponse updateBrand(UUID id, BrandUpdateRequest request);
    void deleteBrand(UUID id);
    void toggleBrandStatus(UUID id);
    void toggleBrandFeatured(UUID id);

    // Helper methods
    boolean isBrandExists(String name);
    long getProductCountByBrand(UUID brandId);
}