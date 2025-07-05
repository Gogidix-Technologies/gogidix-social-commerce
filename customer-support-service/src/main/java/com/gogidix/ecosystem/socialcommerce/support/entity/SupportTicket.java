package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Support Ticket Entity
 * Represents a customer support ticket in the system
 */
@Entity
@Table(name = "support_tickets", indexes = {
    @Index(name = "idx_ticket_number", columnList = "ticketNumber", unique = true),
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_priority", columnList = "priority"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_assigned_agent", columnList = "assignedToAgentId"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_order_id", columnList = "orderId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticketNumber;

    // Customer Information
    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String customerName;

    @Column
    private String customerPhone;

    @Column
    private String customerRegion;

    // Ticket Details
    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TicketSubCategory subCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChannelType channel;

    // Order Related (for delivery tracking integration)
    @Column
    private Long orderId;

    @Column
    private String orderNumber;

    @Column
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DeliveryIssueType deliveryIssueType;

    // Assignment and Resolution
    @Column
    private Long assignedToAgentId;

    @Column
    private String assignedToAgentName;

    @Column
    private Long assignedToTeamId;

    @Column
    private String assignedToTeamName;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    // Customer Satisfaction
    @Column
    private Integer customerSatisfactionRating;

    @Column(columnDefinition = "TEXT")
    private String customerFeedback;

    // SLA and Performance
    @Column
    private LocalDateTime firstResponseAt;

    @Column
    private Integer responseTimeMinutes;

    @Column
    private Integer resolutionTimeHours;

    @Column(nullable = false)
    private Boolean slaBreached = false;

    @Column
    private LocalDateTime slaDueDate;

    // AI Support
    @Column(nullable = false)
    private Boolean aiHandled = false;

    @Column
    private Double aiConfidenceScore;

    @Column
    private Boolean aiEscalated = false;

    @Column(columnDefinition = "TEXT")
    private String aiSuggestedResponse;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column
    private LocalDateTime closedAt;

    @Column
    private LocalDateTime escalatedAt;

    @Column
    private LocalDateTime assignedAt;

    @Column
    private LocalDateTime ratedAt;

    // Escalation tracking
    @Column(nullable = false)
    private Boolean escalated = false;

    @Column(columnDefinition = "TEXT")
    private String escalationReason;

    // Relationships
    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sentAt ASC")
    private List<TicketMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<TicketActivity> activities = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "ticket_tags",
        joinColumns = @JoinColumn(name = "ticket_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<SupportTag> tags = new ArrayList<>();

    // Enums
    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        PENDING_CUSTOMER,
        PENDING_INTERNAL,
        ESCALATED,
        RESOLVED,
        CLOSED,
        REOPENED
    }

    public enum TicketPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT,
        CRITICAL
    }

    public enum TicketCategory {
        PAYMENT_ISSUES,
        SHIPPING_DELAYS,
        ACCOUNT_ACCESS,
        PRODUCT_QUESTIONS,
        REFUND_REQUESTS,
        ORDER_STATUS,
        TECHNICAL_SUPPORT,
        VENDOR_ISSUES,
        MARKETPLACE_ISSUES,
        DELIVERY_TRACKING,
        GENERAL_INQUIRY,
        COMPLAINT,
        FEEDBACK
    }

    public enum TicketSubCategory {
        // Payment Issues
        PAYMENT_FAILED,
        PAYMENT_PROCESSING,
        PAYMENT_METHOD_ADD,
        PAYMENT_REFUND,
        PAYMENT_DISPUTE,
        
        // Shipping & Delivery
        DELAYED_DELIVERY,
        LOST_PACKAGE,
        DAMAGED_PACKAGE,
        WRONG_ADDRESS,
        TRACKING_UPDATE,
        DELIVERY_RESCHEDULE,
        
        // Account
        LOGIN_ISSUES,
        PASSWORD_RESET,
        PROFILE_UPDATE,
        VERIFICATION_ISSUES,
        SECURITY_CONCERN,
        
        // Product
        PRODUCT_INFO,
        AVAILABILITY,
        PRICING,
        QUALITY_ISSUE,
        SIZE_FIT,
        
        // Order
        ORDER_MODIFICATION,
        ORDER_CANCELLATION,
        ORDER_TRACKING,
        MISSING_ITEMS,
        WRONG_ITEMS
    }

    public enum ChannelType {
        LIVE_CHAT,
        EMAIL,
        PHONE,
        SOCIAL_MEDIA,
        IN_APP,
        WEB_FORM,
        WHATSAPP,
        SMS,
        VIDEO_CALL
    }

    public enum DeliveryIssueType {
        DELAYED,
        LOST,
        DAMAGED,
        WRONG_ADDRESS,
        NOT_DELIVERED,
        RETURNED_TO_SENDER,
        CUSTOMS_HOLD,
        DELIVERY_ATTEMPTED
    }

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (this.ticketNumber == null) {
            this.ticketNumber = generateTicketNumber();
        }
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
        if (this.priority == null) {
            this.priority = TicketPriority.MEDIUM;
        }
        calculateSLA();
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.status == TicketStatus.RESOLVED && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
            calculateResolutionTime();
        }
        if (this.status == TicketStatus.CLOSED && this.closedAt == null) {
            this.closedAt = LocalDateTime.now();
        }
        if (this.status == TicketStatus.ESCALATED && this.escalatedAt == null) {
            this.escalatedAt = LocalDateTime.now();
        }
    }

    private String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private void calculateSLA() {
        LocalDateTime now = LocalDateTime.now();
        switch (this.priority) {
            case CRITICAL:
                this.slaDueDate = now.plusHours(1);
                break;
            case URGENT:
                this.slaDueDate = now.plusHours(4);
                break;
            case HIGH:
                this.slaDueDate = now.plusHours(8);
                break;
            case MEDIUM:
                this.slaDueDate = now.plusHours(24);
                break;
            case LOW:
                this.slaDueDate = now.plusHours(48);
                break;
        }
    }

    private void calculateResolutionTime() {
        if (this.createdAt != null && this.resolvedAt != null) {
            long hours = java.time.Duration.between(this.createdAt, this.resolvedAt).toHours();
            this.resolutionTimeHours = (int) hours;
        }
    }

    public void addMessage(TicketMessage message) {
        messages.add(message);
        message.setSupportTicket(this);
    }

    public void addAttachment(TicketAttachment attachment) {
        attachments.add(attachment);
        attachment.setSupportTicket(this);
    }

    public void addActivity(TicketActivity activity) {
        activities.add(activity);
        activity.setSupportTicket(this);
    }

    public void addTag(SupportTag tag) {
        tags.add(tag);
    }
}