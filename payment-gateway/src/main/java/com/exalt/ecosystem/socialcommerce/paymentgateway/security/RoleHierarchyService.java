package com.exalt.ecosystem.socialcommerce.paymentgateway.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Role Hierarchy Service for RBAC Implementation
 * 
 * SECURITY FIX: RBAC Implementation (CVSS 9.3)
 * - Prevents privilege escalation through role hierarchy validation
 * - Manages role inheritance and permission delegation
 * - Implements administrative operation controls
 * - Provides audit trail for role assignments
 */
@Service
public class RoleHierarchyService {
    
    private static final Logger logger = Logger.getLogger(RoleHierarchyService.class.getName());
    
    // Role hierarchy levels (higher number = more privileges)
    private static final Map<String, Integer> ROLE_HIERARCHY = new HashMap<String, Integer>() {{
        put("SUPER_ADMIN", 100);
        put("PLATFORM_ADMIN", 90);
        put("PLATFORM_ANALYST", 85);
        
        // Domain admin roles
        put("COMMERCE_ADMIN", 80);
        put("WAREHOUSE_ADMIN", 80);
        put("COURIER_ADMIN", 80);
        
        // Domain manager roles
        put("COMMERCE_MANAGER", 70);
        put("WAREHOUSE_MANAGER", 70);
        put("FLEET_MANAGER", 70);
        put("BILLING_MANAGER", 70);
        put("PAYOUT_MANAGER", 70);
        
        // Domain analyst roles
        put("COMMERCE_ANALYST", 60);
        put("WAREHOUSE_ANALYST", 60);
        put("COURIER_ANALYST", 60);
        
        // Entity manager roles
        put("VENDOR_MANAGER", 50);
        put("WAREHOUSE_OPERATOR", 50);
        put("DRIVER_SUPERVISOR", 50);
        
        // Entity user roles
        put("VENDOR", 40);
        put("CUSTOMER", 30);
        put("DRIVER", 30);
        put("WAREHOUSE_STAFF", 30);
    }};
    
    // Role domain mapping
    private static final Map<String, String> ROLE_DOMAINS = new HashMap<String, String>() {{
        put("COMMERCE_ADMIN", "SOCIAL_COMMERCE");
        put("COMMERCE_MANAGER", "SOCIAL_COMMERCE");
        put("COMMERCE_ANALYST", "SOCIAL_COMMERCE");
        put("VENDOR_MANAGER", "SOCIAL_COMMERCE");
        put("VENDOR", "SOCIAL_COMMERCE");
        
        put("WAREHOUSE_ADMIN", "WAREHOUSING");
        put("WAREHOUSE_MANAGER", "WAREHOUSING");
        put("WAREHOUSE_ANALYST", "WAREHOUSING");
        put("BILLING_MANAGER", "WAREHOUSING");
        put("WAREHOUSE_OPERATOR", "WAREHOUSING");
        put("WAREHOUSE_STAFF", "WAREHOUSING");
        
        put("COURIER_ADMIN", "COURIER_SERVICES");
        put("FLEET_MANAGER", "COURIER_SERVICES");
        put("COURIER_ANALYST", "COURIER_SERVICES");
        put("PAYOUT_MANAGER", "COURIER_SERVICES");
        put("DRIVER_SUPERVISOR", "COURIER_SERVICES");
        put("DRIVER", "COURIER_SERVICES");
        
        // Global roles
        put("SUPER_ADMIN", "GLOBAL");
        put("PLATFORM_ADMIN", "GLOBAL");
        put("PLATFORM_ANALYST", "GLOBAL");
        put("CUSTOMER", "GLOBAL");
    }};
    
