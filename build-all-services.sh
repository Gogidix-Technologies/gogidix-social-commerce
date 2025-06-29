#!/bin/bash

echo "=== Social Commerce Build Process Starting ==="
echo "Date: $(date)"
echo ""

# Create build log file
BUILD_LOG="SOCIAL_COMMERCE_BUILD_LOG.md"
echo "# Social Commerce Domain - Build Log" > "$BUILD_LOG"
echo "## Date: $(date)" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"

# Java services
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

# Frontend services
FRONTEND_SERVICES=(
    "global-hq-admin"
    "social-media-integration"
    "user-mobile-app"
    "user-web-app"
    "vendor-app"
)

# Counters
JAVA_SUCCESS=0
JAVA_FAILED=0
FRONTEND_SUCCESS=0
FRONTEND_FAILED=0

echo "## Java Services Build Results" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"

# Build Java services
echo "=== Building Java Services ==="
for service in "${JAVA_SERVICES[@]}"; do
    echo "Building $service..."
    echo "### $service" >> "$BUILD_LOG"
    
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        cd "$service"
        
        # Run Maven clean compile
        if $(if [ -f "./mvnw" ]; then echo "./mvnw"; else echo "mvn"; fi) clean compile -DskipTests 2>&1 | tee -a "../build-$service.log" | grep -E "(BUILD SUCCESS|BUILD FAILURE)" | tail -1 | grep -q "BUILD SUCCESS"; then
            echo "✅ $service - BUILD SUCCESS"
            echo "✅ **Status**: BUILD SUCCESS" >> "../$BUILD_LOG"
            ((JAVA_SUCCESS++))
        else
            echo "❌ $service - BUILD FAILED"
            echo "❌ **Status**: BUILD FAILED" >> "../$BUILD_LOG"
            # Capture error summary
            echo "\`\`\`" >> "../$BUILD_LOG"
            tail -50 "../build-$service.log" | grep -E "(ERROR|error:|compilation failure)" | head -10 >> "../$BUILD_LOG"
            echo "\`\`\`" >> "../$BUILD_LOG"
            ((JAVA_FAILED++))
        fi
        
        cd ..
        echo "" >> "$BUILD_LOG"
    else
        echo "⚠️ $service - SKIPPED (no pom.xml)"
        echo "⚠️ **Status**: SKIPPED - No pom.xml found" >> "$BUILD_LOG"
        echo "" >> "$BUILD_LOG"
    fi
done

echo "" >> "$BUILD_LOG"
echo "## Frontend Services Build Results" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"

# Build Frontend services
echo ""
echo "=== Building Frontend Services ==="
for service in "${FRONTEND_SERVICES[@]}"; do
    echo "Building $service..."
    echo "### $service" >> "$BUILD_LOG"
    
    if [ -d "$service" ] && [ -f "$service/package.json" ]; then
        cd "$service"
        
        # For React Native, skip build
        if [ "$service" == "user-mobile-app" ]; then
            echo "ℹ️ $service - React Native app (build skipped)"
            echo "ℹ️ **Status**: React Native - Build skipped" >> "../$BUILD_LOG"
            ((FRONTEND_SUCCESS++))
        else
            # Check if build script exists
            if grep -q '"build"' package.json; then
                echo "Installing dependencies..."
                if npm install --legacy-peer-deps 2>&1 | tee -a "../npm-install-$service.log" | tail -5; then
                    echo "Running build..."
                    if npm run build 2>&1 | tee -a "../build-$service.log" | grep -E "(successfully|failed)" | tail -1 | grep -q "successfully"; then
                        echo "✅ $service - BUILD SUCCESS"
                        echo "✅ **Status**: BUILD SUCCESS" >> "../$BUILD_LOG"
                        ((FRONTEND_SUCCESS++))
                    else
                        echo "❌ $service - BUILD FAILED"
                        echo "❌ **Status**: BUILD FAILED" >> "../$BUILD_LOG"
                        ((FRONTEND_FAILED++))
                    fi
                else
                    echo "❌ $service - NPM INSTALL FAILED"
                    echo "❌ **Status**: NPM INSTALL FAILED" >> "../$BUILD_LOG"
                    ((FRONTEND_FAILED++))
                fi
            else
                echo "⚠️ $service - No build script"
                echo "⚠️ **Status**: No build script in package.json" >> "../$BUILD_LOG"
            fi
        fi
        
        cd ..
        echo "" >> "$BUILD_LOG"
    else
        echo "⚠️ $service - SKIPPED (no package.json)"
        echo "⚠️ **Status**: SKIPPED - No package.json found" >> "$BUILD_LOG"
        echo "" >> "$BUILD_LOG"
    fi
done

# Summary
echo "" >> "$BUILD_LOG"
echo "## Build Summary" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"
echo "### Java Services" >> "$BUILD_LOG"
echo "- Total: ${#JAVA_SERVICES[@]}" >> "$BUILD_LOG"
echo "- Success: $JAVA_SUCCESS" >> "$BUILD_LOG"
echo "- Failed: $JAVA_FAILED" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"
echo "### Frontend Services" >> "$BUILD_LOG"
echo "- Total: ${#FRONTEND_SERVICES[@]}" >> "$BUILD_LOG"
echo "- Success: $FRONTEND_SUCCESS" >> "$BUILD_LOG"
echo "- Failed: $FRONTEND_FAILED" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"

TOTAL_SUCCESS=$((JAVA_SUCCESS + FRONTEND_SUCCESS))
TOTAL_FAILED=$((JAVA_FAILED + FRONTEND_FAILED))
TOTAL_SERVICES=$((${#JAVA_SERVICES[@]} + ${#FRONTEND_SERVICES[@]}))

echo "### Overall" >> "$BUILD_LOG"
echo "- Total Services: $TOTAL_SERVICES" >> "$BUILD_LOG"
echo "- Build Success: $TOTAL_SUCCESS" >> "$BUILD_LOG"
echo "- Build Failed: $TOTAL_FAILED" >> "$BUILD_LOG"
echo "- Success Rate: $(( TOTAL_SUCCESS * 100 / TOTAL_SERVICES ))%" >> "$BUILD_LOG"

echo ""
echo "=== Build Process Complete ==="
echo "Java Services - Success: $JAVA_SUCCESS, Failed: $JAVA_FAILED"
echo "Frontend Services - Success: $FRONTEND_SUCCESS, Failed: $FRONTEND_FAILED"
echo "Build log saved to: $BUILD_LOG"