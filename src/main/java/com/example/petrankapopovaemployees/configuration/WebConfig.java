package com.example.petrankapopovaemployees.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the Spring MVC application.
     * Allows requests from "http://localhost:8000" to the "/api" endpoint with specified HTTP methods, headers, and credentials.
     *
     * @param registry the CORS registry for configuring CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

