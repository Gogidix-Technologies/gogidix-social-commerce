package com.exalt.ecosystem.socialcommerce.subscription.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Subscription Entity
 * 
 * Represents a recurring subscription in the Social Commerce Ecosystem.
 * Handles billing cycles, subscription status, and payment information.
 */
@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String subscriptionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String planId;

    @Column(nullable = false)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime nextBillingDate;

    private LocalDateTime trialEndDate;

    private LocalDateTime cancellationDate;

    @Column(nullable = false)
    private Boolean autoRenewal = true;

    private String paymentMethod;

    private String paymentToken;

    private Integer billingAttempts = 0;

    private LocalDateTime lastBillingAttempt;

    private String cancellationReason;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    private Boolean isTrialActive = false;

    /**
     * Billing Cycle Enumeration
     */
    public enum BillingCycle {
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        ANNUAL
    }

    /**
     * Subscription Status Enumeration
     */
    public enum SubscriptionStatus {
        ACTIVE,
        TRIAL,
        SUSPENDED,
        CANCELLED,
        PENDING_CANCELLATION,
        EXPIRED,
        PAYMENT_FAILED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if subscription is active and not expired
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL;
    }

    /**
     * Check if subscription is in trial period
     */
    public boolean isInTrial() {
        return isTrialActive && trialEndDate != null && LocalDateTime.now().isBefore(trialEndDate);
    }

    /**
     * Check if subscription is due for billing
     */
    public boolean isDueForBilling() {
        return isActive() && nextBillingDate != null && LocalDateTime.now().isAfter(nextBillingDate);
    }

    /**
     * Calculate next billing date based on current billing cycle
     */
    public LocalDateTime calculateNextBillingDate() {
        LocalDateTime currentDate = nextBillingDate != null ? nextBillingDate : LocalDateTime.now();
        
        return switch (billingCycle) {
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case ANNUAL -> currentDate.plusYears(1);
        };
    }

    /**
     * Update billing information after successful payment
     */
    public void updateBillingSuccess() {
        this.lastBillingAttempt = LocalDateTime.now();
        this.nextBillingDate = calculateNextBillingDate();
        this.billingAttempts = 0;
        this.totalPaid = this.totalPaid.add(this.amount);
        
        if (isInTrial()) {
            this.isTrialActive = false;
            this.status = SubscriptionStatus.ACTIVE;
        }
    }

    /**
     * Update billing information after failed payment
     */
    public void updateBillingFailure() {
        this.lastBillingAttempt = LocalDateTime.now();
        this.billingAttempts++;
        
        if (this.billingAttempts >= 3) {
            this.status = SubscriptionStatus.PAYMENT_FAILED;
        }
    }

    /**
     * Cancel subscription
     */
    public void cancel(String reason, boolean immediate) {
        this.cancellationReason = reason;
        this.cancellationDate = LocalDateTime.now();
        
        if (immediate) {
            this.status = SubscriptionStatus.CANCELLED;
        } else {
            this.status = SubscriptionStatus.PENDING_CANCELLATION;
        }
        
        this.autoRenewal = false;
    }
}