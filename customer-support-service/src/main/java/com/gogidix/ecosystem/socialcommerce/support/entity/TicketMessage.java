package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket Message Entity
 * Represents a message in a support ticket conversation
 */
@Entity
@Table(name = "ticket_messages", indexes = {
    @Index(name = "idx_ticket_id", columnList = "support_ticket_id"),
    @Index(name = "idx_sender_id", columnList = "senderId"),
    @Index(name = "idx_sent_at", columnList = "sentAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"supportTicket"})
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket supportTicket;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageSender sender;

    @Column
    private Long senderId;

    @Column
    private String senderName;

    @Column
    private String senderEmail;

    @Column
    private String senderAvatar;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime editedAt;

    @Column(nullable = false)
    private Boolean isInternal = false;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column
    private LocalDateTime readAt;

    @Column
    private Long readByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageType messageType = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MessageStatus status = MessageStatus.SENT;

    // For rich content support
    @Column(columnDefinition = "TEXT")
    private String richContent;

    @Column
    private String contentFormat; // HTML, MARKDOWN, PLAIN

    // AI-related fields
    @Column(nullable = false)
    private Boolean aiGenerated = false;

    @Column
    private Double aiConfidenceScore;

    @Column(columnDefinition = "TEXT")
    private String aiContext;

    // Sentiment analysis
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SentimentType sentiment;

    @Column
    private Double sentimentScore;

    // Translation support
    @Column
    private String originalLanguage;

    @Column
    private String translatedFrom;

    @Column(columnDefinition = "TEXT")
    private String originalContent;

    // Attachments relationship
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageAttachment> attachments = new ArrayList<>();

    // Reactions/Ratings
    @Column
    private Integer helpfulnessRating;

    @Column
    private Boolean markedAsAnswer = false;

    // Enums
    public enum MessageSender {
        CUSTOMER,
        AGENT,
        SYSTEM,
        AI_ASSISTANT,
        VENDOR,
        ADMIN
    }

    public enum MessageType {
        TEXT,
        SYSTEM_NOTE,
        STATUS_CHANGE,
        ASSIGNMENT_CHANGE,
        ESCALATION,
        RESOLUTION,
        CUSTOMER_FEEDBACK,
        INTERNAL_NOTE,
        AUTO_RESPONSE,
        SUGGESTED_ARTICLE,
        VOICE_TRANSCRIPT,
        VIDEO_MESSAGE,
        SCREEN_RECORDING
    }

    public enum MessageStatus {
        DRAFT,
        SENT,
        DELIVERED,
        READ,
        FAILED,
        DELETED
    }

    public enum SentimentType {
        VERY_POSITIVE,
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        VERY_NEGATIVE
    }

    // Helper methods
    public void addAttachment(MessageAttachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }

    public boolean isFromCustomer() {
        return MessageSender.CUSTOMER.equals(this.sender);
    }

    public boolean isFromAgent() {
        return MessageSender.AGENT.equals(this.sender);
    }

    public boolean isSystemMessage() {
        return MessageSender.SYSTEM.equals(this.sender);
    }

    public void markAsRead(Long userId) {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.readByUserId = userId;
    }
}