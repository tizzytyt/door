package com.access.control.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Autowired
    private UploadStorageProperties storage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = storage.getUploadRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        // /uploads/avatars/a.jpg -> {uploadRoot}/avatars/a.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
