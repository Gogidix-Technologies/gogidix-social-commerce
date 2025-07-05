package com.gogidix.ecosystem.socialcommerce.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for order status update events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateEvent {

    private Long orderId;
    private String orderNumber;
    private String previousStatus;
    private String newStatus;
    private String trackingNumber;
    private Long customerId;
    private String customerEmail;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String notes;
    private String estimatedDeliveryDate;
    private String deliveryAddress;
}