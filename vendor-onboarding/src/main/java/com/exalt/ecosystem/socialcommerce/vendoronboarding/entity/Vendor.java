package com.exalt.ecosystem.socialcommerce.vendoronboarding.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * Vendor Entity
 * 
 * Represents a vendor in the onboarding process for the Social Commerce Ecosystem.
 * Tracks registration, KYC verification, and approval status.
 */
@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String vendorId;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType businessType;

    private String registrationNumber;

    private String businessDescription;

    @Embedded
    private BusinessAddress businessAddress;

    private String businessPhone;

    private String businessWebsite;

    private Integer yearEstablished;

    private Integer employeeCount;

    private BigDecimal annualRevenue;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<ProductCategory> productCategories;

    @ElementCollection
    private List<String> shippingLocations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingStatus onboardingStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KYCStatus kycStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceStatus complianceStatus;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private LocalDateTime approvalDate;

    private LocalDateTime rejectionDate;

    private String approvedBy;

    private String rejectedBy;

    private String rejectionReason;

    private String approvalNotes;

    @Enumerated(EnumType.STRING)
    private VendorTier vendorTier;

    private BigDecimal commissionRate;

    @Enumerated(EnumType.STRING)
    private PayoutSchedule payoutSchedule;

    @Enumerated(EnumType.STRING)
    private MarketplaceStatus marketplaceStatus;

    private Boolean canReapply = true;

    private LocalDateTime reapplyAfter;

    private String applicationReference;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // KYC Information
    private String businessOwnerName;

    private String ownerId;

    private String ownerNationality;

    private String businessLicense;

    private String taxId;

    @Embedded
    private BankAccountDetails bankAccountDetails;

    private String authorizedSignatory;

    @Enumerated(EnumType.STRING)
    private VerificationLevel verificationLevel;

    /**
     * Business Type Enumeration
     */
    public enum BusinessType {
        SOLE_PROPRIETORSHIP,
        PARTNERSHIP,
        LIMITED_COMPANY,
        CORPORATION,
        LLC,
        NON_PROFIT,
        COOPERATIVE
    }

    /**
     * Product Category Enumeration
     */
    public enum ProductCategory {
        ELECTRONICS,
        FASHION,
        HOME_GARDEN,
        SPORTS,
        BOOKS,
        HEALTH_BEAUTY,
        TOYS_GAMES,
        AUTOMOTIVE,
        FOOD_BEVERAGES,
        JEWELRY,
        ART_CRAFTS,
        PET_SUPPLIES
    }

    /**
     * Onboarding Status Enumeration
     */
    public enum OnboardingStatus {
        REGISTRATION_STARTED,
        PROFILE_COMPLETED,
        DOCUMENT_UPLOAD,
        KYC_SUBMITTED,
        IN_REVIEW,
        APPROVED,
        REJECTED,
        SUSPENDED
    }

    /**
     * KYC Status Enumeration
     */
    public enum KYCStatus {
        PENDING,
        SUBMITTED,
        UNDER_REVIEW,
        ADDITIONAL_INFO_REQUIRED,
        VERIFIED,
        FAILED
    }

    /**
     * Compliance Status Enumeration
     */
    public enum ComplianceStatus {
        PENDING,
        IN_PROGRESS,
        PASSED,
        FAILED,
        REQUIRES_REVIEW
    }

    /**
     * Vendor Tier Enumeration
     */
    public enum VendorTier {
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM
    }

    /**
     * Payout Schedule Enumeration
     */
    public enum PayoutSchedule {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    /**
     * Marketplace Status Enumeration
     */
    public enum MarketplaceStatus {
        INACTIVE,
        ACTIVE,
        SUSPENDED,
        BANNED
    }

    /**
     * Verification Level Enumeration
     */
    public enum VerificationLevel {
        LEVEL_1,
        LEVEL_2,
        LEVEL_3
    }

    /**
     * Business Address Embedded Class
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessAddress {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    /**
     * Bank Account Details Embedded Class
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankAccountDetails {
        private String bankName;
        private String accountNumber;
        private String routingNumber;
        private String accountType;
        private String accountHolderName;
        private String swiftCode;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if vendor is approved and active
     */
    public boolean isActive() {
        return onboardingStatus == OnboardingStatus.APPROVED && 
               marketplaceStatus == MarketplaceStatus.ACTIVE;
    }

    /**
     * Check if vendor can list products
     */
    public boolean canListProducts() {
        return isActive() && kycStatus == KYCStatus.VERIFIED && 
               complianceStatus == ComplianceStatus.PASSED;
    }

    /**
     * Get onboarding progress percentage
     */
    public int getOnboardingProgress() {
        return switch (onboardingStatus) {
            case REGISTRATION_STARTED -> 20;
            case PROFILE_COMPLETED -> 40;
            case DOCUMENT_UPLOAD -> 60;
            case KYC_SUBMITTED -> 70;
            case IN_REVIEW -> 85;
            case APPROVED -> 100;
            case REJECTED, SUSPENDED -> 0;
        };
    }

    /**
     * Check if vendor can reapply after rejection
     */
    public boolean canReapplyNow() {
        return canReapply && (reapplyAfter == null || LocalDateTime.now().isAfter(reapplyAfter));
    }

    /**
     * Approve vendor application
     */
    public void approve(String approvedBy, String notes) {
        this.onboardingStatus = OnboardingStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvalNotes = notes;
        this.approvalDate = LocalDateTime.now();
        this.marketplaceStatus = MarketplaceStatus.ACTIVE;
        
        if (this.vendorTier == null) {
            this.vendorTier = VendorTier.BRONZE;
        }
        
        if (this.payoutSchedule == null) {
            this.payoutSchedule = PayoutSchedule.WEEKLY;
        }
    }

    /**
     * Reject vendor application
     */
    public void reject(String rejectedBy, String reason) {
        this.onboardingStatus = OnboardingStatus.REJECTED;
        this.rejectedBy = rejectedBy;
        this.rejectionReason = reason;
        this.rejectionDate = LocalDateTime.now();
        this.marketplaceStatus = MarketplaceStatus.INACTIVE;
        this.reapplyAfter = LocalDateTime.now().plusDays(30);
    }

    /**
     * Suspend vendor
     */
    public void suspend(String reason) {
        this.onboardingStatus = OnboardingStatus.SUSPENDED;
        this.marketplaceStatus = MarketplaceStatus.SUSPENDED;
        this.rejectionReason = reason;
    }

    /**
     * Update vendor tier
     */
    public void updateTier(VendorTier newTier) {
        this.vendorTier = newTier;
        
        // Update commission rate based on tier
        this.commissionRate = switch (newTier) {
            case BRONZE -> new BigDecimal("0.06");
            case SILVER -> new BigDecimal("0.05");
            case GOLD -> new BigDecimal("0.04");
            case PLATINUM -> new BigDecimal("0.03");
        };
    }
}