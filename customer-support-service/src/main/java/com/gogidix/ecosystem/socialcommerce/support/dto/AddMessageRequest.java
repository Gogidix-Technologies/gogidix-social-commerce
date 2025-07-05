package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.TicketMessage.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for adding a message to a ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMessageRequest {

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;

    @NotNull(message = "Sender is required")
    private MessageSender sender;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotBlank(message = "Sender name is required")
    @Size(min = 2, max = 100, message = "Sender name must be between 2 and 100 characters")
    private String senderName;

    @Email(message = "Valid email address is required")
    private String senderEmail;

    private String senderAvatar;

    @Builder.Default
    private Boolean isInternal = false;

    @NotNull(message = "Message type is required")
    private MessageType messageType;

    // Rich content support
    private String richContent;

    private String contentFormat; // HTML, MARKDOWN, PLAIN

    // AI-related fields
    @Builder.Default
    private Boolean aiGenerated = false;

    private Double aiConfidenceScore;

    private String aiContext;

    // Translation support
    private String originalLanguage;

    private String translatedFrom;

    private String originalContent;

    // Attachments
    private List<String> attachmentUrls;

    // Metadata
    private String clientId; // For real-time updates

    private String messageId; // For idempotency

    private String parentMessageId; // For threading

    private Boolean markPreviousAsRead;

    // For automated responses
    private String templateId;

    private String automationSource; // chatbot, workflow, integration
}