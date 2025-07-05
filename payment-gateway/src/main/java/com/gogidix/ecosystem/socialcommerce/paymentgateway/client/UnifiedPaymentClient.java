package com.gogidix.ecosystem.socialcommerce.paymentgateway.client;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.logging.Logger;

/**
 * Unified Payment Gateway Client
 * 
 * CROSS-DOMAIN PAYMENT INTEGRATION
 * - Provides unified payment interface for all domains
 * - Warehousing: Billing, analytics, logistics payments
 * - Courier Services: Payouts, commissions, walk-in payments
 * - Social Commerce: Product purchases, vendor payouts
 * 
 * BUSINESS BENEFITS:
 * - Single payment integration point
 * - Consistent error handling
 * - Unified security model
 * - Cross-domain payment analytics
 */
@Component
public class UnifiedPaymentClient {
    
    private static final Logger logger = Logger.getLogger(UnifiedPaymentClient.class.getName());
    
    @Value("${payment.gateway.base-url:http://localhost:8086}")
    private String gatewayBaseUrl;
    
    @Value("${payment.gateway.timeout:30000}")
    private int timeout;
    
    private final RestTemplate restTemplate;
    
    public UnifiedPaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    // ==============================================
    // CORE PAYMENT PROCESSING (All Domains)
    // ==============================================
    
