package com.gogidix.ecosystem.socialcommerce.localization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Localization Service Application
 * 
 * This service acts as a business logic facade for internationalization and localization.
 * It provides APIs for multi-language content management and locale-specific formatting.
 * 
 * Key responsibilities:
 * - Multi-language content management
 * - Currency formatting and conversion
 * - Date/time localization
 * - Regional content adaptation
 * - Translation key management
 * - Locale-specific validation rules
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LocalizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalizationServiceApplication.class, args);
    }
}