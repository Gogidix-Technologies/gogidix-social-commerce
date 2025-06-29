package com.exalt.ecosystem.socialcommerce.paymentgateway.service;

import com.exalt.ecosystem.socialcommerce.paymentgateway.dto.*;
import java.util.Set;

/**
 * Payment Gateway Interface
 * 
 * Common interface for all payment gateway implementations
 * Ensures consistent API across different payment providers
 */
public interface PaymentGateway {
    
    /**
     * Process a payment
     * 
     * @param request Payment request details
     * @return Payment response with transaction details
     */
    PaymentResponse processPayment(PaymentRequest request);
    
    /**
     * Refund a payment
     * 
     * @param request Refund request details
     * @return Refund response with transaction details
     */
    RefundResponse refundPayment(RefundRequest request);
    
    /**
     * Capture a pre-authorized payment
     * 
     * @param transactionId Original transaction ID
     * @param amount Amount to capture
     * @return Capture response
     */
    CaptureResponse capturePayment(String transactionId, Double amount);
    
    /**
     * Verify webhook signature
     * 
     * @param payload Webhook payload
     * @param signature Webhook signature
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature);
    
    /**
     * Process webhook event
     * 
     * @param payload Webhook payload
     * @return Processed webhook response
     */
    WebhookResponse processWebhook(String payload);
    
    /**
     * Get payment status
     * 
     * @param transactionId Transaction ID
     * @return Payment status details
     */
    PaymentStatus getPaymentStatus(String transactionId);
    
    /**
     * Create payment token for card
     * 
     * @param cardDetails Card details for tokenization
     * @return Token response
     */
    TokenResponse createPaymentToken(CardDetails cardDetails);
    
    /**
     * Get gateway name
     * 
     * @return Gateway name (STRIPE, PAYSTACK, etc.)
     */
    String getName();
    
    /**
     * Get supported payment methods
     * 
     * @return Set of supported payment method codes
     */
    Set<String> getSupportedPaymentMethods();
    
    /**
     * Get supported currencies
     * 
     * @return Set of supported currency codes
     */
    Set<String> getSupportedCurrencies();
    
    /**
     * Check if gateway is available
     * 
     * @return true if gateway is operational
     */
    boolean isAvailable();
    
    /**
     * Initialize payout/transfer
     * 
     * @param request Payout request details
     * @return Payout response
     */
    PayoutResponse initiatePayout(PayoutRequest request);
}