package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.TicketMessage.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Ticket Message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageResponse {

    private Long id;
    private Long ticketId;
    private String content;
    private MessageSender sender;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String senderAvatar;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private Boolean isInternal;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Long readByUserId;
    private MessageType messageType;
    private MessageStatus status;

    // Rich content support
    private String richContent;
    private String contentFormat;

    // AI-related fields
    private Boolean aiGenerated;
    private Double aiConfidenceScore;
    private String aiContext;

    // Sentiment analysis
    private SentimentType sentiment;
    private Double sentimentScore;

    // Translation support
    private String originalLanguage;
    private String translatedFrom;
    private String originalContent;

    // Reactions/Ratings
    private Integer helpfulnessRating;
    private Boolean markedAsAnswer;

    // Attachments
    private List<MessageAttachmentDto> attachments;

    // Helper DTO for attachments
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageAttachmentDto {
        private Long id;
        private String fileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
        private String thumbnailUrl;
        private LocalDateTime uploadedAt;
        private Boolean isDeleted;
        private Boolean scanned;
        private Boolean safe;
    }
}