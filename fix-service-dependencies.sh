#!/bin/bash

echo "=== Comprehensive Dependency Fix for Social Commerce Services ==="
echo "Date: $(date)"
echo ""

# Function to check if a service uses JPA
check_jpa_usage() {
    local service=$1
    if grep -r "@Entity\|@Repository\|JpaRepository\|@Table\|@Column\|jakarta.persistence" "$service/src" 2>/dev/null | grep -q .; then
        return 0
    fi
    return 1
}

# Function to check if a service uses validation
check_validation_usage() {
    local service=$1
    if grep -r "@Valid\|@NotNull\|@NotBlank\|@Size\|jakarta.validation" "$service/src" 2>/dev/null | grep -q .; then
        return 0
    fi
    return 1
}

# Function to check if a service uses transaction
check_transaction_usage() {
    local service=$1
    if grep -r "@Transactional\|transaction" "$service/src" 2>/dev/null | grep -q .; then
        return 0
    fi
    return 1
}

# Function to add dependency to pom.xml if not already present
add_dependency_if_missing() {
    local pom_file=$1
    local group_id=$2
    local artifact_id=$3
    local scope=$4
    
    # Check if dependency already exists
    if grep -q "<artifactId>$artifact_id</artifactId>" "$pom_file"; then
        echo "    - $artifact_id already present"
        return
    fi
    
    # Create dependency XML
    local dependency_xml="        <dependency>\n            <groupId>$group_id</groupId>\n            <artifactId>$artifact_id</artifactId>"
    
    if [ -n "$scope" ]; then
        dependency_xml="$dependency_xml\n            <scope>$scope</scope>"
    fi
    
    dependency_xml="$dependency_xml\n        </dependency>"
    
    # Add before closing </dependencies> tag
    sed -i "/<\/dependencies>/i\\$dependency_xml" "$pom_file"
    echo "    + Added $artifact_id"
}

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
    if [ -f "$service/pom.xml" ] && [ -d "$service/src" ]; then
        echo "Analyzing $service..."
        
        # Check for JPA usage
        if check_jpa_usage "$service"; then
            echo "  JPA usage detected"
            add_dependency_if_missing "$service/pom.xml" "org.springframework.boot" "spring-boot-starter-data-jpa"
            add_dependency_if_missing "$service/pom.xml" "org.postgresql" "postgresql" "runtime"
            add_dependency_if_missing "$service/pom.xml" "com.h2database" "h2" "runtime"
        fi
        
        # Check for validation usage (already in POMs but let's ensure)
        if check_validation_usage "$service"; then
            echo "  Validation usage detected"
            add_dependency_if_missing "$service/pom.xml" "org.springframework.boot" "spring-boot-starter-validation"
        fi
        
        # Check for specific service types
        case "$service" in
            "api-gateway")
                echo "  API Gateway service"
                add_dependency_if_missing "$service/pom.xml" "org.springframework.cloud" "spring-cloud-starter-gateway"
                ;;
            "analytics-service")
                echo "  Analytics service"
                add_dependency_if_missing "$service/pom.xml" "org.springframework.boot" "spring-boot-starter-data-jpa"
                add_dependency_if_missing "$service/pom.xml" "org.postgresql" "postgresql" "runtime"
                ;;
            *"payment"*|*"payout"*)
                echo "  Payment-related service"
                add_dependency_if_missing "$service/pom.xml" "org.springframework.boot" "spring-boot-starter-security"
                ;;
            *"notification"*|*"message"*)
                echo "  Messaging service"
                add_dependency_if_missing "$service/pom.xml" "org.springframework.boot" "spring-boot-starter-amqp"
                ;;
        esac
        
        # Add OpenAPI documentation for all services
        add_dependency_if_missing "$service/pom.xml" "org.springdoc" "springdoc-openapi-starter-webmvc-ui"
        
        echo "✅ Fixed dependencies for $service"
        FIXED_COUNT=$((FIXED_COUNT + 1))
        echo ""
    fi
done

echo "=== Summary ==="
echo "Fixed dependencies for $FIXED_COUNT services"
echo ""

# Now fix the specific version issues for springdoc-openapi
echo "Fixing OpenAPI version compatibility..."
for service in "${SERVICES[@]}"; do
    if [ -f "$service/pom.xml" ]; then
        # Add version to springdoc-openapi-starter-webmvc-ui
        sed -i '/<artifactId>springdoc-openapi-starter-webmvc-ui<\/artifactId>/a\            <version>2.2.0</version>' "$service/pom.xml" 2>/dev/null || true
    fi
done

echo "Script completed at: $(date)"