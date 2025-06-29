package com.exalt.socialcommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Application for Social Commerce
 * Standardized package: com.exalt.socialcommerce.gateway
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r.path("/api/orders/**")
                        .uri("http://order-service:8080"))
                .route("product-service", r -> r.path("/api/products/**")
                        .uri("http://product-service:8080"))
                .route("payment-gateway", r -> r.path("/api/payments/**")
                        .uri("http://payment-gateway:8080"))
                .build();
    }
}