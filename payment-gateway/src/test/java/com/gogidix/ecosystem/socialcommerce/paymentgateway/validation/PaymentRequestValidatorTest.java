package com.gogidix.ecosystem.socialcommerce.paymentgateway.validation;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.PaymentRequest;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.security.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Payment Request Validator Test Suite
 * 
 * SECURITY TESTING: Input Validation Implementation (CVSS 8.9)
 * - Tests comprehensive payment request validation
 * - Tests security attack prevention
 * - Tests business logic validation
 * - Tests error message generation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Request Validator Security Tests")
class PaymentRequestValidatorTest {
    
    @Mock
    private InputSanitizer inputSanitizer;
    
    @InjectMocks
    private PaymentRequestValidator validator;
    
    private PaymentRequest validRequest;
    
    @BeforeEach
    void setUp() {
        // Mock InputSanitizer to return valid by default
        when(inputSanitizer.isValidInput(anyString())).thenReturn(true);
        when(inputSanitizer.isValidCustomerName(anyString())).thenReturn(true);
        when(inputSanitizer.isValidPhoneNumber(anyString())).thenReturn(true);
        when(inputSanitizer.isValidCountryCode(anyString())).thenReturn(true);
        when(inputSanitizer.isValidMetadata(any())).thenReturn(true);
        when(inputSanitizer.isValidAddressComponent(anyString(), anyString())).thenReturn(true);
        when(inputSanitizer.sanitizeForLogging(anyString())).thenAnswer(i -> i.getArgument(0));
        
        // Create valid payment request
        validRequest = PaymentRequest.builder()
            .amount(100.00)
            .currency("USD")
            .orderId("ORDER_123")
            .customerId("CUSTOMER_456")
            .customerEmail("test@example.com")
            .customerName("John Doe")
            .customerPhone("+1234567890")
            .countryCode("US")
            .description("Test payment")
            .paymentMethod("card")
            .build();
    }
    
    // ==============================================
    // VALID REQUEST TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate complete valid payment request")
    void shouldValidateCompleteValidRequest() {
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    @DisplayName("Should validate minimal valid payment request")
    void shouldValidateMinimalValidRequest() {
        PaymentRequest minimal = PaymentRequest.builder()
            .amount(50.00)
            .currency("EUR")
            .orderId("MIN_ORDER")
            .customerId("MIN_CUSTOMER")
            .customerEmail("min@test.com")
            .build();
            
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(minimal);
        
        assertTrue(result.isValid());
    }
    
    // ==============================================
    // NULL AND EMPTY VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should reject null payment request")
    void shouldRejectNullRequest() {
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(null);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("cannot be null"));
    }
    
    @Test
    @DisplayName("Should reject null amount")
    void shouldRejectNullAmount() {
        validRequest.setAmount(null);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Amount is required"));
    }
    
    @Test
    @DisplayName("Should reject empty currency")
    void shouldRejectEmptyCurrency() {
        validRequest.setCurrency("");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Currency is required"));
    }
    
    @Test
    @DisplayName("Should reject empty order ID")
    void shouldRejectEmptyOrderId() {
        validRequest.setOrderId("");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Order ID is required"));
    }
    
    @Test
    @DisplayName("Should reject empty customer ID")
    void shouldRejectEmptyCustomerId() {
        validRequest.setCustomerId("");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Customer ID is required"));
    }
    
    @Test
    @DisplayName("Should reject empty customer email")
    void shouldRejectEmptyCustomerEmail() {
        validRequest.setCustomerEmail("");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Customer email is required"));
    }
    
    // ==============================================
    // AMOUNT VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should reject negative amount")
    void shouldRejectNegativeAmount() {
        validRequest.setAmount(-10.00);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Amount must be positive"));
    }
    
    @Test
    @DisplayName("Should reject zero amount")
    void shouldRejectZeroAmount() {
        validRequest.setAmount(0.0);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Amount must be positive"));
    }
    
    @Test
    @DisplayName("Should reject amount exceeding maximum")
    void shouldRejectAmountExceedingMaximum() {
        validRequest.setAmount(1000000.00);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Amount exceeds maximum allowed"));
    }
    
    @Test
    @DisplayName("Should reject amount with too many decimal places")
    void shouldRejectAmountTooManyDecimals() {
        validRequest.setAmount(100.123);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Amount cannot have more than 2 decimal places"));
    }
    