    // Role inheritance mapping (child roles inherit parent permissions)
    private static final Map<String, Set<String>> ROLE_INHERITANCE = new HashMap<String, Set<String>>() {{
        put("SUPER_ADMIN", new HashSet<>(Arrays.asList("PLATFORM_ADMIN", "PLATFORM_ANALYST")));
        put("PLATFORM_ADMIN", new HashSet<>(Arrays.asList("COMMERCE_ADMIN", "WAREHOUSE_ADMIN", "COURIER_ADMIN", "PLATFORM_ANALYST")));
        
        put("COMMERCE_ADMIN", new HashSet<>(Arrays.asList("COMMERCE_MANAGER", "COMMERCE_ANALYST", "VENDOR_MANAGER")));
        put("COMMERCE_MANAGER", new HashSet<>(Arrays.asList("COMMERCE_ANALYST", "VENDOR_MANAGER")));
        put("VENDOR_MANAGER", new HashSet<>(Arrays.asList("VENDOR")));
        
        put("WAREHOUSE_ADMIN", new HashSet<>(Arrays.asList("WAREHOUSE_MANAGER", "WAREHOUSE_ANALYST", "BILLING_MANAGER", "WAREHOUSE_OPERATOR")));
        put("WAREHOUSE_MANAGER", new HashSet<>(Arrays.asList("WAREHOUSE_ANALYST", "WAREHOUSE_OPERATOR", "WAREHOUSE_STAFF")));
        put("BILLING_MANAGER", new HashSet<>(Arrays.asList("WAREHOUSE_ANALYST")));
        put("WAREHOUSE_OPERATOR", new HashSet<>(Arrays.asList("WAREHOUSE_STAFF")));
        
        put("COURIER_ADMIN", new HashSet<>(Arrays.asList("FLEET_MANAGER", "COURIER_ANALYST", "PAYOUT_MANAGER", "DRIVER_SUPERVISOR")));
        put("FLEET_MANAGER", new HashSet<>(Arrays.asList("COURIER_ANALYST", "DRIVER_SUPERVISOR", "DRIVER")));
        put("PAYOUT_MANAGER", new HashSet<>(Arrays.asList("COURIER_ANALYST")));
        put("DRIVER_SUPERVISOR", new HashSet<>(Arrays.asList("DRIVER")));
    }};
    
