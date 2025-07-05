package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Payment Request DTO
 * 
 * SECURITY IMPLEMENTATION: Validated payment request data
 * - Input validation for all fields
 * - Amount validation
 * - Currency validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "999999.99", message = "Amount exceeds maximum allowed")
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be in ISO 4217 format")
    private String currency;
    
    @NotBlank(message = "Order ID is required")
    @Size(max = 100)
    private String orderId;
    
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100)
    private String customerId;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @Size(max = 500)
    private String description;
    
    @Size(max = 50)
    private String paymentMethod;
    
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s'\\-.,]+$", message = "Customer name contains invalid characters")
    private String customerName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number is too long")
    private String customerPhone;
    
    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
    @Pattern(regexp = "[A-Z]{2,3}", message = "Country code must be uppercase ISO format")
    private String countryCode;
    
    @Size(max = 10, message = "Maximum 10 metadata entries allowed")
    private Map<@NotBlank @Size(max = 50) String, @NotBlank @Size(max = 200) String> metadata;
    
    // For recurring payments
    private Boolean setupFutureUsage;
    
    // For 3D Secure
    private Boolean requiresAuthentication;
    
    // Billing address
    private Address billingAddress;
    
    // Shipping address
    private Address shippingAddress;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @NotBlank(message = "Address line 1 is required")
        @Size(max = 100, message = "Address line 1 is too long")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s',.-]+$", message = "Address line 1 contains invalid characters")
        private String line1;
        
        @Size(max = 100, message = "Address line 2 is too long")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s',.-]*$", message = "Address line 2 contains invalid characters")
        private String line2;
        
        @NotBlank(message = "City is required")
        @Size(max = 50, message = "City name is too long")
        @Pattern(regexp = "^[\\p{L}\\s'\\-.,]+$", message = "City name contains invalid characters")
        private String city;
        
        @Size(max = 50, message = "State name is too long")
        @Pattern(regexp = "^[\\p{L}\\s'\\-.,]*$", message = "State name contains invalid characters")
        private String state;
        
        @Size(max = 20, message = "Postal code is too long")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\-]*$", message = "Postal code contains invalid characters")
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
        @Pattern(regexp = "[A-Z]{2,3}", message = "Country code must be uppercase ISO format")
        private String country;
    }
}