package com.hoangdinh.delta_shop_app.config;

import com.cloudinary.Cloudinary;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
public class CloudinaryConfig {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${app.cloudinary.folder:delta-sports}")
    private String defaultFolder;

    @Value("${app.cloudinary.secure:true}")
    private boolean secure;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudName == null || cloudName.isEmpty()) {
            throw new IllegalStateException("Cloudinary configuration is missing. Please check application properties.");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", secure);

        return new Cloudinary(config);
    }

    public Map<String, Object> getUploadOptions(String folder) {
        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder != null ? defaultFolder + "/" + folder : defaultFolder);
        options.put("use_filename", true);
        options.put("unique_filename", true);
        options.put("overwrite", true);
        options.put("resource_type", "auto");
        return options;
    }

    public Map<String, Object> getImageTransformOptions(int width, int height, String crop) {
        Map<String, Object> options = new HashMap<>();
        options.put("width", width);
        options.put("height", height);
        options.put("crop", crop != null ? crop : "limit");
        options.put("quality", "auto");
        options.put("fetch_format", "auto");
        return options;
    }
}