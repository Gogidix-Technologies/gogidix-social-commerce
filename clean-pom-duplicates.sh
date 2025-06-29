#!/bin/bash

echo "=== Cleaning Duplicate Dependencies from POMs ==="
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
        echo "Cleaning $service/pom.xml..."
        
        # Remove duplicate dependencies from dependencyManagement section
        # Keep only spring-cloud-dependencies in dependencyManagement
        sed -i '/<dependencyManagement>/,/<\/dependencyManagement>/{
            /<dependency>/,/<\/dependency>/{
                /spring-cloud-dependencies/!{
                    /groupId.*springframework.boot/d
                    /groupId.*postgresql/d
                    /groupId.*h2database/d
                    /groupId.*springdoc/d
                    /artifactId.*spring-boot-starter-data-jpa/d
                    /artifactId.*postgresql/d
                    /artifactId.*h2/d
                    /artifactId.*spring-boot-starter-validation/d
                    /artifactId.*spring-boot-starter-security/d
                    /artifactId.*springdoc-openapi-starter-webmvc-ui/d
                    /artifactId.*spring-cloud-starter-gateway/d
                    /<scope>runtime<\/scope>/d
                    /<version>2.2.0<\/version>/d
                    /<dependency>$/d
                    /<\/dependency>$/d
                }
            }
        }' "$service/pom.xml"
        
        # Clean up empty lines in dependencyManagement
        sed -i '/<dependencyManagement>/,/<\/dependencyManagement>/{
            /^[[:space:]]*$/d
        }' "$service/pom.xml"
        
        echo "✅ Cleaned $service"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

echo ""
echo "=== Summary ==="
echo "Cleaned $FIXED_COUNT POM files"
echo "Script completed at: $(date)"