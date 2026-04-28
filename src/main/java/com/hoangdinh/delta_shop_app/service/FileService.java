package com.hoangdinh.delta_shop_app.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FileService {

    // Single file upload
    String uploadFile(MultipartFile file, String folder);
    String uploadImage(MultipartFile file, String folder);
    String uploadFileWithOptions(MultipartFile file, Map<String, Object> options);

    // Multiple files upload
    List<String> uploadMultipleFiles(List<MultipartFile> files, String folder);
    List<String> uploadMultipleImages(List<MultipartFile> files, String folder);

    // Delete files
    void deleteFile(String publicId);
    void deleteFiles(List<String> publicIds);

    // Get file info
    Map<String, Object> getFileInfo(String publicId);
    String getFileUrl(String publicId);

    // Transformations
    String resizeImage(String publicId, int width, int height);
    String applyWatermark(String publicId, String watermarkText);

    // Product images
    String uploadProductImage(MultipartFile file, UUID productId);
    void deleteProductImage(String publicId, UUID productId);
    void reorderProductImages(UUID productId, List<String> publicIds);

    // User avatar
    String uploadUserAvatar(MultipartFile file, UUID userId);
    void deleteUserAvatar(UUID userId);

    // Review images
    String uploadReviewImage(MultipartFile file, UUID reviewId);
    void deleteReviewImage(String publicId, UUID reviewId);
}