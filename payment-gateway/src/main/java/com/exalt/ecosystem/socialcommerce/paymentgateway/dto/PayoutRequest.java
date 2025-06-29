package com.exalt.ecosystem.socialcommerce.paymentgateway.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payout Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Vendor ID is required")
    private String vendorId;
    
    private String description;
    
    private String payoutType;
    
    // Bank account details
    private String accountNumber;
    private String accountName;
    private String bankCode;
    private String routingNumber;
    
    // For Paystack specifically
    private String recipientCode;
}