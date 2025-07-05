package com.gogidix.ecosystem.socialcommerce.support.dto;

import com.gogidix.ecosystem.socialcommerce.support.entity.KnowledgeBaseArticle.*;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Knowledge Base Articles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseArticleDto {

    private Long id;
    private String articleId;
    private String title;
    private String content;
    private String summary;
    private String metaDescription;
    private TicketCategory category;
    private TicketSubCategory subCategory;
    private ArticleStatus status;
    private ArticleType articleType;
    private String slug;
    private List<String> keywords;
    private List<String> tags;
    private String language;
    private String authorName;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    
    // Metrics
    private Integer viewCount;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Double helpfulnessScore;
    private Double aiRelevanceScore;
    
    // Publishing
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private Boolean featured;
    private Integer displayOrder;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related articles
    private List<Long> relatedArticleIds;
}