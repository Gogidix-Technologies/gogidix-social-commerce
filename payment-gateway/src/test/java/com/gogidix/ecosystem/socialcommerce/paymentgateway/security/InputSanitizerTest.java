package com.gogidix.ecosystem.socialcommerce.paymentgateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Input Sanitizer Test Suite
 * 
 * SECURITY TESTING: Input Validation Implementation (CVSS 8.9)
 * - Tests SQL injection pattern detection
 * - Tests XSS attack pattern detection
 * - Tests path traversal attack detection
 * - Tests input sanitization functionality
 * - Tests logging safety features
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Input Sanitizer Security Tests")
class InputSanitizerTest {
    
    private InputSanitizer inputSanitizer;
    
    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }
    
    // ==============================================
    // SQL INJECTION DETECTION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should detect SQL injection with UNION attack")
    void shouldDetectSqlInjectionUnionAttack() {
        // Test various SQL injection patterns
        assertTrue(inputSanitizer.isSqlInjectionAttempt("' UNION SELECT * FROM users --"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("1 UNION SELECT password FROM users"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("admin'; DROP TABLE users; --"));
    }
    
    @Test
    @DisplayName("Should detect SQL injection with OR condition")
    void shouldDetectSqlInjectionOrCondition() {
        assertTrue(inputSanitizer.isSqlInjectionAttempt("1 OR 1=1"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("' OR 'a'='a"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("admin' OR '1'='1' --"));
    }
    
    @Test
    @DisplayName("Should detect SQL injection with comments")
    void shouldDetectSqlInjectionComments() {
        assertTrue(inputSanitizer.isSqlInjectionAttempt("admin'; --"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("user/*comment*/"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("test-- comment"));
    }
    
    @Test
    @DisplayName("Should detect SQL injection with semicolon")
    void shouldDetectSqlInjectionSemicolon() {
        assertTrue(inputSanitizer.isSqlInjectionAttempt("user; SELECT * FROM users"));
        assertTrue(inputSanitizer.isSqlInjectionAttempt("admin'; DELETE FROM users;"));
    }
    
    @Test
    @DisplayName("Should allow legitimate input without SQL injection")
    void shouldAllowLegitimateInput() {
        assertFalse(inputSanitizer.isSqlInjectionAttempt("John Doe"));
        assertFalse(inputSanitizer.isSqlInjectionAttempt("user@example.com"));
        assertFalse(inputSanitizer.isSqlInjectionAttempt("Product Name"));
        assertFalse(inputSanitizer.isSqlInjectionAttempt("123 Main Street"));
    }
    
    // ==============================================
    // XSS ATTACK DETECTION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should detect XSS with script tags")
    void shouldDetectXssScriptTags() {
        assertTrue(inputSanitizer.isXssAttempt("<script>alert('xss')</script>"));
        assertTrue(inputSanitizer.isXssAttempt("<SCRIPT>alert('XSS')</SCRIPT>"));
        assertTrue(inputSanitizer.isXssAttempt("<script src='evil.js'></script>"));
    }
    
    @Test
    @DisplayName("Should detect XSS with javascript protocol")
    void shouldDetectXssJavascriptProtocol() {
        assertTrue(inputSanitizer.isXssAttempt("javascript:alert('xss')"));
        assertTrue(inputSanitizer.isXssAttempt("JAVASCRIPT:alert('XSS')"));
        assertTrue(inputSanitizer.isXssAttempt("javascript: alert('xss')"));
    }
    
    @Test
    @DisplayName("Should detect XSS with event handlers")
    void shouldDetectXssEventHandlers() {
        assertTrue(inputSanitizer.isXssAttempt("onclick=alert('xss')"));
        assertTrue(inputSanitizer.isXssAttempt("onload=alert('xss')"));
        assertTrue(inputSanitizer.isXssAttempt("onerror='alert(1)'"));
    }
    
    @Test
    @DisplayName("Should detect XSS with iframe and object tags")
    void shouldDetectXssIframeAndObject() {
        assertTrue(inputSanitizer.isXssAttempt("<iframe src='evil.html'></iframe>"));
        assertTrue(inputSanitizer.isXssAttempt("<object data='evil.swf'></object>"));
        assertTrue(inputSanitizer.isXssAttempt("<embed src='evil.swf'>"));
    }
    
    // ==============================================
    // PATH TRAVERSAL DETECTION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should detect path traversal with dot-dot-slash")
    void shouldDetectPathTraversalDotDotSlash() {
        assertTrue(inputSanitizer.isPathTraversalAttempt("../../../etc/passwd"));
        assertTrue(inputSanitizer.isPathTraversalAttempt("..\\..\\..\\windows\\system32"));
        assertTrue(inputSanitizer.isPathTraversalAttempt("file/../../etc/passwd"));
    }
    
    @Test
    @DisplayName("Should detect path traversal to sensitive files")
    void shouldDetectPathTraversalSensitiveFiles() {
        assertTrue(inputSanitizer.isPathTraversalAttempt("/etc/passwd"));
        assertTrue(inputSanitizer.isPathTraversalAttempt("\\windows\\system32"));
        assertTrue(inputSanitizer.isPathTraversalAttempt("/proc/version"));
    }
    
    // ==============================================
    // INPUT SANITIZATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should sanitize input by removing null bytes")
    void shouldSanitizeNullBytes() {
        String input = "test\0malicious";
        String sanitized = inputSanitizer.sanitizeInput(input);
        assertFalse(sanitized.contains("\0"));
        assertEquals("testmalicious", sanitized);
    }
    
    @Test
    @DisplayName("Should sanitize input by removing control characters")
    void shouldSanitizeControlCharacters() {
        String input = "test\u0001\u0002\u0003normal";
        String sanitized = inputSanitizer.sanitizeInput(input);
        assertEquals("testnormal", sanitized);
    }
    
    @Test
    @DisplayName("Should trim whitespace during sanitization")
    void shouldTrimWhitespace() {
        String input = "  test input  ";
        String sanitized = inputSanitizer.sanitizeInput(input);
        assertEquals("test input", sanitized);
    }
    
    // ==============================================
    // METADATA VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate legitimate metadata")
    void shouldValidateLegitimateMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderType", "purchase");
        metadata.put("source", "web");
        metadata.put("campaign", "summer2024");
        
        assertTrue(inputSanitizer.isValidMetadata(metadata));
    }
    
    @Test
    @DisplayName("Should reject metadata with too many entries")
    void shouldRejectMetadataTooManyEntries() {
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < 15; i++) {
            metadata.put("key" + i, "value" + i);
        }
        
        assertFalse(inputSanitizer.isValidMetadata(metadata));
    }
    
    @Test
    @DisplayName("Should reject metadata with oversized keys")
    void shouldRejectMetadataOversizedKeys() {
        Map<String, String> metadata = new HashMap<>();
        String longKey = "a".repeat(100); // Exceeds 50 character limit
        metadata.put(longKey, "value");
        
        assertFalse(inputSanitizer.isValidMetadata(metadata));
    }
    
    @Test
    @DisplayName("Should reject metadata with oversized values")
    void shouldRejectMetadataOversizedValues() {
        Map<String, String> metadata = new HashMap<>();
        String longValue = "a".repeat(300); // Exceeds 200 character limit
        metadata.put("key", longValue);
        
        assertFalse(inputSanitizer.isValidMetadata(metadata));
    }
    
    @Test
    @DisplayName("Should reject metadata with malicious content")
    void shouldRejectMetadataMaliciousContent() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "<script>alert('xss')</script>");
        
        assertFalse(inputSanitizer.isValidMetadata(metadata));
    }
    
    // ==============================================
    // PHONE NUMBER VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate legitimate phone numbers")
    void shouldValidateLegitimatePhoneNumbers() {
        assertTrue(inputSanitizer.isValidPhoneNumber("+1234567890"));
        assertTrue(inputSanitizer.isValidPhoneNumber("1234567890"));
        assertTrue(inputSanitizer.isValidPhoneNumber("+44 20 7946 0958"));
        assertTrue(inputSanitizer.isValidPhoneNumber("(555) 123-4567"));
    }
    
    @Test
    @DisplayName("Should reject invalid phone numbers")
    void shouldRejectInvalidPhoneNumbers() {
        assertFalse(inputSanitizer.isValidPhoneNumber("123")); // Too short
        assertFalse(inputSanitizer.isValidPhoneNumber("12345678901234567890")); // Too long
        assertFalse(inputSanitizer.isValidPhoneNumber("abc123def")); // Contains letters
        assertFalse(inputSanitizer.isValidPhoneNumber("<script>alert('xss')</script>"));
    }
    
    // ==============================================
    // CUSTOMER NAME VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate legitimate customer names")
    void shouldValidateLegitimateCustomerNames() {
        assertTrue(inputSanitizer.isValidCustomerName("John Doe"));
        assertTrue(inputSanitizer.isValidCustomerName("Mary O'Connor"));
        assertTrue(inputSanitizer.isValidCustomerName("José María"));
        assertTrue(inputSanitizer.isValidCustomerName("Jean-Pierre"));
    }
    
    @Test
    @DisplayName("Should reject invalid customer names")
    void shouldRejectInvalidCustomerNames() {
        assertFalse(inputSanitizer.isValidCustomerName("")); // Empty
        assertFalse(inputSanitizer.isValidCustomerName("X")); // Too short
        assertFalse(inputSanitizer.isValidCustomerName("a".repeat(150))); // Too long
        assertFalse(inputSanitizer.isValidCustomerName("John123")); // Contains numbers
        assertFalse(inputSanitizer.isValidCustomerName("<script>alert('xss')</script>"));
    }
    
    // ==============================================
    // COUNTRY CODE VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate legitimate country codes")
    void shouldValidateLegitimateCountryCodes() {
        assertTrue(inputSanitizer.isValidCountryCode("US"));
        assertTrue(inputSanitizer.isValidCountryCode("GB"));
        assertTrue(inputSanitizer.isValidCountryCode("NGN")); // 3-letter code
    }
    
    @Test
    @DisplayName("Should reject invalid country codes")
    void shouldRejectInvalidCountryCodes() {
        assertFalse(inputSanitizer.isValidCountryCode("")); // Empty
        assertFalse(inputSanitizer.isValidCountryCode("U")); // Too short
        assertFalse(inputSanitizer.isValidCountryCode("USAA")); // Too long
        assertFalse(inputSanitizer.isValidCountryCode("us")); // Lowercase
        assertFalse(inputSanitizer.isValidCountryCode("U1")); // Contains numbers
    }
    
    // ==============================================
    // ADDRESS VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate legitimate address components")
    void shouldValidateLegitimateAddressComponents() {
        assertTrue(inputSanitizer.isValidAddressComponent("123 Main Street", "line1"));
        assertTrue(inputSanitizer.isValidAddressComponent("Apt 4B", "line2"));
        assertTrue(inputSanitizer.isValidAddressComponent("New York", "city"));
        assertTrue(inputSanitizer.isValidAddressComponent("NY", "state"));
        assertTrue(inputSanitizer.isValidAddressComponent("10001", "postalCode"));
    }
    
    @Test
    @DisplayName("Should reject malicious address components")
    void shouldRejectMaliciousAddressComponents() {
        assertFalse(inputSanitizer.isValidAddressComponent("<script>alert('xss')</script>", "line1"));
        assertFalse(inputSanitizer.isValidAddressComponent("' OR 1=1 --", "city"));
        assertFalse(inputSanitizer.isValidAddressComponent("../../../etc/passwd", "state"));
    }
    
    // ==============================================
    // LOGGING SAFETY TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should sanitize input for safe logging")
    void shouldSanitizeForLogging() {
        String input = "test\nmalicious\rlog\tinjection";
        String sanitized = inputSanitizer.sanitizeForLogging(input);
        assertEquals("test\\nmalicious\\rlog\\tinjection", sanitized);
    }
    
    @Test
    @DisplayName("Should truncate long input for logging")
    void shouldTruncateLongInputForLogging() {
        String longInput = "a".repeat(200);
        String sanitized = inputSanitizer.sanitizeForLogging(longInput);
        assertTrue(sanitized.length() <= 100);
        assertTrue(sanitized.endsWith("..."));
    }
    
    @Test
    @DisplayName("Should handle null input gracefully")
    void shouldHandleNullInputGracefully() {
        assertDoesNotThrow(() -> {
            assertNull(inputSanitizer.sanitizeInput(null));
            assertFalse(inputSanitizer.isSqlInjectionAttempt(null));
            assertFalse(inputSanitizer.isXssAttempt(null));
            assertFalse(inputSanitizer.isPathTraversalAttempt(null));
            assertTrue(inputSanitizer.isValidInput(null));
        });
    }
    
    // ==============================================
    // COMPREHENSIVE VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should perform comprehensive input validation")
    void shouldPerformComprehensiveValidation() {
        // Valid inputs
        assertTrue(inputSanitizer.isValidInput("Normal text"));
        assertTrue(inputSanitizer.isValidInput("user@example.com"));
        assertTrue(inputSanitizer.isValidInput("123-456-7890"));
        
        // Invalid inputs (various attack types)
        assertFalse(inputSanitizer.isValidInput("' OR 1=1 --"));
        assertFalse(inputSanitizer.isValidInput("<script>alert('xss')</script>"));
        assertFalse(inputSanitizer.isValidInput("../../../etc/passwd"));
    }
    
    @Test
    @DisplayName("Should create sanitized metadata map")
    void shouldCreateSanitizedMetadataMap() {
        Map<String, String> originalMetadata = new HashMap<>();
        originalMetadata.put("  key1  ", "  value1  ");
        originalMetadata.put("key2\0", "value2\n");
        originalMetadata.put("", "empty_key");
        originalMetadata.put("key3", "");
        
        Map<String, String> sanitized = inputSanitizer.sanitizeMetadata(originalMetadata);
        
        assertEquals("value1", sanitized.get("key1"));
        assertEquals("value2", sanitized.get("key2"));
        assertFalse(sanitized.containsKey(""));
        assertFalse(sanitized.containsKey("key3"));
    }
}