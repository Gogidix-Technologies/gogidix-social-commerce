#!/bin/bash

echo "=== Running Unit Tests ==="
echo "Date: $(date)"
echo ""

SERVICES=(
    "analytics-service"
    "commission-service"
    "multi-currency-service"
    "subscription-service"
)

TOTAL=0
PASSED=0
FAILED=0

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo "Testing $service..."
        cd "$service"
        
        if mvn test -q; then
            echo "  ✅ Tests passed"
            PASSED=$((PASSED + 1))
        else
            echo "  ❌ Tests failed"
            FAILED=$((FAILED + 1))
        fi
        
        TOTAL=$((TOTAL + 1))
        cd ..
    fi
done

echo ""
echo "=== Test Summary ==="
echo "Total services tested: $TOTAL"
echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo "Success rate: $(( PASSED * 100 / TOTAL ))%"
