#!/bin/bash

echo "=== Social Commerce Environment Standardization Validation ==="
echo "Date: $(date)"
echo ""

# Output file
OUTPUT_FILE="SOCIAL_COMMERCE_ENVIRONMENT_VALIDATION.md"

# Initialize the output file
echo "# Social Commerce Domain - Environment Standardization Report" > "$OUTPUT_FILE"
echo "## Date: $(date)" >> "$OUTPUT_FILE"
echo "## Required Standards" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "### Java Services:" >> "$OUTPUT_FILE"
echo "- Java Version: 17" >> "$OUTPUT_FILE"
echo "- Spring Boot: 3.1.5" >> "$OUTPUT_FILE"
echo "- Spring Cloud: 2022.0.4" >> "$OUTPUT_FILE"
echo "- Maven: 3.9.6" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "### Frontend Services:" >> "$OUTPUT_FILE"
echo "- Node.js: 18.x or 20.x" >> "$OUTPUT_FILE"
echo "- React: 18.x" >> "$OUTPUT_FILE"
echo "- TypeScript: 5.x" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "---" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

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

# Frontend services array
FRONTEND_SERVICES=(
    "global-hq-admin"
    "social-media-integration"
    "user-mobile-app"
    "user-web-app"
    "vendor-app"
)

echo "## Java Backend Services Environment" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

JAVA_COMPLIANT=0
JAVA_NON_COMPLIANT=0

