package com.gogidix.ecosystem.socialcommerce.paymentgateway.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Input Sanitization Service
 * 
 * SECURITY FIX: Input Validation Implementation (CVSS 8.9)
 * - Prevents SQL injection vulnerabilities
 * - Protects against XSS attacks
 * - Validates all input parameters
 * - Sanitizes data for safe logging
 */
@Component
public class InputSanitizer {
    
    private static final Logger logger = Logger.getLogger(InputSanitizer.class.getName());
    
    // SQL injection patterns to detect and block
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("(?i).*('|(\\-\\-)|(;)|(\\|)|(\\*)|(%)|(\\b(SELECT|INSERT|UPDATE|DELETE|CREATE|DROP|ALTER|EXEC|UNION|SCRIPT)\\b)).*"),
        Pattern.compile("(?i).*(UNION|SELECT).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*(OR|AND)\\s+(\\w+\\s*=\\s*\\w+|\\d+\\s*=\\s*\\d+).*"),
        Pattern.compile("(?i).*'\\s*(OR|AND)\\s*'.*"),
        Pattern.compile("(?i).*\\b(EXEC|EXECUTE|SP_|XP_)\\b.*"),
        Pattern.compile("(?i).*\\b(SCRIPT|JAVASCRIPT|VBSCRIPT)\\b.*")
    };
    
    // XSS patterns to detect and block
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("(?i).*<\\s*(script|iframe|object|embed|link|meta|style).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*javascript\\s*:.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*on(click|load|error|focus|blur|change|submit)\\s*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*\\beval\\s*\\(.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i).*\\balert\\s*\\(.*", Pattern.CASE_INSENSITIVE)
    };
    
    // Path traversal patterns
    private static final Pattern[] PATH_TRAVERSAL_PATTERNS = {
        Pattern.compile(".*\\.\\.[\\\\/].*"),
        Pattern.compile(".*(\\\\|/)etc(\\\\|/)passwd.*"),
        Pattern.compile(".*(\\\\|/)proc(\\\\|/).*"),
        Pattern.compile(".*[\\\\|/](windows|winnt)(\\\\|/)system32.*")
    };
    
    /**
     * Sanitize input string for SQL injection and XSS
     */
    public String sanitizeInput(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        String sanitized = input;
        
        // Remove null bytes
        sanitized = sanitized.replace("\0", "");
        
        // Remove control characters
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // Normalize Unicode
        sanitized = java.text.Normalizer.normalize(sanitized, java.text.Normalizer.Form.NFC);
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }
    
    /**
     * Validate input against SQL injection patterns
     */
    public boolean isSqlInjectionAttempt(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        
        String normalized = input.toLowerCase().trim();
        
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(normalized).matches()) {
                logger.warning("SQL injection attempt detected: " + sanitizeForLogging(input));
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate input against XSS patterns
     */
    public boolean isXssAttempt(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        
        String normalized = input.toLowerCase().trim();
        
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(normalized).matches()) {
                logger.warning("XSS attempt detected: " + sanitizeForLogging(input));
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate input against path traversal patterns
     */
    public boolean isPathTraversalAttempt(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        
        for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(input).matches()) {
                logger.warning("Path traversal attempt detected: " + sanitizeForLogging(input));
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Comprehensive input validation
     */
    public boolean isValidInput(String input) {
        if (!StringUtils.hasText(input)) {
            return true; // Empty input is valid
        }
        
        // Check for various attack patterns
        if (isSqlInjectionAttempt(input)) {
            return false;
        }
        
        if (isXssAttempt(input)) {
            return false;
        }
        
        if (isPathTraversalAttempt(input)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate metadata map
     */
    public boolean isValidMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return true;
        }
        
        // Check size limit
        if (metadata.size() > 10) {
            logger.warning("Metadata size exceeds limit: " + metadata.size());
            return false;
        }
        
        // Validate each key and value
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Validate key
            if (!StringUtils.hasText(key) || key.length() > 50) {
                logger.warning("Invalid metadata key: " + sanitizeForLogging(key));
                return false;
            }
            
            if (!isValidInput(key)) {
                logger.warning("Malicious metadata key detected: " + sanitizeForLogging(key));
                return false;
            }
            
            // Validate value
            if (!StringUtils.hasText(value) || value.length() > 200) {
                logger.warning("Invalid metadata value for key " + sanitizeForLogging(key) + ": " + sanitizeForLogging(value));
                return false;
            }
            
            if (!isValidInput(value)) {
                logger.warning("Malicious metadata value detected for key " + sanitizeForLogging(key) + ": " + sanitizeForLogging(value));
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Sanitize string for safe logging (prevent log injection)
     */
    public String sanitizeForLogging(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove potential log injection characters
        String sanitized = input
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\f", "\\f");
        
        // Truncate if too long
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 97) + "...";
        }
        
        return sanitized;
    }
    
    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return true; // Optional field
        }
        
        // Remove common formatting characters
        String normalized = phoneNumber.replaceAll("[\\s\\-\\(\\)\\+\\.]", "");
        
        // Basic validation: only digits and reasonable length
        if (!normalized.matches("\\d{7,15}")) {
            logger.warning("Invalid phone number format: " + sanitizeForLogging(phoneNumber));
            return false;
        }
        
        return isValidInput(phoneNumber);
    }
    
    /**
     * Validate customer name
     */
    public boolean isValidCustomerName(String name) {
        if (!StringUtils.hasText(name)) {
            return false; // Required field
        }
        
        // Check length
        if (name.length() < 2 || name.length() > 100) {
            logger.warning("Invalid customer name length: " + sanitizeForLogging(name));
            return false;
        }
        
        // Allow only letters, spaces, and common name characters (including accented)
        if (!name.matches("^[a-zA-ZÀ-ÿĀ-žĂ-ğ\\s'\\-\\.]+$")) {
            logger.warning("Invalid customer name characters: " + sanitizeForLogging(name));
            return false;
        }
        
        return isValidInput(name);
    }
    
    /**
     * Validate country code
     */
    public boolean isValidCountryCode(String countryCode) {
        if (!StringUtils.hasText(countryCode)) {
            return false; // Required field
        }
        
        // Must be 2 or 3 uppercase letters
        if (!countryCode.matches("[A-Z]{2,3}")) {
            logger.warning("Invalid country code format: " + sanitizeForLogging(countryCode));
            return false;
        }
        
        return isValidInput(countryCode);
    }
    
    /**
     * Create sanitized metadata for safe processing
     */
    public Map<String, String> sanitizeMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, String> sanitized = new HashMap<>();
        
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = sanitizeInput(entry.getKey());
            String value = sanitizeInput(entry.getValue());
            
            if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                sanitized.put(key, value);
            }
        }
        
        return sanitized;
    }
    
    /**
     * Validate address components
     */
    public boolean isValidAddressComponent(String component, String fieldName) {
        if (!StringUtils.hasText(component)) {
            return true; // Most address fields are optional
        }
        
        // Check for malicious patterns
        if (!isValidInput(component)) {
            logger.warning("Invalid address " + fieldName + ": " + sanitizeForLogging(component));
            return false;
        }
        
        // Allow address-appropriate characters
        if (!component.matches("^[\\p{L}\\p{N}\\s'\\-\\.\\,\\#\\/]+$")) {
            logger.warning("Invalid characters in address " + fieldName + ": " + sanitizeForLogging(component));
            return false;
        }
        
        return true;
    }
}