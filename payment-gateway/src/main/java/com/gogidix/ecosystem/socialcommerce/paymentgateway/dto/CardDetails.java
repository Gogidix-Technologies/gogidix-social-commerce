package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Card Details DTO
 * 
 * SECURITY WARNING: This class contains sensitive payment data
 * - Only use for tokenization
 * - Never log or store card details
 * - PCI DSS compliance required
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetails {
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "[0-9]{13,19}", message = "Invalid card number format")
    private String cardNumber;
    
    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Invalid month")
    @Max(value = 12, message = "Invalid month")
    private Integer expiryMonth;
    
    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Card has expired")
    private Integer expiryYear;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "[0-9]{3,4}", message = "Invalid CVV format")
    private String cvv;
    
    @NotBlank(message = "Cardholder name is required")
    @Size(max = 100, message = "Name too long")
    private String cardholderName;
}