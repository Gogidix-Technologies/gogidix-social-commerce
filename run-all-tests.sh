#!/bin/bash

echo "=== Running All Service Tests ==="
echo "Date: $(date)"
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Arrays for tracking
declare -a PASSED_SERVICES
declare -a FAILED_SERVICES

# Test each service
SERVICES=(
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
    "payout-service"
    "regional-admin"
    "subscription-service"
    "vendor-onboarding"
)

echo "Running tests for ${#SERVICES[@]} services..."
echo ""

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo -n "Testing $service... "
        cd "$service"
        
        # Run tests and capture output
        if mvn test -DskipTests=false -q > test-output.log 2>&1; then
            echo -e "${GREEN}✅ PASSED${NC}"
            PASSED_SERVICES+=("$service")
        else
            echo -e "${RED}❌ FAILED${NC}"
            FAILED_SERVICES+=("$service")
            # Show error summary
            tail -10 test-output.log | grep -E "ERROR|FAILURE" | head -3
        fi
        
        rm -f test-output.log
        cd ..
    else
        echo "⚠️  Skipping $service (no pom.xml)"
    fi
done

# Generate summary report
echo ""
echo "=== Test Execution Summary ==="
echo "Total services: ${#SERVICES[@]}"
echo -e "${GREEN}Passed: ${#PASSED_SERVICES[@]}${NC}"
echo -e "${RED}Failed: ${#FAILED_SERVICES[@]}${NC}"
echo "Success rate: $(( ${#PASSED_SERVICES[@]} * 100 / ${#SERVICES[@]} ))%"

# List failed services if any
if [ ${#FAILED_SERVICES[@]} -gt 0 ]; then
    echo ""
    echo "Failed services:"
    for failed in "${FAILED_SERVICES[@]}"; do
        echo "  - $failed"
    done
fi

# Create test report
cat > TEST_EXECUTION_REPORT.md << REPORT
# Test Execution Report

## Date: $(date)

## Summary
- Total services tested: ${#SERVICES[@]}
- Passed: ${#PASSED_SERVICES[@]}
- Failed: ${#FAILED_SERVICES[@]}
- Success rate: $(( ${#PASSED_SERVICES[@]} * 100 / ${#SERVICES[@]} ))%

## Passed Services
$(for service in "${PASSED_SERVICES[@]}"; do echo "- ✅ $service"; done)

## Failed Services
$(for service in "${FAILED_SERVICES[@]}"; do echo "- ❌ $service"; done)

## Recommendations
1. Fix failing tests in individual services
2. Add more comprehensive test cases
3. Configure code coverage reporting
4. Integrate with CI/CD pipeline
REPORT

echo ""
echo "Report saved to: TEST_EXECUTION_REPORT.md"
