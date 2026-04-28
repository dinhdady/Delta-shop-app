package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.category.CategoryCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.category.CategoryUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.category.CategoryResponse;
import com.hoangdinh.delta_shop_app.entity.Category;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.CategoryRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.CategoryService;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
        return rootCategories.stream()
                .map(this::mapToTreeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToResponse(category);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return mapToResponse(category);
    }

    @Override
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));
        return parent.getChildren().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<CategoryResponse> getCategoriesPaginated(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "sortOrder");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryResponse> content = categoryPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CategoryResponse>builder()
                .content(content)
                .pageNumber(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .last(categoryPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", request.getParentId()));
        }

        String slug = generateSlug(request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .iconClass(request.getIconClass())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(true)
                .parent(parent)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} by admin {}", saved.getId(), admin.getEmail());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            category.setName(request.getName());
            category.setSlug(generateSlug(request.getName()));
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("Không thể đặt danh mục làm cha của chính nó");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getImageUrl() != null) category.setImageUrl(request.getImageUrl());
        if (request.getIconClass() != null) category.setIconClass(request.getIconClass());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getActive() != null) category.setActive(request.getActive());

        Category saved = categoryRepository.save(category);
        log.info("Category updated: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public String uploadCategoryImage(UUID categoryId, MultipartFile file) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Upload lên Cloudinary - giống như product
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "categories");
        String imageUrl = (String) uploadResult.get("secure_url");

        if (imageUrl == null) {
            imageUrl = (String) uploadResult.get("url");
        }

        category.setImageUrl(imageUrl);
        categoryRepository.save(category);

        log.info("Uploaded image for category: {}", categoryId);
        return imageUrl;
    }

    @Override
    @Transactional
    public void deleteCategoryImage(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Nếu có publicId trong Cloudinary, xóa khỏi Cloudinary
        String imageUrl = category.getImageUrl();
        if (imageUrl != null && imageUrl.contains("/upload/")) {
            // Extract public_id from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId);
            }
        }

        category.setImageUrl(null);
        categoryRepository.save(category);

        log.info("Deleted image for category: {}", categoryId);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Kiểm tra có danh mục con không
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new BusinessException("Không thể xóa danh mục vì có danh mục con");
        }

        // Kiểm tra có sản phẩm không
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new BusinessException("Không thể xóa danh mục vì có sản phẩm thuộc danh mục này");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }

    @Override
    @Transactional
    public void toggleCategoryStatus(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setActive(!category.isActive());
        categoryRepository.save(category);

        log.info("Category status toggled: {} -> {}", id, category.isActive());
    }

    @Override
    @Transactional
    public void reorderCategories(List<UUID> categoryIds) {
        IntStream.range(0, categoryIds.size()).forEach(i -> {
            UUID categoryId = categoryIds.get(i);
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
            category.setSortOrder(i);
            categoryRepository.save(category);
        });
        log.info("Categories reordered");
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("á|à|ả|ã|ạ", "a")
                .replaceAll("ă|ằ|ẳ|ẵ|ặ", "a")
                .replaceAll("â|ầ|ẩ|ẫ|ậ", "a")
                .replaceAll("đ", "d")
                .replaceAll("é|è|ẻ|ẽ|ẹ", "e")
                .replaceAll("ê|ế|ề|ể|ễ|ệ", "e")
                .replaceAll("í|ì|ỉ|ĩ|ị", "i")
                .replaceAll("ó|ò|ỏ|õ|ọ", "o")
                .replaceAll("ô|ố|ồ|ổ|ỗ|ộ", "o")
                .replaceAll("ơ|ớ|ờ|ở|ỡ|ợ", "o")
                .replaceAll("ú|ù|ủ|ũ|ụ", "u")
                .replaceAll("ư|ứ|ừ|ử|ữ|ự", "u")
                .replaceAll("ý|ỳ|ỷ|ỹ|ỵ", "y")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");

        // Ensure unique slug
        String originalSlug = slug;
        int counter = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + counter++;
        }

        return slug;
    }

    private String extractPublicIdFromUrl(String url) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/.../upload/v1234567890/folder/image.jpg
        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Remove version prefix if exists
                if (path.contains("/v")) {
                    path = path.substring(path.indexOf("/v") + 1);
                    path = path.substring(path.indexOf("/") + 1);
                }
                // Remove file extension
                int dotIndex = path.lastIndexOf(".");
                if (dotIndex > 0) {
                    path = path.substring(0, dotIndex);
                }
                return path;
            }
        } catch (Exception e) {
            log.warn("Could not extract public_id from URL: {}", url);
        }
        return null;
    }

    private CategoryResponse mapToResponse(Category category) {
        long productCount = category.getProducts() != null ? category.getProducts().size() : 0;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .iconClass(category.getIconClass())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .productCount((int) productCount)
                .build();
    }

    private CategoryResponse mapToTreeResponse(Category category) {
        CategoryResponse response = mapToResponse(category);

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponse> children = category.getChildren().stream()
                    .filter(child -> child.isActive())
                    .map(child -> {
                        return mapToTreeResponse(child);
                    })
                    .collect(Collectors.toList());
            response.setChildren(children);
        }

        return response;
    }
}