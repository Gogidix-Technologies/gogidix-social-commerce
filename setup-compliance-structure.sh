#!/bin/bash

# Script to setup compliance structure for all domain services
# According to the repository structure requirements

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Function to create directory structure for a service
create_service_structure() {
    local service_path=$1
    local service_type=$2
    
    echo "Setting up structure for: $service_path"
    
    # Create required directories
    mkdir -p "$service_path/.github/workflows"
    mkdir -p "$service_path/tests/unit"
    mkdir -p "$service_path/tests/integration"
    mkdir -p "$service_path/tests/e2e"
    mkdir -p "$service_path/tests/performance"
    mkdir -p "$service_path/k8s"
    mkdir -p "$service_path/api-docs"
    mkdir -p "$service_path/docs/architecture"
    mkdir -p "$service_path/docs/setup"
    mkdir -p "$service_path/docs/operations"
    mkdir -p "$service_path/scripts"
    mkdir -p "$service_path/i18n/en"
    mkdir -p "$service_path/i18n/fr"
    mkdir -p "$service_path/i18n/de"
    mkdir -p "$service_path/i18n/es"
    mkdir -p "$service_path/i18n/ar"
    
    # Create database directories for backend services
    if [ "$service_type" = "java" ]; then
        mkdir -p "$service_path/database/migrations"
        mkdir -p "$service_path/database/seeds"
    fi
    
    # Create placeholder files
    touch "$service_path/api-docs/openapi.yaml"
    touch "$service_path/scripts/setup.sh"
    touch "$service_path/scripts/dev.sh"
    
    # Make scripts executable
    chmod +x "$service_path/scripts/setup.sh"
    chmod +x "$service_path/scripts/dev.sh"
    
    # Copy appropriate workflow templates
    if [ "$service_type" = "java" ]; then
        cp workflow-templates/build.yml "$service_path/.github/workflows/"
        cp workflow-templates/test.yml "$service_path/.github/workflows/"
        cp workflow-templates/code-quality.yml "$service_path/.github/workflows/"
        cp workflow-templates/security-scan.yml "$service_path/.github/workflows/"
        cp workflow-templates/deploy-development.yml "$service_path/.github/workflows/"
    else
        cp workflow-templates/frontend-build.yml "$service_path/.github/workflows/build.yml"
        cp workflow-templates/frontend-test.yml "$service_path/.github/workflows/test.yml"
        cp workflow-templates/security-scan.yml "$service_path/.github/workflows/"
        cp workflow-templates/deploy-development.yml "$service_path/.github/workflows/"
    fi
}

# Process Java services
echo "Processing Java backend services..."
for service in admin-finalization admin-interfaces analytics-service api-gateway commission-service \
               fulfillment-options integration-optimization integration-performance invoice-service \
               localization-service marketplace multi-currency-service order-service payment-gateway \
               payout-service product-service regional-admin social-commerce-production \
               social-commerce-shared social-commerce-staging subscription-service vendor-onboarding; do
    if [ -d "$service" ]; then
        create_service_structure "$service" "java"
    fi
done

# Process Frontend services
echo "Processing frontend services..."
for service in global-hq-admin social-media-integration user-mobile-app user-web-app vendor-app; do
    if [ -d "$service" ]; then
        create_service_structure "$service" "frontend"
    fi
done

echo "Compliance structure setup completed!"