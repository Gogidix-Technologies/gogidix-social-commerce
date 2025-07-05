package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new support ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Valid email address is required")
    private String customerEmail;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Valid phone number is required")
    private String customerPhone;

    @Size(max = 50, message = "Customer region must not exceed 50 characters")
    private String customerRegion;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private TicketCategory category;

    private TicketSubCategory subCategory;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotNull(message = "Channel is required")
    private ChannelType channel;

    // Order related fields for delivery tracking integration
    private Long orderId;

    private String orderNumber;

    private String trackingNumber;

    private DeliveryIssueType deliveryIssueType;

    // Additional metadata
    private String browserInfo;

    private String deviceInfo;

    private String sessionId;

    private List<String> attachmentUrls;

    private List<String> tags;

    // For internal ticket creation
    private Long createdByAgentId;

    private String createdByAgentName;

    private Boolean isInternalTicket;

    // Customer context
    private String customerLanguage;

    private String customerTimezone;

    private Boolean isVipCustomer;

    private Long accountAge; // in days

    private Integer previousTicketCount;

    // Additional context
    private String referrerUrl;

    private String currentPageUrl;

    private String sourceApplication; // web, mobile, api

    private String ipAddress;

    private String userAgent;
}