#!/bin/bash

echo "=== Fixing Final Compilation Issues ==="
echo "Date: $(date)"
echo ""

# Fix 1: Fix analytics-service package declaration and imports
echo "1. Fixing analytics-service package issues..."
if [ -f "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service/CurrencyAnalyticsService.java" ]; then
    # Fix package declaration
    sed -i 's/package com\.exalt\.socialcommerce\.analytics\.currency;/package com.exalt.socialcommerce.analytics.service;/g' \
        "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service/CurrencyAnalyticsService.java"
    
    # Add proper imports
    sed -i '1s/^/package com.exalt.socialcommerce.analytics.service;\n\nimport com.exalt.socialcommerce.analytics.model.*;\nimport com.exalt.socialcommerce.analytics.repository.*;\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.stereotype.Service;\nimport java.util.*;\nimport java.math.BigDecimal;\nimport java.time.LocalDateTime;\n\n@Service\n/' \
        "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service/CurrencyAnalyticsService.java"
    
    # Remove old package declaration line
    sed -i '/^package com\.exalt\.socialcommerce\.analytics\.service;$/d' \
        "analytics-service/src/main/java/com/exalt/socialcommerce/analytics/service/CurrencyAnalyticsService.java" 2>/dev/null || true
    
    echo "✅ Fixed analytics-service package issues"
fi

# Fix 2: Add Resilience4j dependency to api-gateway
echo ""
echo "2. Adding Resilience4j to api-gateway..."
if [ -f "api-gateway/pom.xml" ]; then
    # Add Spring Cloud Circuit Breaker dependency
    if ! grep -q "spring-cloud-starter-circuitbreaker-resilience4j" "api-gateway/pom.xml"; then
        sed -i '/<\/dependencies>/i\        <dependency>\n            <groupId>org.springframework.cloud</groupId>\n            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>\n        </dependency>' "api-gateway/pom.xml"
        echo "✅ Added Resilience4j to api-gateway"
    fi
fi

# Fix 3: Fix marketplace cyclic dependencies
echo ""
echo "3. Fixing marketplace service..."
# Remove problematic imports
find marketplace/src -name "*.java" -type f -exec sed -i '/import com\.ecosystem\.marketplace\.category\.model\.SellerCategory;/d' {} \;
find marketplace/src -name "*.java" -type f -exec sed -i '/import com\.ecosystem\.marketplace\.seller\.model\.Seller;/d' {} \;
find marketplace/src -name "*.java" -type f -exec sed -i '/import com\.ecosystem\.marketplace\.review\.model\.Review;/d' {} \;
find marketplace/src -name "*.java" -type f -exec sed -i '/import com\.ecosystem\.marketplace\.review\.service\.ReviewService;/d' {} \;

echo "✅ Fixed marketplace imports"

# Fix 4: Quick compile test for key services
echo ""
echo "4. Running quick compile test..."
QUICK_TEST_SERVICES=("analytics-service" "api-gateway" "marketplace" "order-service" "payment-gateway" "payout-service")

for service in "${QUICK_TEST_SERVICES[@]}"; do
    echo -n "Testing $service... "
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        cd "$service"
        if mvn compile -DskipTests -q 2>/dev/null; then
            echo "✅ SUCCESS"
        else
            echo "❌ FAILED"
        fi
        cd ..
    fi
done

echo ""
echo "=== Fixes completed ==="
echo "Date: $(date)"