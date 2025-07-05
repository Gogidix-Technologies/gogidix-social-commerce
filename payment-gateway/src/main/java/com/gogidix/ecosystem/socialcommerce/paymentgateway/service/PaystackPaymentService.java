package com.gogidix.ecosystem.socialcommerce.paymentgateway.service;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.config.PaymentSecurityConfig;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.*;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.exception.PaymentProcessingException;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.security.InputSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Paystack Payment Service Implementation
 * 
 * SECURITY IMPLEMENTATION: Paystack payment processing for Africa
 * - Secure API key management
 * - Support for African payment methods (Mobile Money, Bank Transfer, USSD)
 * - Multi-currency support (NGN, GHS, ZAR, KES, UGX, USD)
 * - Webhook signature validation
 */
@Service
public class PaystackPaymentService implements PaymentGateway {
    
    private static final Logger logger = Logger.getLogger(PaystackPaymentService.class.getName());
    
    @Autowired
    private PaymentSecurityConfig securityConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private InputSanitizer inputSanitizer;
    
    // Paystack API endpoints
    private static final String INITIALIZE_TRANSACTION_URL = "/transaction/initialize";
    private static final String VERIFY_TRANSACTION_URL = "/transaction/verify/";
    private static final String REFUND_URL = "/refund";
    private static final String TRANSFER_URL = "/transfer";
    private static final String CHARGE_AUTHORIZATION_URL = "/transaction/charge_authorization";
    
    // Supported payment methods
    private static final Set<String> SUPPORTED_PAYMENT_METHODS = Set.of(
        "card",
        "bank",
        "bank_transfer",
        "mobile_money",
        "ussd",
        "qr",
        "eft"
    );
    
