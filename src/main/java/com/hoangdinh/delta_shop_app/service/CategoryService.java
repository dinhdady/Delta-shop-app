package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.category.CategoryCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.category.CategoryUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.category.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getActiveCategories();

    List<CategoryResponse> getCategoryTree();

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse getCategoryBySlug(String slug);

    List<CategoryResponse> getSubcategories(UUID parentId);

    PageResponse<CategoryResponse> getCategoriesPaginated(int page, int size, String sortBy, String sortDir);

    CategoryResponse createCategory(CategoryCreateRequest request, UUID adminId);

    CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request);

    String uploadCategoryImage(UUID categoryId, MultipartFile file);

    void deleteCategoryImage(UUID categoryId);

    void deleteCategory(UUID id);

    void toggleCategoryStatus(UUID id);

    void reorderCategories(List<UUID> categoryIds);
}