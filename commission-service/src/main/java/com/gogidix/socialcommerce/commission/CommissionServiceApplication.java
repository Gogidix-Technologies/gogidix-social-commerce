package com.gogidix.socialcommerce.commission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Commission Service Application for Social Commerce
 * Standardized package: com.gogidix.socialcommerce.commission
 */
@SpringBootApplication
public class CommissionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommissionServiceApplication.class, args);
    }

    @RestController
    public static class HealthController {
        @GetMapping("/health")
        public String health() {
            return "Commission Service is running";
        }
    }
}