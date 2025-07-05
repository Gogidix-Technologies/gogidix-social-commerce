package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.DeliveryIssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for delivery update events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryUpdateEvent {

    private String trackingNumber;
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String deliveryStatus;
    private String location;
    private String courierName;
    private String courierContact;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private DeliveryIssueType issueType;
    private String issueDescription;
    private Boolean requiresCustomerAction;
    private String actionRequired;
    private LocalDateTime updatedAt;
}