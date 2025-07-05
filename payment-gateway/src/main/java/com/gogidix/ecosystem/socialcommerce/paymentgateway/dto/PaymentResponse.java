package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * Payment Response DTO
 * 
 * SECURITY IMPLEMENTATION: Secure payment response data
 * - Sanitized response data
 * - No sensitive information exposure
 * - Audit-ready transaction tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private String transactionId;
    
    private String status;
    
    private Double amount;
    
    private String currency;
    
    private String gatewayResponse;
    
    private String message;
    
    private Date timestamp;
    
    private String paymentMethod;
    
    private String gateway;
    
    private Map<String, String> metadata;
    
    // For client-side processing
    private String clientSecret;
    
    // For redirect-based payments
    private String redirectUrl;
    
    // For receipt generation
    private String receiptNumber;
    
    // For error handling
    private String errorCode;
    
    private String errorDescription;
}