    // Supported currencies
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "NGN", // Nigerian Naira
        "GHS", // Ghanaian Cedi
        "ZAR", // South African Rand
        "KES", // Kenyan Shilling
        "UGX", // Ugandan Shilling
        "USD"  // US Dollar
    );
    
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // Validate request
            validatePaymentRequest(request);
            
            // Prepare Paystack request
            Map<String, Object> paystackRequest = new HashMap<>();
            paystackRequest.put("amount", convertAmountToKobo(request.getAmount(), request.getCurrency()));
            paystackRequest.put("currency", request.getCurrency());
            paystackRequest.put("email", request.getCustomerEmail());
            paystackRequest.put("reference", generateReference(request.getOrderId()));
            
            // Add metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("order_id", request.getOrderId());
            metadata.put("customer_id", request.getCustomerId());
            metadata.put("region", "AFRICA");
            metadata.put("description", request.getDescription());
            paystackRequest.put("metadata", metadata);
            
            // Add payment channels based on request
            if (request.getPaymentMethod() != null) {
                List<String> channels = getPaymentChannels(request.getPaymentMethod());
                paystackRequest.put("channels", channels);
            }
            
            // Make API call
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paystackRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                securityConfig.getPaystackBaseUrl() + INITIALIZE_TRANSACTION_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            
            logger.info("Paystack transaction initialized: " + inputSanitizer.sanitizeForLogging((String) responseData.get("reference")));
            
            return PaymentResponse.builder()
                .transactionId((String) responseData.get("reference"))
                .status("PENDING")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .gatewayResponse((String) responseData.get("authorization_url"))
                .message("Payment initialized successfully")
                .build();
                
        } catch (HttpClientErrorException e) {
            logger.severe("Paystack payment error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Paystack payment failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Unexpected payment error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Payment processing failed", e);
        }
    }
    
    @Override
    public RefundResponse refundPayment(RefundRequest request) {
        try {
            Map<String, Object> paystackRequest = new HashMap<>();
            paystackRequest.put("transaction", request.getTransactionId());
            paystackRequest.put("amount", convertAmountToKobo(request.getAmount(), request.getCurrency()));
            paystackRequest.put("currency", request.getCurrency());
            paystackRequest.put("merchant_note", request.getReason());
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paystackRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                securityConfig.getPaystackBaseUrl() + REFUND_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            
            logger.info("Paystack refund initiated: " + inputSanitizer.sanitizeForLogging(String.valueOf(responseData.get("id"))));
            
            return RefundResponse.builder()
                .refundId(String.valueOf(responseData.get("id")))
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status((String) responseData.get("status"))
                .message("Refund initiated successfully")
                .build();
                
        } catch (HttpClientErrorException e) {
            logger.severe("Paystack refund error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Paystack refund failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CaptureResponse capturePayment(String transactionId, Double amount) {
        try {
            // Paystack automatically captures authorized payments
            // This method verifies the transaction status
            PaymentStatus status = getPaymentStatus(transactionId);
            
            if ("COMPLETED".equals(status.getStatus())) {
                return CaptureResponse.builder()
                    .transactionId(transactionId)
                    .amount(status.getAmount())
                    .currency(status.getCurrency())
                    .status("CAPTURED")
                    .message("Payment already captured")
                    .build();
            } else {
                throw new PaymentProcessingException("Payment not in capturable state: " + status.getStatus());
            }
            
        } catch (Exception e) {
            logger.severe("Paystack capture error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Capture failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String computedSignature = computeHmacSha512(
                securityConfig.getPaystackSecretKey(),
                payload
            );
            
            return computedSignature.equals(signature);
            
        } catch (Exception e) {
            logger.severe("Paystack webhook verification error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            return false;
        }
    }
    
    @Override
    public WebhookResponse processWebhook(String payload) {
        try {
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);
            String eventType = (String) event.get("event");
            
            // Handle different event types
            switch (eventType) {
                case "charge.success":
                    handleChargeSuccess(event);
                    break;
                case "charge.failed":
                    handleChargeFailed(event);
                    break;
                case "transfer.success":
                    handleTransferSuccess(event);
                    break;
                case "transfer.failed":
                    handleTransferFailed(event);
                    break;
                case "refund.processed":
                    handleRefundProcessed(event);
                    break;
                default:
                    logger.info("Unhandled Paystack event type: " + eventType);
            }
            
            return WebhookResponse.builder()
                .eventId(String.valueOf(event.get("id")))
                .eventType(eventType)
                .processed(true)
                .message("Webhook processed successfully")
                .build();
                
        } catch (Exception e) {
            logger.severe("Paystack webhook processing error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Webhook processing failed", e);
        }
    }
    
    @Override
    public PaymentStatus getPaymentStatus(String transactionId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                securityConfig.getPaystackBaseUrl() + VERIFY_TRANSACTION_URL + transactionId,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            
            return PaymentStatus.builder()
                .transactionId(transactionId)
                .status(mapPaystackStatus((String) responseData.get("status")))
                .amount(convertAmountFromKobo(
                    ((Number) responseData.get("amount")).longValue(),
                    (String) responseData.get("currency")
                ))
                .currency((String) responseData.get("currency"))
                .lastUpdated(new Date())
                .build();
                
        } catch (HttpClientErrorException e) {
            logger.severe("Paystack status check error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Status check failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public TokenResponse createPaymentToken(CardDetails cardDetails) {
        // Paystack tokenization happens on the client side
        // This method is not applicable for server-side implementation
        throw new UnsupportedOperationException(
            "Card tokenization must be performed on the client side using Paystack.js"
        );
    }
    
    @Override
    public PayoutResponse initiatePayout(PayoutRequest request) {
        try {
            Map<String, Object> paystackRequest = new HashMap<>();
            paystackRequest.put("source", "balance");
            paystackRequest.put("amount", convertAmountToKobo(request.getAmount(), request.getCurrency()));
            paystackRequest.put("currency", request.getCurrency());
            paystackRequest.put("reason", request.getDescription());
            
            // Recipient details
            Map<String, String> recipient = new HashMap<>();
            recipient.put("type", "bank_account");
            recipient.put("account_number", request.getAccountNumber());
            recipient.put("bank_code", request.getBankCode());
            recipient.put("name", request.getAccountName());
            paystackRequest.put("recipient", recipient);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paystackRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                securityConfig.getPaystackBaseUrl() + TRANSFER_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
            
            logger.info("Paystack transfer initiated: " + inputSanitizer.sanitizeForLogging((String) responseData.get("transfer_code")));
            
            return PayoutResponse.builder()
                .payoutId((String) responseData.get("transfer_code"))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status((String) responseData.get("status"))
                .estimatedArrival(new Date())
                .message("Transfer initiated successfully")
                .build();
                
        } catch (HttpClientErrorException e) {
            logger.severe("Paystack payout error: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            throw new PaymentProcessingException("Payout failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return PaymentGatewayType.PAYSTACK.name();
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
            // Check Paystack API connectivity
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                securityConfig.getPaystackBaseUrl() + "/bank",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.warning("Paystack availability check failed: " + inputSanitizer.sanitizeForLogging(e.getMessage()));
            return false;
        }
    }
    
    // Helper methods
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(securityConfig.getPaystackSecretKey());
        return headers;
    }
    
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
                "Unsupported currency for Paystack: " + request.getCurrency()
            );
        }
    }
    
    private Long convertAmountToKobo(Double amount, String currency) {
        // Convert to smallest currency unit (kobo for NGN, pesewas for GHS, cents for others)
        return Math.round(amount * 100);
    }
    
    private Double convertAmountFromKobo(Long amount, String currency) {
        return amount / 100.0;
    }
    
    private String generateReference(String orderId) {
        return "PAYSTACK_" + orderId + "_" + System.currentTimeMillis();
    }
    
    private List<String> getPaymentChannels(String paymentMethod) {
        return switch (paymentMethod.toLowerCase()) {
            case "card" -> List.of("card");
            case "bank" -> List.of("bank", "bank_transfer");
            case "mobile_money" -> List.of("mobile_money");
            case "ussd" -> List.of("ussd");
            case "all" -> List.of("card", "bank", "bank_transfer", "mobile_money", "ussd");
            default -> List.of("card", "bank");
        };
    }
    
    private String mapPaystackStatus(String paystackStatus) {
        return switch (paystackStatus.toLowerCase()) {
            case "success" -> "COMPLETED";
            case "pending" -> "PENDING";
            case "failed" -> "FAILED";
            case "abandoned" -> "CANCELLED";
            case "processing" -> "PROCESSING";
            default -> "UNKNOWN";
        };
    }
    
    private String computeHmacSha512(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8),
            "HmacSHA512"
        );
        hmac.init(secretKeySpec);
        byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    private void handleChargeSuccess(Map<String, Object> event) {
        logger.info("Charge succeeded: " + inputSanitizer.sanitizeForLogging(String.valueOf(event.get("reference"))));
        // Implement business logic for successful charge
    }
    
    private void handleChargeFailed(Map<String, Object> event) {
        logger.warning("Charge failed: " + inputSanitizer.sanitizeForLogging(String.valueOf(event.get("reference"))));
        // Implement business logic for failed charge
    }
    
    private void handleTransferSuccess(Map<String, Object> event) {
        logger.info("Transfer succeeded: " + inputSanitizer.sanitizeForLogging(String.valueOf(event.get("transfer_code"))));
        // Implement business logic for successful transfer
    }
    
    private void handleTransferFailed(Map<String, Object> event) {
        logger.warning("Transfer failed: " + inputSanitizer.sanitizeForLogging(String.valueOf(event.get("transfer_code"))));
        // Implement business logic for failed transfer
    }
    
    private void handleRefundProcessed(Map<String, Object> event) {
        logger.info("Refund processed: " + inputSanitizer.sanitizeForLogging(String.valueOf(event.get("id"))));
        // Implement business logic for processed refund
    }
}