#!/bin/bash

echo "=== Social Commerce Test Analysis ==="
echo "Date: $(date)"
echo ""

# Create test results file
TEST_RESULTS="SOCIAL_COMMERCE_TEST_RESULTS.md"

echo "# Social Commerce Domain - Test Results" > "$TEST_RESULTS"
echo "## Date: $(date)" >> "$TEST_RESULTS"
echo "" >> "$TEST_RESULTS"

# Java services
JAVA_SERVICES=(
    "admin-finalization"
    "admin-interfaces"
    "analytics-service"
    "api-gateway"
    "commission-service"
    "fulfillment-options"
    "integration-optimization"
    "integration-performance"
    "invoice-service"
    "localization-service"
    "marketplace"
    "multi-currency-service"
    "order-service"
    "payment-gateway"
    "payout-service"
    "product-service"
    "regional-admin"
    "social-commerce-production"
    "social-commerce-shared"
    "social-commerce-staging"
    "subscription-service"
    "vendor-onboarding"
)

echo "## Java Services Test Analysis" >> "$TEST_RESULTS"
echo "" >> "$TEST_RESULTS"

# Analyze Java tests
for service in "${JAVA_SERVICES[@]}"; do
    echo "Analyzing $service tests..."
    echo "### $service" >> "$TEST_RESULTS"
    
    if [ -d "$service/src/test/java" ]; then
        # Count test files
        TEST_COUNT=$(find "$service/src/test/java" -name "*Test.java" -o -name "*Tests.java" 2>/dev/null | wc -l)
        echo "- **Test Files**: $TEST_COUNT" >> "$TEST_RESULTS"
        
        # Check test types
        UNIT_TESTS=$(find "$service/src/test/java" -name "*UnitTest.java" 2>/dev/null | wc -l)
        INTEGRATION_TESTS=$(find "$service/src/test/java" -name "*IntegrationTest.java" 2>/dev/null | wc -l)
        API_TESTS=$(find "$service/src/test/java" -name "*ApiTest.java" 2>/dev/null | wc -l)
        
        echo "- **Unit Tests**: $UNIT_TESTS" >> "$TEST_RESULTS"
        echo "- **Integration Tests**: $INTEGRATION_TESTS" >> "$TEST_RESULTS"
        echo "- **API Tests**: $API_TESTS" >> "$TEST_RESULTS"
        
        # Check for test dependencies
        if grep -q "spring-boot-starter-test" "$service/pom.xml" 2>/dev/null; then
            echo "- **Test Framework**: Spring Boot Test ✅" >> "$TEST_RESULTS"
        else
            echo "- **Test Framework**: Not configured ❌" >> "$TEST_RESULTS"
        fi
        
        # List test files
        if [ $TEST_COUNT -gt 0 ]; then
            echo "- **Test Classes**:" >> "$TEST_RESULTS"
            find "$service/src/test/java" -name "*Test.java" -o -name "*Tests.java" 2>/dev/null | while read test; do
                echo "  - \`$(basename $test)\`" >> "$TEST_RESULTS"
            done
        fi
    else
        echo "- **Test Directory**: Missing ❌" >> "$TEST_RESULTS"
    fi
    
    echo "" >> "$TEST_RESULTS"
done

# Frontend services
FRONTEND_SERVICES=(
    "global-hq-admin"
    "social-media-integration"
    "user-mobile-app"
    "user-web-app"
    "vendor-app"
)

echo "## Frontend Services Test Analysis" >> "$TEST_RESULTS"
echo "" >> "$TEST_RESULTS"

for service in "${FRONTEND_SERVICES[@]}"; do
    echo "Analyzing $service tests..."
    echo "### $service" >> "$TEST_RESULTS"
    
    if [ -f "$service/package.json" ]; then
        # Count test files
        TEST_COUNT=$(find "$service" -name "*.test.js" -o -name "*.test.ts" -o -name "*.test.tsx" -o -name "*.spec.js" -o -name "*.spec.ts" 2>/dev/null | grep -v node_modules | wc -l)
        echo "- **Test Files**: $TEST_COUNT" >> "$TEST_RESULTS"
        
        # Check for test script
        if grep -q '"test"' "$service/package.json"; then
            echo "- **Test Script**: Configured ✅" >> "$TEST_RESULTS"
            TEST_SCRIPT=$(grep '"test"' "$service/package.json" | head -1 | cut -d'"' -f4)
            echo "- **Test Command**: \`$TEST_SCRIPT\`" >> "$TEST_RESULTS"
        else
            echo "- **Test Script**: Not configured ❌" >> "$TEST_RESULTS"
        fi
        
        # Check test framework
        if grep -q "jest" "$service/package.json"; then
            echo "- **Test Framework**: Jest" >> "$TEST_RESULTS"
        elif grep -q "mocha" "$service/package.json"; then
            echo "- **Test Framework**: Mocha" >> "$TEST_RESULTS"
        elif grep -q "vitest" "$service/package.json"; then
            echo "- **Test Framework**: Vitest" >> "$TEST_RESULTS"
        else
            echo "- **Test Framework**: Not detected" >> "$TEST_RESULTS"
        fi
    else
        echo "- **Status**: No package.json ❌" >> "$TEST_RESULTS"
    fi
    
    echo "" >> "$TEST_RESULTS"
done

# Summary
echo "## Test Coverage Summary" >> "$TEST_RESULTS"
echo "" >> "$TEST_RESULTS"

# Java test coverage
JAVA_WITH_TESTS=$(find . -path "*/src/test/java/*Test.java" -o -path "*/src/test/java/*Tests.java" | grep -v node_modules | cut -d'/' -f2 | sort | uniq | wc -l)
echo "### Java Services" >> "$TEST_RESULTS"
echo "- **Total Services**: ${#JAVA_SERVICES[@]}" >> "$TEST_RESULTS"
echo "- **Services with Tests**: $JAVA_WITH_TESTS" >> "$TEST_RESULTS"
echo "- **Test Coverage**: $(( JAVA_WITH_TESTS * 100 / ${#JAVA_SERVICES[@]} ))%" >> "$TEST_RESULTS"
echo "" >> "$TEST_RESULTS"

# Frontend test coverage
FRONTEND_WITH_TESTS=0
for service in "${FRONTEND_SERVICES[@]}"; do
    if [ $(find "$service" -name "*.test.*" -o -name "*.spec.*" 2>/dev/null | grep -v node_modules | wc -l) -gt 0 ]; then
        ((FRONTEND_WITH_TESTS++))
    fi
done

echo "### Frontend Services" >> "$TEST_RESULTS"
echo "- **Total Services**: ${#FRONTEND_SERVICES[@]}" >> "$TEST_RESULTS"
echo "- **Services with Tests**: $FRONTEND_WITH_TESTS" >> "$TEST_RESULTS"
echo "- **Test Coverage**: $(( FRONTEND_WITH_TESTS * 100 / ${#FRONTEND_SERVICES[@]} ))%" >> "$TEST_RESULTS"

echo ""
echo "Test analysis complete. Results saved to: $TEST_RESULTS"