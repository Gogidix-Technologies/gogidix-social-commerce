package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Payout Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {
    
    private String payoutId;
    private Double amount;
    private String currency;
    private String status;
    private Date estimatedArrival;
    private String message;
    private String gateway;
}