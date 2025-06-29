package com.exalt.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Capture Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureResponse {
    
    private String transactionId;
    private Double amount;
    private String currency;
    private String status;
    private String message;
    private Date capturedAt;
    private String gateway;
}