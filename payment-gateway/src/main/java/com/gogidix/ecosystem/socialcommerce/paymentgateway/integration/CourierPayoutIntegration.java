package com.gogidix.ecosystem.socialcommerce.paymentgateway.integration;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.client.UnifiedPaymentClient;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Courier Payout Integration Service
 * 
 * CROSS-DOMAIN PAYMENT INTEGRATION FOR COURIER SERVICES
 * - Driver earnings and commission payouts
 * - Walk-in payment processing at courier locations
 * - International shipping payments
 * - Courier partner commission management
 * - Fare calculation and payment processing
 * 
 * INTEGRATION POINTS:
 * - courier-services/payout-service
 * - courier-services/commission-service
 * - courier-services/courier-network-locations
 * - courier-services/international-shipping
 * - courier-services/courier-fare-calculator
 */
@Service
public class CourierPayoutIntegration {
    
    private static final Logger logger = Logger.getLogger(CourierPayoutIntegration.class.getName());
    
    @Autowired
    private UnifiedPaymentClient paymentClient;
    
    // ==============================================
    // COURIER DRIVER PAYOUT SERVICES
    // ==============================================
    
    /**
     * Process courier driver earnings payout
     * Called by: courier-services/payout-service
     */
    public PayoutResponse processDriverPayout(String driverId, Double earnings, String currency,
                                             String payoutPeriod, String driverEmail) {
        try {
            logger.info("Processing courier driver payout for driver: " + driverId + ", amount: " + earnings);
            
            PayoutRequest request = PayoutRequest.builder()
                .vendorId(driverId)
                .amount(earnings)
                .currency(currency)
                .description("Courier driver earnings payout - Period: " + payoutPeriod)
                .payoutType("DRIVER_EARNINGS")
                .build();
                
            PayoutResponse response = paymentClient.initiatePayout(request);
            
            logger.info("Courier driver payout processed successfully: " + response.getPayoutId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Courier driver payout failed: " + e.getMessage());
            throw new RuntimeException("Driver payout failed", e);
        }
    }
    
    /**
     * Process courier partner commission payout
     * Called by: courier-services/commission-service
     */
    public PayoutResponse processPartnerCommission(String partnerId, Double commission, String currency,
                                                  String commissionPeriod, String partnerEmail) {
        try {
            logger.info("Processing courier partner commission for partner: " + partnerId + ", amount: " + commission);
            
            PayoutRequest request = PayoutRequest.builder()
                .vendorId(partnerId)
                .amount(commission)
                .currency(currency)
                .description("Courier partner commission - Period: " + commissionPeriod)
                .payoutType("PARTNER_COMMISSION")
                .build();
                
            PayoutResponse response = paymentClient.initiatePayout(request);
            
            logger.info("Courier partner commission processed successfully: " + response.getPayoutId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Courier partner commission failed: " + e.getMessage());
            throw new RuntimeException("Partner commission failed", e);
        }
    }
    
    // ==============================================
    // WALK-IN PAYMENT SERVICES
    // ==============================================
    
    /**
     * Process walk-in payment at courier location
     * Called by: courier-services/courier-network-locations
     */
    public PaymentResponse processWalkInPayment(String locationId, String customerId, Double amount, 
                                               String currency, String paymentMethod, String serviceType) {
        try {
            logger.info("Processing walk-in payment at location: " + locationId + ", service: " + serviceType);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("location_id", locationId);
            metadata.put("service_type", serviceType);
            metadata.put("payment_type", "WALK_IN");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("WALKIN_" + locationId + "_" + System.currentTimeMillis())
                .customerId(customerId)
                .customerEmail(customerId + "@walkin.local")
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .description("Walk-in payment - Service: " + serviceType + ", Location: " + locationId)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Walk-in payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Walk-in payment failed: " + e.getMessage());
            throw new RuntimeException("Walk-in payment failed", e);
        }
    }
    
    /**
     * Process courier pickup payment
     * Called by: courier-services/courier-pickup-engine
     */
    public PaymentResponse processPickupPayment(String pickupId, String customerId, String customerEmail,
                                               Double pickupFee, String currency, String pickupLocation) {
        try {
            logger.info("Processing courier pickup payment for pickup: " + pickupId);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("pickup_id", pickupId);
            metadata.put("pickup_location", pickupLocation);
            metadata.put("payment_type", "PICKUP_FEE");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("PICKUP_" + pickupId)
                .customerId(customerId)
                .customerEmail(customerEmail)
                .amount(pickupFee)
                .currency(currency)
                .description("Courier pickup service - Location: " + pickupLocation)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Courier pickup payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Courier pickup payment failed: " + e.getMessage());
            throw new RuntimeException("Pickup payment failed", e);
        }
    }
    
    // ==============================================
    // INTERNATIONAL SHIPPING SERVICES
    // ==============================================
    
    /**
     * Process international shipping payment
     * Called by: courier-services/international-shipping
     */
    public PaymentResponse processInternationalShipping(String shipmentId, String customerId, String customerEmail,
                                                       Double shippingCost, String currency, String fromCountry, 
                                                       String toCountry, String courierPartner) {
        try {
            logger.info("Processing international shipping payment from " + fromCountry + " to " + toCountry);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("shipment_id", shipmentId);
            metadata.put("from_country", fromCountry);
            metadata.put("to_country", toCountry);
            metadata.put("courier_partner", courierPartner);
            metadata.put("payment_type", "INTERNATIONAL_SHIPPING");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("INTL_SHIPPING_" + shipmentId)
                .customerId(customerId)
                .customerEmail(customerEmail)
                .amount(shippingCost)
                .currency(currency)
                .description("International shipping: " + fromCountry + " → " + toCountry + " via " + courierPartner)
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("International shipping payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("International shipping payment failed: " + e.getMessage());
            throw new RuntimeException("International shipping payment failed", e);
        }
    }
    
    // ==============================================
    // FARE CALCULATION AND PAYMENT
    // ==============================================
    
    /**
     * Process calculated fare payment
     * Called by: courier-services/courier-fare-calculator
     */
    public PaymentResponse processFarePayment(String rideId, String customerId, String customerEmail,
                                             Double fareAmount, String currency, String fromLocation, 
                                             String toLocation, Double distance) {
        try {
            logger.info("Processing fare payment for ride: " + rideId + ", distance: " + distance + "km");
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("ride_id", rideId);
            metadata.put("from_location", fromLocation);
            metadata.put("to_location", toLocation);
            metadata.put("distance", distance.toString());
            metadata.put("payment_type", "FARE_PAYMENT");
            
            PaymentRequest request = PaymentRequest.builder()
                .orderId("FARE_" + rideId)
                .customerId(customerId)
                .customerEmail(customerEmail)
                .amount(fareAmount)
                .currency(currency)
                .description("Courier fare: " + fromLocation + " → " + toLocation + " (" + distance + "km)")
                .metadata(metadata)
                .build();
                
            PaymentResponse response = paymentClient.processPayment(request);
            
            logger.info("Fare payment processed successfully: " + response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Fare payment failed: " + e.getMessage());
            throw new RuntimeException("Fare payment failed", e);
        }
    }
    
    // ==============================================
    // COURIER REFUNDS AND ADJUSTMENTS
    // ==============================================
    
    /**
     * Process courier service refund
     */
    public RefundResponse processCourierRefund(String transactionId, Double refundAmount, String reason, String refundType) {
        try {
            logger.info("Processing courier service refund for transaction: " + transactionId + ", type: " + refundType);
            
            RefundRequest request = RefundRequest.builder()
                .transactionId(transactionId)
                .amount(refundAmount)
                .reason(reason)
                .build();
                
            RefundResponse response = paymentClient.refundPayment(request);
            
            logger.info("Courier service refund processed successfully: " + response.getRefundId());
            return response;
            
        } catch (Exception e) {
            logger.severe("Courier service refund failed: " + e.getMessage());
            throw new RuntimeException("Courier refund failed", e);
        }
    }
    
    // ==============================================
    // COURIER PAYMENT ANALYTICS
    // ==============================================
    
    /**
     * Get courier driver earnings analytics
     */
    public PaymentMetrics getDriverEarningsMetrics(String driverId, String dateRange) {
        try {
            PaymentMetrics metrics = paymentClient.getPaymentMetrics(driverId, "COURIER_DRIVER", dateRange);
            
            logger.info("Retrieved driver earnings metrics for driver: " + driverId);
            return metrics;
            
        } catch (Exception e) {
            logger.warning("Failed to retrieve driver earnings metrics: " + e.getMessage());
            return PaymentMetrics.empty();
        }
    }
    
    /**
     * Get courier location revenue analytics
     */
    public PaymentMetrics getLocationRevenueMetrics(String locationId, String dateRange) {
        try {
            PaymentMetrics metrics = paymentClient.getPaymentMetrics(locationId, "COURIER_LOCATION", dateRange);
            
            // Calculate location-specific revenue breakdowns
            Double walkInRevenue = calculateWalkInRevenue(metrics);
            Double pickupRevenue = calculatePickupRevenue(metrics);
            Double fareRevenue = calculateFareRevenue(metrics);
            
            // Update metrics with courier-specific calculations
            metrics = PaymentMetrics.builder()
                .entityId(locationId)
                .entityType("COURIER_LOCATION")
                .dateRange(dateRange)
                .totalRevenue(metrics.getTotalRevenue())
                .walkInRevenue(walkInRevenue)
                .fareRevenue(fareRevenue)
                .netRevenue(metrics.getNetRevenue())
                .transactionCount(metrics.getTransactionCount())
                .averageTransaction(metrics.getAverageTransaction())
                .successRate(metrics.getSuccessRate())
                .build();
            
            logger.info("Generated location revenue metrics for location: " + locationId);
            return metrics;
            
        } catch (Exception e) {
            logger.warning("Failed to generate location revenue metrics: " + e.getMessage());
            return PaymentMetrics.empty();
        }
    }
    
    /**
     * Get courier commission analytics
     */
    public PaymentMetrics getCommissionMetrics(String partnerId, String dateRange) {
        try {
            PaymentMetrics metrics = paymentClient.getPaymentMetrics(partnerId, "COURIER_PARTNER", dateRange);
            
            logger.info("Retrieved commission metrics for partner: " + partnerId);
            return metrics;
            
        } catch (Exception e) {
            logger.warning("Failed to retrieve commission metrics: " + e.getMessage());
            return PaymentMetrics.empty();
        }
    }
    
    // ==============================================
    // HELPER METHODS
    // ==============================================
    
    private Double calculateWalkInRevenue(PaymentMetrics metrics) {
        // Implementation would analyze transaction metadata for WALK_IN payment type
        return metrics.getTotalRevenue() * 0.3; // Placeholder: 30% of revenue from walk-in payments
    }
    
    private Double calculatePickupRevenue(PaymentMetrics metrics) {
        // Implementation would analyze transaction metadata for PICKUP_FEE payment type
        return metrics.getTotalRevenue() * 0.25; // Placeholder: 25% of revenue from pickup fees
    }
    
    private Double calculateFareRevenue(PaymentMetrics metrics) {
        // Implementation would analyze transaction metadata for FARE_PAYMENT payment type
        return metrics.getTotalRevenue() * 0.45; // Placeholder: 45% of revenue from fare payments
    }
    
    /**
     * Validate courier payout request
     */
    public boolean validatePayoutRequest(String vendorId, Double amount, String currency) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
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
     * Validate walk-in payment request
     */
    public boolean validateWalkInRequest(String locationId, String customerId, Double amount, String paymentMethod) {
        if (locationId == null || locationId.trim().isEmpty()) {
            return false;
        }
        
        if (customerId == null || customerId.trim().isEmpty()) {
            return false;
        }
        
        if (amount == null || amount <= 0) {
            return false;
        }
        
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate driver commission rate based on performance
     */
    public Double calculateDriverCommissionRate(String driverId, PaymentMetrics performanceMetrics) {
        // Base commission rate
        Double baseRate = 0.15; // 15%
        
        // Performance bonuses
        if (performanceMetrics.getSuccessRate() > 0.95) {
            baseRate += 0.02; // 2% bonus for >95% success rate
        }
        
        if (performanceMetrics.getTransactionCount() > 100) {
            baseRate += 0.01; // 1% bonus for >100 transactions
        }
        
        return Math.min(baseRate, 0.20); // Cap at 20%
    }
    
    /**
     * Get courier payment status
     */
    public PaymentStatus getCourierPaymentStatus(String transactionId) {
        try {
            return paymentClient.getPaymentStatus(transactionId);
        } catch (Exception e) {
            logger.severe("Failed to get courier payment status: " + e.getMessage());
            throw new RuntimeException("Payment status check failed", e);
        }
    }
    
    /**
     * Check if payment gateway is available for courier services
     */
    public boolean isCourierPaymentServiceAvailable() {
        try {
            return paymentClient.isGatewayAvailable();
        } catch (Exception e) {
            logger.warning("Courier payment service availability check failed: " + e.getMessage());
            return false;
        }
    }
}