package com.gogidix.ecosystem.socialcommerce.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for AI suggested responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestedResponseDto {

    private String suggestedResponse;
    private Double confidenceScore;
    private String responseType; // auto_response, suggested_template, knowledge_base
    private List<String> suggestedActions;
    private List<KnowledgeBaseArticleDto> relatedArticles;
    private String intentDetected;
    private String sentiment;
    private Boolean requiresHumanReview;
    private String reasoning;
    private List<String> tags;
}