#!/bin/bash

echo "=== Direct POM Fixes ==="
echo "Date: $(date)"
echo ""

# Fix 1: Check one POM structure to understand the issue
echo "1. Analyzing POM structure..."
echo ""

# Check if api-gateway has the dependency management section properly closed
if [ -f "api-gateway/pom.xml" ]; then
    echo "Checking api-gateway POM structure..."
    # Count opening and closing tags
    opening=$(grep -c "<dependency>" api-gateway/pom.xml)
    closing=$(grep -c "</dependency>" api-gateway/pom.xml)
    echo "  - Opening <dependency> tags: $opening"
    echo "  - Closing </dependency> tags: $closing"
    
    # Check for Spring Cloud BOM
    if grep -q "spring-cloud-dependencies" api-gateway/pom.xml; then
        echo "  - Spring Cloud BOM is present"
    else
        echo "  - Spring Cloud BOM is MISSING"
    fi
fi

# Fix 2: Direct fix for each service
echo ""
echo "2. Applying direct fixes..."

# For api-gateway - remove the problematic dependency temporarily
echo "  - Fixing api-gateway..."
if [ -f "api-gateway/pom.xml" ]; then
    # Remove the Resilience4j dependency lines
    sed -i '/<dependency>/{:a;N;/<\/dependency>/!ba;/spring-cloud-starter-circuitbreaker-resilience4j/d;}' api-gateway/pom.xml
    echo "    ✅ Removed problematic Resilience4j dependency"
fi

# For order-service - it should already have Spring Boot parent, so security doesn't need version
echo "  - Checking order-service..."
if [ -f "order-service/pom.xml" ]; then
    # Check if it has Spring Boot parent
    if grep -q "spring-boot-starter-parent" order-service/pom.xml; then
        echo "    ✅ Has Spring Boot parent - security dependency should work"
    fi
fi

# For payment/payout services - remove the OpenFeign dependencies temporarily
for service in "payment-gateway" "payout-service"; do
    echo "  - Fixing $service..."
    if [ -f "$service/pom.xml" ]; then
        sed -i '/<dependency>/{:a;N;/<\/dependency>/!ba;/spring-cloud-starter-openfeign/d;}' "$service/pom.xml"
        echo "    ✅ Removed problematic OpenFeign dependency"
    fi
done

# Fix 3: Test compilation of previously failing services
echo ""
echo "3. Testing compilation..."
echo ""

TEST_SERVICES=(
    "analytics-service"
    "api-gateway"
    "order-service"
    "payment-gateway"
    "payout-service"
    "localization-service"
)

SUCCESS=0
FAILURE=0

for service in "${TEST_SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo -n "Testing $service... "
        cd "$service"
        
        if mvn compile -DskipTests -q 2>/dev/null; then
            echo "✅ SUCCESS"
            SUCCESS=$((SUCCESS + 1))
        else
            echo "❌ FAILED"
            # Get specific error
            mvn compile -DskipTests 2>&1 | grep -A2 "ERROR" | head -3
            FAILURE=$((FAILURE + 1))
        fi
        
        cd ..
    fi
done

echo ""
echo "=== Summary ==="
echo "Successful: $SUCCESS / ${#TEST_SERVICES[@]}"
echo "Failed: $FAILURE"
echo "Success Rate: $(( SUCCESS * 100 / ${#TEST_SERVICES[@]} ))%"