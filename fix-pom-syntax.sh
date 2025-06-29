#!/bin/bash

echo "=== Fixing POM Syntax Issues ==="
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
        echo "Fixing $service/pom.xml..."
        
        # Fix the broken dependency management section
        sed -i '/<dependencyManagement>/,/<\/dependencyManagement>/{
            s/<dependencies>$/        <dependencies>/
            s/^[[:space:]]*<groupId>org.springframework.cloud<\/groupId>/            <dependency>\n                <groupId>org.springframework.cloud<\/groupId>/
            s/^[[:space:]]*<scope>import<\/scope>/                <scope>import<\/scope>\n            <\/dependency>/
        }' "$service/pom.xml"
        
        echo "✅ Fixed $service"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

echo ""
echo "=== Summary ==="
echo "Fixed $FIXED_COUNT POM files"
echo "Script completed at: $(date)"