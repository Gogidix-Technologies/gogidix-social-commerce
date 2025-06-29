package com.socialcommerce.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Subscription Service Application
 * 
 * Handles recurring billing, subscription management, and automated billing cycles
 * for the Social Commerce Ecosystem.
 * 
 * Features:
 * - Recurring subscription billing
 * - Multiple billing cycles (monthly, quarterly, annual)
 * - Payment gateway integration for automated billing
 * - Subscription lifecycle management
 * - Billing notifications and reminders
 * - Analytics and reporting
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class SubscriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
    }

}
