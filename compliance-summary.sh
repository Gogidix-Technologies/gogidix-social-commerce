#!/bin/bash

# Script to verify compliance structure for all services

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Social Commerce Services Compliance Check ==="
echo

check_service_compliance() {
    local service=$1
    local service_type=$2
    
    echo "Checking $service..."
    
    # Check required directories
    local missing_dirs=""
    for dir in ".github/workflows" "tests" "k8s" "api-docs" "docs" "scripts" "i18n"; do
        if [ ! -d "$service/$dir" ]; then
            missing_dirs="$missing_dirs $dir"
        fi
    done
    
    # Check database directories for Java services
    if [ "$service_type" = "java" ] && [ ! -d "$service/database" ]; then
        missing_dirs="$missing_dirs database"
    fi
    
    # Check required files
    local missing_files=""
    for file in "README.md" "Dockerfile"; do
        if [ ! -f "$service/$file" ]; then
            missing_files="$missing_files $file"
        fi
    done
    
    # Check CI/CD workflows
    local workflow_count=$(ls -1 "$service/.github/workflows/"*.yml 2>/dev/null | wc -l)
    
    if [ -z "$missing_dirs" ] && [ -z "$missing_files" ] && [ "$workflow_count" -ge 3 ]; then
        echo "  ✓ Fully compliant"
    else
        [ -n "$missing_dirs" ] && echo "  ✗ Missing directories:$missing_dirs"
        [ -n "$missing_files" ] && echo "  ✗ Missing files:$missing_files"
        [ "$workflow_count" -lt 3 ] && echo "  ✗ Only $workflow_count workflow files (need at least 3)"
    fi
    echo
}

echo "Java Backend Services:"
echo "====================="
for service in admin-finalization admin-interfaces analytics-service api-gateway commission-service \
               fulfillment-options integration-optimization integration-performance invoice-service \
               localization-service marketplace multi-currency-service order-service payment-gateway \
               payout-service product-service regional-admin social-commerce-production \
               social-commerce-shared social-commerce-staging subscription-service vendor-onboarding; do
    if [ -d "$service" ]; then
        check_service_compliance "$service" "java"
    fi
done

echo
echo "Frontend Services:"
echo "=================="
for service in global-hq-admin social-media-integration user-mobile-app user-web-app vendor-app; do
    if [ -d "$service" ]; then
        check_service_compliance "$service" "frontend"
    fi
done

echo
echo "=== Compliance Check Complete ==="