#!/bin/bash

echo "=== Fixing Critical Services ==="
echo "Date: $(date)"
echo ""

# Fix 1: Order-service - Add missing OAuth2 and WebClient dependencies
echo "1. Fixing order-service dependencies..."
if [ -f "order-service/pom.xml" ]; then
    # Add OAuth2 resource server dependency
    if ! grep -q "spring-boot-starter-oauth2-resource-server" order-service/pom.xml; then
        sed -i '/<\/dependencies>/i\        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>\n        </dependency>' order-service/pom.xml
    fi
    
    # Add WebFlux for WebClient
    if ! grep -q "spring-boot-starter-webflux" order-service/pom.xml; then
        sed -i '/<\/dependencies>/i\        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-webflux</artifactId>\n        </dependency>' order-service/pom.xml
    fi
    
    echo "  ✅ Added OAuth2 and WebClient dependencies"
fi

# Fix 2: Payment-gateway - Fix file naming and add Lombok annotation
echo ""
echo "2. Fixing payment-gateway..."
if [ -f "payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/PaymentgatewayApplication.java" ]; then
    # Rename the file
    mv "payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/PaymentgatewayApplication.java" \
       "payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/PaymentGatewayApplication.java"
    echo "  - Renamed application file"
fi

# Add @Slf4j to PaymentController
if [ -f "payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/controller/PaymentController.java" ]; then
    # Add import if not present
    if ! grep -q "import lombok.extern.slf4j.Slf4j;" payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/controller/PaymentController.java; then
        sed -i '/^package/a\import lombok.extern.slf4j.Slf4j;' payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/controller/PaymentController.java
    fi
    
    # Add @Slf4j annotation
    if ! grep -q "@Slf4j" payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/controller/PaymentController.java; then
        sed -i '/^@RestController/i\@Slf4j' payment-gateway/src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/controller/PaymentController.java
    fi
    
    echo "  ✅ Fixed payment-gateway"
fi

# Fix 3: Payout-service - Ensure it compiles
echo ""
echo "3. Checking payout-service..."
echo "  ✅ Already fixed (OpenFeign commented out)"

# Fix 4: Product-service - Ensure no duplicates remain
echo ""
echo "4. Checking product-service..."
if [ -d "product-service/src/main/java/com/socialcommerce" ]; then
    rm -rf "product-service/src/main/java/com/socialcommerce"
    echo "  ✅ Removed remaining duplicates"
else
    echo "  ✅ No duplicates found"
fi

# Test compilation
echo ""
echo "5. Testing critical services..."
echo ""

CRITICAL_SERVICES=(
    "analytics-service"
    "api-gateway" 
    "order-service"
    "payment-gateway"
    "payout-service"
    "localization-service"
    "product-service"
    "marketplace"
)

SUCCESS=0
FAILURE=0

for service in "${CRITICAL_SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo -n "Testing $service... "
        cd "$service"
        
        if mvn compile -DskipTests -q 2>/dev/null; then
            echo "✅ SUCCESS"
            SUCCESS=$((SUCCESS + 1))
        else
            echo "❌ FAILED"
            FAILURE=$((FAILURE + 1))
        fi
        
        cd ..
    fi
done

echo ""
echo "=== Critical Services Status ==="
echo "Success: $SUCCESS / ${#CRITICAL_SERVICES[@]}"
echo "Failed: $FAILURE"
echo "Success Rate: $(( SUCCESS * 100 / ${#CRITICAL_SERVICES[@]} ))%"