    // ==============================================
    // CURRENCY VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should reject invalid currency length")
    void shouldRejectInvalidCurrencyLength() {
        validRequest.setCurrency("USDD");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Currency must be 3 characters"));
    }
    
    @Test
    @DisplayName("Should reject lowercase currency")
    void shouldRejectLowercaseCurrency() {
        validRequest.setCurrency("usd");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Currency must be uppercase ISO 4217 format"));
    }
    
    @Test
    @DisplayName("Should reject unsupported currency")
    void shouldRejectUnsupportedCurrency() {
        validRequest.setCurrency("XYZ");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Unsupported currency"));
    }
    
    // ==============================================
    // EMAIL VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should reject invalid email format")
    void shouldRejectInvalidEmailFormat() {
        validRequest.setCustomerEmail("invalid-email");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid email format"));
    }
    
    @Test
    @DisplayName("Should reject email too long")
    void shouldRejectEmailTooLong() {
        String longEmail = "a".repeat(250) + "@example.com";
        validRequest.setCustomerEmail(longEmail);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Email address is too long"));
    }
    
    // ==============================================
    // SECURITY VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should detect SQL injection in order ID")
    void shouldDetectSqlInjectionInOrderId() {
        validRequest.setOrderId("'; DROP TABLE orders; --");
        when(inputSanitizer.isValidInput("'; DROP TABLE orders; --")).thenReturn(false);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid order ID format detected"));
    }
    
    @Test
    @DisplayName("Should detect XSS attempt in customer name")
    void shouldDetectXssInCustomerName() {
        validRequest.setCustomerName("<script>alert('xss')</script>");
        when(inputSanitizer.isValidCustomerName("<script>alert('xss')</script>")).thenReturn(false);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid customer name format or content"));
    }
    
    @Test
    @DisplayName("Should detect malicious metadata")
    void shouldDetectMaliciousMetadata() {
        Map<String, String> maliciousMetadata = new HashMap<>();
        maliciousMetadata.put("key", "<script>alert('xss')</script>");
        validRequest.setMetadata(maliciousMetadata);
        when(inputSanitizer.isValidMetadata(maliciousMetadata)).thenReturn(false);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid metadata detected"));
    }
    
    // ==============================================
    // FIELD LENGTH VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should reject order ID too long")
    void shouldRejectOrderIdTooLong() {
        String longOrderId = "a".repeat(150);
        validRequest.setOrderId(longOrderId);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Order ID is too long"));
    }
    
    @Test
    @DisplayName("Should reject customer ID too long")
    void shouldRejectCustomerIdTooLong() {
        String longCustomerId = "a".repeat(150);
        validRequest.setCustomerId(longCustomerId);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Customer ID is too long"));
    }
    
    @Test
    @DisplayName("Should reject description too long")
    void shouldRejectDescriptionTooLong() {
        String longDescription = "a".repeat(600);
        validRequest.setDescription(longDescription);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Description is too long"));
    }
    
    // ==============================================
    // CHARACTER VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should reject order ID with invalid characters")
    void shouldRejectOrderIdInvalidCharacters() {
        validRequest.setOrderId("ORDER@123#");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Order ID contains invalid characters"));
    }
    
    @Test
    @DisplayName("Should reject customer ID with invalid characters")
    void shouldRejectCustomerIdInvalidCharacters() {
        validRequest.setCustomerId("CUSTOMER@456#");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Customer ID contains invalid characters"));
    }
    
    // ==============================================
    // PAYMENT METHOD VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate supported payment methods")
    void shouldValidateSupportedPaymentMethods() {
        String[] supportedMethods = {"card", "bank_transfer", "mobile_money", "ussd", "paypal"};
        
        for (String method : supportedMethods) {
            validRequest.setPaymentMethod(method);
            PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
            assertTrue(result.isValid(), "Should support payment method: " + method);
        }
    }
    
    @Test
    @DisplayName("Should reject unsupported payment method")
    void shouldRejectUnsupportedPaymentMethod() {
        validRequest.setPaymentMethod("unsupported_method");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Unsupported payment method"));
    }
    