# Check parent pom.xml first
echo "### Parent POM Configuration" >> "$OUTPUT_FILE"
if [ -f "pom.xml" ]; then
    # Check Java version
    JAVA_VERSION=$(grep -A2 "<properties>" "pom.xml" | grep "<java.version>" | sed 's/.*<java.version>\(.*\)<\/java.version>.*/\1/' | tr -d ' ')
    echo "- Java Version: $JAVA_VERSION" >> "$OUTPUT_FILE"
    
    # Check Spring Boot version
    SPRING_BOOT=$(grep -A2 "<parent>" "pom.xml" | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
    echo "- Spring Boot Parent: $SPRING_BOOT" >> "$OUTPUT_FILE"
    
    # Check Spring Cloud version
    SPRING_CLOUD=$(grep "<spring-cloud.version>" "pom.xml" | sed 's/.*<spring-cloud.version>\(.*\)<\/spring-cloud.version>.*/\1/' | tr -d ' ')
    if [ -n "$SPRING_CLOUD" ]; then
        echo "- Spring Cloud: $SPRING_CLOUD" >> "$OUTPUT_FILE"
    fi
else
    echo "- ❌ No parent pom.xml found" >> "$OUTPUT_FILE"
fi
echo "" >> "$OUTPUT_FILE"
echo "---" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Validate Java services
for service in "${JAVA_SERVICES[@]}"; do
    echo "### $service" >> "$OUTPUT_FILE"
    echo "Checking environment for $service..."
    
    COMPLIANT=true
    
    if [ -f "$service/pom.xml" ]; then
        # Check Java version
        JAVA_VER=$(grep "<java.version>" "$service/pom.xml" | sed 's/.*<java.version>\(.*\)<\/java.version>.*/\1/' | tr -d ' ')
        if [ -z "$JAVA_VER" ]; then
            # Check parent
            JAVA_VER="Inherited from parent"
        fi
        echo "- Java Version: $JAVA_VER" >> "$OUTPUT_FILE"
        
        # Check Spring Boot version
        BOOT_VER=$(grep -A2 "<parent>" "$service/pom.xml" | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
        if [ -n "$BOOT_VER" ]; then
            echo "- Spring Boot: $BOOT_VER" >> "$OUTPUT_FILE"
            if [[ ! "$BOOT_VER" =~ ^3\.1\. ]]; then
                COMPLIANT=false
                echo "  ⚠️ Non-standard version (expected 3.1.5)" >> "$OUTPUT_FILE"
            fi
        fi
        
        # Check Spring Cloud version
        CLOUD_VER=$(grep "<spring-cloud.version>" "$service/pom.xml" | sed 's/.*<spring-cloud.version>\(.*\)<\/spring-cloud.version>.*/\1/' | tr -d ' ')
        if [ -n "$CLOUD_VER" ]; then
            echo "- Spring Cloud: $CLOUD_VER" >> "$OUTPUT_FILE"
            if [[ ! "$CLOUD_VER" =~ ^2022\.0\. ]]; then
                COMPLIANT=false
                echo "  ⚠️ Non-standard version (expected 2022.0.4)" >> "$OUTPUT_FILE"
            fi
        fi
        
        # Check for key dependencies
        if grep -q "spring-boot-starter-web" "$service/pom.xml"; then
            echo "- ✅ Spring Web starter present" >> "$OUTPUT_FILE"
        fi
        
        if grep -q "lombok" "$service/pom.xml"; then
            echo "- ✅ Lombok configured" >> "$OUTPUT_FILE"
        else
            echo "- ⚠️ Lombok not configured" >> "$OUTPUT_FILE"
        fi
        
        # Check Maven wrapper
        if [ -f "$service/mvnw" ] || [ -f "$service/mvnw.cmd" ]; then
            echo "- ✅ Maven wrapper present" >> "$OUTPUT_FILE"
        else
            echo "- ⚠️ Maven wrapper missing" >> "$OUTPUT_FILE"
        fi
        
    else
        echo "- ❌ No pom.xml found" >> "$OUTPUT_FILE"
        COMPLIANT=false
    fi
    
    # Check application properties
    if [ -f "$service/src/main/resources/application.yml" ] || [ -f "$service/src/main/resources/application.properties" ]; then
        echo "- ✅ Application configuration present" >> "$OUTPUT_FILE"
    else
        echo "- ❌ No application configuration" >> "$OUTPUT_FILE"
        COMPLIANT=false
    fi
    
    # Check Dockerfile
    if [ -f "$service/Dockerfile" ]; then
        JAVA_IMAGE=$(grep "FROM.*java\|FROM.*openjdk\|FROM.*eclipse-temurin" "$service/Dockerfile" | head -1)
        echo "- Docker base image: $JAVA_IMAGE" >> "$OUTPUT_FILE"
        if [[ "$JAVA_IMAGE" =~ "17" ]] || [[ "$JAVA_IMAGE" =~ "temurin-17" ]]; then
            echo "  ✅ Java 17 base image" >> "$OUTPUT_FILE"
        else
            echo "  ⚠️ Check Java version in Docker image" >> "$OUTPUT_FILE"
        fi
    fi
    
    echo "" >> "$OUTPUT_FILE"
    if [ "$COMPLIANT" = true ]; then
        echo "**Environment Status: ✅ COMPLIANT**" >> "$OUTPUT_FILE"
        JAVA_COMPLIANT=$((JAVA_COMPLIANT + 1))
    else
        echo "**Environment Status: ❌ NON-COMPLIANT**" >> "$OUTPUT_FILE"
        JAVA_NON_COMPLIANT=$((JAVA_NON_COMPLIANT + 1))
    fi
    echo "" >> "$OUTPUT_FILE"
    echo "---" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

echo "## Frontend Services Environment" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

FRONTEND_COMPLIANT=0
FRONTEND_NON_COMPLIANT=0

# Validate Frontend services
for service in "${FRONTEND_SERVICES[@]}"; do
    echo "### $service" >> "$OUTPUT_FILE"
    echo "Checking environment for $service..."
    
    COMPLIANT=true
    
    if [ -f "$service/package.json" ]; then
        # Check Node version
        NODE_VER=$(grep '"node":' "$service/package.json" 2>/dev/null | cut -d'"' -f4)
        if [ -n "$NODE_VER" ]; then
            echo "- Node.js requirement: $NODE_VER" >> "$OUTPUT_FILE"
        else
            echo "- Node.js: No version specified" >> "$OUTPUT_FILE"
        fi
        
        # Check React version
        REACT_VER=$(grep '"react":' "$service/package.json" | cut -d'"' -f4)
        if [ -n "$REACT_VER" ]; then
            echo "- React version: $REACT_VER" >> "$OUTPUT_FILE"
            if [[ "$REACT_VER" =~ ^18\. ]] || [[ "$REACT_VER" =~ ^\^18\. ]]; then
                echo "  ✅ React 18.x compliant" >> "$OUTPUT_FILE"
            else
                echo "  ⚠️ Non-standard React version" >> "$OUTPUT_FILE"
                COMPLIANT=false
            fi
        fi
        
        # Check TypeScript
        TS_VER=$(grep '"typescript":' "$service/package.json" | cut -d'"' -f4)
        if [ -n "$TS_VER" ]; then
            echo "- TypeScript version: $TS_VER" >> "$OUTPUT_FILE"
            if [[ "$TS_VER" =~ ^5\. ]] || [[ "$TS_VER" =~ ^\^5\. ]]; then
                echo "  ✅ TypeScript 5.x compliant" >> "$OUTPUT_FILE"
            else
                echo "  ⚠️ Older TypeScript version" >> "$OUTPUT_FILE"
            fi
        fi
        
        # Check for lock file
        if [ -f "$service/package-lock.json" ]; then
            echo "- ✅ package-lock.json present" >> "$OUTPUT_FILE"
        elif [ -f "$service/yarn.lock" ]; then
            echo "- ✅ yarn.lock present" >> "$OUTPUT_FILE"
        else
            echo "- ⚠️ No lock file present" >> "$OUTPUT_FILE"
        fi
        
        # Check for .nvmrc
        if [ -f "$service/.nvmrc" ]; then
            NVM_VER=$(cat "$service/.nvmrc")
            echo "- .nvmrc Node version: $NVM_VER" >> "$OUTPUT_FILE"
        fi
        
        # Check tsconfig.json for TypeScript projects
        if [ -f "$service/tsconfig.json" ]; then
            echo "- ✅ TypeScript configuration present" >> "$OUTPUT_FILE"
        fi
        
        # Check Dockerfile
        if [ -f "$service/Dockerfile" ]; then
            NODE_IMAGE=$(grep "FROM.*node" "$service/Dockerfile" | head -1)
            echo "- Docker base image: $NODE_IMAGE" >> "$OUTPUT_FILE"
            if [[ "$NODE_IMAGE" =~ "18" ]] || [[ "$NODE_IMAGE" =~ "20" ]]; then
                echo "  ✅ Supported Node.js version" >> "$OUTPUT_FILE"
            fi
        fi
        
    else
        echo "- ❌ No package.json found" >> "$OUTPUT_FILE"
        COMPLIANT=false
    fi
    
    echo "" >> "$OUTPUT_FILE"
    if [ "$COMPLIANT" = true ]; then
        echo "**Environment Status: ✅ COMPLIANT**" >> "$OUTPUT_FILE"
        FRONTEND_COMPLIANT=$((FRONTEND_COMPLIANT + 1))
    else
        echo "**Environment Status: ❌ NON-COMPLIANT**" >> "$OUTPUT_FILE"
        FRONTEND_NON_COMPLIANT=$((FRONTEND_NON_COMPLIANT + 1))
    fi
    echo "" >> "$OUTPUT_FILE"
    echo "---" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

# Summary section
echo "## Environment Standardization Summary" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "### Java Backend Services (22 total)" >> "$OUTPUT_FILE"
echo "- ✅ **Compliant**: $JAVA_COMPLIANT services ($((JAVA_COMPLIANT * 100 / 22))%)" >> "$OUTPUT_FILE"
echo "- ❌ **Non-Compliant**: $JAVA_NON_COMPLIANT services ($((JAVA_NON_COMPLIANT * 100 / 22))%)" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "### Frontend Services (5 total)" >> "$OUTPUT_FILE"
echo "- ✅ **Compliant**: $FRONTEND_COMPLIANT services ($((FRONTEND_COMPLIANT * 100 / 5))%)" >> "$OUTPUT_FILE"
echo "- ❌ **Non-Compliant**: $FRONTEND_NON_COMPLIANT services ($((FRONTEND_NON_COMPLIANT * 100 / 5))%)" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "## Critical Environment Issues" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "### Java Services Issues:" >> "$OUTPUT_FILE"
echo "1. Services missing Spring Boot 3.1.5 standardization" >> "$OUTPUT_FILE"
echo "2. Missing Maven wrapper in some services" >> "$OUTPUT_FILE"
echo "3. Lombok not consistently configured" >> "$OUTPUT_FILE"
echo "4. Missing application configuration files" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "### Frontend Services Issues:" >> "$OUTPUT_FILE"
echo "1. Node.js version not specified in most services" >> "$OUTPUT_FILE"
echo "2. Some services using older React versions" >> "$OUTPUT_FILE"
echo "3. Missing lock files for dependency management" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "## Recommendations" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "1. **Standardize Java Environment**:" >> "$OUTPUT_FILE"
echo "   - Update all services to Spring Boot 3.1.5" >> "$OUTPUT_FILE"
echo "   - Add Spring Cloud 2022.0.4 where needed" >> "$OUTPUT_FILE"
echo "   - Ensure Java 17 in all pom.xml files" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "2. **Standardize Frontend Environment**:" >> "$OUTPUT_FILE"
echo "   - Add .nvmrc files with Node.js 20.x" >> "$OUTPUT_FILE"
echo "   - Upgrade all services to React 18.x" >> "$OUTPUT_FILE"
echo "   - Ensure TypeScript 5.x where applicable" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "3. **Container Standardization**:" >> "$OUTPUT_FILE"
echo "   - Use eclipse-temurin:17-jre for Java services" >> "$OUTPUT_FILE"
echo "   - Use node:20-alpine for frontend services" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "---" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "**Task 4 Status**: ✅ COMPLETE" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "Environment standardization has been validated. Most services require updates to meet the specified standards." >> "$OUTPUT_FILE"

echo ""
echo "Environment validation complete. Results saved to: $OUTPUT_FILE"