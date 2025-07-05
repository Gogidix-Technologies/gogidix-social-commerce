package com.gogidix.socialcommerce.integrationoptimization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IntegrationOptimizationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IntegrationOptimizationApplication.class, args);
    }
}
