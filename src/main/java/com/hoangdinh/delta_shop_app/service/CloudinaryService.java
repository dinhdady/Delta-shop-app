package com.hoangdinh.delta_shop_app.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CloudinaryService {

    // Single file upload
    Map<String, Object> uploadFile(MultipartFile file, String folder);
    Map<String, Object> uploadImage(MultipartFile file, String folder);
    Map<String, Object> uploadFileWithOptions(MultipartFile file, String folder, Map<String, Object> options);

    // Multiple files upload
    List<Map<String, Object>> uploadMultipleFiles(List<MultipartFile> files, String folder);
    List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder);

    // Delete files
    void deleteFile(String publicId);
    void deleteFiles(List<String> publicIds);
    Map<String, Object> deleteFileWithResult(String publicId);

    // Get file info
    Map<String, Object> getFileInfo(String publicId);
    String getFileUrl(String publicId);
    String getFileUrl(String publicId, Map<String, Object> transformation);

    // Transformations
    String resizeImage(String publicId, int width, int height);
    String cropImage(String publicId, int width, int height, String gravity);
    String applyWatermark(String publicId, String watermarkText);
    String applyEffects(String publicId, Map<String, Object> effects);

    // Product images
    Map<String, Object> uploadProductImage(MultipartFile file, String productId);
    void deleteProductImage(String publicId);
    String getProductImageUrl(String publicId, int width, int height);

    // User avatar
    Map<String, Object> uploadUserAvatar(MultipartFile file, String userId);
    void deleteUserAvatar(String publicId);
    String getUserAvatarUrl(String publicId, int width, int height);

    // Review images
    Map<String, Object> uploadReviewImage(MultipartFile file, String reviewId);
    void deleteReviewImage(String publicId);

    // Generate URLs
    String generateDefaultAvatarUrl(String name);
    String generatePlaceholderImage(String text, int width, int height);
    public String uploadImageAndGetUrl(MultipartFile file, String folder);
}