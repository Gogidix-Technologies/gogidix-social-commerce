package com.gogidix.socialcommerce.integrationperformance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IntegrationPerformanceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IntegrationPerformanceApplication.class, args);
    }
}
