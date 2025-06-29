package com.exalt.ecosystem.socialcommerce.paymentgateway.validation;

import com.exalt.ecosystem.socialcommerce.paymentgateway.dto.PaymentRequest;
import com.exalt.ecosystem.socialcommerce.paymentgateway.security.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Payment Request Validation Service
 * 
 * SECURITY FIX: Input Validation Implementation (CVSS 8.9)
 * - Comprehensive validation for all payment request fields
 * - Prevents SQL injection and XSS attacks
 * - Validates business logic constraints
 * - Provides detailed validation error messages
 */
@Component
public class PaymentRequestValidator {
    
    private static final Logger logger = Logger.getLogger(PaymentRequestValidator.class.getName());
    
    @Autowired
    private InputSanitizer inputSanitizer;
    
    // Supported currencies (ISO 4217)
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "SEK", "NOK", "DKK",
        "NGN", "GHS", "ZAR", "KES", "UGX", "XAF", "XOF", "EGP", "MAD", "TND"
    );
    
    // Supported payment methods
    private static final Set<String> SUPPORTED_PAYMENT_METHODS = Set.of(
        "card", "bank_transfer", "mobile_money", "ussd", "qr", "eft", 
        "paypal", "apple_pay", "google_pay", "bank", "wallet"
    );
    
    // Valid country codes (subset for validation)
    private static final Set<String> VALID_COUNTRY_CODES = Set.of(
        "US", "CA", "GB", "FR", "DE", "ES", "IT", "NL", "SE", "NO", "DK", "FI",
        "NG", "GH", "ZA", "KE", "UG", "EG", "MA", "TN", "CI", "SN", "ML", "BF",
        "AU", "NZ", "JP", "KR", "SG", "MY", "TH", "IN", "CN", "HK", "TW"
    );
    
    /**
     * Comprehensive validation of payment request
     */
    public ValidationResult validatePaymentRequest(PaymentRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            errors.add("Payment request cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Validate basic required fields
        validateAmount(request.getAmount(), errors);
        validateCurrency(request.getCurrency(), errors);
        validateOrderId(request.getOrderId(), errors);
        validateCustomerId(request.getCustomerId(), errors);
        validateCustomerEmail(request.getCustomerEmail(), errors);
        
        // Validate optional fields with security checks
        validateCustomerName(request.getCustomerName(), errors);
        validateCustomerPhone(request.getCustomerPhone(), errors);
        validateCountryCode(request.getCountryCode(), errors);
        validatePaymentMethod(request.getPaymentMethod(), errors);
        validateDescription(request.getDescription(), errors);
        validateMetadata(request.getMetadata(), errors);
        
        // Validate address objects
        if (request.getBillingAddress() != null) {
            validateAddress(request.getBillingAddress(), "billing", errors);
        }
        
        if (request.getShippingAddress() != null) {
            validateAddress(request.getShippingAddress(), "shipping", errors);
        }
        
        // Log validation results
        if (!errors.isEmpty()) {
            logger.warning("Payment request validation failed: " + String.join(", ", errors));
        } else {
            logger.info("Payment request validation successful for order: " + 
                inputSanitizer.sanitizeForLogging(request.getOrderId()));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private void validateAmount(Double amount, List<String> errors) {
        if (amount == null) {
            errors.add("Amount is required");
            return;
        }
        
        if (amount <= 0) {
            errors.add("Amount must be positive");
        }
        
        if (amount > 999999.99) {
            errors.add("Amount exceeds maximum allowed (999,999.99)");
        }
        
        // Check for reasonable decimal places (max 2)
        String amountStr = amount.toString();
        if (amountStr.contains(".") && amountStr.substring(amountStr.indexOf(".") + 1).length() > 2) {
            errors.add("Amount cannot have more than 2 decimal places");
        }
    }
    
    private void validateCurrency(String currency, List<String> errors) {
        if (!StringUtils.hasText(currency)) {
            errors.add("Currency is required");
            return;
        }
        
        // Security validation
        if (!inputSanitizer.isValidInput(currency)) {
            errors.add("Invalid currency format detected");
            return;
        }
        
        if (currency.length() != 3) {
            errors.add("Currency must be 3 characters (ISO 4217 format)");
            return;
        }
        
        if (!currency.matches("[A-Z]{3}")) {
            errors.add("Currency must be uppercase ISO 4217 format");
            return;
        }
        
        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            errors.add("Unsupported currency: " + inputSanitizer.sanitizeForLogging(currency));
        }
    }
    
    private void validateOrderId(String orderId, List<String> errors) {
        if (!StringUtils.hasText(orderId)) {
            errors.add("Order ID is required");
            return;
        }
        
        // Security validation
        if (!inputSanitizer.isValidInput(orderId)) {
            errors.add("Invalid order ID format detected");
            return;
        }
        
        if (orderId.length() > 100) {
            errors.add("Order ID is too long (max 100 characters)");
        }
        
        // Order ID should be alphanumeric with hyphens/underscores
        if (!orderId.matches("^[a-zA-Z0-9\\-_]+$")) {
            errors.add("Order ID contains invalid characters (alphanumeric, hyphens, underscores only)");
        }
    }
    
    private void validateCustomerId(String customerId, List<String> errors) {
        if (!StringUtils.hasText(customerId)) {
            errors.add("Customer ID is required");
            return;
        }
        
        // Security validation
        if (!inputSanitizer.isValidInput(customerId)) {
            errors.add("Invalid customer ID format detected");
            return;
        }
        
        if (customerId.length() > 100) {
            errors.add("Customer ID is too long (max 100 characters)");
        }
        
        // Customer ID should be alphanumeric with hyphens/underscores
        if (!customerId.matches("^[a-zA-Z0-9\\-_]+$")) {
            errors.add("Customer ID contains invalid characters (alphanumeric, hyphens, underscores only)");
        }
    }
    
    private void validateCustomerEmail(String email, List<String> errors) {
        if (!StringUtils.hasText(email)) {
            errors.add("Customer email is required");
            return;
        }
        
        // Security validation
        if (!inputSanitizer.isValidInput(email)) {
            errors.add("Invalid email format detected");
            return;
        }
        
        // Basic email validation
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            errors.add("Invalid email format");
        }
        
        if (email.length() > 254) {
            errors.add("Email address is too long (max 254 characters)");
        }
    }
    
    private void validateCustomerName(String name, List<String> errors) {
        if (StringUtils.hasText(name)) {
            if (!inputSanitizer.isValidCustomerName(name)) {
                errors.add("Invalid customer name format or content");
            }
        }
    }
    
    private void validateCustomerPhone(String phone, List<String> errors) {
        if (StringUtils.hasText(phone)) {
            if (!inputSanitizer.isValidPhoneNumber(phone)) {
                errors.add("Invalid phone number format");
            }
        }
    }
    
    private void validateCountryCode(String countryCode, List<String> errors) {
        if (StringUtils.hasText(countryCode)) {
            if (!inputSanitizer.isValidCountryCode(countryCode)) {
                errors.add("Invalid country code format");
                return;
            }
            
            if (!VALID_COUNTRY_CODES.contains(countryCode)) {
                errors.add("Unsupported country code: " + inputSanitizer.sanitizeForLogging(countryCode));
            }
        }
    }
    
    private void validatePaymentMethod(String paymentMethod, List<String> errors) {
        if (StringUtils.hasText(paymentMethod)) {
            // Security validation
            if (!inputSanitizer.isValidInput(paymentMethod)) {
                errors.add("Invalid payment method format detected");
                return;
            }
            
            if (paymentMethod.length() > 50) {
                errors.add("Payment method name is too long (max 50 characters)");
                return;
            }
            
            String normalizedMethod = paymentMethod.toLowerCase();
            if (!SUPPORTED_PAYMENT_METHODS.contains(normalizedMethod)) {
                errors.add("Unsupported payment method: " + inputSanitizer.sanitizeForLogging(paymentMethod));
            }
        }
    }
    
    private void validateDescription(String description, List<String> errors) {
        if (StringUtils.hasText(description)) {
            // Security validation
            if (!inputSanitizer.isValidInput(description)) {
                errors.add("Invalid description format detected");
                return;
            }
            
            if (description.length() > 500) {
                errors.add("Description is too long (max 500 characters)");
            }
        }
    }
    
    private void validateMetadata(java.util.Map<String, String> metadata, List<String> errors) {
        if (metadata != null && !metadata.isEmpty()) {
            if (!inputSanitizer.isValidMetadata(metadata)) {
                errors.add("Invalid metadata detected");
            }
        }
    }
    
    private void validateAddress(PaymentRequest.Address address, String type, List<String> errors) {
        if (address == null) {
            return;
        }
        
        // Validate required address fields
        if (!StringUtils.hasText(address.getLine1())) {
            errors.add(type + " address line 1 is required");
        } else {
            if (!inputSanitizer.isValidAddressComponent(address.getLine1(), "line1")) {
                errors.add("Invalid " + type + " address line 1");
            }
            if (address.getLine1().length() > 100) {
                errors.add(type + " address line 1 is too long (max 100 characters)");
            }
        }
        
        if (!StringUtils.hasText(address.getCity())) {
            errors.add(type + " address city is required");
        } else {
            if (!inputSanitizer.isValidAddressComponent(address.getCity(), "city")) {
                errors.add("Invalid " + type + " address city");
            }
            if (address.getCity().length() > 50) {
                errors.add(type + " address city is too long (max 50 characters)");
            }
        }
        
        if (!StringUtils.hasText(address.getCountry())) {
            errors.add(type + " address country is required");
        } else {
            if (!inputSanitizer.isValidCountryCode(address.getCountry())) {
                errors.add("Invalid " + type + " address country code");
            }
        }
        
        // Validate optional address fields
        if (StringUtils.hasText(address.getLine2())) {
            if (!inputSanitizer.isValidAddressComponent(address.getLine2(), "line2")) {
                errors.add("Invalid " + type + " address line 2");
            }
            if (address.getLine2().length() > 100) {
                errors.add(type + " address line 2 is too long (max 100 characters)");
            }
        }
        
        if (StringUtils.hasText(address.getState())) {
            if (!inputSanitizer.isValidAddressComponent(address.getState(), "state")) {
                errors.add("Invalid " + type + " address state");
            }
            if (address.getState().length() > 50) {
                errors.add(type + " address state is too long (max 50 characters)");
            }
        }
        
        if (StringUtils.hasText(address.getPostalCode())) {
            if (!inputSanitizer.isValidAddressComponent(address.getPostalCode(), "postal code")) {
                errors.add("Invalid " + type + " address postal code");
            }
            if (address.getPostalCode().length() > 20) {
                errors.add(type + " address postal code is too long (max 20 characters)");
            }
        }
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}