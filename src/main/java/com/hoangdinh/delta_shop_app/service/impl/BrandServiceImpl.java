package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.brand.BrandCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.brand.BrandUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.brand.BrandResponse;
import com.hoangdinh.delta_shop_app.entity.Brand;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.BrandRepository;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.BrandService;
import com.hoangdinh.delta_shop_app.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<BrandResponse> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BrandResponse> getActiveBrands() {
        // SỬA: dùng findByActiveTrueOrderBySortOrderAsc
        List<Brand> brands = brandRepository.findByActiveTrueOrderBySortOrderAsc();
        return brands.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BrandResponse> getFeaturedBrands() {
        // SỬA: dùng findByFeaturedTrueAndActiveTrue
        List<Brand> brands = brandRepository.findByFeaturedTrueAndActiveTrue();
        return brands.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BrandResponse getBrandById(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return mapToResponse(brand);
    }

    @Override
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "slug", slug));
        return mapToResponse(brand);
    }

    @Override
    public PageResponse<BrandResponse> getBrandsPaginated(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "sortOrder");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Brand> brandPage = brandRepository.findAll(pageable);

        return PageResponse.of(brandPage.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public BrandResponse createBrand(BrandCreateRequest request, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));

        if (!admin.isAdmin()) {
            throw new BusinessException("Chỉ admin mới có quyền tạo thương hiệu");
        }

        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Thương hiệu đã tồn tại");
        }

        String slug = SlugUtils.generate(request.getName());

        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(slug)
                .logoUrl(request.getLogoUrl())
                .websiteUrl(request.getWebsiteUrl())
                .description(request.getDescription())
                .countryOfOrigin(request.getCountryOfOrigin())
                .featured(request.getIsFeatured() != null ? request.getIsFeatured() : false)  // SỬA: dùng featured()
                .active(true)  // SỬA: dùng active()
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        Brand saved = brandRepository.save(brand);
        log.info("Brand created: {} ({})", saved.getName(), saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(UUID id, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        if (request.getName() != null && !request.getName().equals(brand.getName())) {
            if (brandRepository.existsByNameIgnoreCase(request.getName())) {
                throw new BusinessException("Thương hiệu đã tồn tại");
            }
            brand.setName(request.getName());
            brand.setSlug(SlugUtils.generate(request.getName()));
        }
        if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl());
        }
        if (request.getWebsiteUrl() != null) {
            brand.setWebsiteUrl(request.getWebsiteUrl());
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }
        if (request.getCountryOfOrigin() != null) {
            brand.setCountryOfOrigin(request.getCountryOfOrigin());
        }
        if (request.getIsFeatured() != null) {
            brand.setFeatured(request.getIsFeatured());  // SỬA: dùng setFeatured
        }
        if (request.getIsActive() != null) {
            brand.setActive(request.getIsActive());  // SỬA: dùng setActive
        }
        if (request.getSortOrder() != null) {
            brand.setSortOrder(request.getSortOrder());
        }

        Brand saved = brandRepository.save(brand);
        log.info("Brand updated: {}", id);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBrand(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // SỬA: dùng existsByBrandIdAndDeletedAtIsNull
        if (productRepository.existsByBrandIdAndDeletedAtIsNull(id)) {
            throw new BusinessException("Không thể xóa thương hiệu vì có sản phẩm liên quan");
        }

        brand.setActive(false);  // SỬA: dùng setActive
        brandRepository.save(brand);
        log.info("Brand deleted: {}", id);
    }

    @Override
    @Transactional
    public void toggleBrandStatus(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brand.setActive(!brand.isActive());  // SỬA: dùng setActive
        brandRepository.save(brand);
        log.info("Brand status toggled: {} -> {}", id, brand.isActive());
    }


    @Override
    @Transactional
    public void toggleBrandFeatured(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brand.setFeatured(!brand.isFeatured());  // SỬA: dùng setFeatured
        brandRepository.save(brand);
        log.info("Brand featured toggled: {} -> {}", id, brand.isFeatured());
    }

    @Override
    public boolean isBrandExists(String name) {
        return false;
    }

    @Override
    public long getProductCountByBrand(UUID brandId) {
        // SỬA: dùng countByBrandIdAndDeletedAtIsNull
        return productRepository.countByBrandIdAndDeletedAtIsNull(brandId);
    }

    private BrandResponse mapToResponse(Brand brand) {
        long productCount = getProductCountByBrand(brand.getId());

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logoUrl(brand.getLogoUrl())
                .websiteUrl(brand.getWebsiteUrl())
                .description(brand.getDescription())
                .countryOfOrigin(brand.getCountryOfOrigin())
                .featured(brand.isFeatured())
                .active(brand.isActive())
                .productCount((int) productCount)
                .build();
    }
}