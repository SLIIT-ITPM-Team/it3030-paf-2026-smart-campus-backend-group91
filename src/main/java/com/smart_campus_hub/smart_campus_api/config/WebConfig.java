package com.smart_campus_hub.smart_campus_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        // legacy path compatibility for records stored with /api/uploads/ prefix
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:uploads/tickets/");
    }
}
