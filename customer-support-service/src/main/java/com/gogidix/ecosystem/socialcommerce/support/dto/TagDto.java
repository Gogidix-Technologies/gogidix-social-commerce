package com.gogidix.ecosystem.socialcommerce.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Support Tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {

    private Long id;
    private String name;
    private String description;
    private String color;
    private String category;
    private Boolean isActive;
    private Integer usageCount;
}