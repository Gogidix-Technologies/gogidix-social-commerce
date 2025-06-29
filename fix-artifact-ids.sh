#!/bin/bash

echo "=== Fixing Missing ArtifactIds in POMs ==="
echo "Date: $(date)"
echo ""

# List of all services
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

FIXED_COUNT=0

for service in "${SERVICES[@]}"; do
    if [ -f "$service/pom.xml" ]; then
        echo "Fixing artifactId in $service/pom.xml..."
        
        # Extract the service name for artifactId
        ARTIFACT_ID="$service"
        
        # Fix the empty artifactId and name
        sed -i "s|<artifactId></artifactId>|<artifactId>$ARTIFACT_ID</artifactId>|g" "$service/pom.xml"
        sed -i "s|<name></name>|<name>$ARTIFACT_ID</name>|g" "$service/pom.xml"
        
        echo "✅ Fixed $service (artifactId: $ARTIFACT_ID)"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

echo ""
echo "=== Summary ==="
echo "Fixed $FIXED_COUNT POM files"
echo "Script completed at: $(date)"