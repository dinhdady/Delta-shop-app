package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.file.FileUploadRequest;
import com.hoangdinh.delta_shop_app.dto.response.file.FileUploadResponse;
import com.hoangdinh.delta_shop_app.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for file upload and management")
public class FileController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload a single file")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {

        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, folder);
        return ResponseEntity.ok(FileUploadResponse.from(uploadResult));
    }

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload a single image")
    public ResponseEntity<FileUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {

        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, folder);
        return ResponseEntity.ok(FileUploadResponse.from(uploadResult));
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload multiple files")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", required = false) String folder) {

        List<Map<String, Object>> uploadResults = cloudinaryService.uploadMultipleFiles(files, folder);
        List<FileUploadResponse> responses = uploadResults.stream()
                .map(FileUploadResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping(value = "/upload/multiple/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload multiple images")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", required = false) String folder) {

        List<Map<String, Object>> uploadResults = cloudinaryService.uploadMultipleImages(files, folder);
        List<FileUploadResponse> responses = uploadResults.stream()
                .map(FileUploadResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a file by public ID")
    public ResponseEntity<Void> deleteFile(@PathVariable String publicId) {
        cloudinaryService.deleteFile(publicId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete multiple files")
    public ResponseEntity<Void> deleteFiles(@RequestParam List<String> publicIds) {
        cloudinaryService.deleteFiles(publicIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/url/{publicId}")
    @Operation(summary = "Get file URL by public ID")
    public ResponseEntity<String> getFileUrl(@PathVariable String publicId) {
        String url = cloudinaryService.getFileUrl(publicId);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/info/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get file information")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String publicId) {
        Map<String, Object> fileInfo = cloudinaryService.getFileInfo(publicId);
        return ResponseEntity.ok(fileInfo);
    }

    @PostMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload product image")
    public ResponseEntity<FileUploadResponse> uploadProductImage(
            @PathVariable String productId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> uploadResult = cloudinaryService.uploadProductImage(file, productId);
        return ResponseEntity.ok(FileUploadResponse.from(uploadResult));
    }

    @PostMapping("/user/avatar")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload user avatar")
    public ResponseEntity<FileUploadResponse> uploadUserAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") String userId) {

        Map<String, Object> uploadResult = cloudinaryService.uploadUserAvatar(file, userId);
        return ResponseEntity.ok(FileUploadResponse.from(uploadResult));
    }

    @PostMapping("/review/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload review image")
    public ResponseEntity<FileUploadResponse> uploadReviewImage(
            @PathVariable String reviewId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> uploadResult = cloudinaryService.uploadReviewImage(file, reviewId);
        return ResponseEntity.ok(FileUploadResponse.from(uploadResult));
    }
}
