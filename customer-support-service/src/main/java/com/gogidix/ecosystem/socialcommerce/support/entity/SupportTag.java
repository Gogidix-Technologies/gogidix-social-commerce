package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Support Tag Entity
 * Represents tags that can be applied to support tickets for categorization
 */
@Entity
@Table(name = "support_tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "name", unique = true),
    @Index(name = "idx_tag_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"tickets"})
public class SupportTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(length = 7)
    private String color; // Hex color code

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer usageCount = 0;

    @ManyToMany(mappedBy = "tags")
    private List<SupportTicket> tickets = new ArrayList<>();

    // Helper methods
    public void incrementUsage() {
        this.usageCount++;
    }

    public void decrementUsage() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
}