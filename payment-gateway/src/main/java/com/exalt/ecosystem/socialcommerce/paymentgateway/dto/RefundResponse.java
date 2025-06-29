package com.exalt.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Refund Response DTO
 * 
 * SECURITY IMPLEMENTATION: Secure refund response
 * - Audit trail information
 * - Transaction linking
 * - Status tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    
    private String refundId;
    
    private String transactionId;
    
    private Double amount;
    
    private String currency;
    
    private String status;
    
    private String message;
    
    private Date processedAt;
    
    private String gateway;
    
    private String reason;
    
    private Date estimatedSettlement;
    
    private String receiptNumber;
}