package com.hoangdinh.delta_shop_app.dto.response.file;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class FileUploadResponse {
    private String publicId;
    private String url;
    private String secureUrl;
    private String format;
    private Long bytes;
    private Integer width;
    private Integer height;
    private String etag;
    private ZonedDateTime createdAt;
    private String folder;

    public static FileUploadResponse from(Map<String, Object> uploadResult) {
        if (uploadResult == null) return null;

        return FileUploadResponse.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("url"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .bytes(uploadResult.get("bytes") != null ? ((Number) uploadResult.get("bytes")).longValue() : null)
                .width(uploadResult.get("width") != null ? ((Number) uploadResult.get("width")).intValue() : null)
                .height(uploadResult.get("height") != null ? ((Number) uploadResult.get("height")).intValue() : null)
                .etag((String) uploadResult.get("etag"))
                .folder((String) uploadResult.get("folder"))
                .build();
    }
}