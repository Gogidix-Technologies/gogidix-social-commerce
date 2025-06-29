package com.exalt.ecosystem.socialcommerce.paymentgateway.integration;

import com.exalt.ecosystem.socialcommerce.paymentgateway.client.UnifiedPaymentClient;
import com.exalt.ecosystem.socialcommerce.paymentgateway.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Warehouse Billing Integration Service
 * 
 * CROSS-DOMAIN PAYMENT INTEGRATION FOR WAREHOUSING
 * - Warehouse billing and invoicing payments
 * - Storage fee processing
 * - Logistics cost management
 * - Cross-region shipping payments
 * - Vendor warehouse subscription billing
 * 
 * INTEGRATION POINTS:
 * - warehousing/billing-service
 * - warehousing/warehouse-subscription
 * - warehousing/cross-region-logistics-service
 * - warehousing/self-storage-service
 */
@Service
public class WarehouseBillingIntegration {
    
    private static final Logger logger = Logger.getLogger(WarehouseBillingIntegration.class.getName());
    
    @Autowired
    private UnifiedPaymentClient paymentClient;
    
    // ==============================================
    // WAREHOUSE BILLING SERVICES
    // ==============================================
    
    /**
     * Process warehouse storage billing payment
     * Called by: warehousing/billing-service
     */
    public PaymentResponse processStorageBilling(String warehouseId, String invoiceId, 
                                                Double amount, String currency, String customerEmail) {
        try {
            logger.info("Processing warehouse storage billing for warehouse: " + warehouseId + ", invoice: " + invoiceId);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("warehouse_id", warehouseId);
            metadata.put("invoice_id", invoiceId);
            metadata.put("billing_type", "STORAGE");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("WAREHOUSE_BILLING_" + invoiceId)
                .customerId(warehouseId)
                .customerEmail(customerEmail)
                .amount(amount)
                .currency(currency)
                .description("Warehouse storage billing - Invoice: " + invoiceId)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Warehouse storage billing processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Warehouse storage billing failed: " + e.getMessage());
            throw new RuntimeException("Storage billing failed", e);
        }
    }
    
    /**
     * Process warehouse subscription payment
     * Called by: warehousing/warehouse-subscription
     */
    public PaymentResponse processSubscriptionPayment(String warehouseId, String subscriptionId,
                                                     Double amount, String currency, String customerEmail,
                                                     String subscriptionPlan) {
        try {
            logger.info("Processing warehouse subscription payment for warehouse: " + warehouseId);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("warehouse_id", warehouseId);
            metadata.put("subscription_id", subscriptionId);
            metadata.put("subscription_plan", subscriptionPlan);
            metadata.put("billing_type", "SUBSCRIPTION");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("WAREHOUSE_SUBSCRIPTION_" + subscriptionId)
                .customerId(warehouseId)
                .customerEmail(customerEmail)
                .amount(amount)
                .currency(currency)
                .description("Warehouse subscription - Plan: " + subscriptionPlan)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Warehouse subscription payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Warehouse subscription payment failed: " + e.getMessage());
            throw new RuntimeException("Subscription payment failed", e);
        }
    }
    
    /**
     * Process cross-region shipping payment
     * Called by: warehousing/cross-region-logistics-service
     */
    public PaymentResponse processShippingPayment(String orderId, String warehouseId, String customerEmail,
                                                 Double shippingCost, String currency, String fromRegion, String toRegion) {
        try {
            logger.info("Processing cross-region shipping payment from " + fromRegion + " to " + toRegion);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("warehouse_id", warehouseId);
            metadata.put("order_id", orderId);
            metadata.put("from_region", fromRegion);
            metadata.put("to_region", toRegion);
            metadata.put("billing_type", "SHIPPING");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("WAREHOUSE_SHIPPING_" + orderId)
                .customerId(warehouseId)
                .customerEmail(customerEmail)
                .amount(shippingCost)
                .currency(currency)
                .description("Cross-region shipping: " + fromRegion + " → " + toRegion)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Cross-region shipping payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Cross-region shipping payment failed: " + e.getMessage());
            throw new RuntimeException("Shipping payment failed", e);
        }
    }
    
    /**
     * Process self-storage rental payment
     * Called by: warehousing/self-storage-service
     */
    public PaymentResponse processSelfStoragePayment(String vendorId, String storageId, String customerEmail,
                                                   Double rentalFee, String currency, String storagePeriod) {
        try {
            logger.info("Processing self-storage rental payment for vendor: " + vendorId);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("vendor_id", vendorId);
            metadata.put("storage_id", storageId);
            metadata.put("storage_period", storagePeriod);
            metadata.put("billing_type", "SELF_STORAGE");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("SELF_STORAGE_" + storageId + "_" + System.currentTimeMillis())
                .customerId(vendorId)
                .customerEmail(customerEmail)
                .amount(rentalFee)
                .currency(currency)
                .description("Self-storage rental - Period: " + storagePeriod)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Self-storage rental payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Self-storage rental payment failed: " + e.getMessage());
            throw new RuntimeException("Self-storage payment failed", e);
        }
    }
    
