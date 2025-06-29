#\!/bin/bash

echo "=== Quick Build Test for Critical Services ==="
echo "Date: $(date)"
echo ""

# Test only the previously failing services
FAILING_SERVICES=(
    "analytics-service"
    "order-service"
    "payment-gateway"
    "payout-service"
)

SUCCESS=0
FAILURE=0

for service in "${FAILING_SERVICES[@]}"; do
    echo -n "Testing $service... "
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
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
echo "Summary: $SUCCESS / ${#FAILING_SERVICES[@]} services building successfully"
EOF < /dev/null