    /**
     * Check if a user with assignerRole can assign targetRole to another user
     */
    public boolean canAssignRole(String assignerRole, String targetRole) {
        if (assignerRole == null || targetRole == null) {
            logger.warning("Role assignment validation failed: null role provided");
            return false;
        }
        
        try {
            Integer assignerLevel = ROLE_HIERARCHY.get(assignerRole);
            Integer targetLevel = ROLE_HIERARCHY.get(targetRole);
            
            if (assignerLevel == null || targetLevel == null) {
                logger.warning(String.format("Role assignment validation failed: unknown role - assigner: %s, target: %s", 
                    assignerRole, targetRole));
                return false;
            }
            
            // Can only assign roles of lower hierarchy level (prevent privilege escalation)
            // Base roles (CUSTOMER, DRIVER, VENDOR, WAREHOUSE_STAFF) cannot assign any roles
            if (assignerLevel <= 40) {
                return false;
            }
            
            // Can only assign roles of strictly lower hierarchy level
            boolean canAssign = assignerLevel > targetLevel;
            
            // Additional domain checks
            if (canAssign) {
                canAssign = validateDomainAssignment(assignerRole, targetRole);
            }
            
            logger.info(String.format("Role assignment validation - assigner: %s (level %d), target: %s (level %d): %s", 
                assignerRole, assignerLevel, targetRole, targetLevel, canAssign ? "ALLOWED" : "DENIED"));
                
            return canAssign;
            
        } catch (Exception e) {
            logger.severe("Error validating role assignment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a user with userRole can access a resource requiring requiredRole
     */
    public boolean canAccessResource(String userRole, String requiredRole) {
        if (userRole == null || requiredRole == null) {
            return false;
        }
        
        try {
            // Direct role match
            if (userRole.equals(requiredRole)) {
                return true;
            }
            
            // Check hierarchy levels
            Integer userLevel = ROLE_HIERARCHY.get(userRole);
            Integer requiredLevel = ROLE_HIERARCHY.get(requiredRole);
            
            if (userLevel == null || requiredLevel == null) {
                return false;
            }
            
            // Higher level roles can access lower level resources
            if (userLevel >= requiredLevel) {
                return validateDomainAccess(userRole, requiredRole);
            }
            
            // Check role inheritance
            return hasInheritedRole(userRole, requiredRole);
            
        } catch (Exception e) {
            logger.severe("Error checking resource access: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all roles that a user role can inherit/access
     */
    public Set<String> getInheritedRoles(String userRole) {
        Set<String> inheritedRoles = new HashSet<>();
        
        if (userRole == null) {
            return inheritedRoles;
        }
        
        try {
            // Add the user's own role
            inheritedRoles.add(userRole);
            
            // Add directly inherited roles
            Set<String> directInherited = ROLE_INHERITANCE.get(userRole);
            if (directInherited != null) {
                inheritedRoles.addAll(directInherited);
                
                // Recursively add inherited roles
                for (String role : directInherited) {
                    inheritedRoles.addAll(getInheritedRoles(role));
                }
            }
            
            logger.fine(String.format("User role %s inherits roles: %s", userRole, inheritedRoles));
            
        } catch (Exception e) {
            logger.severe("Error getting inherited roles: " + e.getMessage());
        }
        
        return inheritedRoles;
    }
    
    /**
     * Get the domain for a specific role
     */
    public String getRoleDomain(String role) {
        return ROLE_DOMAINS.getOrDefault(role, "UNKNOWN");
    }
    
    /**
     * Check if user has administrative privileges for a domain
     */
    public boolean isDomainAdmin(String userRole, String domain) {
        if (userRole == null || domain == null) {
            return false;
        }
        
        // Global admin roles
        if ("SUPER_ADMIN".equals(userRole) || "PLATFORM_ADMIN".equals(userRole)) {
            return true;
        }
        
        // Domain-specific admin roles
        String roleDomain = getRoleDomain(userRole);
        return domain.equals(roleDomain) && userRole.endsWith("_ADMIN");
    }
    
    /**
     * Validate domain-specific role assignment
     */
    private boolean validateDomainAssignment(String assignerRole, String targetRole) {
        String assignerDomain = getRoleDomain(assignerRole);
        String targetDomain = getRoleDomain(targetRole);
        
        // Global admins can assign any role
        if ("GLOBAL".equals(assignerDomain)) {
            return true;
        }
        
        // Domain admins can assign roles within their domain
        if (assignerDomain.equals(targetDomain)) {
            return true;
        }
        
        // Cross-domain assignment not allowed for non-global admins
        logger.warning(String.format("Cross-domain role assignment denied - assigner domain: %s, target domain: %s", 
            assignerDomain, targetDomain));
        return false;
    }
    
    /**
     * Validate domain-specific resource access
     */
    private boolean validateDomainAccess(String userRole, String requiredRole) {
        String userDomain = getRoleDomain(userRole);
        String requiredDomain = getRoleDomain(requiredRole);
        
        // Global roles can access any domain
        if ("GLOBAL".equals(userDomain)) {
            return true;
        }
        
        // Same domain access allowed
        if (userDomain.equals(requiredDomain)) {
            return true;
        }
        
        // Cross-domain access for compatible roles
        return isCompatibleCrossDomainAccess(userRole, requiredRole);
    }
    
    /**
     * Check if cross-domain access is compatible (e.g., analysts can view across domains)
     */
    private boolean isCompatibleCrossDomainAccess(String userRole, String requiredRole) {
        // Analyst roles can have read access across domains
        if (userRole.endsWith("_ANALYST") && requiredRole.endsWith("_ANALYST")) {
            return true;
        }
        
        // Manager roles can have limited cross-domain access
        if (userRole.endsWith("_MANAGER") && requiredRole.endsWith("_ANALYST")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if user role has inherited the required role
     */
    private boolean hasInheritedRole(String userRole, String requiredRole) {
        Set<String> inheritedRoles = getInheritedRoles(userRole);
        return inheritedRoles.contains(requiredRole);
    }
    
    /**
     * Get all roles at or below a specific hierarchy level
     */
    public Set<String> getRolesAtOrBelowLevel(int maxLevel) {
        Set<String> roles = new HashSet<>();
        
        for (Map.Entry<String, Integer> entry : ROLE_HIERARCHY.entrySet()) {
            if (entry.getValue() <= maxLevel) {
                roles.add(entry.getKey());
            }
        }
        
        return roles;
    }
    
    /**
     * Get role hierarchy level
     */
    public Integer getRoleLevel(String role) {
        return ROLE_HIERARCHY.get(role);
    }
    
    /**
     * Validate if a role is valid for the system
     */
    public boolean isValidRole(String role) {
        return role != null && ROLE_HIERARCHY.containsKey(role);
    }
}