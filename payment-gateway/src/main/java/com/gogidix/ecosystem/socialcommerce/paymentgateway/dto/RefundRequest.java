package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refund Request DTO
 * 
 * SECURITY IMPLEMENTATION: Validated refund request
 * - Amount validation
 * - Transaction ID verification
 * - Reason tracking for audit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be positive")
    @DecimalMax(value = "999999.99", message = "Refund amount exceeds maximum allowed")
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be in ISO 4217 format")
    private String currency;
    
    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    @Size(max = 100)
    private String requestedBy;
    
    private String orderId;
    
    private String customerId;
    
    private Boolean isPartialRefund;
}