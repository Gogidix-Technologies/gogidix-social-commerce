package com.exalt.socialcommerce.adminfinalization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AdminFinalizationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AdminFinalizationApplication.class, args);
    }
}