package com.exalt.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Webhook Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
    
    private String eventId;
    private String eventType;
    private boolean processed;
    private String message;
    private Date processedAt;
}