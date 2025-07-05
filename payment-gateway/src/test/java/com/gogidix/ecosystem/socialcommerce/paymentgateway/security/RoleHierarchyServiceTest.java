package com.gogidix.ecosystem.socialcommerce.paymentgateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Role Hierarchy Service Unit Tests
 * 
 * SECURITY FIX: RBAC Implementation (CVSS 9.3)
 * - Tests privilege escalation prevention mechanisms
 * - Validates role hierarchy enforcement
 * - Ensures proper domain access controls
 * - Verifies role assignment authorization
 */
public class RoleHierarchyServiceTest {

    private RoleHierarchyService roleHierarchyService;

    @BeforeEach
    void setUp() {
        roleHierarchyService = new RoleHierarchyService();
    }

    // ==============================================
    // PRIVILEGE ESCALATION PREVENTION TESTS
    // ==============================================

    @Test
    @DisplayName("Vendor cannot assign admin roles")
    void testVendorCannotAssignAdminRoles() {
        // Vendor should not be able to assign higher-level roles
        assertFalse(roleHierarchyService.canAssignRole("VENDOR", "COMMERCE_ADMIN"));
        assertFalse(roleHierarchyService.canAssignRole("VENDOR", "PLATFORM_ADMIN"));
        assertFalse(roleHierarchyService.canAssignRole("VENDOR", "SUPER_ADMIN"));
    }

    @Test
    @DisplayName("Customer cannot assign any roles")
    void testCustomerCannotAssignRoles() {
        // Customer is lowest level and should not assign any roles
        assertFalse(roleHierarchyService.canAssignRole("CUSTOMER", "VENDOR"));
        assertFalse(roleHierarchyService.canAssignRole("CUSTOMER", "DRIVER"));
        assertFalse(roleHierarchyService.canAssignRole("CUSTOMER", "CUSTOMER"));
    }

    @Test
    @DisplayName("Driver cannot escalate to management roles")
    void testDriverCannotEscalateToManagement() {
        assertFalse(roleHierarchyService.canAssignRole("DRIVER", "DRIVER_SUPERVISOR"));
        assertFalse(roleHierarchyService.canAssignRole("DRIVER", "FLEET_MANAGER"));
        assertFalse(roleHierarchyService.canAssignRole("DRIVER", "COURIER_ADMIN"));
    }

    @Test
    @DisplayName("Warehouse Staff cannot assign manager roles")
    void testWarehouseStaffCannotAssignManagerRoles() {
        assertFalse(roleHierarchyService.canAssignRole("WAREHOUSE_STAFF", "WAREHOUSE_OPERATOR"));
        assertFalse(roleHierarchyService.canAssignRole("WAREHOUSE_STAFF", "WAREHOUSE_MANAGER"));
        assertFalse(roleHierarchyService.canAssignRole("WAREHOUSE_STAFF", "WAREHOUSE_ADMIN"));
    }

    // ==============================================
    // VALID ROLE ASSIGNMENT TESTS
    // ==============================================

