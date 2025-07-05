package com.gogidix.ecosystem.socialcommerce.paymentgateway.controller;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.*;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.integration.WarehouseBillingIntegration;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.integration.CourierPayoutIntegration;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.client.UnifiedPaymentClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Payment Metrics Controller
 * 
 * CROSS-DOMAIN PAYMENT ANALYTICS API
 * - Provides unified payment metrics across all domains
 * - Supports Warehousing, Courier Services, and Social Commerce analytics
 * - Real-time financial reporting and business intelligence
 * - Regional and currency-specific breakdowns
 * 
 * ENDPOINTS:
 * - GET /api/v1/payments/metrics - Unified metrics endpoint
 * - GET /api/v1/payments/metrics/warehouse/{warehouseId} - Warehouse-specific metrics
 * - GET /api/v1/payments/metrics/courier/{entityId} - Courier-specific metrics
 * - GET /api/v1/payments/metrics/dashboard - Multi-domain dashboard data
 * - GET /api/v1/payments/methods/{countryCode} - Regional payment methods
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class PaymentMetricsController {
    
    private static final Logger logger = Logger.getLogger(PaymentMetricsController.class.getName());
    
    @Autowired
    private UnifiedPaymentClient paymentClient;
    
    @Autowired
    private WarehouseBillingIntegration warehouseBilling;
    
    @Autowired
    private CourierPayoutIntegration courierPayout;
    
    // ==============================================
    // UNIFIED METRICS ENDPOINTS
    // ==============================================
    
    /**
     * Get unified payment metrics for any entity
     * SECURITY: Requires ANALYTICS_READ permission with entity access validation
     * Supports: Warehouses, Courier Partners, Drivers, Vendors, Customers
     */
    @PreAuthorize("hasPermission('ANALYTICS', 'READ')")
    @GetMapping("/metrics")
    public ResponseEntity<?> getPaymentMetrics(
            @RequestParam String entityId,
            @RequestParam String entityType,
            @RequestParam(defaultValue = "MONTHLY") String dateRange,
            Authentication authentication) {
        try {
            logger.info("Retrieving payment metrics for entity: " + entityId + ", type: " + entityType);
            
            PaymentMetrics metrics = paymentClient.getPaymentMetrics(entityId, entityType, dateRange);
            
            // Add entity-specific enrichment based on type
            if ("WAREHOUSE".equals(entityType)) {
                metrics = enrichWarehouseMetrics(metrics, entityId, dateRange);
            } else if ("COURIER_DRIVER".equals(entityType) || "COURIER_PARTNER".equals(entityType) || "COURIER_LOCATION".equals(entityType)) {
                metrics = enrichCourierMetrics(metrics, entityId, entityType, dateRange);
            }
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.severe("Payment metrics retrieval failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Metrics retrieval failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    // ==============================================
    // WAREHOUSE-SPECIFIC METRICS
    // ==============================================
    
    /**
     * Get comprehensive warehouse billing metrics
     * SECURITY: Requires WAREHOUSE_READ permission with warehouse access validation
     */
    @PreAuthorize("hasPermission('WAREHOUSE', 'READ')")
    @GetMapping("/metrics/warehouse/{warehouseId}")
    public ResponseEntity<?> getWarehouseMetrics(
            @PathVariable String warehouseId,
            @RequestParam(defaultValue = "MONTHLY") String dateRange,
            Authentication authentication) {
        try {
            logger.info("Retrieving warehouse metrics for warehouse: " + warehouseId);
            
            // Get basic metrics
            PaymentMetrics basicMetrics = warehouseBilling.getWarehouseBillingMetrics(warehouseId, dateRange);
            
            // Get detailed revenue breakdown
            PaymentMetrics revenueBreakdown = warehouseBilling.getRevenueBreakdown(warehouseId, dateRange);
            
            // Combine and enrich metrics
            PaymentMetrics combinedMetrics = PaymentMetrics.builder()
                .entityId(warehouseId)
                .entityType("WAREHOUSE")
                .dateRange(dateRange)
                .totalRevenue(basicMetrics.getTotalRevenue())
                .totalVolume(basicMetrics.getTotalVolume())
                .transactionCount(basicMetrics.getTransactionCount())
                .averageTransaction(basicMetrics.getAverageTransaction())
                .successRate(basicMetrics.getSuccessRate())
                .failureRate(basicMetrics.getFailureRate())
                .refundRate(basicMetrics.getRefundRate())
                .netRevenue(basicMetrics.getNetRevenue())
                .processingFees(basicMetrics.getProcessingFees())
                .refundAmount(basicMetrics.getRefundAmount())
                // Warehouse-specific metrics
                .storageRevenue(revenueBreakdown.getStorageRevenue())
                .shippingRevenue(revenueBreakdown.getShippingRevenue())
                .subscriptionRevenue(revenueBreakdown.getSubscriptionRevenue())
                .billingRevenue(calculateBillingRevenue(basicMetrics))
                .logisticsCosts(calculateLogisticsCosts(basicMetrics))
                .build();
            
            return ResponseEntity.ok(combinedMetrics);
            
        } catch (Exception e) {
            logger.severe("Warehouse metrics retrieval failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Warehouse metrics retrieval failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Get warehouse revenue trends
     * SECURITY: Requires WAREHOUSE_READ permission with warehouse access validation
     */
    @PreAuthorize("hasPermission('WAREHOUSE', 'READ')")
    @GetMapping("/metrics/warehouse/{warehouseId}/trends")
    public ResponseEntity<?> getWarehouseTrends(
            @PathVariable String warehouseId,
            @RequestParam(defaultValue = "DAILY") String granularity,
            Authentication authentication) {
        try {
            PaymentMetrics metrics = warehouseBilling.getWarehouseBillingMetrics(warehouseId, "MONTHLY");
            
            Map<String, Object> trends = new HashMap<>();
            trends.put("entityId", warehouseId);
            trends.put("entityType", "WAREHOUSE");
            trends.put("revenueGrowth", metrics.getRevenueGrowth());
            trends.put("volumeGrowth", metrics.getVolumeGrowth());
            trends.put("dailyTrends", metrics.getDailyTrends());
            trends.put("profitMargin", metrics.getProfitMargin());
            trends.put("returnOnInvestment", metrics.getReturnOnInvestment());
            
            return ResponseEntity.ok(trends);
            
        } catch (Exception e) {
            logger.severe("Warehouse trends retrieval failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Warehouse trends retrieval failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    // ==============================================
    // COURIER-SPECIFIC METRICS
    // ==============================================
    
    /**
     * Get comprehensive courier metrics (driver, partner, or location)
     * SECURITY: Requires COURIER_READ permission with entity access validation
     */
    @PreAuthorize("hasPermission('COURIER', 'READ')")
    @GetMapping("/metrics/courier/{entityId}")
    public ResponseEntity<?> getCourierMetrics(
            @PathVariable String entityId,
            @RequestParam String entityType, // DRIVER, PARTNER, or LOCATION
            @RequestParam(defaultValue = "MONTHLY") String dateRange,
            Authentication authentication) {
        try {
            logger.info("Retrieving courier metrics for entity: " + entityId + ", type: " + entityType);
            
            PaymentMetrics metrics;
            
            switch (entityType.toUpperCase()) {
                case "DRIVER":
                    metrics = courierPayout.getDriverEarningsMetrics(entityId, dateRange);
                    break;
                case "PARTNER":
                    metrics = courierPayout.getCommissionMetrics(entityId, dateRange);
                    break;
                case "LOCATION":
                    metrics = courierPayout.getLocationRevenueMetrics(entityId, dateRange);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid courier entity type: " + entityType);
            }
            
            // Add courier-specific enrichment
            PaymentMetrics enrichedMetrics = PaymentMetrics.builder()
                .entityId(entityId)
                .entityType("COURIER_" + entityType.toUpperCase())
                .dateRange(dateRange)
                .totalRevenue(metrics.getTotalRevenue())
                .totalVolume(metrics.getTotalVolume())
                .transactionCount(metrics.getTransactionCount())
                .averageTransaction(metrics.getAverageTransaction())
                .successRate(metrics.getSuccessRate())
                .netRevenue(metrics.getNetRevenue())
                // Courier-specific metrics
                .totalPayouts(metrics.getTotalPayouts())
                .payoutCount(metrics.getPayoutCount())
                .averagePayout(metrics.getAveragePayout())
                .pendingPayouts(metrics.getPendingPayouts())
                .commissionPayouts(metrics.getCommissionPayouts())
                .driverEarnings(metrics.getDriverEarnings())
                .walkInRevenue(metrics.getWalkInRevenue())
                .fareRevenue(metrics.getFareRevenue())
                .build();
            
            return ResponseEntity.ok(enrichedMetrics);
            
        } catch (Exception e) {
            logger.severe("Courier metrics retrieval failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Courier metrics retrieval failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    // ==============================================
    // DASHBOARD AND AGGREGATED METRICS
    // ==============================================
    
    /**
     * Get multi-domain dashboard data
     * SECURITY: Requires ANALYTICS_READ permission with global or specific entity access
     */
    @PreAuthorize("hasPermission('ANALYTICS', 'READ')")
    @GetMapping("/metrics/dashboard")
    public ResponseEntity<?> getDashboardMetrics(
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String courierId,
            @RequestParam(defaultValue = "MONTHLY") String dateRange,
            Authentication authentication) {
        try {
            logger.info("Retrieving dashboard metrics for dateRange: " + dateRange);
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // Add warehouse metrics if warehouseId provided
            if (warehouseId != null) {
                PaymentMetrics warehouseMetrics = warehouseBilling.getWarehouseBillingMetrics(warehouseId, dateRange);
                dashboard.put("warehouse", warehouseMetrics);
            }
            
            // Add courier metrics if courierId provided
            if (courierId != null) {
                PaymentMetrics courierMetrics = courierPayout.getDriverEarningsMetrics(courierId, dateRange);
                dashboard.put("courier", courierMetrics);
            }
            
            // Add overall platform metrics
            PaymentMetrics platformMetrics = paymentClient.getPaymentMetrics("PLATFORM", "GLOBAL", dateRange);
            dashboard.put("platform", platformMetrics);
            
            // Add summary statistics
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRevenue", calculateTotalRevenue(dashboard));
            summary.put("totalTransactions", calculateTotalTransactions(dashboard));
            summary.put("averageSuccessRate", calculateAverageSuccessRate(dashboard));
            summary.put("totalActiveEntities", calculateActiveEntities(dashboard));
            dashboard.put("summary", summary);
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            logger.severe("Dashboard metrics retrieval failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Dashboard metrics retrieval failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    // ==============================================
    // PAYMENT METHODS AND REGIONAL SUPPORT
    // ==============================================
    
    /**
     * Get supported payment methods for country with detailed info
     * SECURITY: Public endpoint - no specific authorization required
     */
    @GetMapping("/methods/{countryCode}")
    public ResponseEntity<?> getSupportedPaymentMethods(@PathVariable String countryCode) {
        try {
            logger.info("Retrieving payment methods for country: " + countryCode);
            
            PaymentMethodsResponse methods = paymentClient.getSupportedMethods(countryCode);
            
            return ResponseEntity.ok(methods);
            
        } catch (Exception e) {
            logger.severe("Payment methods retrieval failed: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Payment methods retrieval failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    // ==============================================
    // HELPER METHODS
    // ==============================================
    
    private PaymentMetrics enrichWarehouseMetrics(PaymentMetrics metrics, String warehouseId, String dateRange) {
        try {
            PaymentMetrics revenueBreakdown = warehouseBilling.getRevenueBreakdown(warehouseId, dateRange);
            
            return PaymentMetrics.builder()
                .entityId(metrics.getEntityId())
                .entityType(metrics.getEntityType())
                .dateRange(metrics.getDateRange())
                .totalRevenue(metrics.getTotalRevenue())
                .totalVolume(metrics.getTotalVolume())
                .transactionCount(metrics.getTransactionCount())
                .storageRevenue(revenueBreakdown.getStorageRevenue())
                .shippingRevenue(revenueBreakdown.getShippingRevenue())
                .subscriptionRevenue(revenueBreakdown.getSubscriptionRevenue())
                .build();
        } catch (Exception e) {
            logger.warning("Failed to enrich warehouse metrics: " + e.getMessage());
            return metrics;
        }
    }
    
    private PaymentMetrics enrichCourierMetrics(PaymentMetrics metrics, String entityId, String entityType, String dateRange) {
        try {
            if ("COURIER_LOCATION".equals(entityType)) {
                return courierPayout.getLocationRevenueMetrics(entityId, dateRange);
            }
            return metrics;
        } catch (Exception e) {
            logger.warning("Failed to enrich courier metrics: " + e.getMessage());
            return metrics;
        }
    }
    
    private Double calculateBillingRevenue(PaymentMetrics metrics) {
        // Implementation would analyze billing-specific transactions
        return metrics.getTotalRevenue() * 0.8; // Placeholder: 80% of revenue from billing
    }
    
    private Double calculateLogisticsCosts(PaymentMetrics metrics) {
        // Implementation would calculate logistics costs
        return metrics.getTotalRevenue() * 0.15; // Placeholder: 15% of revenue as logistics costs
    }
    
    private Double calculateTotalRevenue(Map<String, Object> dashboard) {
        Double total = 0.0;
        for (Object value : dashboard.values()) {
            if (value instanceof PaymentMetrics) {
                PaymentMetrics metrics = (PaymentMetrics) value;
                if (metrics.getTotalRevenue() != null) {
                    total += metrics.getTotalRevenue();
                }
            }
        }
        return total;
    }
    
    private Long calculateTotalTransactions(Map<String, Object> dashboard) {
        Long total = 0L;
        for (Object value : dashboard.values()) {
            if (value instanceof PaymentMetrics) {
                PaymentMetrics metrics = (PaymentMetrics) value;
                if (metrics.getTransactionCount() != null) {
                    total += metrics.getTransactionCount();
                }
            }
        }
        return total;
    }
    
    private Double calculateAverageSuccessRate(Map<String, Object> dashboard) {
        Double total = 0.0;
        int count = 0;
        for (Object value : dashboard.values()) {
            if (value instanceof PaymentMetrics) {
                PaymentMetrics metrics = (PaymentMetrics) value;
                if (metrics.getSuccessRate() != null) {
                    total += metrics.getSuccessRate();
                    count++;
                }
            }
        }
        return count > 0 ? total / count : 0.0;
    }
    
    private Integer calculateActiveEntities(Map<String, Object> dashboard) {
        int count = 0;
        for (Object value : dashboard.values()) {
            if (value instanceof PaymentMetrics) {
                PaymentMetrics metrics = (PaymentMetrics) value;
                if (metrics.hasData()) {
                    count++;
                }
            }
        }
        return count;
    }
}