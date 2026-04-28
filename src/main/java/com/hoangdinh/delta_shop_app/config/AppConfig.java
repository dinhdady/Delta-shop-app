package com.hoangdinh.delta_shop_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public String getFrontendUrl() {
        return frontendUrl;
    }
}