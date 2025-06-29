package com.exalt.ecosystem.socialcommerce.paymentgateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Custom Permission Evaluator for RBAC Implementation
 * 
 * SECURITY FIX: RBAC Implementation (CVSS 9.3)
 * - Implements fine-grained permission evaluation
 * - Supports domain-aware authorization
 * - Prevents privilege escalation attacks
 * - Provides comprehensive audit logging
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    private static final Logger logger = Logger.getLogger(CustomPermissionEvaluator.class.getName());
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private RoleHierarchyService roleHierarchyService;
    
    /**
     * Evaluate permission for specific target object
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warning("Permission check failed: User not authenticated");
            return false;
        }
        
        String username = authentication.getName();
        String permissionStr = permission.toString();
        
        boolean hasPermission = evaluatePermission(authentication, permissionStr, null, null);
        
        logger.info(String.format("Permission check for user '%s', permission '%s': %s", 
            username, permissionStr, hasPermission ? "GRANTED" : "DENIED"));
            
        return hasPermission;
    }
    
    /**
     * Evaluate permission for specific resource type and action
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warning("Permission check failed: User not authenticated");
            return false;
        }
        
        String username = authentication.getName();
        String resource = targetType;
        String action = permission.toString();
        String resourceId = targetId != null ? targetId.toString() : null;
        
        boolean hasPermission = evaluatePermission(authentication, null, resource, action);
        
        // Additional context-specific checks
        if (hasPermission && resourceId != null) {
            hasPermission = evaluateResourceAccess(authentication, resource, resourceId);
        }
        
        logger.info(String.format("Permission check for user '%s', resource '%s', action '%s', id '%s': %s", 
            username, resource, action, resourceId, hasPermission ? "GRANTED" : "DENIED"));
            
        return hasPermission;
    }
    
    /**
     * Core permission evaluation logic
     */
    private boolean evaluatePermission(Authentication authentication, String permissionName, String resource, String action) {
        try {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String username = authentication.getName();
            
            // Check for super admin role (bypass all checks)
            if (hasRole(authorities, "SUPER_ADMIN")) {
                logger.fine("Super admin access granted for user: " + username);
                return true;
            }
            
            // Build permission string if not provided
            if (permissionName == null && resource != null && action != null) {
                permissionName = resource.toUpperCase() + "_" + action.toUpperCase();
            }
            
            // Check direct permission authority
            if (permissionName != null) {
                String permAuthority = "PERM_" + permissionName.replace("_", "_").toUpperCase();
                if (hasAuthority(authorities, permAuthority)) {
                    return true;
                }
                
                // Check domain-specific permission
                String[] parts = permissionName.split("_");
                if (parts.length >= 2) {
                    String resourcePart = parts[0];
                    String actionPart = parts[1];
                    
                    // Check all domain variations
                    String[] domains = {"SOCIAL_COMMERCE", "WAREHOUSING", "COURIER_SERVICES", "GLOBAL"};
                    for (String domain : domains) {
                        String domainPermAuthority = String.format("PERM_%s_%s_%s", domain, resourcePart, actionPart);
                        if (hasAuthority(authorities, domainPermAuthority)) {
                            return true;
                        }
                    }
                }
            }
            
            // Check role-based access
            if (resource != null && action != null) {
                return evaluateRoleBasedAccess(authorities, resource, action);
            }
            
            return false;
            
        } catch (Exception e) {
            logger.severe("Error evaluating permission: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Evaluate role-based access for resource and action
     */
    private boolean evaluateRoleBasedAccess(Collection<? extends GrantedAuthority> authorities, String resource, String action) {
        // Payment-specific role checks
        if ("PAYMENT".equals(resource)) {
            switch (action.toUpperCase()) {
                case "PROCESS":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COMMERCE_ADMIN", "COMMERCE_MANAGER", "VENDOR");
                case "REFUND":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COMMERCE_ADMIN", "COMMERCE_MANAGER");
                case "READ":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COMMERCE_ADMIN", "COMMERCE_MANAGER", 
                                     "COMMERCE_ANALYST", "VENDOR");
                case "CAPTURE":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COMMERCE_ADMIN", "COMMERCE_MANAGER");
            }
        }
        
        // Analytics-specific role checks
        if ("ANALYTICS".equals(resource)) {
            switch (action.toUpperCase()) {
                case "READ":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COMMERCE_ADMIN", "COMMERCE_MANAGER", 
                                     "COMMERCE_ANALYST", "WAREHOUSE_ADMIN", "WAREHOUSE_ANALYST", 
                                     "COURIER_ADMIN", "COURIER_ANALYST");
            }
        }
        
        // Warehouse-specific role checks
        if ("WAREHOUSE".equals(resource)) {
            switch (action.toUpperCase()) {
                case "READ":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "WAREHOUSE_ADMIN", "WAREHOUSE_MANAGER", 
                                     "WAREHOUSE_ANALYST", "BILLING_MANAGER");
                case "CREATE":
                case "UPDATE":
                case "DELETE":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "WAREHOUSE_ADMIN", "WAREHOUSE_MANAGER");
            }
        }
        
        // Courier-specific role checks
        if ("COURIER".equals(resource)) {
            switch (action.toUpperCase()) {
                case "READ":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COURIER_ADMIN", "FLEET_MANAGER", 
                                     "COURIER_ANALYST", "PAYOUT_MANAGER");
                case "CREATE":
                case "UPDATE":
                case "DELETE":
                    return hasAnyRole(authorities, "PLATFORM_ADMIN", "COURIER_ADMIN", "FLEET_MANAGER");
            }
        }
        
        return false;
    }
    
    /**
     * Evaluate resource-specific access (ownership, team membership, etc.)
     */
    private boolean evaluateResourceAccess(Authentication authentication, String resource, String resourceId) {
        String username = authentication.getName();
        
        try {
            // Payment ownership checks
            if ("PAYMENT".equals(resource)) {
                return permissionService.isPaymentOwner(username, resourceId) ||
                       hasGlobalAccess(authentication.getAuthorities());
            }
            
            // Warehouse access checks
            if ("WAREHOUSE".equals(resource)) {
                return permissionService.hasWarehouseAccess(username, resourceId) ||
                       hasGlobalAccess(authentication.getAuthorities());
            }
            
            // Courier entity access checks
            if ("COURIER".equals(resource)) {
                return permissionService.hasCourierAccess(username, resourceId) ||
                       hasGlobalAccess(authentication.getAuthorities());
            }
            
            return true; // Default allow if no specific restrictions
            
        } catch (Exception e) {
            logger.severe("Error evaluating resource access: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has specific role
     */
    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream()
                         .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Check if user has any of the specified roles
     */
    private boolean hasAnyRole(Collection<? extends GrantedAuthority> authorities, String... roles) {
        for (String role : roles) {
            if (hasRole(authorities, role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has specific authority
     */
    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        return authorities.stream()
                         .anyMatch(auth -> auth.getAuthority().equals(authority));
    }
    
    /**
     * Check if user has global access (admin roles)
     */
    private boolean hasGlobalAccess(Collection<? extends GrantedAuthority> authorities) {
        return hasAnyRole(authorities, "SUPER_ADMIN", "PLATFORM_ADMIN");
    }
}