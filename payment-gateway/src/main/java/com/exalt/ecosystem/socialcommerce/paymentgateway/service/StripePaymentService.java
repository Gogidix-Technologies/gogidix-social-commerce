package com.exalt.ecosystem.socialcommerce.paymentgateway.service;

import com.exalt.ecosystem.socialcommerce.paymentgateway.config.PaymentSecurityConfig;
import com.exalt.ecosystem.socialcommerce.paymentgateway.dto.*;
import com.exalt.ecosystem.socialcommerce.paymentgateway.exception.PaymentProcessingException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

/**
 * Stripe Payment Service Implementation
 * 
 * SECURITY IMPLEMENTATION: Stripe payment processing for Europe & Rest of World
 * - Secure API key management
 * - PCI DSS compliant tokenization
 * - SCA (Strong Customer Authentication) support for European payments
 * - Webhook signature validation
 */
@Service
public class StripePaymentService implements PaymentGateway {
    
    private static final Logger logger = Logger.getLogger(StripePaymentService.class.getName());
    
    @Autowired
    private PaymentSecurityConfig securityConfig;
    
    // Supported payment methods
    private static final Set<String> SUPPORTED_PAYMENT_METHODS = Set.of(
        "card",
        "sepa_debit",
        "ideal",
        "bancontact",
        "giropay",
        "sofort",
        "klarna",
        "afterpay_clearpay",
        "alipay",
        "wechat_pay"
    );
    
