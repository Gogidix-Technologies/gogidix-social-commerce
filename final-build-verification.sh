#!/bin/bash

echo "=== Final Build Verification for All Services ==="
echo "Date: $(date)"
echo ""

# Output file
RESULTS_FILE="PHASE2_FINAL_BUILD_RESULTS.txt"
echo "Final Build Verification Results - $(date)" > "$RESULTS_FILE"
echo "==========================================" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"

# Java services array
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

# Counters
SUCCESS=0
FAILURE=0

echo "## Java Services Build Results" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"

# Test Java compilation
echo "Testing Java services compilation..."
echo ""

for service in "${JAVA_SERVICES[@]}"; do
    echo -n "Building $service... "
    echo "### $service" >> "$RESULTS_FILE"
    
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        cd "$service"
        
        # Compile quietly
        if mvn clean compile -DskipTests -q 2>/dev/null; then
            echo "✅ SUCCESS"
            echo "✅ BUILD SUCCESS" >> "../$RESULTS_FILE"
            SUCCESS=$((SUCCESS + 1))
        else
            echo "❌ FAILED"
            echo "❌ BUILD FAILED" >> "../$RESULTS_FILE"
            # Capture error for report
            mvn compile -DskipTests 2>&1 | grep -E "ERROR|error:" | head -5 >> "../$RESULTS_FILE"
            FAILURE=$((FAILURE + 1))
        fi
        
        cd ..
    else
        echo "⚠️ NO POM"
        echo "⚠️ No pom.xml found" >> "$RESULTS_FILE"
        FAILURE=$((FAILURE + 1))
    fi
    
    echo "" >> "$RESULTS_FILE"
done

# Summary
echo "" >> "$RESULTS_FILE"
echo "## Build Summary" >> "$RESULTS_FILE"
echo "===============" >> "$RESULTS_FILE"
echo "Total Services: ${#JAVA_SERVICES[@]}" >> "$RESULTS_FILE"
echo "✅ Success: $SUCCESS" >> "$RESULTS_FILE"
echo "❌ Failed: $FAILURE" >> "$RESULTS_FILE"
echo "Success Rate: $(( SUCCESS * 100 / ${#JAVA_SERVICES[@]} ))%" >> "$RESULTS_FILE"

echo ""
echo "=== Build Verification Complete ==="
echo "Success: $SUCCESS / ${#JAVA_SERVICES[@]}"
echo "Failed: $FAILURE"
echo "Results saved to: $RESULTS_FILE"