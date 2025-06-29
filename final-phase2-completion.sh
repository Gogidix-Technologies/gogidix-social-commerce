#!/bin/bash

echo "=== Final Phase 2 Completion Script ==="
echo "Date: $(date)"
echo ""

# Fix 1: Remove version from Spring Boot dependencies in order-service
echo "1. Fixing order-service Spring Security dependency..."
if [ -f "order-service/pom.xml" ]; then
    # Spring Boot dependencies don't need version when using parent
    sed -i '/<artifactId>spring-boot-starter-security<\/artifactId>/,/<\/dependency>/{s/<version>.*<\/version>//g}' "order-service/pom.xml"
    echo "✅ Fixed order-service"
fi

# Fix 2: Remove version from Spring Cloud dependencies (managed by Spring Cloud BOM)
echo ""
echo "2. Fixing OpenFeign dependencies..."
for service in "payment-gateway" "payout-service"; do
    if [ -f "$service/pom.xml" ]; then
        # Remove version tag from OpenFeign dependency
        sed -i '/<artifactId>spring-cloud-starter-openfeign<\/artifactId>/,/<\/dependency>/{s/<version>.*<\/version>//g}' "$service/pom.xml"
        echo "✅ Fixed $service"
    fi
done

# Fix 3: Remove version from Resilience4j in api-gateway
echo ""
echo "3. Fixing api-gateway Resilience4j dependency..."
if [ -f "api-gateway/pom.xml" ]; then
    sed -i '/<artifactId>spring-cloud-starter-circuitbreaker-resilience4j<\/artifactId>/,/<\/dependency>/{s/<version>.*<\/version>//g}' "api-gateway/pom.xml"
    echo "✅ Fixed api-gateway"
fi

# Fix 4: Run compilation test
echo ""
echo "4. Running compilation tests..."
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
        echo -n "Building $service... "
        cd "$service"
        if mvn compile -DskipTests -q 2>/dev/null; then
            echo "✅ SUCCESS"
            SUCCESS=$((SUCCESS + 1))
        else
            echo "❌ FAILED"
            FAILURE=$((FAILURE + 1))
        fi
        cd ..
    else
        echo "$service - ⚠️ NO POM"
    fi
done

echo ""
echo "=== Phase 2 Final Summary ==="
echo "Successful builds: $SUCCESS / ${#CRITICAL_SERVICES[@]}"
echo "Failed builds: $FAILURE"
echo "Success rate: $(( SUCCESS * 100 / ${#CRITICAL_SERVICES[@]} ))%"
echo ""

# Create final status report
REPORT_FILE="PHASE2_COMPLETION_STATUS.md"
cat > "$REPORT_FILE" << EOF
# Phase 2 Completion Status

## Date: $(date)

## Build Results
- Total Services Tested: ${#CRITICAL_SERVICES[@]}
- Successful Builds: $SUCCESS
- Failed Builds: $FAILURE
- Success Rate: $(( SUCCESS * 100 / ${#CRITICAL_SERVICES[@]} ))%

## Services Status
EOF

for service in "${CRITICAL_SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        cd "$service"
        if mvn compile -DskipTests -q 2>/dev/null; then
            echo "- ✅ $service - BUILD SUCCESS" >> "../$REPORT_FILE"
        else
            echo "- ❌ $service - BUILD FAILED" >> "../$REPORT_FILE"
        fi
        cd ..
    fi
done

echo ""
echo "Status report saved to: $REPORT_FILE"