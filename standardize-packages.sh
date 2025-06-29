#!/bin/bash

echo "=== Standardizing package names to com.exalt ==="

# Define the services to process
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

# Counter for changes
TOTAL_CHANGES=0

for service in "${JAVA_SERVICES[@]}"; do
    echo "Processing $service..."
    
    if [ -d "$service" ]; then
        # Find all Java files
        find "$service" -name "*.java" -type f | while read -r file; do
            # Check if file contains non-com.exalt packages
            if grep -q "^package com\.socialecommerceecosystem" "$file" 2>/dev/null; then
                echo "  Updating: $file"
                sed -i 's/package com\.socialecommerceecosystem/package com.exalt/g' "$file"
                ((TOTAL_CHANGES++))
            elif grep -q "^package com\.ecosystem" "$file" 2>/dev/null; then
                echo "  Updating: $file"
                sed -i 's/package com\.ecosystem/package com.exalt.ecosystem/g' "$file"
                ((TOTAL_CHANGES++))
            elif grep -q "^import com\.socialecommerceecosystem" "$file" 2>/dev/null; then
                echo "  Updating imports in: $file"
                sed -i 's/import com\.socialecommerceecosystem/import com.exalt/g' "$file"
            elif grep -q "^import com\.ecosystem" "$file" 2>/dev/null; then
                echo "  Updating imports in: $file"
                sed -i 's/import com\.ecosystem/import com.exalt.ecosystem/g' "$file"
            fi
        done
        
        # Also update pom.xml groupId if needed
        if [ -f "$service/pom.xml" ]; then
            if grep -q "<groupId>com\.socialecommerceecosystem</groupId>" "$service/pom.xml" 2>/dev/null; then
                echo "  Updating pom.xml groupId"
                sed -i 's/<groupId>com\.socialecommerceecosystem<\/groupId>/<groupId>com.exalt<\/groupId>/g' "$service/pom.xml"
            fi
            if grep -q "<groupId>com\.ecosystem</groupId>" "$service/pom.xml" 2>/dev/null; then
                echo "  Updating pom.xml groupId"
                sed -i 's/<groupId>com\.ecosystem<\/groupId>/<groupId>com.exalt.ecosystem<\/groupId>/g' "$service/pom.xml"
            fi
        fi
    fi
done

echo "=== Package standardization complete ==="
echo "Total files processed. Please run 'mvn clean compile' to verify."