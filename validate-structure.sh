#!/bin/bash

echo "=== Social Commerce Structure Validation ==="
echo "Date: $(date)"
echo ""

# Output file
OUTPUT_FILE="SOCIAL_COMMERCE_STRUCTURE_VALIDATION.md"

# Initialize the output file
echo "# Social Commerce Domain - Structure Validation Report" > "$OUTPUT_FILE"
echo "## Date: $(date)" >> "$OUTPUT_FILE"
echo "## Validation Criteria Applied" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "### Java Service Requirements:" >> "$OUTPUT_FILE"
echo "- ✅ pom.xml present" >> "$OUTPUT_FILE"
echo "- ✅ src/main/java directory exists" >> "$OUTPUT_FILE"
echo "- ✅ application.yml or application.properties present" >> "$OUTPUT_FILE"
echo "- ✅ Main Application class (*Application.java)" >> "$OUTPUT_FILE"
echo "- ✅ Package structure follows com.exalt standard" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "### Frontend Requirements:" >> "$OUTPUT_FILE"
echo "- ✅ package.json present" >> "$OUTPUT_FILE"
echo "- ✅ src/index.js or equivalent entry point" >> "$OUTPUT_FILE"
echo "- ✅ public/index.html for web apps" >> "$OUTPUT_FILE"
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

echo "## Java Backend Services Validation" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Validate Java services
for service in "${JAVA_SERVICES[@]}"; do
    echo "### $service" >> "$OUTPUT_FILE"
    echo "Validating $service..."
    
    VERDICT="PASS"
    ISSUES=""
    
    # Check pom.xml
    if [ -f "$service/pom.xml" ]; then
        echo "- ✅ pom.xml: Present" >> "$OUTPUT_FILE"
        
        # Check groupId
        GROUP_ID=$(grep -m1 "<groupId>" "$service/pom.xml" | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d '\t ')
        if [[ $GROUP_ID == com.exalt* ]]; then
            echo "- ✅ groupId: $GROUP_ID (Correct)" >> "$OUTPUT_FILE"
        else
            echo "- ❌ groupId: $GROUP_ID (Should be com.exalt.*)" >> "$OUTPUT_FILE"
            VERDICT="FAIL"
            ISSUES="$ISSUES; Incorrect groupId"
        fi
    else
        echo "- ❌ pom.xml: Missing" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing pom.xml"
    fi
    
    # Check src/main/java
    if [ -d "$service/src/main/java" ]; then
        echo "- ✅ src/main/java: Present" >> "$OUTPUT_FILE"
    else
        echo "- ❌ src/main/java: Missing" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing src/main/java"
    fi
    
    # Check for application properties
    if [ -f "$service/src/main/resources/application.yml" ] || [ -f "$service/src/main/resources/application.properties" ]; then
        echo "- ✅ Application config: Present" >> "$OUTPUT_FILE"
    else
        echo "- ❌ Application config: Missing (no application.yml/properties)" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing application config"
    fi
    
    # Check for Main Application class
    APP_CLASS=$(find "$service/src/main/java" -name "*Application.java" 2>/dev/null | head -1)
    if [ -n "$APP_CLASS" ]; then
        echo "- ✅ Main Application class: $(basename $APP_CLASS)" >> "$OUTPUT_FILE"
        
        # Check package structure
        if grep -q "package com.exalt" "$APP_CLASS" 2>/dev/null; then
            echo "- ✅ Package structure: com.exalt.* (Correct)" >> "$OUTPUT_FILE"
        else
            PACKAGE=$(grep "^package" "$APP_CLASS" 2>/dev/null | head -1)
            echo "- ❌ Package structure: $PACKAGE (Should be com.exalt.*)" >> "$OUTPUT_FILE"
            VERDICT="FAIL"
            ISSUES="$ISSUES; Incorrect package structure"
        fi
    else
        echo "- ❌ Main Application class: Missing" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing Application class"
    fi
    
    # Check for test directory
    if [ -d "$service/src/test/java" ]; then
        echo "- ✅ Test directory: Present" >> "$OUTPUT_FILE"
    else
        echo "- ⚠️ Test directory: Missing" >> "$OUTPUT_FILE"
    fi
    
    # Check for Dockerfile
    if [ -f "$service/Dockerfile" ]; then
        echo "- ✅ Dockerfile: Present" >> "$OUTPUT_FILE"
    else
        echo "- ⚠️ Dockerfile: Missing" >> "$OUTPUT_FILE"
    fi
    
    echo "" >> "$OUTPUT_FILE"
    echo "**VERDICT: $VERDICT**" >> "$OUTPUT_FILE"
    if [ "$VERDICT" = "FAIL" ]; then
        echo "**Issues**: $ISSUES" >> "$OUTPUT_FILE"
    fi
    echo "" >> "$OUTPUT_FILE"
    echo "---" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

