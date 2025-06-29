package com.exalt.ecosystem.socialcommerce.paymentgateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.util.logging.Logger;

/**
 * Security Headers Configuration
 * 
 * SECURITY FIX: Security Headers Implementation
 * - Prevents clickjacking attacks (X-Frame-Options)
 * - Prevents MIME sniffing (X-Content-Type-Options)
 * - Enforces XSS protection (X-XSS-Protection)
 * - Controls referrer information (Referrer-Policy)
 * - Implements content security policy (CSP)
 * - Enforces HTTPS with HSTS
 */
@Configuration
public class SecurityHeadersConfig {
    
    private static final Logger logger = Logger.getLogger(SecurityHeadersConfig.class.getName());
    
    /**
     * Configure comprehensive security headers
     * This method should be called from the main security configuration
     */
    public static void configureSecurityHeaders(HttpSecurity http) throws Exception {
        logger.info("Configuring comprehensive security headers for payment gateway");
        
        http.headers(headers -> headers
            // X-Frame-Options: Prevent clickjacking
            .frameOptions(frameOptions -> frameOptions
                .deny() // DENY is more secure than SAMEORIGIN for payment processing
            )
            
            // X-Content-Type-Options: Prevent MIME sniffing
            .contentTypeOptions(contentTypeOptions -> {})
            
            // X-XSS-Protection: Enable browser XSS protection
            .xssProtection(xss -> xss
                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
            )
            
            // Referrer-Policy: Control referrer information
            .referrerPolicy(referrerPolicy -> referrerPolicy
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
            
            // HTTP Strict Transport Security (HSTS)
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000) // 1 year
                .preload(true)
            )
            
            // Content Security Policy (CSP)
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' https://js.stripe.com https://js.paystack.co; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self' https://api.stripe.com https://api.paystack.co; " +
                    "frame-src https://js.stripe.com https://checkout.paystack.com; " +
                    "frame-ancestors 'none'; " +
                    "form-action 'self'; " +
                    "base-uri 'self'; " +
                    "object-src 'none'"
                )
            )
            
            // Permissions Policy (formerly Feature Policy)
            .permissionsPolicy(permissions -> permissions
                .policy(
                    "geolocation=(), " +
                    "microphone=(), " +
                    "camera=(), " +
                    "payment=(self), " +
                    "usb=(), " +
                    "magnetometer=(), " +
                    "accelerometer=()"
                )
            )
        );
        
        logger.info("Security headers configuration completed successfully");
    }
}