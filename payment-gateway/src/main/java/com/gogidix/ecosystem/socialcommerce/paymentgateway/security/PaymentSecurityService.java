package com.gogidix.ecosystem.socialcommerce.paymentgateway.security;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.PaymentRequest;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.PayoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Payment Security Service for RBAC Implementation
 * 
 * SECURITY FIX: RBAC Implementation (CVSS 9.3)
 * - Implements domain-specific payment authorization
 * - Validates payment amount limits based on user roles
 * - Prevents privilege escalation in payment operations
 * - Provides ownership and team-based access control
 */
@Service("paymentSecurityService")
public class PaymentSecurityService {
    
    private static final Logger logger = Logger.getLogger(PaymentSecurityService.class.getName());
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private RoleHierarchyService roleHierarchyService;
    
    // Payment amount limits by role
    private static final Map<String, Double> PAYMENT_LIMITS = new HashMap<String, Double>() {{
        put("VENDOR", 10000.0);           // $10,000 limit for vendors
        put("CUSTOMER", 5000.0);          // $5,000 limit for customers
        put("WAREHOUSE_STAFF", 1000.0);   // $1,000 limit for warehouse staff
        put("DRIVER", 500.0);             // $500 limit for drivers
        put("COMMERCE_MANAGER", 50000.0); // $50,000 limit for commerce managers
        put("WAREHOUSE_MANAGER", 25000.0);// $25,000 limit for warehouse managers
        put("FLEET_MANAGER", 15000.0);    // $15,000 limit for fleet managers
        put("COMMERCE_ADMIN", 100000.0);  // $100,000 limit for domain admins
        put("WAREHOUSE_ADMIN", 100000.0);
        put("COURIER_ADMIN", 100000.0);
        put("PLATFORM_ADMIN", Double.MAX_VALUE); // No limit for platform admins
        put("SUPER_ADMIN", Double.MAX_VALUE);    // No limit for super admins
    }};
    
    // Payout amount limits by role
    private static final Map<String, Double> PAYOUT_LIMITS = new HashMap<String, Double>() {{
        put("VENDOR_MANAGER", 25000.0);   // $25,000 limit for vendor managers
        put("BILLING_MANAGER", 50000.0);  // $50,000 limit for billing managers
        put("PAYOUT_MANAGER", 75000.0);   // $75,000 limit for payout managers
        put("COMMERCE_ADMIN", 200000.0);  // $200,000 limit for domain admins
        put("WAREHOUSE_ADMIN", 200000.0);
        put("COURIER_ADMIN", 200000.0);
        put("PLATFORM_ADMIN", Double.MAX_VALUE); // No limit for platform admins
        put("SUPER_ADMIN", Double.MAX_VALUE);    // No limit for super admins
    }};
    
