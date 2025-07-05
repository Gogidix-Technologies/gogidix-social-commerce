package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge Base Article Entity
 * Represents articles in the knowledge base used for self-service and AI responses
 */
@Entity
@Table(name = "knowledge_base_articles", indexes = {
    @Index(name = "idx_kb_category", columnList = "category"),
    @Index(name = "idx_kb_status", columnList = "status"),
    @Index(name = "idx_kb_views", columnList = "viewCount"),
    @Index(name = "idx_kb_helpful", columnList = "helpfulCount")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class KnowledgeBaseArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String articleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String metaDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SupportTicket.TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SupportTicket.TicketSubCategory subCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleType articleType = ArticleType.SOLUTION;

    // SEO and search
    @Column(length = 200)
    private String slug;

    @ElementCollection
    @CollectionTable(name = "article_keywords", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    // Multi-language support
    @Column(nullable = false, length = 10)
    private String language = "en";

    @Column
    private Long parentArticleId; // For translated articles

    @ElementCollection
    @CollectionTable(name = "article_translations", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "translation_id")
    private List<Long> translationIds = new ArrayList<>();

    // Author and review
    @Column
    private Long authorId;

    @Column
    private String authorName;

    @Column
    private Long reviewedBy;

    @Column
    private String reviewerName;

    @Column
    private LocalDateTime reviewedAt;

    // Metrics
    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer helpfulCount = 0;

    @Column(nullable = false)
    private Integer notHelpfulCount = 0;

    @Column(nullable = false)
    private Double helpfulnessScore = 0.0;

    @Column(nullable = false)
    private Integer searchAppearances = 0;

    @Column(nullable = false)
    private Integer aiSuggestions = 0;

    @Column(nullable = false)
    private Double aiRelevanceScore = 0.0;

    // Related articles
    @ElementCollection
    @CollectionTable(name = "related_articles", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "related_article_id")
    private List<Long> relatedArticleIds = new ArrayList<>();

    // Version control
    @Column(nullable = false)
    private Integer version = 1;

    @Column
    private Long previousVersionId;

    @Column(columnDefinition = "TEXT")
    private String changeLog;

    // Publishing
    @Column
    private LocalDateTime publishedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum ArticleStatus {
        DRAFT,
        REVIEW,
        PUBLISHED,
        ARCHIVED,
        OUTDATED
    }

    public enum ArticleType {
        SOLUTION,
        FAQ,
        GUIDE,
        TROUBLESHOOTING,
        POLICY,
        ANNOUNCEMENT,
        VIDEO_TUTORIAL,
        QUICK_TIP
    }

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (this.articleId == null) {
            this.articleId = generateArticleId();
        }
        if (this.slug == null) {
            this.slug = generateSlug();
        }
        calculateHelpfulnessScore();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateHelpfulnessScore();
    }

    private String generateArticleId() {
        return "KB-" + System.currentTimeMillis();
    }

    private String generateSlug() {
        if (title != null) {
            return title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
        }
        return articleId.toLowerCase();
    }

    private void calculateHelpfulnessScore() {
        int total = helpfulCount + notHelpfulCount;
        if (total > 0) {
            this.helpfulnessScore = (double) helpfulCount / total * 100;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void markAsHelpful() {
        this.helpfulCount++;
        calculateHelpfulnessScore();
    }

    public void markAsNotHelpful() {
        this.notHelpfulCount++;
        calculateHelpfulnessScore();
    }

    public void incrementSearchAppearances() {
        this.searchAppearances++;
    }

    public void incrementAiSuggestions() {
        this.aiSuggestions++;
    }

    public boolean isPublished() {
        return status == ArticleStatus.PUBLISHED && 
               (publishedAt == null || publishedAt.isBefore(LocalDateTime.now())) &&
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
}