package com.gogidix.ecosystem.socialcommerce.paymentgateway.exception;

/**
 * Payment Processing Exception
 * 
 * SECURITY IMPLEMENTATION: Secure exception handling
 * - No sensitive information in exception messages
 * - Audit-friendly error tracking
 * - Proper error categorization
 */
public class PaymentProcessingException extends RuntimeException {
    
    private final String errorCode;
    private final String gatewayName;
    
    public PaymentProcessingException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
        this.gatewayName = "UNKNOWN";
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAYMENT_ERROR";
        this.gatewayName = "UNKNOWN";
    }
    
    public PaymentProcessingException(String message, String errorCode, String gatewayName) {
        super(message);
        this.errorCode = errorCode;
        this.gatewayName = gatewayName;
    }
    
    public PaymentProcessingException(String message, String errorCode, String gatewayName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.gatewayName = gatewayName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
}