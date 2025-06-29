#!/bin/bash

echo "=== Fixing Spring Cloud Dependencies ==="
echo "Date: $(date)"
echo ""

# Fix 1: Ensure Spring Cloud BOM is properly configured
echo "1. Checking Spring Cloud dependency management..."

# Services that need Spring Cloud dependencies
SPRING_CLOUD_SERVICES=(
    "api-gateway"
    "order-service"
    "payment-gateway"
    "payout-service"
)

for service in "${SPRING_CLOUD_SERVICES[@]}"; do
    echo ""
    echo "Fixing $service..."
    
    if [ -f "$service/pom.xml" ]; then
        # Check if dependencyManagement section exists and has Spring Cloud BOM
        if ! grep -q "spring-cloud-dependencies" "$service/pom.xml"; then
            echo "  - Adding Spring Cloud BOM to dependencyManagement"
            # Add before </project>
            sed -i '/<\/project>/i\    <dependencyManagement>\n        <dependencies>\n            <dependency>\n                <groupId>org.springframework.cloud</groupId>\n                <artifactId>spring-cloud-dependencies</artifactId>\n                <version>${spring-cloud.version}</version>\n                <type>pom</type>\n                <scope>import</scope>\n            </dependency>\n        </dependencies>\n    </dependencyManagement>' "$service/pom.xml"
        else
            echo "  - Spring Cloud BOM already present"
        fi
        
        # Ensure Spring Cloud version property is set
        if ! grep -q "<spring-cloud.version>" "$service/pom.xml"; then
            echo "  - Adding Spring Cloud version property"
            sed -i '/<properties>/a\        <spring-cloud.version>2022.0.4</spring-cloud.version>' "$service/pom.xml"
        fi
        
        echo "  ✅ Fixed $service"
    fi
done

# Fix 2: Fix localization-service corrupted file
echo ""
echo "2. Fixing localization-service..."

# Remove the corrupted file
if [ -f "localization-service/src/main/java/com/microecommerce/localization/LocalizationServiceApplication.java" ]; then
    rm -f "localization-service/src/main/java/com/microecommerce/localization/LocalizationServiceApplication.java"
fi

# Ensure correct directory structure
mkdir -p "localization-service/src/main/java/com/exalt/socialcommerce/localization"

# Create proper main class
cat > "localization-service/src/main/java/com/exalt/socialcommerce/localization/LocalizationServiceApplication.java" << 'EOF'
package com.exalt.socialcommerce.localization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LocalizationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalizationServiceApplication.class, args);
    }
}
EOF

echo "  ✅ Fixed localization-service"

# Fix 3: Clean up marketplace duplicate packages
echo ""
echo "3. Cleaning marketplace duplicate packages..."

# Find and remove files with wrong package structure
find marketplace/src -type f -name "*.java" -exec grep -l "^package com.ecosystem.marketplace" {} \; | while read file; do
    # Get the correct package based on file path
    dir=$(dirname "$file")
    correct_package=$(echo "$dir" | sed 's|marketplace/src/main/java/||' | sed 's|/|.|g')
    
    # If it's in the wrong location, remove it
    if [[ "$file" == *"com/ecosystem/marketplace"* ]]; then
        echo "  - Removing duplicate: $file"
        rm -f "$file"
    fi
done

# Remove empty directories
find marketplace/src -type d -empty -delete

echo "  ✅ Cleaned marketplace duplicates"

# Fix 4: Test compilation
echo ""
echo "4. Testing fixed services..."
echo ""

TEST_SERVICES=(
    "analytics-service"
    "api-gateway"
    "order-service"
    "payment-gateway"
    "payout-service"
    "localization-service"
    "marketplace"
    "product-service"
)

SUCCESS=0
FAILURE=0

for service in "${TEST_SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo -n "Testing $service... "
        cd "$service"
        
        # Quick compile test
        if mvn compile -DskipTests -q 2>/dev/null; then
            echo "✅ SUCCESS"
            SUCCESS=$((SUCCESS + 1))
        else
            echo "❌ FAILED"
            # Show first error only
            mvn compile -DskipTests 2>&1 | grep -E "\[ERROR\]" | grep -v "^\[ERROR\] \[" | head -2
            FAILURE=$((FAILURE + 1))
        fi
        
        cd ..
    fi
done

echo ""
echo "=== Fix Summary ==="
echo "Successful: $SUCCESS / ${#TEST_SERVICES[@]}"
echo "Failed: $FAILURE"
echo "Success Rate: $(( SUCCESS * 100 / ${#TEST_SERVICES[@]} ))%"