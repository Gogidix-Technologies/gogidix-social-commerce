package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ticket Attachment Entity
 * Represents file attachments on support tickets
 */
@Entity
@Table(name = "ticket_attachments", indexes = {
    @Index(name = "idx_ticket_attachment_id", columnList = "support_ticket_id"),
    @Index(name = "idx_uploaded_by", columnList = "uploadedBy")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"supportTicket"})
public class TicketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket supportTicket;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    private Long uploadedBy;

    @Column(nullable = false)
    private String uploadedByName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadedByType uploadedByType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt;

    @Column
    private Long deletedBy;

    // Security scanning
    @Column(nullable = false)
    private Boolean scanned = false;

    @Column(nullable = false)
    private Boolean safe = true;

    @Column
    private String scanResult;

    // Enums
    public enum UploadedByType {
        CUSTOMER,
        AGENT,
        SYSTEM,
        VENDOR
    }

    // Helper methods
    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    public boolean isImage() {
        String ext = getFileExtension().toLowerCase();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
               ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }

    public boolean isDocument() {
        String ext = getFileExtension().toLowerCase();
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
               ext.equals("txt") || ext.equals("xls") || ext.equals("xlsx");
    }
}