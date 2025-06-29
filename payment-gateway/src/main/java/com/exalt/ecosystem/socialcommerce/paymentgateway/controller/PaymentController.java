package com.exalt.ecosystem.socialcommerce.paymentgateway.controller;

import com.exalt.ecosystem.socialcommerce.paymentgateway.dto.*;
import com.exalt.ecosystem.socialcommerce.paymentgateway.service.*;
import com.exalt.ecosystem.socialcommerce.paymentgateway.exception.PaymentProcessingException;
import com.exalt.ecosystem.socialcommerce.paymentgateway.validation.PaymentRequestValidator;
import com.exalt.ecosystem.socialcommerce.paymentgateway.security.InputSanitizer;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Payment Controller
 * 
 * SECURITY IMPLEMENTATION: Unified payment gateway API
 * - Regional payment routing (Africa → Paystack, Rest → Stripe)
 * - Input validation and sanitization
 * - Comprehensive error handling
 * - Audit logging for all transactions
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class PaymentController {
    
    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());
    
    @Autowired
    private RegionalPaymentRouter paymentRouter;
    
    @Autowired
    private PaymentGatewayFactory gatewayFactory;
    
    @Autowired
    private PaymentRequestValidator requestValidator;
    
    @Autowired
    private InputSanitizer inputSanitizer;
    
    /**
     * Process payment with automatic regional routing
     * SECURITY: Requires PAYMENT_PROCESS permission with domain validation
     */
    @PreAuthorize("hasPermission('PAYMENT', 'PROCESS') and @paymentSecurityService.canProcessPayment(authentication, #request)")
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest request, Authentication authentication) {
        try {
            // SECURITY FIX: Comprehensive input validation
            PaymentRequestValidator.ValidationResult validation = requestValidator.validatePaymentRequest(request);
            if (!validation.isValid()) {
                logger.warning("Payment request validation failed: " + validation.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Validation failed",
                        "message", "Invalid payment request data",
                        "details", validation.getErrors()
                    ));
            }
            
            logger.info("Processing payment for order: " + inputSanitizer.sanitizeForLogging(request.getOrderId()) + 
                       ", country: " + inputSanitizer.sanitizeForLogging(request.getCountryCode()));
            
            // Route to appropriate gateway based on country
            PaymentGateway gateway = paymentRouter.routePayment(request.getCountryCode());
            
            // Process payment
            PaymentResponse response = gateway.processPayment(request);
            response.setGateway(gateway.getName());
            
            logger.info("Payment processed successfully: " + inputSanitizer.sanitizeForLogging(response.getTransactionId()));
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentProcessingException e) {
            logger.severe("Payment processing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Payment processing failed",
                    "message", e.getMessage(),
                    "errorCode", e.getErrorCode(),
                    "gateway", e.getGatewayName()
                ));
        } catch (Exception e) {
            logger.severe("Unexpected payment error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Internal server error",
                    "message", "An unexpected error occurred"
                ));
        }
    }
    
    /**
     * Get payment status
     * SECURITY: Requires PAYMENT_READ permission with ownership validation
     */
    @PreAuthorize("hasPermission('PAYMENT', 'READ') and (@paymentSecurityService.canViewAllPayments(authentication) or @paymentSecurityService.canViewOwnPayments(authentication, #transactionId))")
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable String transactionId,
            @RequestParam(required = false) String gateway,
            Authentication authentication) {
        try {
            PaymentGateway paymentGateway;
            
            if (gateway != null) {
                paymentGateway = gatewayFactory.getGateway(gateway);
            } else {
                // Try to determine gateway from transaction ID format
                paymentGateway = determineGatewayFromTransactionId(transactionId);
            }
            
            PaymentStatus status = paymentGateway.getPaymentStatus(transactionId);
            status.setGateway(paymentGateway.getName());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.severe("Payment status check failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Status check failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Process refund
     * SECURITY: Requires PAYMENT_REFUND permission with transaction ownership validation
     */
    @PreAuthorize("hasPermission('PAYMENT', 'REFUND') and @paymentSecurityService.canRefundPayment(authentication, #request.transactionId)")
    @PostMapping("/refund")
    public ResponseEntity<?> processRefund(@Valid @RequestBody RefundRequest request, Authentication authentication) {
        try {
            logger.info("Processing refund for transaction: " + request.getTransactionId());
            
            // Determine gateway from transaction ID
            PaymentGateway gateway = determineGatewayFromTransactionId(request.getTransactionId());
            
            RefundResponse response = gateway.refundPayment(request);
            response.setGateway(gateway.getName());
            
            logger.info("Refund processed successfully: " + response.getRefundId());
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentProcessingException e) {
            logger.severe("Refund processing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Refund processing failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Capture pre-authorized payment
     * SECURITY: Requires PAYMENT_CAPTURE permission with transaction ownership validation
     */
    @PreAuthorize("hasPermission('PAYMENT', 'CAPTURE') and @paymentSecurityService.canCapturePayment(authentication, #transactionId)")
    @PostMapping("/capture/{transactionId}")
    public ResponseEntity<?> capturePayment(
            @PathVariable String transactionId,
            @RequestParam Double amount,
            Authentication authentication) {
        try {
            PaymentGateway gateway = determineGatewayFromTransactionId(transactionId);
            
            CaptureResponse response = gateway.capturePayment(transactionId, amount);
            response.setGateway(gateway.getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Payment capture failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Capture failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Initiate vendor payout
     * SECURITY: Requires PAYOUT_PROCESS permission with domain and amount validation
     */
    @PreAuthorize("hasPermission('PAYOUT', 'PROCESS') and @paymentSecurityService.canProcessPayout(authentication, #request)")
    @PostMapping("/payout")
    public ResponseEntity<?> initiatePayout(@Valid @RequestBody PayoutRequest request, Authentication authentication) {
        try {
            // Route to appropriate gateway based on vendor location
            PaymentGateway gateway = paymentRouter.routePayout(request.getVendorId());
            
            PayoutResponse response = gateway.initiatePayout(request);
            response.setGateway(gateway.getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Payout initiation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Payout failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Webhook endpoint for payment notifications
     */
    @PostMapping("/webhook/{gateway}")
    public ResponseEntity<?> handleWebhook(
            @PathVariable String gateway,
            @RequestHeader("X-Signature") String signature,
            @RequestBody String payload) {
        try {
            PaymentGateway paymentGateway = gatewayFactory.getGateway(gateway);
            
            // Verify webhook signature
            if (!paymentGateway.verifyWebhookSignature(payload, signature)) {
                logger.warning("Invalid webhook signature from " + gateway);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid signature"));
            }
            
            // Process webhook
            WebhookResponse response = paymentGateway.processWebhook(payload);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Webhook processing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Webhook processing failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Get gateway availability status
     */
    @GetMapping("/gateways/status")
    public ResponseEntity<Map<PaymentGatewayType, Boolean>> getGatewayStatus() {
        return ResponseEntity.ok(gatewayFactory.getAvailableGateways());
    }
    
    /**
     * Get supported payment methods for a country
     */
    @GetMapping("/methods/{countryCode}")
    public ResponseEntity<?> getSupportedMethods(@PathVariable String countryCode) {
        try {
            PaymentGateway gateway = paymentRouter.routePayment(countryCode);
            
            return ResponseEntity.ok(Map.of(
                "gateway", gateway.getName(),
                "paymentMethods", gateway.getSupportedPaymentMethods(),
                "currencies", gateway.getSupportedCurrencies()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "Country not supported",
                    "message", e.getMessage()
                ));
        }
    }
    
    // Helper method to determine gateway from transaction ID format
    private PaymentGateway determineGatewayFromTransactionId(String transactionId) {
        if (transactionId.startsWith("pi_") || transactionId.startsWith("ch_")) {
            return gatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        } else if (transactionId.startsWith("PAYSTACK_")) {
            return gatewayFactory.getGateway(PaymentGatewayType.PAYSTACK);
        } else {
            throw new PaymentProcessingException("Unable to determine payment gateway from transaction ID");
        }
    }
}