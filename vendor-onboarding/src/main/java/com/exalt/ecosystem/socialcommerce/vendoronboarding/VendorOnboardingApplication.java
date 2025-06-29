package com.exalt.ecosystem.socialcommerce.vendoronboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Vendor Onboarding Service Application
 * 
 * Handles vendor registration, KYC verification, and onboarding processes
 * for the Social Commerce Ecosystem.
 * 
 * Features:
 * - Vendor registration and profile management
 * - KYC (Know Your Customer) verification
 * - Document upload and verification
 * - Compliance checking
 * - Vendor approval workflows
 * - Integration with marketplace for vendor activation
 */
@SpringBootApplication
@EnableDiscoveryClient
public class VendorOnboardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendorOnboardingApplication.class, args);
    }

}