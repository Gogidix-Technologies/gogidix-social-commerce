#!/bin/bash

echo "=== Implementing Empty Services ==="
echo "Date: $(date)"
echo ""

# Function to create Application class
create_application_class() {
    local service=$1
    local serviceName=$2
    local packageName=$3
    
    echo "Creating Application class for $service..."
    
    cat > "$service/src/main/java/com/exalt/socialcommerce/$packageName/${serviceName}Application.java" << EOF
package com.exalt.socialcommerce.$packageName;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ${serviceName}Application {
    
    public static void main(String[] args) {
        SpringApplication.run(${serviceName}Application.class, args);
    }
}
EOF
}

# Function to create HealthCheck controller
create_health_controller() {
    local service=$1
    local packageName=$2
    
    mkdir -p "$service/src/main/java/com/exalt/socialcommerce/$packageName/controller"
    
    cat > "$service/src/main/java/com/exalt/socialcommerce/$packageName/controller/HealthCheckController.java" << EOF
package com.exalt.socialcommerce.$packageName.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/$service")
public class HealthCheckController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "$service");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}
EOF
}

# Function to create application.yml
create_application_yml() {
    local service=$1
    local port=$2
    
    mkdir -p "$service/src/main/resources"
    
    cat > "$service/src/main/resources/application.yml" << EOF
spring:
  application:
    name: $service-service
  profiles:
    active: \${SPRING_PROFILES_ACTIVE:default}

server:
  port: \${SERVER_PORT:$port}

eureka:
  client:
    service-url:
      defaultZone: \${EUREKA_SERVER:http://localhost:8761/eureka}
    enabled: \${EUREKA_ENABLED:true}
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    com.exalt.socialcommerce: \${LOG_LEVEL:INFO}
EOF
}

# Services to implement
declare -A services=(
    ["admin-interfaces"]="AdminInterfaces:admininterfaces:8082"
    ["analytics-service"]="Analytics:analytics:8083"
    ["integration-optimization"]="IntegrationOptimization:integrationoptimization:8084"
    ["integration-performance"]="IntegrationPerformance:integrationperformance:8085"
    ["invoice-service"]="Invoice:invoice:8086"
    ["regional-admin"]="RegionalAdmin:regionaladmin:8087"
    ["social-commerce-production"]="SocialCommerceProduction:production:8088"
    ["social-commerce-shared"]="SocialCommerceShared:shared:8089"
    ["social-commerce-staging"]="SocialCommerceStaging:staging:8090"
)

# Process each service
for service in "${!services[@]}"; do
    IFS=':' read -r className packageName port <<< "${services[$service]}"
    
    echo "Processing $service..."
    
    # Create directory structure
    mkdir -p "$service/src/main/java/com/exalt/socialcommerce/$packageName"
    mkdir -p "$service/src/main/resources"
    mkdir -p "$service/src/test/java/com/exalt/socialcommerce/$packageName"
    
    # Create Application class
    create_application_class "$service" "$className" "$packageName"
    
    # Create Health controller
    create_health_controller "$service" "$packageName"
    
    # Create application.yml
    create_application_yml "$service" "$port"
    
    echo "✅ Implemented $service"
    echo ""
done

# Fix commission-service configuration
echo "Fixing commission-service configuration..."
if [ ! -f "commission-service/src/main/resources/application.yml" ]; then
    create_application_yml "commission-service" "8091"
    echo "✅ Fixed commission-service configuration"
fi

echo "=== Implementation Complete ==="
echo "Script completed at: $(date)"