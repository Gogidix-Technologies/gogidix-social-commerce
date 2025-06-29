#!/bin/bash

echo "=== Standardizing Lombok across all Java services ==="

# Define the Lombok dependency to add
LOMBOK_DEPENDENCY='        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>'

# Define all Java services
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

SERVICES_UPDATED=0

for service in "${JAVA_SERVICES[@]}"; do
    if [ -f "$service/pom.xml" ]; then
        # Check if Lombok is already present
        if ! grep -q "lombok" "$service/pom.xml"; then
            echo "Adding Lombok to $service/pom.xml"
            
            # Add Lombok dependency before the closing </dependencies> tag
            sed -i '/<\/dependencies>/i\
        <dependency>\
            <groupId>org.projectlombok</groupId>\
            <artifactId>lombok</artifactId>\
            <optional>true</optional>\
        </dependency>' "$service/pom.xml"
            
            ((SERVICES_UPDATED++))
        else
            echo "$service already has Lombok"
        fi
    fi
done

echo "=== Lombok standardization complete ==="
echo "Services updated: $SERVICES_UPDATED"

# Now let's add common Lombok annotations to entity classes
echo ""
echo "=== Adding Lombok annotations to entity classes ==="

for service in "${JAVA_SERVICES[@]}"; do
    if [ -d "$service/src/main/java" ]; then
        # Find entity/model classes
        find "$service/src/main/java" -name "*.java" -type f | while read -r file; do
            # Check if it's likely an entity class (contains @Entity or class name ends with Entity/Model/DTO)
            if grep -q "@Entity" "$file" || [[ "$file" =~ (Entity|Model|DTO)\.java$ ]]; then
                # Check if Lombok is already used
                if ! grep -q "import lombok" "$file"; then
                    # Get the class name
                    CLASS_NAME=$(basename "$file" .java)
                    
                    # Add Lombok imports after package declaration
                    sed -i '/^package .*;$/a\\nimport lombok.Data;\nimport lombok.NoArgsConstructor;\nimport lombok.AllArgsConstructor;\nimport lombok.Builder;' "$file"
                    
                    # Add Lombok annotations before class declaration
                    sed -i "s/public class $CLASS_NAME/@Data\n@NoArgsConstructor\n@AllArgsConstructor\n@Builder\npublic class $CLASS_NAME/g" "$file"
                    
                    echo "  Added Lombok annotations to: $file"
                fi
            fi
        done
    fi
done

echo "=== Lombok annotation standardization complete ===" 