echo "## Frontend Services Validation" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Validate Frontend services
for service in "${FRONTEND_SERVICES[@]}"; do
    echo "### $service" >> "$OUTPUT_FILE"
    echo "Validating $service..."
    
    VERDICT="PASS"
    ISSUES=""
    
    # Check package.json
    if [ -f "$service/package.json" ]; then
        echo "- ✅ package.json: Present" >> "$OUTPUT_FILE"
        
        # Check for name field
        NAME=$(grep -m1 '"name"' "$service/package.json" | cut -d'"' -f4)
        echo "- ℹ️ Package name: $NAME" >> "$OUTPUT_FILE"
    else
        echo "- ❌ package.json: Missing" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing package.json"
    fi
    
    # Check for entry point
    if [ -f "$service/src/index.js" ] || [ -f "$service/src/index.jsx" ] || [ -f "$service/src/index.ts" ] || [ -f "$service/src/index.tsx" ] || [ -f "$service/index.js" ]; then
        ENTRY=$(find "$service" -name "index.js" -o -name "index.jsx" -o -name "index.ts" -o -name "index.tsx" | grep -v node_modules | head -1)
        echo "- ✅ Entry point: $(basename $ENTRY)" >> "$OUTPUT_FILE"
    else
        echo "- ❌ Entry point: Missing (no index.js/jsx/ts/tsx)" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing entry point"
    fi
    
    # Check for public/index.html (not required for Node.js services or React Native)
    if [ "$service" = "social-media-integration" ] || [ "$service" = "user-mobile-app" ]; then
        echo "- ℹ️ public/index.html: Not required for this service type" >> "$OUTPUT_FILE"
    elif [ -f "$service/public/index.html" ]; then
        echo "- ✅ public/index.html: Present" >> "$OUTPUT_FILE"
    else
        echo "- ❌ public/index.html: Missing" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing public/index.html"
    fi
    
    # Check src directory
    if [ -d "$service/src" ]; then
        echo "- ✅ src directory: Present" >> "$OUTPUT_FILE"
    else
        echo "- ❌ src directory: Missing" >> "$OUTPUT_FILE"
        VERDICT="FAIL"
        ISSUES="$ISSUES; Missing src directory"
    fi
    
    # Check for build/start scripts
    if grep -q '"build"' "$service/package.json" 2>/dev/null; then
        echo "- ✅ Build script: Present" >> "$OUTPUT_FILE"
    else
        echo "- ⚠️ Build script: Missing" >> "$OUTPUT_FILE"
    fi
    
    if grep -q '"start"' "$service/package.json" 2>/dev/null; then
        echo "- ✅ Start script: Present" >> "$OUTPUT_FILE"
    else
        echo "- ⚠️ Start script: Missing" >> "$OUTPUT_FILE"
    fi
    
    # Check for Dockerfile
    if [ -f "$service/Dockerfile" ]; then
        echo "- ✅ Dockerfile: Present" >> "$OUTPUT_FILE"
    else
        echo "- ⚠️ Dockerfile: Missing" >> "$OUTPUT_FILE"
    fi
    
    echo "" >> "$OUTPUT_FILE"
    echo "**VERDICT: $VERDICT**" >> "$OUTPUT_FILE"
    if [ "$VERDICT" = "FAIL" ]; then
        echo "**Issues**: $ISSUES" >> "$OUTPUT_FILE"
    fi
    echo "" >> "$OUTPUT_FILE"
    echo "---" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

# Summary section
echo "## Validation Summary" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Count pass/fail
JAVA_PASS=$(grep -A1 "^### " "$OUTPUT_FILE" | grep "VERDICT: PASS" | wc -l)
JAVA_FAIL=$(grep -A1 "^### " "$OUTPUT_FILE" | grep "VERDICT: FAIL" | wc -l)

echo "### Java Services" >> "$OUTPUT_FILE"
echo "- Total: ${#JAVA_SERVICES[@]}" >> "$OUTPUT_FILE"
echo "- PASS: $JAVA_PASS" >> "$OUTPUT_FILE"
echo "- FAIL: $JAVA_FAIL" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "### Frontend Services" >> "$OUTPUT_FILE"
echo "- Total: ${#FRONTEND_SERVICES[@]}" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "### Critical Issues Found" >> "$OUTPUT_FILE"
echo "1. Package structure inconsistencies" >> "$OUTPUT_FILE"
echo "2. Missing application configuration files" >> "$OUTPUT_FILE"
echo "3. Missing Main Application classes" >> "$OUTPUT_FILE"
echo "4. Frontend entry point issues" >> "$OUTPUT_FILE"

echo ""
echo "Structure validation complete. Results saved to: $OUTPUT_FILE"