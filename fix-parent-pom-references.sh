#!/bin/bash

echo "=== Fixing Parent POM References ==="
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
        echo "Fixing parent POM reference in $service..."
        
        # Create a temporary file with correct parent POM
        cat > "$service/pom-temp.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version>
        <relativePath/>
    </parent>

    <groupId>com.exalt.socialcommerce</groupId>
EOF
        
        # Extract artifactId from original pom
        ARTIFACT_ID=$(grep -m1 "<artifactId>" "$service/pom.xml" | grep -v "spring-boot" | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d '\t ')
        echo "    <artifactId>$ARTIFACT_ID</artifactId>" >> "$service/pom-temp.xml"
        echo "    <version>1.0.0</version>" >> "$service/pom-temp.xml"
        echo "    <name>$ARTIFACT_ID</name>" >> "$service/pom-temp.xml"
        echo "" >> "$service/pom-temp.xml"
        echo "    <properties>" >> "$service/pom-temp.xml"
        echo "        <java.version>17</java.version>" >> "$service/pom-temp.xml"
        echo "        <spring-cloud.version>2022.0.4</spring-cloud.version>" >> "$service/pom-temp.xml"
        echo "    </properties>" >> "$service/pom-temp.xml"
        echo "" >> "$service/pom-temp.xml"
        
        # Extract dependencies section if it exists
        if grep -q "<dependencies>" "$service/pom.xml"; then
            echo "    <dependencies>" >> "$service/pom-temp.xml"
            
            # Add basic Spring Boot dependencies
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-boot-starter-web</artifactId>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.cloud</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-boot-starter-actuator</artifactId>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.projectlombok</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>lombok</artifactId>" >> "$service/pom-temp.xml"
            echo "            <optional>true</optional>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-boot-starter-test</artifactId>" >> "$service/pom-temp.xml"
            echo "            <scope>test</scope>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            
            # If service has JPA dependencies, add them
            if grep -q "jpa\|data-jpa\|Entity" "$service/pom.xml"; then
                echo "        <dependency>" >> "$service/pom-temp.xml"
                echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
                echo "            <artifactId>spring-boot-starter-data-jpa</artifactId>" >> "$service/pom-temp.xml"
                echo "        </dependency>" >> "$service/pom-temp.xml"
                echo "        <dependency>" >> "$service/pom-temp.xml"
                echo "            <groupId>org.postgresql</groupId>" >> "$service/pom-temp.xml"
                echo "            <artifactId>postgresql</artifactId>" >> "$service/pom-temp.xml"
                echo "            <scope>runtime</scope>" >> "$service/pom-temp.xml"
                echo "        </dependency>" >> "$service/pom-temp.xml"
                echo "        <dependency>" >> "$service/pom-temp.xml"
                echo "            <groupId>com.h2database</groupId>" >> "$service/pom-temp.xml"
                echo "            <artifactId>h2</artifactId>" >> "$service/pom-temp.xml"
                echo "            <scope>runtime</scope>" >> "$service/pom-temp.xml"
                echo "        </dependency>" >> "$service/pom-temp.xml"
            fi
            
            # If it has validation
            if grep -q "validation\|@Valid" "$service/pom.xml" || [ "$service" == "admin-finalization" ]; then
                echo "        <dependency>" >> "$service/pom-temp.xml"
                echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
                echo "            <artifactId>spring-boot-starter-validation</artifactId>" >> "$service/pom-temp.xml"
                echo "        </dependency>" >> "$service/pom-temp.xml"
            fi
            
            echo "    </dependencies>" >> "$service/pom-temp.xml"
        else
            # Add minimal dependencies
            echo "    <dependencies>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-boot-starter-web</artifactId>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.cloud</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "        <dependency>" >> "$service/pom-temp.xml"
            echo "            <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
            echo "            <artifactId>spring-boot-starter-test</artifactId>" >> "$service/pom-temp.xml"
            echo "            <scope>test</scope>" >> "$service/pom-temp.xml"
            echo "        </dependency>" >> "$service/pom-temp.xml"
            echo "    </dependencies>" >> "$service/pom-temp.xml"
        fi
        
        echo "" >> "$service/pom-temp.xml"
        echo "    <dependencyManagement>" >> "$service/pom-temp.xml"
        echo "        <dependencies>" >> "$service/pom-temp.xml"
        echo "            <dependency>" >> "$service/pom-temp.xml"
        echo "                <groupId>org.springframework.cloud</groupId>" >> "$service/pom-temp.xml"
        echo "                <artifactId>spring-cloud-dependencies</artifactId>" >> "$service/pom-temp.xml"
        echo "                <version>\${spring-cloud.version}</version>" >> "$service/pom-temp.xml"
        echo "                <type>pom</type>" >> "$service/pom-temp.xml"
        echo "                <scope>import</scope>" >> "$service/pom-temp.xml"
        echo "            </dependency>" >> "$service/pom-temp.xml"
        echo "        </dependencies>" >> "$service/pom-temp.xml"
        echo "    </dependencyManagement>" >> "$service/pom-temp.xml"
        echo "" >> "$service/pom-temp.xml"
        echo "    <build>" >> "$service/pom-temp.xml"
        echo "        <plugins>" >> "$service/pom-temp.xml"
        echo "            <plugin>" >> "$service/pom-temp.xml"
        echo "                <groupId>org.springframework.boot</groupId>" >> "$service/pom-temp.xml"
        echo "                <artifactId>spring-boot-maven-plugin</artifactId>" >> "$service/pom-temp.xml"
        echo "                <configuration>" >> "$service/pom-temp.xml"
        echo "                    <excludes>" >> "$service/pom-temp.xml"
        echo "                        <exclude>" >> "$service/pom-temp.xml"
        echo "                            <groupId>org.projectlombok</groupId>" >> "$service/pom-temp.xml"
        echo "                            <artifactId>lombok</artifactId>" >> "$service/pom-temp.xml"
        echo "                        </exclude>" >> "$service/pom-temp.xml"
        echo "                    </excludes>" >> "$service/pom-temp.xml"
        echo "                </configuration>" >> "$service/pom-temp.xml"
        echo "            </plugin>" >> "$service/pom-temp.xml"
        echo "        </plugins>" >> "$service/pom-temp.xml"
        echo "    </build>" >> "$service/pom-temp.xml"
        echo "" >> "$service/pom-temp.xml"
        echo "</project>" >> "$service/pom-temp.xml"
        
        # Replace the original pom.xml
        mv "$service/pom.xml" "$service/pom.xml.broken"
        mv "$service/pom-temp.xml" "$service/pom.xml"
        
        echo "✅ Fixed $service"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

echo ""
echo "=== Summary ==="
echo "Fixed $FIXED_COUNT POM files"
echo "Script completed at: $(date)"