    // ==============================================
    // WAREHOUSE REFUNDS AND ADJUSTMENTS
    // ==============================================
    
    /**
     * Process warehouse billing refund
     */
    public RefundResponse processWarehouseRefund(String transactionId, Double refundAmount, String reason) {
        try {
            logger.info("Processing warehouse billing refund for transaction: " + transactionId);
            
            RefundRequest request = RefundRequest.builder()
                .transactionId(transactionId)
                .amount(refundAmount)
                .reason(reason)
                .build();
                
            RefundResponse response = paymentClient.refundPayment(request);
            
            logger.info("Warehouse billing refund processed successfully: " + response.getRefundId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Warehouse billing refund failed: " + e.getMessage());
            throw new RuntimeException("Warehouse refund failed", e);
        }
    }
    
    // ==============================================
    // WAREHOUSE PAYMENT ANALYTICS
    // ==============================================
    
    /**
     * Get warehouse billing analytics
     */
    public PaymentMetrics getWarehouseBillingMetrics(String warehouseId, String dateRange) {
        try {
            PaymentMetrics metrics = paymentClient.getPaymentMetrics(warehouseId, "WAREHOUSE", dateRange);
            
            logger.info("Retrieved warehouse billing metrics for warehouse: " + warehouseId);
            return metrics;
            
        } catch (Exception e) {
            logger.warning("Failed to retrieve warehouse billing metrics: " + e.getMessage());
            return PaymentMetrics.empty();
        }
    }
    
    /**
     * Get warehouse revenue breakdown by billing type
     */
    public PaymentMetrics getRevenueBreakdown(String warehouseId, String dateRange) {
        try {
            PaymentMetrics metrics = paymentClient.getPaymentMetrics(warehouseId, "WAREHOUSE", dateRange);
            
            // Calculate specific warehouse revenue breakdowns
            Double storageRevenue = calculateStorageRevenue(metrics);
            Double shippingRevenue = calculateShippingRevenue(metrics);
            Double subscriptionRevenue = calculateSubscriptionRevenue(metrics);
            
            // Update metrics with warehouse-specific calculations
            metrics = PaymentMetrics.builder()
                .entityId(warehouseId)
                .entityType("WAREHOUSE")
                .dateRange(dateRange)
                .totalRevenue(metrics.getTotalRevenue())
                .storageRevenue(storageRevenue)
                .shippingRevenue(shippingRevenue)
                .subscriptionRevenue(subscriptionRevenue)
                .netRevenue(metrics.getNetRevenue())
                .transactionCount(metrics.getTransactionCount())
                .averageTransaction(metrics.getAverageTransaction())
                .successRate(metrics.getSuccessRate())
                .build();
            
            logger.info("Generated warehouse revenue breakdown for warehouse: " + warehouseId);
            return metrics;
            
        } catch (Exception e) {
            logger.warning("Failed to generate warehouse revenue breakdown: " + e.getMessage());
            return PaymentMetrics.empty();
        }
    }
    
    // ==============================================
    // HELPER METHODS
    // ==============================================
    
    private Double calculateStorageRevenue(PaymentMetrics metrics) {
        // Implementation would analyze transaction metadata for STORAGE billing type
        return metrics.getTotalRevenue() * 0.4; // Placeholder: 40% of revenue from storage
    }
    
    private Double calculateShippingRevenue(PaymentMetrics metrics) {
        // Implementation would analyze transaction metadata for SHIPPING billing type
        return metrics.getTotalRevenue() * 0.35; // Placeholder: 35% of revenue from shipping
    }
    
    private Double calculateSubscriptionRevenue(PaymentMetrics metrics) {
        // Implementation would analyze transaction metadata for SUBSCRIPTION billing type
        return metrics.getTotalRevenue() * 0.25; // Placeholder: 25% of revenue from subscriptions
    }
    
    /**
     * Validate warehouse billing request
     */
    public boolean validateBillingRequest(String warehouseId, Double amount, String currency) {
        if (warehouseId == null || warehouseId.trim().isEmpty()) {
            return false;
        }
        
        if (amount == null || amount <= 0) {
            return false;
        }
        
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get warehouse payment status
     */
    public PaymentStatus getWarehousePaymentStatus(String transactionId) {
        try {
            return paymentClient.getPaymentStatus(transactionId);
        } catch (Exception e) {
            logger.severe("Failed to get warehouse payment status: " + e.getMessage());
            throw new RuntimeException("Payment status check failed", e);
        }
    }
}