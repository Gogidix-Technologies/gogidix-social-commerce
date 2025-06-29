package com.exalt.ecosystem.socialcommerce.paymentgateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * CORS Configuration for Payment Gateway
 * 
 * SECURITY FIX: CORS Configuration (CVSS 7.2)
 * - Prevents XSS and CSRF attacks
 * - Implements strict origin validation
 * - Controls allowed methods and headers
 * - Properly configured credentials handling
 * - Environment-based configuration
 */
@Configuration
public class CorsConfig {
    
    private static final Logger logger = Logger.getLogger(CorsConfig.class.getName());
    
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;
    
    @Value("${app.cors.max-age}")
    private long maxAge;
    
    /**
     * Configure CORS with environment-specific settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS with environment-specific settings");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse and set allowed origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        logger.info("CORS allowed origins: " + origins);
        
        // Parse and set allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        logger.info("CORS allowed methods: " + methods);
        
        // Parse and set allowed headers
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        configuration.setAllowedHeaders(headers);
        
        // Configure exposed headers for pagination and metadata
        configuration.setExposedHeaders(Arrays.asList(
            "X-Total-Count",
            "X-Page-Number", 
            "X-Page-Size",
            "X-Request-ID",
            "X-Rate-Limit-Remaining",
            "X-Rate-Limit-Reset"
        ));
        
        // Set credentials support
        configuration.setAllowCredentials(allowCredentials);
        
        // Set preflight cache duration
        configuration.setMaxAge(maxAge);
        
        // Apply configuration to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/actuator/**", createActuatorCorsConfig());
        
        logger.info("CORS configuration completed successfully");
        return source;
    }
    
    /**
     * Separate CORS configuration for actuator endpoints
     */
    private CorsConfiguration createActuatorCorsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        
        // More restrictive for actuator endpoints
        config.setAllowedOrigins(Arrays.asList("https://admin.exalt.com", "http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        return config;
    }
}