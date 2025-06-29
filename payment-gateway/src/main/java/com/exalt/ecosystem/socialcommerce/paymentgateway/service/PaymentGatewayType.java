package com.exalt.ecosystem.socialcommerce.paymentgateway.service;

/**
 * Payment Gateway Types
 * 
 * Enumeration of supported payment gateways
 */
public enum PaymentGatewayType {
    STRIPE("Stripe", "Europe & Rest of World"),
    PAYSTACK("Paystack", "Africa"),
    PAYPAL("PayPal", "Global - Disabled"),
    SQUARE("Square", "Future Implementation");
    
    private final String displayName;
    private final String region;
    
    PaymentGatewayType(String displayName, String region) {
        this.displayName = displayName;
        this.region = region;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getRegion() {
        return region;
    }
}