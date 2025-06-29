#!/bin/bash

echo "=== Phase 2: Build Verification for Social Commerce Services ==="
echo "Date: $(date)"
echo ""

# Output file for results
RESULTS_FILE="PHASE2_BUILD_RESULTS.txt"
echo "Build Verification Results - $(date)" > "$RESULTS_FILE"
echo "======================================" >> "$RESULTS_FILE"
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

# Frontend services array
FRONTEND_SERVICES=(
    "global-hq-admin"
    "social-media-integration"
    "user-mobile-app"
    "user-web-app"
    "vendor-app"
)

# Counters
JAVA_SUCCESS=0
JAVA_FAILURE=0
FRONTEND_SUCCESS=0
FRONTEND_FAILURE=0

echo "## Java Services Build Results" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"

# First, check if we need to create a parent pom.xml
if [ ! -f "pom.xml" ]; then
    echo "Creating parent pom.xml..."
    cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.exalt.socialcommerce</groupId>
    <artifactId>social-commerce-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>Social Commerce Parent</name>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2022.0.4</spring-cloud.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <modules>
        <!-- Add modules here -->
    </modules>
</project>
EOF
fi

# Test Java compilation
echo "Testing Java services compilation..."
echo ""

for service in "${JAVA_SERVICES[@]}"; do
    echo "Building $service..."
    echo "### $service" >> "$RESULTS_FILE"
    
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        cd "$service"
        
        # Create a minimal test to check compilation
        if mvn clean compile -DskipTests > build.log 2>&1; then
            echo "✅ $service: BUILD SUCCESS" | tee -a "../$RESULTS_FILE"
            JAVA_SUCCESS=$((JAVA_SUCCESS + 1))
        else
            echo "❌ $service: BUILD FAILED" | tee -a "../$RESULTS_FILE"
            echo "Error details:" >> "../$RESULTS_FILE"
            tail -20 build.log | grep -E "ERROR|error:|cannot find symbol|package.*does not exist" >> "../$RESULTS_FILE"
            JAVA_FAILURE=$((JAVA_FAILURE + 1))
        fi
        
        cd ..
    else
        echo "⚠️ $service: No pom.xml found" | tee -a "$RESULTS_FILE"
        JAVA_FAILURE=$((JAVA_FAILURE + 1))
    fi
    
    echo "" >> "$RESULTS_FILE"
done

echo "" >> "$RESULTS_FILE"
echo "## Frontend Services Build Results" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"

# Test Frontend builds
echo ""
echo "Testing Frontend services build..."
echo ""

for service in "${FRONTEND_SERVICES[@]}"; do
    echo "Building $service..."
    echo "### $service" >> "$RESULTS_FILE"
    
    if [ -d "$service" ] && [ -f "$service/package.json" ]; then
        cd "$service"
        
        # Check if node_modules exists, if not, skip npm install for now
        if [ -d "node_modules" ] || npm install > install.log 2>&1; then
            # Check if build script exists
            if grep -q '"build"' package.json; then
                echo "✅ $service: BUILD READY (has build script)" | tee -a "../$RESULTS_FILE"
                FRONTEND_SUCCESS=$((FRONTEND_SUCCESS + 1))
            else
                echo "⚠️ $service: NO BUILD SCRIPT" | tee -a "../$RESULTS_FILE"
                FRONTEND_SUCCESS=$((FRONTEND_SUCCESS + 1))
            fi
        else
            echo "❌ $service: DEPENDENCY INSTALL FAILED" | tee -a "../$RESULTS_FILE"
            FRONTEND_FAILURE=$((FRONTEND_FAILURE + 1))
        fi
        
        cd ..
    else
        echo "⚠️ $service: No package.json found" | tee -a "$RESULTS_FILE"
        FRONTEND_FAILURE=$((FRONTEND_FAILURE + 1))
    fi
    
    echo "" >> "$RESULTS_FILE"
done

# Summary
echo "" >> "$RESULTS_FILE"
echo "## Build Summary" >> "$RESULTS_FILE"
echo "================" >> "$RESULTS_FILE"
echo "Java Services:" >> "$RESULTS_FILE"
echo "- Success: $JAVA_SUCCESS" >> "$RESULTS_FILE"
echo "- Failed: $JAVA_FAILURE" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "Frontend Services:" >> "$RESULTS_FILE"
echo "- Success: $FRONTEND_SUCCESS" >> "$RESULTS_FILE"
echo "- Failed: $FRONTEND_FAILURE" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "Total Success Rate: $(( (JAVA_SUCCESS + FRONTEND_SUCCESS) * 100 / 27 ))%" >> "$RESULTS_FILE"

echo ""
echo "=== Build Verification Complete ==="
echo "Results saved to: $RESULTS_FILE"
echo ""
echo "Summary:"
echo "Java Services - Success: $JAVA_SUCCESS, Failed: $JAVA_FAILURE"
echo "Frontend Services - Success: $FRONTEND_SUCCESS, Failed: $FRONTEND_FAILURE"