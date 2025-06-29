#!/bin/bash

echo "=== Fixing GroupIds for all Social Commerce Services ==="
echo "Date: $(date)"
echo ""

# List of services that need groupId fixes
SERVICES=(
    "admin-finalization"
    "admin-interfaces"
    "analytics-service"
    "api-gateway"
    "fulfillment-options"
    "integration-optimization"
    "integration-performance"
    "invoice-service"
    "localization-service"
    "marketplace"
    "multi-currency-service"
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
FAILED_COUNT=0

for service in "${SERVICES[@]}"; do
    if [ -f "$service/pom.xml" ]; then
        echo "Fixing groupId in $service/pom.xml..."
        
        # Backup original
        cp "$service/pom.xml" "$service/pom.xml.backup"
        
        # Fix groupId - replace common incorrect patterns
        sed -i 's/<groupId>org\.springframework\.boot</<groupId>com.exalt.socialcommerce</g' "$service/pom.xml"
        sed -i 's/<groupId>com\.socialcommerce</<groupId>com.exalt.socialcommerce</g' "$service/pom.xml"
        
        # Verify the change
        if grep -q "<groupId>com.exalt.socialcommerce</groupId>" "$service/pom.xml"; then
            echo "✅ Fixed groupId for $service"
            FIXED_COUNT=$((FIXED_COUNT + 1))
        else
            echo "❌ Failed to fix groupId for $service"
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo "⚠️ No pom.xml found for $service"
    fi
    echo ""
done

echo "=== Summary ==="
echo "Fixed: $FIXED_COUNT services"
echo "Failed: $FAILED_COUNT services"
echo "Script completed at: $(date)"