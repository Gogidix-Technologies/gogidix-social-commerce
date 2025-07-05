package com.gogidix.socialcommerce.shared.constants;

public final class ApiConstants {
    
    private ApiConstants() {
        // Utility class
    }
    
    public static final String API_VERSION = "/api/v1";
    public static final String HEALTH_ENDPOINT = "/health";
    public static final String METRICS_ENDPOINT = "/metrics";
    
    // Common HTTP Headers
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    // Service Names
    public static final String ORDER_SERVICE = "order-service";
    public static final String PRODUCT_SERVICE = "product-service";
    public static final String PAYMENT_SERVICE = "payment-gateway";
    public static final String USER_SERVICE = "user-service";
    
    // Status Constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
}