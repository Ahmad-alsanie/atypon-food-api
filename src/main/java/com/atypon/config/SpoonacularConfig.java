package com.atypon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spoonacular")
public class SpoonacularConfig {
    private String baseUrl;
    private String apiKey;
}
