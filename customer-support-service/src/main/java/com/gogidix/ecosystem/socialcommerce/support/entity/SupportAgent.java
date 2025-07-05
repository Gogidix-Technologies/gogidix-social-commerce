package com.gogidix.ecosystem.socialcommerce.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Support Agent Entity
 * Represents customer support agents in the system
 */
@Entity
@Table(name = "support_agents", indexes = {
    @Index(name = "idx_agent_email", columnList = "email", unique = true),
    @Index(name = "idx_agent_status", columnList = "status"),
    @Index(name = "idx_agent_team", columnList = "teamId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class SupportAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String agentId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentStatus status = AgentStatus.OFFLINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentTier tier = AgentTier.LEVEL_1;

    @Column
    private Long teamId;

    @Column
    private String teamName;

    // Skills and Languages
    @ElementCollection
    @CollectionTable(name = "agent_skills", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "agent_languages", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "language")
    private List<String> languages = new ArrayList<>();

    // Specializations
    @ElementCollection
    @CollectionTable(name = "agent_specializations", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "specialization")
    @Enumerated(EnumType.STRING)
    private List<SupportTicket.TicketCategory> specializations = new ArrayList<>();

    // Working hours and availability
    @Column
    private String timezone;

    @Column
    private String shiftSchedule;

    @Column
    private LocalDateTime lastActiveAt;

    @Column
    private LocalDateTime availableUntil;

    // Performance metrics
    @Column(nullable = false)
    private Integer activeTickets = 0;

    @Column(nullable = false)
    private Integer totalTicketsHandled = 0;

    @Column(nullable = false)
    private Double averageResponseTimeMinutes = 0.0;

    @Column(nullable = false)
    private Double averageResolutionTimeHours = 0.0;

    @Column(nullable = false)
    private Double customerSatisfactionScore = 0.0;

    @Column(nullable = false)
    private Double firstContactResolutionRate = 0.0;

    @Column(nullable = false)
    private Integer escalationCount = 0;

    // Capacity and workload
    @Column(nullable = false)
    private Integer maxConcurrentTickets = 10;

    @Column(nullable = false)
    private Double workloadPercentage = 0.0;

    // Training and certifications
    @Column
    private LocalDateTime lastTrainingDate;

    @ElementCollection
    @CollectionTable(name = "agent_certifications", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "certification")
    private List<String> certifications = new ArrayList<>();

    // Access permissions
    @Column(nullable = false)
    private Boolean canHandleEscalations = false;

    @Column(nullable = false)
    private Boolean canAccessInternalNotes = true;

    @Column(nullable = false)
    private Boolean canIssueRefunds = false;

    @Column(nullable = false)
    private Boolean canModifyOrders = false;

    @Column(nullable = false)
    private Boolean isQualityAssurance = false;

    @Column(nullable = false)
    private Boolean isTrainer = false;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deactivatedAt;

    // Enums
    public enum AgentStatus {
        ONLINE,
        BUSY,
        AWAY,
        OFFLINE,
        IN_TRAINING,
        ON_BREAK
    }

    public enum AgentTier {
        LEVEL_1,    // Basic support
        LEVEL_2,    // Advanced support
        LEVEL_3,    // Expert/Escalation
        TEAM_LEAD,
        MANAGER,
        QA_SPECIALIST,
        TRAINER
    }

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (this.agentId == null) {
            this.agentId = generateAgentId();
        }
    }

    private String generateAgentId() {
        return "AGT-" + System.currentTimeMillis();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAvailable() {
        return status == AgentStatus.ONLINE && activeTickets < maxConcurrentTickets;
    }

    public boolean canTakeMoreTickets() {
        return activeTickets < maxConcurrentTickets;
    }

    public void incrementActiveTickets() {
        this.activeTickets++;
        updateWorkloadPercentage();
    }

    public void decrementActiveTickets() {
        if (this.activeTickets > 0) {
            this.activeTickets--;
        }
        updateWorkloadPercentage();
    }

    private void updateWorkloadPercentage() {
        this.workloadPercentage = (double) activeTickets / maxConcurrentTickets * 100;
    }

    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    public boolean speaksLanguage(String language) {
        return languages.contains(language);
    }

    public boolean isSpecializedIn(SupportTicket.TicketCategory category) {
        return specializations.contains(category);
    }
}