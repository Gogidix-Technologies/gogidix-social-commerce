package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ticket Activity Entity
 * Tracks all activities and state changes on a support ticket
 */
@Entity
@Table(name = "ticket_activities", indexes = {
    @Index(name = "idx_activity_ticket_id", columnList = "support_ticket_id"),
    @Index(name = "idx_activity_type", columnList = "activityType"),
    @Index(name = "idx_activity_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"supportTicket"})
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket supportTicket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false)
    private String description;

    @Column
    private String oldValue;

    @Column
    private String newValue;

    @Column
    private Long performedBy;

    @Column
    private String performedByName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PerformedByType performedByType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    // Enums
    public enum ActivityType {
        TICKET_CREATED,
        STATUS_CHANGED,
        PRIORITY_CHANGED,
        CATEGORY_CHANGED,
        ASSIGNED_TO_AGENT,
        ASSIGNED_TO_TEAM,
        ESCALATED,
        MESSAGE_ADDED,
        INTERNAL_NOTE_ADDED,
        ATTACHMENT_ADDED,
        ATTACHMENT_REMOVED,
        TAG_ADDED,
        TAG_REMOVED,
        MERGED_WITH_TICKET,
        SPLIT_FROM_TICKET,
        SLA_BREACHED,
        CUSTOMER_RESPONDED,
        AGENT_RESPONDED,
        AI_RESPONDED,
        RESOLVED,
        REOPENED,
        CLOSED,
        RATED,
        EDITED,
        VIEWED,
        EXPORTED,
        SHARED
    }

    public enum PerformedByType {
        CUSTOMER,
        AGENT,
        SYSTEM,
        AI_ASSISTANT,
        ADMIN,
        AUTOMATION
    }

    // Helper methods
    public static TicketActivity createActivity(SupportTicket ticket, ActivityType type, 
                                               String description, Long performedBy, 
                                               String performedByName, PerformedByType performedByType) {
        return TicketActivity.builder()
                .supportTicket(ticket)
                .activityType(type)
                .description(description)
                .performedBy(performedBy)
                .performedByName(performedByName)
                .performedByType(performedByType)
                .build();
    }

    public static TicketActivity createStatusChangeActivity(SupportTicket ticket, 
                                                           String oldStatus, String newStatus,
                                                           Long performedBy, String performedByName) {
        return TicketActivity.builder()
                .supportTicket(ticket)
                .activityType(ActivityType.STATUS_CHANGED)
                .description(String.format("Status changed from %s to %s", oldStatus, newStatus))
                .oldValue(oldStatus)
                .newValue(newStatus)
                .performedBy(performedBy)
                .performedByName(performedByName)
                .performedByType(PerformedByType.AGENT)
                .build();
    }
}