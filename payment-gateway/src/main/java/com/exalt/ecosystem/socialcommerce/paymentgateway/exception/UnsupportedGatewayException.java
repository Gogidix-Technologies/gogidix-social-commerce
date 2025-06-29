package com.exalt.ecosystem.socialcommerce.paymentgateway.exception;

/**
 * Unsupported Gateway Exception
 * 
 * Thrown when attempting to use an unsupported payment gateway
 */
public class UnsupportedGatewayException extends RuntimeException {
    
    public UnsupportedGatewayException(String message) {
        super(message);
    }
    
    public UnsupportedGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}