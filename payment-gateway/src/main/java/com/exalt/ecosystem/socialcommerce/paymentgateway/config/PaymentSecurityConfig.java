package com.exalt.ecosystem.socialcommerce.paymentgateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * Payment Security Configuration
 * 
 * SECURITY FIX: Payment Credentials Security (CVSS 9.5)
 * - Implements secure payment credential management
 * - Environment variable based configuration
 * - Regional payment gateway separation
 * - Webhook security validation
 * 
 * Regional Strategy:
 * - Stripe: Europe, Americas, Asia-Pacific, Middle East (Rest of World)
 * - Paystack: Africa (All African countries)
 */
@Configuration
public class PaymentSecurityConfig {
    
    private static final Logger logger = Logger.getLogger(PaymentSecurityConfig.class.getName());
    
    // Stripe Configuration (Europe & Rest of World)
    @Value("${payment.stripe.secret-key}")
    private String stripeSecretKey;
    
    @Value("${payment.stripe.publishable-key}")
    private String stripePublishableKey;
    
    @Value("${payment.stripe.webhook-secret}")
    private String stripeWebhookSecret;
    
    @Value("${payment.stripe.api-version:2023-10-16}")
    private String stripeApiVersion;
    
    // Paystack Configuration (Africa)
    @Value("${payment.paystack.secret-key}")
    private String paystackSecretKey;
    
    @Value("${payment.paystack.public-key}")
    private String paystackPublicKey;
    
    @Value("${payment.paystack.webhook-secret:#{null}}")
    private String paystackWebhookSecret;
    
    @Value("${payment.paystack.base-url:https://api.paystack.co}")
    private String paystackBaseUrl;
    
    // Security Configuration
    @Value("${payment.security.pci-compliance-mode:true}")
    private boolean pciComplianceMode;
    
    @Value("${payment.security.tokenization-required:true}")
    private boolean tokenizationRequired;
    
    @Value("${payment.security.max-amount-per-transaction:10000.00}")
    private double maxAmountPerTransaction;
    
    @Value("${payment.security.webhook-signature-validation:true}")
    private boolean webhookSignatureValidation;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    /**
     * Initialize and validate payment configuration on startup
     */
    @PostConstruct
    public void initializePaymentConfiguration() {
        validatePaymentCredentials();
        configureStripe();
        logger.info("Payment security configuration initialized successfully");
    }
    
    /**
     * Configure Stripe SDK
     */
    private void configureStripe() {
        Stripe.apiKey = stripeSecretKey;
        // Stripe API version is set globally in newer versions
        logger.info("Stripe SDK configured with secret key");
    }
    
    /**
     * Validate payment credentials security
     */
    private void validatePaymentCredentials() {
        // Validate Stripe credentials
        validateStripeCredentials();
        
        // Validate Paystack credentials
        validatePaystackCredentials();
        
        // Check for test credentials in production
        if (isProduction()) {
            ensureProductionCredentials();
        }
        
        logger.info("Payment credentials validation completed successfully");
    }
    
    /**
     * Validate Stripe credentials
     */
    private void validateStripeCredentials() {
        if (stripeSecretKey == null || stripeSecretKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "Stripe secret key is not configured. Please set STRIPE_SECRET_KEY environment variable."
            );
        }
        
        if (stripePublishableKey == null || stripePublishableKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "Stripe publishable key is not configured. Please set STRIPE_PUBLISHABLE_KEY environment variable."
            );
        }
        
        if (!stripeSecretKey.startsWith("sk_")) {
            throw new IllegalStateException(
                "Invalid Stripe secret key format. Key should start with 'sk_'"
            );
        }
        
        if (!stripePublishableKey.startsWith("pk_")) {
            throw new IllegalStateException(
                "Invalid Stripe publishable key format. Key should start with 'pk_'"
            );
        }
        
        // Check for default/mock values
        if (stripeSecretKey.contains("mock") || stripeSecretKey.contains("default")) {
            throw new IllegalStateException(
                "Stripe secret key contains mock/default values. Use real credentials."
            );
        }
        
        logger.info("Stripe credentials validation passed");
    }
    
    /**
     * Validate Paystack credentials
     */
    private void validatePaystackCredentials() {
        if (paystackSecretKey == null || paystackSecretKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "Paystack secret key is not configured. Please set PAYSTACK_SECRET_KEY environment variable."
            );
        }
        
        if (paystackPublicKey == null || paystackPublicKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "Paystack public key is not configured. Please set PAYSTACK_PUBLIC_KEY environment variable."
            );
        }
        
        if (!paystackSecretKey.startsWith("sk_")) {
            throw new IllegalStateException(
                "Invalid Paystack secret key format. Key should start with 'sk_'"
            );
        }
        
        if (!paystackPublicKey.startsWith("pk_")) {
            throw new IllegalStateException(
                "Invalid Paystack public key format. Key should start with 'pk_'"
            );
        }
        
        logger.info("Paystack credentials validation passed");
    }
    
    /**
     * Ensure production credentials in production environment
     */
    private void ensureProductionCredentials() {
        // Check for test keys in production
        if (stripeSecretKey.startsWith("sk_test_")) {
            logger.warning("WARNING: Using Stripe TEST credentials in production environment!");
        }
        
        if (paystackSecretKey.startsWith("sk_test_")) {
            logger.warning("WARNING: Using Paystack TEST credentials in production environment!");
        }
        
        // In production, webhook secrets must be configured
        if (stripeWebhookSecret == null || stripeWebhookSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "Stripe webhook secret must be configured in production. Set STRIPE_WEBHOOK_SECRET."
            );
        }
    }
    
    /**
     * Check if running in production
     */
    private boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }
    
    // Getter methods for configuration values
    
    public String getStripeSecretKey() {
        return stripeSecretKey;
    }
    
    public String getStripePublishableKey() {
        return stripePublishableKey;
    }
    
    public String getStripeWebhookSecret() {
        return stripeWebhookSecret;
    }
    
    public String getPaystackSecretKey() {
        return paystackSecretKey;
    }
    
    public String getPaystackPublicKey() {
        return paystackPublicKey;
    }
    
    public String getPaystackWebhookSecret() {
        return paystackWebhookSecret;
    }
    
    public String getPaystackBaseUrl() {
        return paystackBaseUrl;
    }
    
    public boolean isPciComplianceMode() {
        return pciComplianceMode;
    }
    
    public boolean isTokenizationRequired() {
        return tokenizationRequired;
    }
    
    public double getMaxAmountPerTransaction() {
        return maxAmountPerTransaction;
    }
    
    public boolean isWebhookSignatureValidation() {
        return webhookSignatureValidation;
    }
}