    /**
     * Process payment with automatic regional routing
     * Used by: Social Commerce, Warehousing Billing, Courier Walk-in Payments
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            logger.info("Processing payment for order: " + request.getOrderId());
            
            HttpHeaders headers = createHeaders();
            HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                gatewayBaseUrl + "/api/v1/payments/process",
                HttpMethod.POST,
                entity,
                PaymentResponse.class
            );
            
            logger.info("Payment processed successfully: " + response.getBody().getTransactionId());
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.severe("Payment processing failed: " + e.getMessage());
            throw new RuntimeException("Payment failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get payment status
     * Used by: All domains for payment tracking
     */
    public PaymentStatus getPaymentStatus(String transactionId) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<PaymentStatus> response = restTemplate.exchange(
                gatewayBaseUrl + "/api/v1/payments/status/" + transactionId,
                HttpMethod.GET,
                entity,
                PaymentStatus.class
            );
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.severe("Payment status check failed: " + e.getMessage());
            throw new RuntimeException("Status check failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process refund
     * Used by: Social Commerce, Warehousing, Courier Services
     */
    public RefundResponse refundPayment(RefundRequest request) {
        try {
            logger.info("Processing refund for transaction: " + request.getTransactionId());
            
            HttpHeaders headers = createHeaders();
            HttpEntity<RefundRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<RefundResponse> response = restTemplate.exchange(
                gatewayBaseUrl + "/api/v1/payments/refund",
                HttpMethod.POST,
                entity,
                RefundResponse.class
            );
            
            logger.info("Refund processed successfully: " + response.getBody().getRefundId());
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.severe("Refund processing failed: " + e.getMessage());
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
    }
    
    // ==============================================
    // PAYOUT PROCESSING (Warehousing & Courier)
    // ==============================================
    
    /**
     * Initiate vendor/courier payout
     * Used by: Social Commerce vendor payouts, Courier driver payouts, Warehousing partner payments
     */
    public PayoutResponse initiatePayout(PayoutRequest request) {
        try {
            logger.info("Initiating payout for vendor/courier: " + request.getVendorId());
            
            HttpHeaders headers = createHeaders();
            HttpEntity<PayoutRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PayoutResponse> response = restTemplate.exchange(
                gatewayBaseUrl + "/api/v1/payments/payout",
                HttpMethod.POST,
                entity,
                PayoutResponse.class
            );
            
            logger.info("Payout initiated successfully: " + response.getBody().getPayoutId());
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.severe("Payout initiation failed: " + e.getMessage());
            throw new RuntimeException("Payout failed: " + e.getMessage(), e);
        }
    }
    
    // ==============================================
    // SPECIALIZED DOMAIN METHODS
    // ==============================================
    
    /**
     * Process courier commission payment
     * Used by: Courier Services commission-service
     */
    public PayoutResponse processCommissionPayout(String partnerId, Double amount, String currency) {
        PayoutRequest request = PayoutRequest.builder()
            .vendorId(partnerId)
            .amount(amount)
            .currency(currency)
            .description("Courier commission payout")
            .payoutType("COMMISSION")
            .build();
            
        return initiatePayout(request);
    }
    
    /**
     * Process warehouse billing payment
     * Used by: Warehousing billing-service
     */
    public PaymentResponse processWarehouseBilling(String warehouseId, String invoiceId, 
                                                   Double amount, String currency, String customerEmail) {
        PaymentRequest request = PaymentRequest.builder()
            .orderId(invoiceId)
            .customerId(warehouseId)
            .customerEmail(customerEmail)
            .amount(amount)
            .currency(currency)
            .description("Warehouse billing payment")
            .build();
            
        return processPayment(request);
    }
    
    /**
     * Process courier driver payout
     * Used by: Courier Services payout-service
     */
    public PayoutResponse processCourierPayout(String driverId, Double earnings, String currency) {
        PayoutRequest request = PayoutRequest.builder()
            .vendorId(driverId)
            .amount(earnings)
            .currency(currency)
            .description("Courier driver earnings payout")
            .payoutType("EARNINGS")
            .build();
            
        return initiatePayout(request);
    }
    
    /**
     * Process walk-in payment (Courier POS)
     * Used by: Courier Services courier-network-locations
     */
    public PaymentResponse processWalkInPayment(String locationId, String customerId, 
                                               Double amount, String currency, String paymentMethod) {
        PaymentRequest request = PaymentRequest.builder()
            .orderId("WALKIN_" + System.currentTimeMillis())
            .customerId(customerId)
            .customerEmail(customerId + "@walkin.local")
            .amount(amount)
            .currency(currency)
            .paymentMethod(paymentMethod)
            .description("Walk-in payment at location: " + locationId)
            .build();
            
        return processPayment(request);
    }
    
    /**
     * Process shipping cost payment
     * Used by: Warehousing cross-region-logistics-service
     */
    public PaymentResponse processShippingPayment(String orderId, String customerId, String customerEmail,
                                                 Double shippingCost, String currency) {
        PaymentRequest request = PaymentRequest.builder()
            .orderId(orderId)
            .customerId(customerId)
            .customerEmail(customerEmail)
            .amount(shippingCost)
            .currency(currency)
            .description("Shipping cost payment")
            .build();
            
        return processPayment(request);
    }
    
    // ==============================================
    // PAYMENT ANALYTICS (All Domains)
    // ==============================================
    
    /**
     * Get payment metrics for analytics
     * Used by: Warehousing analytics, Social Commerce analytics, Courier analytics
     */
    public PaymentMetrics getPaymentMetrics(String entityId, String entityType, String dateRange) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            String url = String.format("%s/api/v1/payments/metrics?entityId=%s&entityType=%s&dateRange=%s",
                gatewayBaseUrl, entityId, entityType, dateRange);
            
            ResponseEntity<PaymentMetrics> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PaymentMetrics.class
            );
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.warning("Payment metrics retrieval failed: " + e.getMessage());
            return PaymentMetrics.empty();
        }
    }
    
    // ==============================================
    // HELPER METHODS
    // ==============================================
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "UnifiedPaymentClient/1.0");
        return headers;
    }
    
    /**
     * Check gateway availability
     */
    public boolean isGatewayAvailable() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                gatewayBaseUrl + "/actuator/health",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.warning("Payment gateway health check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get supported payment methods for country
     */
    public PaymentMethodsResponse getSupportedMethods(String countryCode) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<PaymentMethodsResponse> response = restTemplate.exchange(
                gatewayBaseUrl + "/api/v1/payments/methods/" + countryCode,
                HttpMethod.GET,
                entity,
                PaymentMethodsResponse.class
            );
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.severe("Payment methods retrieval failed: " + e.getMessage());
            throw new RuntimeException("Cannot get payment methods: " + e.getMessage(), e);
        }
    }
}