    // Supported currencies (major ones)
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "CNY", "SEK", "NOK",
        "DKK", "PLN", "CZK", "HUF", "RON", "BGN", "HRK", "RUB", "TRY", "INR",
        "IDR", "ILS", "KRW", "MXN", "MYR", "NZD", "PHP", "SGD", "THB", "ZAR",
        "AED", "SAR", "QAR", "KWD", "BHD", "OMR"
    );
    
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // Validate request
            validatePaymentRequest(request);
            
            // Create payment intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertAmountToSmallestUnit(request.getAmount(), request.getCurrency()))
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription())
                .setReceiptEmail(request.getCustomerEmail())
                .putMetadata("order_id", request.getOrderId())
                .putMetadata("customer_id", request.getCustomerId())
                .putMetadata("region", "EUROPE_REST_OF_WORLD")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            logger.info("Stripe payment intent created: " + paymentIntent.getId());
            
            return PaymentResponse.builder()
                .transactionId(paymentIntent.getId())
                .status(mapStripeStatus(paymentIntent.getStatus()))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .gatewayResponse(paymentIntent.getClientSecret())
                .message("Payment intent created successfully")
                .build();
                
        } catch (StripeException e) {
            logger.severe("Stripe payment error: " + e.getMessage());
            throw new PaymentProcessingException("Stripe payment failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public RefundResponse refundPayment(RefundRequest request) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(request.getTransactionId())
                .setAmount(convertAmountToSmallestUnit(request.getAmount(), request.getCurrency()))
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("refund_reason", request.getReason())
                .build();
            
            Refund refund = Refund.create(params);
            
            logger.info("Stripe refund created: " + refund.getId());
            
            return RefundResponse.builder()
                .refundId(refund.getId())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(refund.getStatus())
                .message("Refund processed successfully")
                .build();
                
        } catch (StripeException e) {
            logger.severe("Stripe refund error: " + e.getMessage());
            throw new PaymentProcessingException("Stripe refund failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CaptureResponse capturePayment(String transactionId, Double amount) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(transactionId);
            
            PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder()
                .setAmountToCapture(convertAmountToSmallestUnit(amount, paymentIntent.getCurrency()))
                .build();
            
            PaymentIntent captured = paymentIntent.capture(params);
            
            logger.info("Stripe payment captured: " + captured.getId());
            
            return CaptureResponse.builder()
                .transactionId(captured.getId())
                .amount(amount)
                .currency(captured.getCurrency().toUpperCase())
                .status(mapStripeStatus(captured.getStatus()))
                .message("Payment captured successfully")
                .build();
                
        } catch (StripeException e) {
            logger.severe("Stripe capture error: " + e.getMessage());
            throw new PaymentProcessingException("Stripe capture failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Webhook.constructEvent(
                payload,
                signature,
                securityConfig.getStripeWebhookSecret()
            );
            return true;
        } catch (SignatureVerificationException e) {
            logger.warning("Stripe webhook signature verification failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.severe("Stripe webhook verification error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public WebhookResponse processWebhook(String payload) {
        try {
            Event event = Webhook.constructEvent(
                payload, 
                "", // Signature already verified in controller
                securityConfig.getStripeWebhookSecret()
            );
            
            // Handle different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailed(event);
                    break;
                case "charge.refunded":
                    handleRefundCompleted(event);
                    break;
                case "payout.paid":
                    handlePayoutCompleted(event);
                    break;
                default:
                    logger.info("Unhandled Stripe event type: " + event.getType());
            }
            
            return WebhookResponse.builder()
                .eventId(event.getId())
                .eventType(event.getType())
                .processed(true)
                .message("Webhook processed successfully")
                .build();
                
        } catch (Exception e) {
            logger.severe("Stripe webhook processing error: " + e.getMessage());
            throw new PaymentProcessingException("Webhook processing failed", e);
        }
    }
    
    @Override
    public PaymentStatus getPaymentStatus(String transactionId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(transactionId);
            
            return PaymentStatus.builder()
                .transactionId(paymentIntent.getId())
                .status(mapStripeStatus(paymentIntent.getStatus()))
                .amount(convertAmountFromSmallestUnit(paymentIntent.getAmount(), paymentIntent.getCurrency()))
                .currency(paymentIntent.getCurrency().toUpperCase())
                .lastUpdated(new Date(paymentIntent.getCreated() * 1000))
                .build();
                
        } catch (StripeException e) {
            logger.severe("Stripe status check error: " + e.getMessage());
            throw new PaymentProcessingException("Status check failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public TokenResponse createPaymentToken(CardDetails cardDetails) {
        try {
            // In production, tokenization happens on the client side using Stripe.js
            // This is for server-side tokenization (testing only)
            
            TokenCreateParams params = TokenCreateParams.builder()
                .setCard(
                    TokenCreateParams.Card.builder()
                        .setNumber(cardDetails.getCardNumber())
                        .setExpMonth(String.valueOf(cardDetails.getExpiryMonth()))
                        .setExpYear(String.valueOf(cardDetails.getExpiryYear()))
                        .setCvc(cardDetails.getCvv())
                        .build()
                )
                .build();
            
            Token token = Token.create(params);
            
            return TokenResponse.builder()
                .token(token.getId())
                .lastFourDigits(token.getCard().getLast4())
                .cardBrand(token.getCard().getBrand())
                .expiryMonth(String.valueOf(token.getCard().getExpMonth()))
                .expiryYear(String.valueOf(token.getCard().getExpYear()))
                .build();
                
        } catch (StripeException e) {
            logger.severe("Stripe tokenization error: " + e.getMessage());
            throw new PaymentProcessingException("Tokenization failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PayoutResponse initiatePayout(PayoutRequest request) {
        try {
            PayoutCreateParams params = PayoutCreateParams.builder()
                .setAmount(convertAmountToSmallestUnit(request.getAmount(), request.getCurrency()))
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription())
                .putMetadata("vendor_id", request.getVendorId())
                .putMetadata("payout_type", request.getPayoutType())
                .build();
            
            Payout payout = Payout.create(params);
            
            logger.info("Stripe payout initiated: " + payout.getId());
            
            return PayoutResponse.builder()
                .payoutId(payout.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(payout.getStatus())
                .estimatedArrival(new Date(payout.getArrivalDate() * 1000))
                .message("Payout initiated successfully")
                .build();
                
        } catch (StripeException e) {
            logger.severe("Stripe payout error: " + e.getMessage());
            throw new PaymentProcessingException("Payout failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return PaymentGatewayType.STRIPE.name();
    }
    
    @Override
    public Set<String> getSupportedPaymentMethods() {
        return new HashSet<>(SUPPORTED_PAYMENT_METHODS);
    }
    
    @Override
    public Set<String> getSupportedCurrencies() {
        return new HashSet<>(SUPPORTED_CURRENCIES);
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Check Stripe API connectivity
            Balance.retrieve();
            return true;
        } catch (Exception e) {
            logger.warning("Stripe availability check failed: " + e.getMessage());
            return false;
        }
    }
    
    // Helper methods
    
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        if (request.getAmount() > securityConfig.getMaxAmountPerTransaction()) {
            throw new PaymentProcessingException(
                "Payment amount exceeds maximum allowed: " + securityConfig.getMaxAmountPerTransaction()
            );
        }
        
        if (!SUPPORTED_CURRENCIES.contains(request.getCurrency().toUpperCase())) {
            throw new PaymentProcessingException(
                "Unsupported currency for Stripe: " + request.getCurrency()
            );
        }
    }
    
    private Long convertAmountToSmallestUnit(Double amount, String currency) {
        // Most currencies use cents (multiply by 100)
        // Some currencies like JPY don't use decimal places
        Set<String> zeroDecimalCurrencies = Set.of("JPY", "KRW", "VND", "CLP", "PYG", "UGX");
        
        if (zeroDecimalCurrencies.contains(currency.toUpperCase())) {
            return amount.longValue();
        } else {
            return Math.round(amount * 100);
        }
    }
    
    private Double convertAmountFromSmallestUnit(Long amount, String currency) {
        Set<String> zeroDecimalCurrencies = Set.of("JPY", "KRW", "VND", "CLP", "PYG", "UGX");
        
        if (zeroDecimalCurrencies.contains(currency.toUpperCase())) {
            return amount.doubleValue();
        } else {
            return amount / 100.0;
        }
    }
    
    private String mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> "COMPLETED";
            case "processing" -> "PROCESSING";
            case "requires_payment_method" -> "PENDING";
            case "requires_confirmation" -> "PENDING";
            case "requires_action" -> "REQUIRES_ACTION";
            case "canceled" -> "CANCELLED";
            default -> "FAILED";
        };
    }
    
    private void handlePaymentSucceeded(Event event) {
        logger.info("Payment succeeded: " + event.getId());
        // Implement business logic for successful payment
    }
    
    private void handlePaymentFailed(Event event) {
        logger.warning("Payment failed: " + event.getId());
        // Implement business logic for failed payment
    }
    
    private void handleRefundCompleted(Event event) {
        logger.info("Refund completed: " + event.getId());
        // Implement business logic for completed refund
    }
    
    private void handlePayoutCompleted(Event event) {
        logger.info("Payout completed: " + event.getId());
        // Implement business logic for completed payout
    }
}