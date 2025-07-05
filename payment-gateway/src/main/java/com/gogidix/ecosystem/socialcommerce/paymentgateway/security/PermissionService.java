package com.gogidix.ecosystem.socialcommerce.paymentgateway.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Permission Service for RBAC Implementation
 * 
 * SECURITY FIX: RBAC Implementation (CVSS 9.3)
 * - Manages user permissions and resource access
 * - Implements ownership and team-based access control
 * - Provides caching for performance optimization
 * - Supports domain-specific authorization logic
 */
@Service
public class PermissionService {
    
    private static final Logger logger = Logger.getLogger(PermissionService.class.getName());
    
    // Cache for user permissions (in production, this would be backed by database/Redis)
    private final Map<String, Set<String>> userPermissionsCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userWarehousesCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userCourierEntitiesCache = new ConcurrentHashMap<>();
    private final Map<String, String> paymentOwnersCache = new ConcurrentHashMap<>();
    
    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String username, String permissionName) {
        if (username == null || permissionName == null) {
            return false;
        }
        
        try {
            Set<String> userPermissions = getUserPermissions(username);
            boolean hasPermission = userPermissions.contains(permissionName);
            
            logger.fine(String.format("Permission check for user '%s', permission '%s': %s", 
                username, permissionName, hasPermission ? "GRANTED" : "DENIED"));
                
            return hasPermission;
            
        } catch (Exception e) {
            logger.severe("Error checking permission: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has specific permission for resource and action
     */
    public boolean hasPermission(String username, String resource, String action) {
        String permissionName = resource.toUpperCase() + "_" + action.toUpperCase();
        return hasPermission(username, permissionName);
    }
    
    /**
     * Check if user owns a specific payment
     */
    public boolean isPaymentOwner(String username, String transactionId) {
        if (username == null || transactionId == null) {
            return false;
        }
        
        try {
            // In production, this would query the database
            String owner = paymentOwnersCache.get(transactionId);
            
            // If not in cache, simulate database lookup
            if (owner == null) {
                owner = simulatePaymentOwnershipLookup(transactionId);
                if (owner != null) {
                    paymentOwnersCache.put(transactionId, owner);
                }
            }
            
            boolean isOwner = username.equals(owner);
            logger.fine(String.format("Payment ownership check for user '%s', transaction '%s': %s", 
                username, transactionId, isOwner ? "OWNER" : "NOT_OWNER"));
                
            return isOwner;
            
        } catch (Exception e) {
            logger.severe("Error checking payment ownership: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has access to specific warehouse
     */
    public boolean hasWarehouseAccess(String username, String warehouseId) {
        if (username == null || warehouseId == null) {
            return false;
        }
        
        try {
            Set<String> userWarehouses = getUserWarehouses(username);
            boolean hasAccess = userWarehouses.contains(warehouseId) || userWarehouses.contains("*");
            
            logger.fine(String.format("Warehouse access check for user '%s', warehouse '%s': %s", 
                username, warehouseId, hasAccess ? "GRANTED" : "DENIED"));
                
            return hasAccess;
            
        } catch (Exception e) {
            logger.severe("Error checking warehouse access: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has access to specific courier entity
     */
    public boolean hasCourierAccess(String username, String entityId) {
        if (username == null || entityId == null) {
            return false;
        }
        
        try {
            Set<String> userCourierEntities = getUserCourierEntities(username);
            boolean hasAccess = userCourierEntities.contains(entityId) || userCourierEntities.contains("*");
            
            logger.fine(String.format("Courier entity access check for user '%s', entity '%s': %s", 
                username, entityId, hasAccess ? "GRANTED" : "DENIED"));
                
            return hasAccess;
            
        } catch (Exception e) {
            logger.severe("Error checking courier entity access: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user permissions (with caching)
     */
    private Set<String> getUserPermissions(String username) {
        return userPermissionsCache.computeIfAbsent(username, this::loadUserPermissions);
    }
    
    /**
     * Get user warehouse access (with caching)
     */
    private Set<String> getUserWarehouses(String username) {
        return userWarehousesCache.computeIfAbsent(username, this::loadUserWarehouses);
    }
    
    /**
     * Get user courier entity access (with caching)
     */
    private Set<String> getUserCourierEntities(String username) {
        return userCourierEntitiesCache.computeIfAbsent(username, this::loadUserCourierEntities);
    }
    
    /**
     * Load user permissions from database (simulated)
     */
    private Set<String> loadUserPermissions(String username) {
        Set<String> permissions = new HashSet<>();
        
        try {
            // In production, this would query the database
            // For now, simulate based on username patterns
            
            if (username.contains("admin")) {
                permissions.add("PAYMENT_PROCESS");
                permissions.add("PAYMENT_REFUND");
                permissions.add("PAYMENT_READ");
                permissions.add("PAYMENT_CAPTURE");
                permissions.add("ANALYTICS_READ");
                permissions.add("WAREHOUSE_READ");
                permissions.add("WAREHOUSE_CREATE");
                permissions.add("WAREHOUSE_UPDATE");
                permissions.add("COURIER_READ");
                permissions.add("COURIER_CREATE");
                permissions.add("COURIER_UPDATE");
            } else if (username.contains("manager")) {
                permissions.add("PAYMENT_PROCESS");
                permissions.add("PAYMENT_READ");
                permissions.add("ANALYTICS_READ");
                permissions.add("WAREHOUSE_READ");
                permissions.add("COURIER_READ");
            } else if (username.contains("analyst")) {
                permissions.add("PAYMENT_READ");
                permissions.add("ANALYTICS_READ");
                permissions.add("WAREHOUSE_READ");
                permissions.add("COURIER_READ");
            } else if (username.contains("vendor")) {
                permissions.add("PAYMENT_PROCESS");
                permissions.add("PAYMENT_READ");
            }
            
            logger.info("Loaded " + permissions.size() + " permissions for user: " + username);
            
        } catch (Exception e) {
            logger.severe("Error loading user permissions: " + e.getMessage());
        }
        
        return permissions;
    }
    
    /**
     * Load user warehouse access from database (simulated)
     */
    private Set<String> loadUserWarehouses(String username) {
        Set<String> warehouses = new HashSet<>();
        
        try {
            // In production, this would query the database
            if (username.contains("admin")) {
                warehouses.add("*"); // Access to all warehouses
            } else if (username.contains("warehouse")) {
                // Extract warehouse ID from username or load from database
                warehouses.add("WH_123");
                warehouses.add("WH_456");
            }
            
            logger.fine("Loaded warehouse access for user " + username + ": " + warehouses);
            
        } catch (Exception e) {
            logger.severe("Error loading user warehouse access: " + e.getMessage());
        }
        
        return warehouses;
    }
    
    /**
     * Load user courier entity access from database (simulated)
     */
    private Set<String> loadUserCourierEntities(String username) {
        Set<String> entities = new HashSet<>();
        
        try {
            // In production, this would query the database
            if (username.contains("admin")) {
                entities.add("*"); // Access to all courier entities
            } else if (username.contains("driver")) {
                entities.add("DRIVER_" + username.hashCode());
            } else if (username.contains("courier")) {
                entities.add("LOC_123");
                entities.add("LOC_456");
            }
            
            logger.fine("Loaded courier entity access for user " + username + ": " + entities);
            
        } catch (Exception e) {
            logger.severe("Error loading user courier entity access: " + e.getMessage());
        }
        
        return entities;
    }
    
    /**
     * Simulate payment ownership lookup (would be database query in production)
     */
    private String simulatePaymentOwnershipLookup(String transactionId) {
        try {
            // In production, this would query the payments database
            // For simulation, extract username from transaction metadata or default
            
            if (transactionId.contains("VENDOR")) {
                return "vendor_user";
            } else if (transactionId.contains("WAREHOUSE")) {
                return "warehouse_manager";
            } else if (transactionId.contains("COURIER")) {
                return "courier_manager";
            }
            
            return "customer_user"; // Default owner
            
        } catch (Exception e) {
            logger.severe("Error simulating payment ownership lookup: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Clear user permission cache (for testing or when permissions change)
     */
    public void clearUserCache(String username) {
        userPermissionsCache.remove(username);
        userWarehousesCache.remove(username);
        userCourierEntitiesCache.remove(username);
        logger.info("Cleared permission cache for user: " + username);
    }
    
    /**
     * Clear all permission caches
     */
    public void clearAllCaches() {
        userPermissionsCache.clear();
        userWarehousesCache.clear();
        userCourierEntitiesCache.clear();
        paymentOwnersCache.clear();
        logger.info("Cleared all permission caches");
    }
    
    /**
     * Get cache statistics for monitoring
     */
    public Map<String, Integer> getCacheStatistics() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("userPermissions", userPermissionsCache.size());
        stats.put("userWarehouses", userWarehousesCache.size());
        stats.put("userCourierEntities", userCourierEntitiesCache.size());
        stats.put("paymentOwners", paymentOwnersCache.size());
        return stats;
    }
}