package com.gogidix.ecosystem.socialcommerce.paymentgateway.service;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.exception.UnsupportedGatewayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Payment Gateway Factory
 * 
 * Factory pattern implementation for managing multiple payment gateways
 * Provides centralized gateway creation and management
 */
@Component
public class PaymentGatewayFactory {
    
    private static final Logger logger = Logger.getLogger(PaymentGatewayFactory.class.getName());
    
    private final Map<PaymentGatewayType, PaymentGateway> gateways = new HashMap<>();
    
    @Autowired
    private StripePaymentService stripePaymentService;
    
    @Autowired
    private PaystackPaymentService paystackPaymentService;
    
    /**
     * Initialize gateway mappings
     */
    @Autowired
    public void initializeGateways() {
        gateways.put(PaymentGatewayType.STRIPE, stripePaymentService);
        gateways.put(PaymentGatewayType.PAYSTACK, paystackPaymentService);
        
        logger.info("Payment gateway factory initialized with " + gateways.size() + " gateways");
    }
    
    /**
     * Get payment gateway by type
     * 
     * @param gatewayType Type of payment gateway
     * @return Payment gateway implementation
     * @throws UnsupportedGatewayException if gateway not supported
     */
    public PaymentGateway getGateway(PaymentGatewayType gatewayType) {
        if (gatewayType == null) {
            throw new IllegalArgumentException("Gateway type cannot be null");
        }
        
        PaymentGateway gateway = gateways.get(gatewayType);
        
        if (gateway == null) {
            throw new UnsupportedGatewayException(
                "Payment gateway not supported: " + gatewayType.name()
            );
        }
        
        // Check if gateway is available
        if (!gateway.isAvailable()) {
            logger.warning("Payment gateway " + gatewayType.name() + " is not available");
            throw new UnsupportedGatewayException(
                "Payment gateway is currently unavailable: " + gatewayType.name()
            );
        }
        
        logger.info("Returning payment gateway: " + gatewayType.name());
        return gateway;
    }
    
    /**
     * Get gateway by name
     * 
     * @param gatewayName Name of the gateway
     * @return Payment gateway implementation
     */
    public PaymentGateway getGateway(String gatewayName) {
        try {
            PaymentGatewayType gatewayType = PaymentGatewayType.valueOf(gatewayName.toUpperCase());
            return getGateway(gatewayType);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedGatewayException(
                "Invalid payment gateway name: " + gatewayName
            );
        }
    }
    
    /**
     * Check if gateway is supported
     * 
     * @param gatewayType Gateway type to check
     * @return true if supported
     */
    public boolean isGatewaySupported(PaymentGatewayType gatewayType) {
        return gateways.containsKey(gatewayType);
    }
    
    /**
     * Get all available gateways
     * 
     * @return Map of available gateways
     */
    public Map<PaymentGatewayType, Boolean> getAvailableGateways() {
        Map<PaymentGatewayType, Boolean> availability = new HashMap<>();
        
        for (Map.Entry<PaymentGatewayType, PaymentGateway> entry : gateways.entrySet()) {
            availability.put(entry.getKey(), entry.getValue().isAvailable());
        }
        
        return availability;
    }
}