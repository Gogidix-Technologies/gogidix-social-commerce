package com.gogidix.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * Payment Metrics DTO
 * 
 * CROSS-DOMAIN ANALYTICS SUPPORT
 * - Warehousing: Financial summary, cost analysis, ROI metrics
 * - Courier Services: Payout analytics, commission tracking
 * - Social Commerce: Revenue tracking, conversion metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMetrics {
    
    // ==============================================
    // CORE FINANCIAL METRICS
    // ==============================================
    
    private String entityId;          // Warehouse ID, Courier ID, Vendor ID
    private String entityType;        // WAREHOUSE, COURIER, VENDOR, CUSTOMER
    private String dateRange;         // DAILY, WEEKLY, MONTHLY, YEARLY
    private Date startDate;
    private Date endDate;
    
    // ==============================================
    // REVENUE & VOLUME METRICS
    // ==============================================
    
    private Double totalRevenue;      // Total payment revenue
    private Double totalVolume;       // Total payment volume
    private Long transactionCount;    // Number of transactions
    private Double averageTransaction; // Average transaction amount
    
    // ==============================================
    // COST & FEE METRICS
    // ==============================================
    
    private Double processingFees;    // Total payment processing fees
    private Double refundAmount;      // Total refunds processed
    private Double chargebackAmount;  // Total chargebacks
    private Double netRevenue;        // Revenue minus fees and refunds
    
    // ==============================================
    // PERFORMANCE METRICS
    // ==============================================
    
    private Double successRate;       // Payment success rate (%)
    private Double failureRate;       // Payment failure rate (%)
    private Double refundRate;        // Refund rate (%)
    private Long averageProcessingTime; // Avg processing time (ms)
    
    // ==============================================
    // PAYOUT METRICS (Courier & Warehousing)
    // ==============================================
    
    private Double totalPayouts;      // Total payouts made
    private Long payoutCount;         // Number of payouts
    private Double averagePayout;     // Average payout amount
    private Double pendingPayouts;    // Outstanding payout amount
    
    // ==============================================
    // CURRENCY BREAKDOWN
    // ==============================================
    
    private Map<String, Double> revenueByurrency;    // Revenue per currency
    private Map<String, Long> transactionsByCurrency; // Transactions per currency
    
    // ==============================================
    // PAYMENT METHOD BREAKDOWN
    // ==============================================
    
    private Map<String, Double> revenueByMethod;     // Revenue per payment method
    private Map<String, Long> transactionsByMethod;  // Transactions per method
    private Map<String, Double> successRateByMethod; // Success rate per method
    
    // ==============================================
    // REGIONAL BREAKDOWN
    // ==============================================
    
    private Map<String, Double> revenueByCountry;    // Revenue per country
    private Map<String, Long> transactionsByCountry; // Transactions per country
    private Map<String, String> gatewayByCountry;    // Gateway used per country
    
    // ==============================================
    // WAREHOUSING-SPECIFIC METRICS
    // ==============================================
    
    private Double billingRevenue;    // Revenue from warehouse billing
    private Double shippingRevenue;   // Revenue from shipping costs
    private Double storageRevenue;    // Revenue from storage fees
    private Double logisticsCosts;    // Total logistics costs
    
    // ==============================================
    // COURIER-SPECIFIC METRICS
    // ==============================================
    
    private Double commissionPayouts; // Total commission payouts
    private Double driverEarnings;    // Total driver earnings
    private Double walkInRevenue;     // Revenue from walk-in payments
    private Double fareRevenue;       // Revenue from fare calculations
    
    // ==============================================
    // SOCIAL COMMERCE-SPECIFIC METRICS
    // ==============================================
    
    private Double productRevenue;    // Revenue from product sales
    private Double vendorPayouts;     // Total vendor payouts
    private Double commissionEarnings; // Platform commission earnings
    private Double subscriptionRevenue; // Subscription service revenue
    
    // ==============================================
    // TREND ANALYTICS
    // ==============================================
    
    private Double revenueGrowth;     // Revenue growth rate (%)
    private Double volumeGrowth;      // Volume growth rate (%)
    private Double customerGrowth;    // Customer growth rate (%)
    private Map<String, Double> dailyTrends;   // Daily trend data
    private Map<String, Double> monthlyTrends; // Monthly trend data
    
    // ==============================================
    // BUSINESS INTELLIGENCE
    // ==============================================
    
    private Double customerLifetimeValue; // Average CLV
    private Double customerAcquisitionCost; // Average CAC
    private Double profitMargin;           // Overall profit margin (%)
    private Double returnOnInvestment;     // ROI percentage
    
    // ==============================================
    // HELPER METHODS
    // ==============================================
    
    /**
     * Calculate net profit (revenue - fees - costs)
     */
    public Double getNetProfit() {
        if (totalRevenue == null) return 0.0;
        
        double fees = processingFees != null ? processingFees : 0.0;
        double refunds = refundAmount != null ? refundAmount : 0.0;
        double chargebacks = chargebackAmount != null ? chargebackAmount : 0.0;
        
        return totalRevenue - fees - refunds - chargebacks;
    }
    
    /**
     * Calculate effective processing rate
     */
    public Double getEffectiveProcessingRate() {
        if (totalRevenue == null || totalRevenue == 0) return 0.0;
        if (processingFees == null) return 0.0;
        
        return (processingFees / totalRevenue) * 100;
    }
    
    /**
     * Calculate daily average revenue
     */
    public Double getDailyAverageRevenue() {
        if (totalRevenue == null || startDate == null || endDate == null) return 0.0;
        
        long daysDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
        if (daysDiff == 0) daysDiff = 1;
        
        return totalRevenue / daysDiff;
    }
    
    /**
     * Create empty metrics object
     */
    public static PaymentMetrics empty() {
        return PaymentMetrics.builder()
            .totalRevenue(0.0)
            .totalVolume(0.0)
            .transactionCount(0L)
            .successRate(0.0)
            .build();
    }
    
    /**
     * Check if metrics have data
     */
    public boolean hasData() {
        return transactionCount != null && transactionCount > 0;
    }
}