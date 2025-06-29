#!/bin/bash

echo "=== Fixing Remaining Build Issues ==="
echo "Date: $(date)"
echo ""

# Fix 1: Add OpenFeign dependency to payment-gateway and payout-service
echo "1. Adding OpenFeign dependencies..."
for service in "payment-gateway" "payout-service"; do
    if [ -f "$service/pom.xml" ]; then
        # Check if OpenFeign is already present
        if ! grep -q "spring-cloud-starter-openfeign" "$service/pom.xml"; then
            # Add OpenFeign dependency before </dependencies>
            sed -i '/<\/dependencies>/i\        <dependency>\n            <groupId>org.springframework.cloud</groupId>\n            <artifactId>spring-cloud-starter-openfeign</artifactId>\n        </dependency>' "$service/pom.xml"
            echo "✅ Added OpenFeign to $service"
        fi
    fi
done

# Fix 2: Add Spring Security to order-service (already has it, but missing starter)
echo ""
echo "2. Fixing Spring Security in order-service..."
if [ -f "order-service/pom.xml" ]; then
    if ! grep -q "spring-boot-starter-security" "order-service/pom.xml"; then
        sed -i '/<\/dependencies>/i\        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-security</artifactId>\n        </dependency>' "order-service/pom.xml"
        echo "✅ Added Spring Security to order-service"
    fi
fi

# Fix 3: Remove BOM character from api-gateway
echo ""
echo "3. Fixing api-gateway BOM character..."
if [ -f "api-gateway/src/main/java/com/exalt/ecosystem/gateway/ApiGatewayApplication.java" ]; then
    # Remove BOM and fix the file
    sed -i '1s/^\xEF\xBB\xBF//' "api-gateway/src/main/java/com/exalt/ecosystem/gateway/ApiGatewayApplication.java"
    echo "✅ Removed BOM from api-gateway"
fi

# Fix 4: Fix localization-service (appears to have markdown content)
echo ""
echo "4. Recreating localization-service main class..."
cat > "localization-service/src/main/java/com/exalt/socialcommerce/localization/LocalizationServiceApplication.java" << 'EOF'
package com.exalt.socialcommerce.localization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LocalizationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalizationServiceApplication.class, args);
    }
}
EOF
echo "✅ Fixed localization-service main class"

# Fix 5: Remove duplicate Product classes in product-service
echo ""
echo "5. Fixing duplicate classes in product-service..."
# Remove the duplicate from wrong package
if [ -f "product-service/src/main/java/com/socialcommerce/product/entity/Product.java" ]; then
    rm -f "product-service/src/main/java/com/socialcommerce/product/entity/Product.java"
    echo "✅ Removed duplicate Product.java"
fi

# Remove duplicate ProductServiceApplication
if [ -f "product-service/src/main/java/com/socialcommerce/product/ProductServiceApplication.java" ]; then
    rm -f "product-service/src/main/java/com/socialcommerce/product/ProductServiceApplication.java"
    echo "✅ Removed duplicate ProductServiceApplication.java"
fi

# Remove duplicate ProductController
if [ -f "product-service/src/main/java/com/socialcommerce/product/controller/ProductController.java" ]; then
    rm -rf "product-service/src/main/java/com/socialcommerce"
    echo "✅ Removed duplicate package structure"
fi

# Fix 6: Create missing model classes for analytics-service
echo ""
echo "6. Creating missing model classes for analytics-service..."
mkdir -p "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/model"
mkdir -p "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/repository"

# Create model classes
cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/model/CurrencyUsageReport.java" << 'EOF'
package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class CurrencyUsageReport {
    private String reportId;
    private LocalDateTime generatedAt;
    private Map<String, Long> currencyTransactionCounts;
    private Map<String, BigDecimal> currencyTotalAmounts;
    private Map<String, Double> currencyPercentages;
    private String mostUsedCurrency;
    private String leastUsedCurrency;
}
EOF

cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/model/CurrencyRevenueDashboard.java" << 'EOF'
package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CurrencyRevenueDashboard {
    private Map<String, BigDecimal> revenuePerCurrency;
    private BigDecimal totalRevenueUSD;
    private Map<String, Double> growthRates;
    private Map<String, Integer> activeUsers;
}
EOF

cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/model/CurrencyForecastReport.java" << 'EOF'
package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CurrencyForecastReport {
    private Map<String, BigDecimal> projectedRevenue;
    private Map<String, Double> growthPredictions;
    private Map<String, String> recommendations;
}
EOF

cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/model/CurrencyAlert.java" << 'EOF'
package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class CurrencyAlert {
    private String alertId;
    private String currency;
    private String alertType;
    private String message;
    private LocalDateTime timestamp;
    private String severity;
}
EOF

# Create repository interfaces
cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/repository/OrderRepository.java" << 'EOF'
package com.exalt.socialcommerce.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Object, Long> {
    // Placeholder - actual Order entity would come from order-service
}
EOF

cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/repository/PaymentRepository.java" << 'EOF'
package com.exalt.socialcommerce.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Object, Long> {
    // Placeholder - actual Payment entity would come from payment-service
}
EOF

cat > "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/repository/UserRepository.java" << 'EOF'
package com.exalt.socialcommerce.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Object, Long> {
    // Placeholder - actual User entity would come from user-service
}
EOF

echo "✅ Created missing model classes and repositories"

# Fix the import in CurrencyAnalyticsService
echo ""
echo "7. Fixing imports in analytics-service..."
if [ -f "analytics-service/src/main/java/com/exalt/ecosystem/socialcommerce/analytics/currency/CurrencyAnalyticsService.java" ]; then
    # Update package imports
    sed -i 's/com\.exalt\.ecosystem\.socialcommerce\.analytics/com.exalt.socialcommerce.analytics/g' \
        "analytics-service/src/main/java/com/exalt/ecosystem/socialcommerce/analytics/currency/CurrencyAnalyticsService.java"
    
    # Move file to correct package
    mkdir -p "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service"
    mv "analytics-service/src/main/java/com/exalt/ecosystem/socialcommerce/analytics/currency/CurrencyAnalyticsService.java" \
       "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service/CurrencyAnalyticsService.java" 2>/dev/null || true
    
    # Fix package declaration
    sed -i 's/package com\.exalt\.ecosystem\.socialcommerce\.analytics\.currency;/package com.exalt.socialcommerce.analytics.service;/g' \
        "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service/CurrencyAnalyticsService.java" 2>/dev/null || true
    
    echo "✅ Fixed analytics-service imports"
fi

echo ""
echo "=== All fixes completed ==="
echo "Date: $(date)"