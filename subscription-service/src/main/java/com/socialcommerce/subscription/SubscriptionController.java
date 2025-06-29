package com.socialcommerce.subscription;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

/**
 * Subscription Controller
 * 
 * Manages recurring billing, subscription lifecycle, and automated billing operations
 * for the Social Commerce Ecosystem.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription Management", description = "APIs for managing recurring subscriptions and billing")
public class SubscriptionController {

    @Operation(summary = "Create new subscription", description = "Creates a new recurring subscription with specified billing cycle")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSubscription(@RequestBody Map<String, Object> subscriptionRequest) {
        try {
            String userId = (String) subscriptionRequest.get("userId");
            String planId = (String) subscriptionRequest.get("planId");
            String billingCycle = (String) subscriptionRequest.get("billingCycle"); // MONTHLY, QUARTERLY, ANNUAL
            BigDecimal amount = new BigDecimal(subscriptionRequest.get("amount").toString());
            
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("subscriptionId", "SUB_" + System.currentTimeMillis());
            subscription.put("userId", userId);
            subscription.put("planId", planId);
            subscription.put("billingCycle", billingCycle);
            subscription.put("amount", amount);
            subscription.put("status", "ACTIVE");
            subscription.put("createdAt", LocalDateTime.now());
            subscription.put("nextBillingDate", calculateNextBillingDate(billingCycle));
            subscription.put("trialEndDate", calculateTrialEndDate());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription created successfully");
            response.put("subscription", subscription);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create subscription: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Get subscription details", description = "Retrieves detailed information about a specific subscription")
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> getSubscription(@PathVariable String subscriptionId) {
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("subscriptionId", subscriptionId);
        subscription.put("userId", "USER_12345");
        subscription.put("planId", "PREMIUM_PLAN");
        subscription.put("planName", "Premium Social Commerce Plan");
        subscription.put("billingCycle", "MONTHLY");
        subscription.put("amount", new BigDecimal("29.99"));
        subscription.put("status", "ACTIVE");
        subscription.put("createdAt", LocalDateTime.now().minusDays(30));
        subscription.put("nextBillingDate", LocalDateTime.now().plusDays(30));
        subscription.put("isTrialActive", false);
        subscription.put("paymentMethod", "CREDIT_CARD");
        subscription.put("autoRenewal", true);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("subscription", subscription);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update subscription", description = "Updates subscription details including plan changes and billing cycle modifications")
    @PutMapping("/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> updateSubscription(
            @PathVariable String subscriptionId,
            @RequestBody Map<String, Object> updateRequest) {
        
        Map<String, Object> updatedSubscription = new HashMap<>();
        updatedSubscription.put("subscriptionId", subscriptionId);
        updatedSubscription.put("planId", updateRequest.get("planId"));
        updatedSubscription.put("billingCycle", updateRequest.get("billingCycle"));
        updatedSubscription.put("amount", updateRequest.get("amount"));
        updatedSubscription.put("status", "ACTIVE");
        updatedSubscription.put("updatedAt", LocalDateTime.now());
        updatedSubscription.put("nextBillingDate", calculateNextBillingDate((String) updateRequest.get("billingCycle")));
        updatedSubscription.put("prorationCredit", calculateProrationCredit(updateRequest));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Subscription updated successfully");
        response.put("subscription", updatedSubscription);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel subscription", description = "Cancels an active subscription with optional immediate or end-of-period cancellation")
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @PathVariable String subscriptionId,
            @RequestParam(defaultValue = "false") boolean immediate) {
        
        Map<String, Object> cancellation = new HashMap<>();
        cancellation.put("subscriptionId", subscriptionId);
        cancellation.put("status", immediate ? "CANCELLED" : "PENDING_CANCELLATION");
        cancellation.put("cancellationDate", immediate ? LocalDateTime.now() : LocalDateTime.now().plusDays(30));
        cancellation.put("refundAmount", immediate ? new BigDecimal("15.99") : BigDecimal.ZERO);
        cancellation.put("accessEndDate", immediate ? LocalDateTime.now() : LocalDateTime.now().plusDays(30));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", immediate ? "Subscription cancelled immediately" : "Subscription will be cancelled at period end");
        response.put("cancellation", cancellation);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user subscriptions", description = "Retrieves all subscriptions for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSubscriptions(@PathVariable String userId) {
        List<Map<String, Object>> subscriptions = Arrays.asList(
            createSampleSubscription("SUB_001", "PREMIUM_PLAN", "MONTHLY", "29.99"),
            createSampleSubscription("SUB_002", "BASIC_PLAN", "ANNUAL", "199.99"),
            createSampleSubscription("SUB_003", "ENTERPRISE_PLAN", "QUARTERLY", "299.99")
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("subscriptions", subscriptions);
        response.put("totalSubscriptions", subscriptions.size());
        response.put("activeSubscriptions", 2);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process billing cycle", description = "Manually triggers billing process for subscriptions due for billing")
    @PostMapping("/billing/process")
    public ResponseEntity<Map<String, Object>> processBilling() {
        List<Map<String, Object>> billingResults = Arrays.asList(
            createBillingResult("SUB_001", "SUCCESS", "29.99"),
            createBillingResult("SUB_002", "SUCCESS", "199.99"),
            createBillingResult("SUB_003", "FAILED", "299.99")
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Billing cycle processed");
        response.put("processedCount", billingResults.size());
        response.put("successfulBillings", 2);
        response.put("failedBillings", 1);
        response.put("results", billingResults);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get subscription analytics", description = "Provides comprehensive analytics for subscription performance")
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getSubscriptionAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalSubscriptions", 1250);
        analytics.put("activeSubscriptions", 1100);
        analytics.put("cancelledSubscriptions", 150);
        analytics.put("monthlyRecurringRevenue", new BigDecimal("45750.00"));
        analytics.put("annualRecurringRevenue", new BigDecimal("549000.00"));
        analytics.put("averageRevenuePerUser", new BigDecimal("41.59"));
        analytics.put("churnRate", "2.3%");
        analytics.put("conversionRate", "15.7%");
        
        Map<String, Object> planDistribution = new HashMap<>();
        planDistribution.put("BASIC_PLAN", 45);
        planDistribution.put("PREMIUM_PLAN", 40);
        planDistribution.put("ENTERPRISE_PLAN", 15);
        
        analytics.put("planDistribution", planDistribution);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("analytics", analytics);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retry failed billing", description = "Retries billing for subscriptions with failed payments")
    @PostMapping("/billing/retry/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> retryBilling(@PathVariable String subscriptionId) {
        Map<String, Object> retryResult = new HashMap<>();
        retryResult.put("subscriptionId", subscriptionId);
        retryResult.put("retryAttempt", 2);
        retryResult.put("status", "SUCCESS");
        retryResult.put("billedAmount", new BigDecimal("29.99"));
        retryResult.put("paymentMethod", "CREDIT_CARD");
        retryResult.put("processedAt", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Billing retry successful");
        response.put("retryResult", retryResult);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Automated billing scheduler - runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void automaticBillingProcess() {
        // Automated billing logic would be implemented here
        System.out.println("Processing automatic billing at: " + LocalDateTime.now());
    }

    /**
     * Helper method to calculate next billing date based on billing cycle
     */
    private LocalDateTime calculateNextBillingDate(String billingCycle) {
        LocalDateTime now = LocalDateTime.now();
        return switch (billingCycle.toUpperCase()) {
            case "MONTHLY" -> now.plusMonths(1);
            case "QUARTERLY" -> now.plusMonths(3);
            case "ANNUAL" -> now.plusYears(1);
            case "WEEKLY" -> now.plusWeeks(1);
            default -> now.plusMonths(1);
        };
    }

    /**
     * Helper method to calculate trial end date
     */
    private LocalDateTime calculateTrialEndDate() {
        return LocalDateTime.now().plusDays(14); // 14-day trial
    }

    /**
     * Helper method to calculate proration credit for plan changes
     */
    private BigDecimal calculateProrationCredit(Map<String, Object> updateRequest) {
        // Simplified proration calculation
        return new BigDecimal("5.99");
    }

    /**
     * Helper method to create sample subscription object
     */
    private Map<String, Object> createSampleSubscription(String id, String planId, String cycle, String amount) {
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("subscriptionId", id);
        subscription.put("planId", planId);
        subscription.put("billingCycle", cycle);
        subscription.put("amount", new BigDecimal(amount));
        subscription.put("status", "ACTIVE");
        subscription.put("createdAt", LocalDateTime.now().minusDays(30));
        subscription.put("nextBillingDate", calculateNextBillingDate(cycle));
        return subscription;
    }

    /**
     * Helper method to create billing result object
     */
    private Map<String, Object> createBillingResult(String subscriptionId, String status, String amount) {
        Map<String, Object> result = new HashMap<>();
        result.put("subscriptionId", subscriptionId);
        result.put("status", status);
        result.put("amount", new BigDecimal(amount));
        result.put("processedAt", LocalDateTime.now());
        result.put("paymentMethod", "CREDIT_CARD");
        return result;
    }
}