    /**
     * Check if user can process a payment request
     */
    public boolean canProcessPayment(Authentication auth, PaymentRequest request) {
        if (auth == null || !auth.isAuthenticated() || request == null) {
            logger.warning("Payment authorization failed: Invalid authentication or request");
            return false;
        }
        
        try {
            String username = auth.getName();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            
            // Check amount limits based on user role
            if (!validatePaymentAmount(authorities, request.getAmount())) {
                logger.warning(String.format("Payment amount limit exceeded for user '%s': amount=%s", 
                    username, request.getAmount()));
                return false;
            }
            
            // Check domain access
            String domain = extractDomainFromOrderId(request.getOrderId());
            if (!validateDomainAccess(authorities, domain)) {
                logger.warning(String.format("Domain access denied for user '%s', domain '%s'", 
                    username, domain));
                return false;
            }
            
            // Additional business logic validation
            if (!validateBusinessRules(auth, request)) {
                logger.warning(String.format("Business rule validation failed for user '%s'", username));
                return false;
            }
            
            logger.info(String.format("Payment authorization granted for user '%s', order '%s', amount=%s", 
                username, request.getOrderId(), request.getAmount()));
            return true;
            
        } catch (Exception e) {
            logger.severe("Error in payment authorization: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user can process a payout request
     */
    public boolean canProcessPayout(Authentication auth, PayoutRequest request) {
        if (auth == null || !auth.isAuthenticated() || request == null) {
            logger.warning("Payout authorization failed: Invalid authentication or request");
            return false;
        }
        
        try {
            String username = auth.getName();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            
            // Check payout amount limits based on user role
            if (!validatePayoutAmount(authorities, request.getAmount())) {
                logger.warning(String.format("Payout amount limit exceeded for user '%s': amount=%s", 
                    username, request.getAmount()));
                return false;
            }
            
            // Check if user can initiate payouts for specific vendor
            if (!validateVendorAccess(username, request.getVendorId())) {
                logger.warning(String.format("Vendor access denied for user '%s', vendor '%s'", 
                    username, request.getVendorId()));
                return false;
            }
            
            logger.info(String.format("Payout authorization granted for user '%s', vendor '%s', amount=%s", 
                username, request.getVendorId(), request.getAmount()));
            return true;
            
        } catch (Exception e) {
            logger.severe("Error in payout authorization: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user can refund a payment
     */
    public boolean canRefundPayment(Authentication auth, String transactionId) {
        if (auth == null || !auth.isAuthenticated() || transactionId == null) {
            return false;
        }
        
        try {
            String username = auth.getName();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            
            // Global refund permission (admin roles)
            if (hasGlobalRefundPermission(authorities)) {
                return true;
            }
            
            // Check ownership of the transaction
            if (permissionService.isPaymentOwner(username, transactionId)) {
                return true;
            }
            
            // Check team or domain-based access
            String domain = extractDomainFromTransactionId(transactionId);
            return validateDomainAccess(authorities, domain) && 
                   hasRole(authorities, domain + "_MANAGER", domain + "_ADMIN");
                   
        } catch (Exception e) {
            logger.severe("Error checking refund authorization: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user can capture a payment
     */
    public boolean canCapturePayment(Authentication auth, String transactionId) {
        if (auth == null || !auth.isAuthenticated() || transactionId == null) {
            return false;
        }
        
        try {
            String username = auth.getName();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            
            // Global capture permission (admin and manager roles)
            if (hasGlobalCapturePermission(authorities)) {
                return true;
            }
            
            // Check ownership of the transaction
            if (permissionService.isPaymentOwner(username, transactionId)) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.severe("Error checking capture authorization: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user can view all payments
     */
    public boolean canViewAllPayments(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        
        // Admin and manager roles can view all payments
        return hasAnyRole(authorities, "SUPER_ADMIN", "PLATFORM_ADMIN", "PLATFORM_ANALYST",
                         "COMMERCE_ADMIN", "COMMERCE_MANAGER", "COMMERCE_ANALYST",
                         "WAREHOUSE_ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_ANALYST",
                         "COURIER_ADMIN", "FLEET_MANAGER", "COURIER_ANALYST");
    }
    
    /**
     * Check if user can view their own payments
     */
    public boolean canViewOwnPayments(Authentication auth, String transactionId) {
        if (auth == null || !auth.isAuthenticated() || transactionId == null) {
            return false;
        }
        
        try {
            String username = auth.getName();
            return permissionService.isPaymentOwner(username, transactionId);
        } catch (Exception e) {
            logger.severe("Error checking payment ownership: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate payment amount against user role limits
     */
    private boolean validatePaymentAmount(Collection<? extends GrantedAuthority> authorities, Double amount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        
        // Find the highest limit the user has based on their roles
        Double maxLimit = 0.0;
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().startsWith("ROLE_")) {
                String role = authority.getAuthority().substring(5); // Remove "ROLE_" prefix
                Double roleLimit = PAYMENT_LIMITS.get(role);
                if (roleLimit != null && roleLimit > maxLimit) {
                    maxLimit = roleLimit;
                }
            }
        }
        
        return amount <= maxLimit;
    }
    
    /**
     * Validate payout amount against user role limits
     */
    private boolean validatePayoutAmount(Collection<? extends GrantedAuthority> authorities, Double amount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        
        // Find the highest payout limit the user has based on their roles
        Double maxLimit = 0.0;
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().startsWith("ROLE_")) {
                String role = authority.getAuthority().substring(5); // Remove "ROLE_" prefix
                Double roleLimit = PAYOUT_LIMITS.get(role);
                if (roleLimit != null && roleLimit > maxLimit) {
                    maxLimit = roleLimit;
                }
            }
        }
        
        return amount <= maxLimit;
    }
    
    /**
     * Extract domain from order ID
     */
    private String extractDomainFromOrderId(String orderId) {
        if (orderId == null) {
            return "UNKNOWN";
        }
        
        if (orderId.startsWith("WAREHOUSE_") || orderId.startsWith("SELF_STORAGE_")) {
            return "WAREHOUSING";
        } else if (orderId.startsWith("WALKIN_") || orderId.startsWith("PICKUP_") || 
                   orderId.startsWith("INTL_SHIPPING_") || orderId.startsWith("FARE_")) {
            return "COURIER_SERVICES";
        } else {
            return "SOCIAL_COMMERCE";
        }
    }
    
    /**
     * Extract domain from transaction ID
     */
    private String extractDomainFromTransactionId(String transactionId) {
        // In production, this would query the database to get the original order ID
        // For now, use similar logic to order ID extraction
        return extractDomainFromOrderId(transactionId);
    }
    
    /**
     * Validate domain access for user
     */
    private boolean validateDomainAccess(Collection<? extends GrantedAuthority> authorities, String domain) {
        // Global roles have access to all domains
        if (hasAnyRole(authorities, "SUPER_ADMIN", "PLATFORM_ADMIN", "PLATFORM_ANALYST")) {
            return true;
        }
        
        // Check domain-specific authorities
        String domainAuthority = "DOMAIN_" + domain.toUpperCase();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(domainAuthority)) {
                return true;
            }
        }
        
        // Check domain-specific roles
        switch (domain) {
            case "SOCIAL_COMMERCE":
                return hasAnyRole(authorities, "COMMERCE_ADMIN", "COMMERCE_MANAGER", "COMMERCE_ANALYST", "VENDOR");
            case "WAREHOUSING":
                return hasAnyRole(authorities, "WAREHOUSE_ADMIN", "WAREHOUSE_MANAGER", "WAREHOUSE_ANALYST", 
                                 "BILLING_MANAGER", "WAREHOUSE_OPERATOR", "WAREHOUSE_STAFF");
            case "COURIER_SERVICES":
                return hasAnyRole(authorities, "COURIER_ADMIN", "FLEET_MANAGER", "COURIER_ANALYST", 
                                 "PAYOUT_MANAGER", "DRIVER_SUPERVISOR", "DRIVER");
            default:
                return false;
        }
    }
    
    /**
     * Validate vendor access for user
     */
    private boolean validateVendorAccess(String username, String vendorId) {
        try {
            // Check if user has global payout permissions or vendor-specific access
            return permissionService.hasPermission(username, "PAYOUT_PROCESS") ||
                   permissionService.hasWarehouseAccess(username, vendorId) ||
                   permissionService.hasCourierAccess(username, vendorId);
        } catch (Exception e) {
            logger.severe("Error validating vendor access: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate business rules for payment
     */
    private boolean validateBusinessRules(Authentication auth, PaymentRequest request) {
        // Add custom business logic validation here
        // For example:
        // - Check if customer has sufficient credit limit
        // - Validate payment method for the region
        // - Check for fraud detection flags
        // - Validate currency and amount combinations
        
        return true; // Default allow if no specific business rules fail
    }
    
    /**
     * Check if user has global refund permission
     */
    private boolean hasGlobalRefundPermission(Collection<? extends GrantedAuthority> authorities) {
        return hasAnyRole(authorities, "SUPER_ADMIN", "PLATFORM_ADMIN", 
                         "COMMERCE_ADMIN", "COMMERCE_MANAGER",
                         "WAREHOUSE_ADMIN", "WAREHOUSE_MANAGER", "BILLING_MANAGER",
                         "COURIER_ADMIN", "FLEET_MANAGER", "PAYOUT_MANAGER");
    }
    
    /**
     * Check if user has global capture permission
     */
    private boolean hasGlobalCapturePermission(Collection<? extends GrantedAuthority> authorities) {
        return hasAnyRole(authorities, "SUPER_ADMIN", "PLATFORM_ADMIN", 
                         "COMMERCE_ADMIN", "COMMERCE_MANAGER",
                         "WAREHOUSE_ADMIN", "WAREHOUSE_MANAGER",
                         "COURIER_ADMIN", "FLEET_MANAGER");
    }
    
    /**
     * Check if user has specific role
     */
    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String... roles) {
        for (String role : roles) {
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if user has any of the specified roles
     */
    private boolean hasAnyRole(Collection<? extends GrantedAuthority> authorities, String... roles) {
        return hasRole(authorities, roles);
    }
}