    @Test
    @DisplayName("Super Admin can assign any role")
    void testSuperAdminCanAssignAnyRole() {
        assertTrue(roleHierarchyService.canAssignRole("SUPER_ADMIN", "PLATFORM_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("SUPER_ADMIN", "COMMERCE_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("SUPER_ADMIN", "WAREHOUSE_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("SUPER_ADMIN", "COURIER_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("SUPER_ADMIN", "VENDOR"));
        assertTrue(roleHierarchyService.canAssignRole("SUPER_ADMIN", "CUSTOMER"));
    }

    @Test
    @DisplayName("Platform Admin can assign domain roles")
    void testPlatformAdminCanAssignDomainRoles() {
        assertTrue(roleHierarchyService.canAssignRole("PLATFORM_ADMIN", "COMMERCE_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("PLATFORM_ADMIN", "WAREHOUSE_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("PLATFORM_ADMIN", "COURIER_ADMIN"));
        assertTrue(roleHierarchyService.canAssignRole("PLATFORM_ADMIN", "PLATFORM_ANALYST"));
        
        // But cannot assign super admin
        assertFalse(roleHierarchyService.canAssignRole("PLATFORM_ADMIN", "SUPER_ADMIN"));
    }

    @Test
    @DisplayName("Commerce Admin can assign commerce roles")
    void testCommerceAdminCanAssignCommerceRoles() {
        assertTrue(roleHierarchyService.canAssignRole("COMMERCE_ADMIN", "COMMERCE_MANAGER"));
        assertTrue(roleHierarchyService.canAssignRole("COMMERCE_ADMIN", "COMMERCE_ANALYST"));
        assertTrue(roleHierarchyService.canAssignRole("COMMERCE_ADMIN", "VENDOR_MANAGER"));
        assertTrue(roleHierarchyService.canAssignRole("COMMERCE_ADMIN", "VENDOR"));
        
        // But cannot assign cross-domain or higher roles
        assertFalse(roleHierarchyService.canAssignRole("COMMERCE_ADMIN", "WAREHOUSE_ADMIN"));
        assertFalse(roleHierarchyService.canAssignRole("COMMERCE_ADMIN", "PLATFORM_ADMIN"));
    }

    @Test
    @DisplayName("Manager roles can assign lower roles in same domain")
    void testManagerRolesCanAssignLowerRoles() {
        // Commerce Manager
        assertTrue(roleHierarchyService.canAssignRole("COMMERCE_MANAGER", "COMMERCE_ANALYST"));
        assertTrue(roleHierarchyService.canAssignRole("COMMERCE_MANAGER", "VENDOR_MANAGER"));
        assertFalse(roleHierarchyService.canAssignRole("COMMERCE_MANAGER", "COMMERCE_ADMIN"));
        
        // Warehouse Manager
        assertTrue(roleHierarchyService.canAssignRole("WAREHOUSE_MANAGER", "WAREHOUSE_ANALYST"));
        assertTrue(roleHierarchyService.canAssignRole("WAREHOUSE_MANAGER", "WAREHOUSE_OPERATOR"));
        assertTrue(roleHierarchyService.canAssignRole("WAREHOUSE_MANAGER", "WAREHOUSE_STAFF"));
        assertFalse(roleHierarchyService.canAssignRole("WAREHOUSE_MANAGER", "WAREHOUSE_ADMIN"));
        
        // Fleet Manager
        assertTrue(roleHierarchyService.canAssignRole("FLEET_MANAGER", "COURIER_ANALYST"));
        assertTrue(roleHierarchyService.canAssignRole("FLEET_MANAGER", "DRIVER_SUPERVISOR"));
        assertTrue(roleHierarchyService.canAssignRole("FLEET_MANAGER", "DRIVER"));
        assertFalse(roleHierarchyService.canAssignRole("FLEET_MANAGER", "COURIER_ADMIN"));
    }

    // ==============================================
    // ROLE HIERARCHY ACCESS TESTS
    // ==============================================

    @Test
    @DisplayName("Higher roles can access lower role resources")
    void testHigherRolesCanAccessLowerResources() {
        assertTrue(roleHierarchyService.canAccessResource("COMMERCE_ADMIN", "COMMERCE_MANAGER"));
        assertTrue(roleHierarchyService.canAccessResource("COMMERCE_ADMIN", "VENDOR"));
        assertTrue(roleHierarchyService.canAccessResource("PLATFORM_ADMIN", "COMMERCE_ADMIN"));
        assertTrue(roleHierarchyService.canAccessResource("SUPER_ADMIN", "PLATFORM_ADMIN"));
    }

    @Test
    @DisplayName("Lower roles cannot access higher role resources")
    void testLowerRolesCannotAccessHigherResources() {
        assertFalse(roleHierarchyService.canAccessResource("VENDOR", "COMMERCE_MANAGER"));
        assertFalse(roleHierarchyService.canAccessResource("COMMERCE_MANAGER", "COMMERCE_ADMIN"));
        assertFalse(roleHierarchyService.canAccessResource("COMMERCE_ADMIN", "PLATFORM_ADMIN"));
        assertFalse(roleHierarchyService.canAccessResource("PLATFORM_ADMIN", "SUPER_ADMIN"));
    }

    @Test
    @DisplayName("Same level roles can access equivalent resources")
    void testSameLevelRolesCanAccessEquivalentResources() {
        assertTrue(roleHierarchyService.canAccessResource("COMMERCE_ADMIN", "COMMERCE_ADMIN"));
        assertTrue(roleHierarchyService.canAccessResource("WAREHOUSE_ADMIN", "WAREHOUSE_ADMIN"));
        assertTrue(roleHierarchyService.canAccessResource("COURIER_ADMIN", "COURIER_ADMIN"));
    }

    @Test
    @DisplayName("Managers cannot assign roles at same level")
    void testManagersCannotAssignSameLevelRoles() {
        // Commerce Manager cannot assign other Commerce Managers
        assertFalse(roleHierarchyService.canAssignRole("COMMERCE_MANAGER", "COMMERCE_MANAGER"));
        assertFalse(roleHierarchyService.canAssignRole("WAREHOUSE_MANAGER", "WAREHOUSE_MANAGER"));
        assertFalse(roleHierarchyService.canAssignRole("FLEET_MANAGER", "FLEET_MANAGER"));
    }

    // ==============================================
    // ROLE INHERITANCE TESTS
    // ==============================================

    @Test
    @DisplayName("Super Admin inherits all roles")
    void testSuperAdminInheritsAllRoles() {
        Set<String> inheritedRoles = roleHierarchyService.getInheritedRoles("SUPER_ADMIN");
        
        assertTrue(inheritedRoles.contains("SUPER_ADMIN"));
        assertTrue(inheritedRoles.contains("PLATFORM_ADMIN"));
        assertTrue(inheritedRoles.contains("COMMERCE_ADMIN"));
        assertTrue(inheritedRoles.contains("WAREHOUSE_ADMIN"));
        assertTrue(inheritedRoles.contains("COURIER_ADMIN"));
    }

    @Test
    @DisplayName("Platform Admin inherits domain admin roles")
    void testPlatformAdminInheritsDomainRoles() {
        Set<String> inheritedRoles = roleHierarchyService.getInheritedRoles("PLATFORM_ADMIN");
        
        assertTrue(inheritedRoles.contains("PLATFORM_ADMIN"));
        assertTrue(inheritedRoles.contains("COMMERCE_ADMIN"));
        assertTrue(inheritedRoles.contains("WAREHOUSE_ADMIN"));
        assertTrue(inheritedRoles.contains("COURIER_ADMIN"));
        assertTrue(inheritedRoles.contains("PLATFORM_ANALYST"));
        
        // Should not inherit super admin
        assertFalse(inheritedRoles.contains("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("Commerce Admin inherits commerce hierarchy")
    void testCommerceAdminInheritsCommerceHierarchy() {
        Set<String> inheritedRoles = roleHierarchyService.getInheritedRoles("COMMERCE_ADMIN");
        
        assertTrue(inheritedRoles.contains("COMMERCE_ADMIN"));
        assertTrue(inheritedRoles.contains("COMMERCE_MANAGER"));
        assertTrue(inheritedRoles.contains("COMMERCE_ANALYST"));
        assertTrue(inheritedRoles.contains("VENDOR_MANAGER"));
        assertTrue(inheritedRoles.contains("VENDOR"));
        
        // Should not inherit other domains
        assertFalse(inheritedRoles.contains("WAREHOUSE_ADMIN"));
        assertFalse(inheritedRoles.contains("COURIER_ADMIN"));
    }

    @Test
    @DisplayName("Base roles have no inheritance")
    void testBaseRolesHaveNoInheritance() {
        Set<String> vendorRoles = roleHierarchyService.getInheritedRoles("VENDOR");
        Set<String> customerRoles = roleHierarchyService.getInheritedRoles("CUSTOMER");
        Set<String> driverRoles = roleHierarchyService.getInheritedRoles("DRIVER");
        
        assertEquals(1, vendorRoles.size());
        assertEquals(1, customerRoles.size());
        assertEquals(1, driverRoles.size());
        
        assertTrue(vendorRoles.contains("VENDOR"));
        assertTrue(customerRoles.contains("CUSTOMER"));
        assertTrue(driverRoles.contains("DRIVER"));
    }

    // ==============================================
    // DOMAIN ACCESS VALIDATION TESTS
    // ==============================================

    @Test
    @DisplayName("Global roles have access to all domains")
    void testGlobalRolesHaveAllDomainAccess() {
        assertTrue(roleHierarchyService.isDomainAdmin("SUPER_ADMIN", "SOCIAL_COMMERCE"));
        assertTrue(roleHierarchyService.isDomainAdmin("SUPER_ADMIN", "WAREHOUSING"));
        assertTrue(roleHierarchyService.isDomainAdmin("SUPER_ADMIN", "COURIER_SERVICES"));
        
        assertTrue(roleHierarchyService.isDomainAdmin("PLATFORM_ADMIN", "SOCIAL_COMMERCE"));
        assertTrue(roleHierarchyService.isDomainAdmin("PLATFORM_ADMIN", "WAREHOUSING"));
        assertTrue(roleHierarchyService.isDomainAdmin("PLATFORM_ADMIN", "COURIER_SERVICES"));
    }

    @Test
    @DisplayName("Domain admins have access only to their domains")
    void testDomainAdminsHaveSpecificDomainAccess() {
        // Commerce Admin
        assertTrue(roleHierarchyService.isDomainAdmin("COMMERCE_ADMIN", "SOCIAL_COMMERCE"));
        assertFalse(roleHierarchyService.isDomainAdmin("COMMERCE_ADMIN", "WAREHOUSING"));
        assertFalse(roleHierarchyService.isDomainAdmin("COMMERCE_ADMIN", "COURIER_SERVICES"));
        
        // Warehouse Admin
        assertFalse(roleHierarchyService.isDomainAdmin("WAREHOUSE_ADMIN", "SOCIAL_COMMERCE"));
        assertTrue(roleHierarchyService.isDomainAdmin("WAREHOUSE_ADMIN", "WAREHOUSING"));
        assertFalse(roleHierarchyService.isDomainAdmin("WAREHOUSE_ADMIN", "COURIER_SERVICES"));
        
        // Courier Admin
        assertFalse(roleHierarchyService.isDomainAdmin("COURIER_ADMIN", "SOCIAL_COMMERCE"));
        assertFalse(roleHierarchyService.isDomainAdmin("COURIER_ADMIN", "WAREHOUSING"));
        assertTrue(roleHierarchyService.isDomainAdmin("COURIER_ADMIN", "COURIER_SERVICES"));
    }

    @Test
    @DisplayName("Non-admin roles cannot be domain admins")
    void testNonAdminRolesCannotBeDomainAdmins() {
        assertFalse(roleHierarchyService.isDomainAdmin("COMMERCE_MANAGER", "SOCIAL_COMMERCE"));
        assertFalse(roleHierarchyService.isDomainAdmin("WAREHOUSE_MANAGER", "WAREHOUSING"));
        assertFalse(roleHierarchyService.isDomainAdmin("FLEET_MANAGER", "COURIER_SERVICES"));
        assertFalse(roleHierarchyService.isDomainAdmin("VENDOR", "SOCIAL_COMMERCE"));
        assertFalse(roleHierarchyService.isDomainAdmin("CUSTOMER", "GLOBAL"));
    }

    // ==============================================
    // ROLE VALIDATION TESTS
    // ==============================================

    @Test
    @DisplayName("Valid roles are properly recognized")
    void testValidRolesAreRecognized() {
        assertTrue(roleHierarchyService.isValidRole("SUPER_ADMIN"));
        assertTrue(roleHierarchyService.isValidRole("PLATFORM_ADMIN"));
        assertTrue(roleHierarchyService.isValidRole("COMMERCE_ADMIN"));
        assertTrue(roleHierarchyService.isValidRole("VENDOR"));
        assertTrue(roleHierarchyService.isValidRole("CUSTOMER"));
        assertTrue(roleHierarchyService.isValidRole("DRIVER"));
    }

    @Test
    @DisplayName("Invalid roles are rejected")
    void testInvalidRolesAreRejected() {
        assertFalse(roleHierarchyService.isValidRole("INVALID_ROLE"));
        assertFalse(roleHierarchyService.isValidRole("FAKE_ADMIN"));
        assertFalse(roleHierarchyService.isValidRole(""));
        assertFalse(roleHierarchyService.isValidRole(null));
    }

    @Test
    @DisplayName("Role levels are correctly assigned")
    void testRoleLevelsAreCorrect() {
        assertEquals(100, roleHierarchyService.getRoleLevel("SUPER_ADMIN"));
        assertEquals(90, roleHierarchyService.getRoleLevel("PLATFORM_ADMIN"));
        assertEquals(80, roleHierarchyService.getRoleLevel("COMMERCE_ADMIN"));
        assertEquals(70, roleHierarchyService.getRoleLevel("COMMERCE_MANAGER"));
        assertEquals(40, roleHierarchyService.getRoleLevel("VENDOR"));
        assertEquals(30, roleHierarchyService.getRoleLevel("CUSTOMER"));
        
        assertNull(roleHierarchyService.getRoleLevel("INVALID_ROLE"));
    }

    // ==============================================
    // SECURITY BOUNDARY TESTS
    // ==============================================

    @Test
    @DisplayName("Null role assignments are rejected")
    void testNullRoleAssignmentsAreRejected() {
        assertFalse(roleHierarchyService.canAssignRole(null, "VENDOR"));
        assertFalse(roleHierarchyService.canAssignRole("ADMIN", null));
        assertFalse(roleHierarchyService.canAssignRole(null, null));
    }

    @Test
    @DisplayName("Invalid role assignments are rejected")
    void testInvalidRoleAssignmentsAreRejected() {
        assertFalse(roleHierarchyService.canAssignRole("INVALID_ROLE", "VENDOR"));
        assertFalse(roleHierarchyService.canAssignRole("ADMIN", "INVALID_ROLE"));
        assertFalse(roleHierarchyService.canAssignRole("INVALID_1", "INVALID_2"));
    }

    @Test
    @DisplayName("Role access with null parameters is rejected")
    void testRoleAccessWithNullParametersIsRejected() {
        assertFalse(roleHierarchyService.canAccessResource(null, "VENDOR"));
        assertFalse(roleHierarchyService.canAccessResource("ADMIN", null));
        assertFalse(roleHierarchyService.canAccessResource(null, null));
    }
}