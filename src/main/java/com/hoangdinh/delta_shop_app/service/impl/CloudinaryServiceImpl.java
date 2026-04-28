package com.hoangdinh.delta_shop_app.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.hoangdinh.delta_shop_app.config.CloudinaryConfig;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryConfig cloudinaryConfig;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // ========== UPLOAD METHODS ==========
    @Override
    public String uploadImageAndGetUrl(MultipartFile file, String folder) {
        Map<String, Object> uploadResult = uploadImage(file, folder);
        String secureUrl = (String) uploadResult.get("secure_url");
        if (secureUrl == null) {
            secureUrl = (String) uploadResult.get("url");
        }
        return secureUrl;
    }
    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        validateFile(file);

        try {
            Map<String, Object> options = cloudinaryConfig.getUploadOptions(folder);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            log.info("File uploaded successfully: {}", uploadResult.get("public_id"));
            return uploadResult;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new BusinessException("Không thể upload file: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        validateImage(file);
        return uploadFile(file, folder);
    }

    @Override
    public Map<String, Object> uploadFileWithOptions(MultipartFile file, String folder, Map<String, Object> options) {
        validateFile(file);

        try {
            Map<String, Object> uploadOptions = cloudinaryConfig.getUploadOptions(folder);
            if (options != null) {
                uploadOptions.putAll(options);
            }

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            log.info("File uploaded with custom options: {}", uploadResult.get("public_id"));
            return uploadResult;
        } catch (IOException e) {
            log.error("Failed to upload file with options: {}", e.getMessage());
            throw new BusinessException("Không thể upload file: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> uploadMultipleFiles(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(file -> uploadFile(file, folder))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(file -> uploadImage(file, folder))
                .collect(Collectors.toList());
    }

    // ========== DELETE METHODS ==========

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted successfully: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new BusinessException("Không thể xóa file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFiles(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) {
            return;
        }

        publicIds.forEach(this::deleteFile);
    }

    @Override
    public Map<String, Object> deleteFileWithResult(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted with result: {}", result);
            return result;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new BusinessException("Không thể xóa file: " + e.getMessage());
        }
    }

    // ========== FILE INFO METHODS ==========

    @Override
    public Map<String, Object> getFileInfo(String publicId) {
        try {
            return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Failed to get file info: {}", e.getMessage());
            throw new BusinessException("Không thể lấy thông tin file: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }

    @Override
    public String getFileUrl(String publicId, Map<String, Object> transformationParams) {
        Transformation transformation = new Transformation();
        if (transformationParams != null) {
            for (Map.Entry<String, Object> entry : transformationParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                switch (key) {
                    case "width":
                        transformation.width((Integer) value);
                        break;
                    case "height":
                        transformation.height((Integer) value);
                        break;
                    case "crop":
                        transformation.crop((String) value);
                        break;
                    case "gravity":
                        transformation.gravity((String) value);
                        break;
                    case "quality":
                        transformation.quality((String) value);
                        break;
                    case "fetch_format":
                        transformation.fetchFormat((String) value);
                        break;
                    case "radius":
                        transformation.radius((Integer) value);
                        break;
                    case "angle":
                        transformation.angle((Integer) value);
                        break;
                    case "effect":
                        transformation.effect((String) value);
                        break;
                    case "overlay":
                        transformation.overlay((String) value);
                        break;
                    case "x":
                        transformation.x((Integer) value);
                        break;
                    case "y":
                        transformation.y((Integer) value);
                        break;
                }
            }
        }
        return cloudinary.url().transformation(transformation).generate(publicId);
    }

    // ========== TRANSFORMATION METHODS ==========

    @Override
    public String resizeImage(String publicId, int width, int height) {
        Transformation transformation = new Transformation()
                .width(width)
                .height(height)
                .crop("limit")
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url().transformation(transformation).generate(publicId);
    }

    @Override
    public String cropImage(String publicId, int width, int height, String gravity) {
        Transformation transformation = new Transformation()
                .width(width)
                .height(height)
                .crop("crop")
                .gravity(gravity != null ? gravity : "center")
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url().transformation(transformation).generate(publicId);
    }

    @Override
    public String applyWatermark(String publicId, String watermarkText) {
        Transformation transformation = new Transformation()
                .overlay("text:Arial_60:" + watermarkText.replace(" ", "_"))
                .gravity("south_east")
                .x(10)
                .y(10)
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url().transformation(transformation).generate(publicId);
    }


    @Override
    public String applyEffects(String publicId, Map<String, Object> effects) {
        Transformation transformation = new Transformation();

        if (effects != null) {
            if (effects.containsKey("effect")) {
                transformation.effect((String) effects.get("effect"));
            }
            if (effects.containsKey("radius")) {
                transformation.radius((Integer) effects.get("radius"));
            }
            if (effects.containsKey("angle")) {
                transformation.angle((Integer) effects.get("angle"));
            }
        }

        transformation.quality("auto").fetchFormat("auto");

        return cloudinary.url().transformation(transformation).generate(publicId);
    }

    // ========== PRODUCT IMAGE METHODS ==========

    @Override
    public Map<String, Object> uploadProductImage(MultipartFile file, String productId) {
        String folder = "products/" + productId;
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", cloudinaryConfig.getDefaultFolder() + "/" + folder,
                "public_id", generateProductImagePublicId(productId),
                "tags", new String[]{"product", productId},
                "context", String.format("product=%s", productId)
        );

        return uploadFileWithOptions(file, folder, options);
    }

    @Override
    public void deleteProductImage(String publicId) {
        deleteFile(publicId);
    }

    @Override
    public String getProductImageUrl(String publicId, int width, int height) {
        Transformation transformation = new Transformation()
                .width(width)
                .height(height)
                .crop("fill")
                .gravity("auto")
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url().transformation(transformation).generate(publicId);
    }

    // ========== USER AVATAR METHODS ==========

    @Override
    public Map<String, Object> uploadUserAvatar(MultipartFile file, String userId) {
        String folder = "users/" + userId + "/avatar";
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", cloudinaryConfig.getDefaultFolder() + "/" + folder,
                "public_id", "avatar",
                "tags", new String[]{"user", userId, "avatar"},
                "overwrite", true
        );

        return uploadFileWithOptions(file, folder, options);
    }

    @Override
    public void deleteUserAvatar(String publicId) {
        deleteFile(publicId);
    }

    @Override
    public String getUserAvatarUrl(String publicId, int width, int height) {
        Transformation transformation = new Transformation()
                .width(width)
                .height(height)
                .crop("thumb")
                .gravity("face")
                .radius("max")
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url().transformation(transformation).generate(publicId);
    }

    // ========== REVIEW IMAGE METHODS ==========

    @Override
    public Map<String, Object> uploadReviewImage(MultipartFile file, String reviewId) {
        String folder = "reviews/" + reviewId;
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", cloudinaryConfig.getDefaultFolder() + "/" + folder,
                "tags", new String[]{"review", reviewId},
                "context", String.format("review=%s", reviewId)
        );

        return uploadFileWithOptions(file, folder, options);
    }

    @Override
    public void deleteReviewImage(String publicId) {
        deleteFile(publicId);
    }

    // ========== GENERATE URL METHODS ==========

    @Override
    public String generateDefaultAvatarUrl(String name) {
        String initials = generateInitials(name);
        return String.format("https://ui-avatars.com/api/?name=%s&background=0D8F81&color=fff&size=128",
                initials);
    }

    @Override
    public String generatePlaceholderImage(String text, int width, int height) {
        return String.format("https://placehold.co/%dx%d?text=%s", width, height, text);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File vượt quá kích thước cho phép (10MB)");
        }
    }

    private void validateImage(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Định dạng file không được hỗ trợ. Chấp nhận: JPEG, PNG, WEBP, GIF");
        }
    }

    private String generateProductImagePublicId(String productId) {
        return String.format("product_%s_%d", productId, System.currentTimeMillis());
    }

    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}