package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Payment Status DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatus {
    
    private String transactionId;
    private String status;
    private Double amount;
    private String currency;
    private Date lastUpdated;
    private String gateway;
    private String paymentMethod;
}