    @Test
    @DisplayName("Should reject payment method too long")
    void shouldRejectPaymentMethodTooLong() {
        String longMethod = "a".repeat(60);
        validRequest.setPaymentMethod(longMethod);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Payment method name is too long"));
    }
    
    // ==============================================
    // COUNTRY CODE VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate supported country codes")
    void shouldValidateSupportedCountryCodes() {
        String[] supportedCodes = {"US", "GB", "CA", "DE", "NG", "GH"};
        
        for (String code : supportedCodes) {
            validRequest.setCountryCode(code);
            PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
            assertTrue(result.isValid(), "Should support country code: " + code);
        }
    }
    
    @Test
    @DisplayName("Should reject unsupported country code")
    void shouldRejectUnsupportedCountryCode() {
        validRequest.setCountryCode("XX");
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Unsupported country code"));
    }
    
    // ==============================================
    // ADDRESS VALIDATION TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should validate complete billing address")
    void shouldValidateCompleteBillingAddress() {
        PaymentRequest.Address address = PaymentRequest.Address.builder()
            .line1("123 Main Street")
            .line2("Apt 4B")
            .city("New York")
            .state("NY")
            .postalCode("10001")
            .country("US")
            .build();
            
        validRequest.setBillingAddress(address);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertTrue(result.isValid());
    }
    
    @Test
    @DisplayName("Should reject address missing required fields")
    void shouldRejectAddressMissingRequiredFields() {
        PaymentRequest.Address address = PaymentRequest.Address.builder()
            .line2("Apt 4B") // Missing line1, city, country
            .state("NY")
            .postalCode("10001")
            .build();
            
        validRequest.setBillingAddress(address);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("billing address line 1 is required"));
        assertTrue(result.getErrors().contains("billing address city is required"));
        assertTrue(result.getErrors().contains("billing address country is required"));
    }
    
    @Test
    @DisplayName("Should reject address with malicious content")
    void shouldRejectAddressMaliciousContent() {
        PaymentRequest.Address address = PaymentRequest.Address.builder()
            .line1("<script>alert('xss')</script>")
            .city("' OR 1=1 --")
            .country("US")
            .build();
            
        when(inputSanitizer.isValidAddressComponent("<script>alert('xss')</script>", "line1")).thenReturn(false);
        when(inputSanitizer.isValidAddressComponent("' OR 1=1 --", "city")).thenReturn(false);
        
        validRequest.setBillingAddress(address);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid billing address line 1"));
        assertTrue(result.getErrors().contains("Invalid billing address city"));
    }
    
    // ==============================================
    // MULTIPLE ERRORS TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should collect multiple validation errors")
    void shouldCollectMultipleValidationErrors() {
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .amount(-10.00) // Invalid amount
            .currency("usd") // Invalid currency
            .orderId("") // Empty order ID
            .customerId("") // Empty customer ID
            .customerEmail("invalid-email") // Invalid email
            .build();
            
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(invalidRequest);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 5);
        
        String errorMessage = result.getErrorMessage();
        assertTrue(errorMessage.contains("Amount must be positive"));
        assertTrue(errorMessage.contains("Currency must be uppercase"));
        assertTrue(errorMessage.contains("Order ID is required"));
        assertTrue(errorMessage.contains("Customer ID is required"));
        assertTrue(errorMessage.contains("Invalid email format"));
    }
    
    // ==============================================
    // EDGE CASE TESTS
    // ==============================================
    
    @Test
    @DisplayName("Should handle very small valid amount")
    void shouldHandleVerySmallValidAmount() {
        validRequest.setAmount(0.01);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertTrue(result.isValid());
    }
    
    @Test
    @DisplayName("Should handle maximum valid amount")
    void shouldHandleMaximumValidAmount() {
        validRequest.setAmount(999999.99);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertTrue(result.isValid());
    }
    
    @Test
    @DisplayName("Should handle optional fields set to null")
    void shouldHandleOptionalFieldsSetToNull() {
        validRequest.setCustomerName(null);
        validRequest.setCustomerPhone(null);
        validRequest.setCountryCode(null);
        validRequest.setDescription(null);
        validRequest.setPaymentMethod(null);
        validRequest.setMetadata(null);
        
        PaymentRequestValidator.ValidationResult result = validator.validatePaymentRequest(validRequest);
        
        assertTrue(result.isValid());
    }
}