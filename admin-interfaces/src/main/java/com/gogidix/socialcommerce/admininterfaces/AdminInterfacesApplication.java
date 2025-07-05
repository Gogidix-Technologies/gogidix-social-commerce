package com.gogidix.socialcommerce.admininterfaces;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AdminInterfacesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AdminInterfacesApplication.class, args);
    }
}
