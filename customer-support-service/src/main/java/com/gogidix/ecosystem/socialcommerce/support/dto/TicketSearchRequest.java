package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ticket search requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSearchRequest {

    private String searchTerm;
    private List<TicketStatus> statuses;
    private List<TicketPriority> priorities;
    private List<TicketCategory> categories;
    private List<ChannelType> channels;
    private Long customerId;
    private String customerEmail;
    private String customerRegion;
    private Long assignedToAgentId;
    private Long assignedToTeamId;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime updatedAfter;
    private LocalDateTime updatedBefore;
    private Boolean slaBreached;
    private Boolean aiHandled;
    private Boolean hasUnreadMessages;
    private List<String> tags;
    private Long orderId;
    private String trackingNumber;
    private DeliveryIssueType deliveryIssueType;
    private Integer minRating;
    private Integer maxRating;
}