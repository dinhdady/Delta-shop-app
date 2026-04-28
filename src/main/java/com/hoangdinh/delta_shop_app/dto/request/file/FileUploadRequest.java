package com.hoangdinh.delta_shop_app.dto.request.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private MultipartFile file;
    private String folder;
    private Boolean isImage;
    private Integer width;
    private Integer height;
    private String crop;
}