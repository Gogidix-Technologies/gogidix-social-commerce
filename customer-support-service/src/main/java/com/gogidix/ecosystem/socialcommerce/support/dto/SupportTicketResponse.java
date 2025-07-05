package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Support Ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {

    private Long id;
    private String ticketNumber;

    // Customer Information
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private String customerRegion;

    // Ticket Details
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketCategory category;
    private TicketSubCategory subCategory;
    private ChannelType channel;

    // Order Related
    private Long orderId;
    private String orderNumber;
    private String trackingNumber;
    private DeliveryIssueType deliveryIssueType;

    // Assignment
    private Long assignedToAgentId;
    private String assignedToAgentName;
    private Long assignedToTeamId;
    private String assignedToTeamName;

    // Resolution
    private String resolutionNotes;
    private String internalNotes;

    // Customer Satisfaction
    private Integer customerSatisfactionRating;
    private String customerFeedback;

    // SLA and Performance
    private LocalDateTime firstResponseAt;
    private Integer responseTimeMinutes;
    private Integer resolutionTimeHours;
    private Boolean slaBreached;
    private LocalDateTime slaDueDate;

    // AI Support
    private Boolean aiHandled;
    private Double aiConfidenceScore;
    private Boolean aiEscalated;
    private String aiSuggestedResponse;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime escalatedAt;

    // Counts and Summary
    private Integer messageCount;
    private Integer unreadMessageCount;
    private Integer attachmentCount;
    private LocalDateTime lastMessageAt;
    private String lastMessageBy;

    // Tags
    private List<TagDto> tags;

    // Related data
    private List<TicketMessageSummary> recentMessages;
    private List<AttachmentSummary> attachments;
    private List<ActivitySummary> recentActivities;

    // Helper DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketMessageSummary {
        private Long id;
        private String content;
        private String senderName;
        private String senderType;
        private LocalDateTime sentAt;
        private Boolean isRead;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentSummary {
        private Long id;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySummary {
        private String activityType;
        private String description;
        private String performedBy;
        private LocalDateTime createdAt;
    }
}