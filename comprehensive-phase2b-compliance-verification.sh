#!/bin/bash

# Comprehensive Phase 2B Compliance Verification Script
# Checks all three domains: social-commerce, warehousing, and courier-services

echo "=== PHASE 2B COMPREHENSIVE COMPLIANCE VERIFICATION ==="
echo "Date: $(date)"
echo "Verifying: social-commerce, warehousing, and courier-services domains"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Initialize counters
total_services=0
compliant_services=0
missing_files=0

# Function to check service compliance
check_service_compliance() {
    local service_path="$1"
    local service_name="$(basename "$service_path")"
    local domain="$2"
    
    if [ ! -d "$service_path" ]; then
        return 1
    fi
    
    echo -e "${BLUE}Checking: $domain/$service_name${NC}"
    total_services=$((total_services + 1))
    
    local compliance_score=0
    local max_score=0
    local issues=()
    
    # Check for standardization files
    echo "  📋 Checking standardization files..."
    
    # Check .env.template
    max_score=$((max_score + 1))
    if [ -f "$service_path/.env.template" ]; then
        compliance_score=$((compliance_score + 1))
        echo -e "    ✅ .env.template found"
    else
        issues+=("Missing .env.template")
        echo -e "    ❌ .env.template missing"
        missing_files=$((missing_files + 1))
    fi
    
    # Check language-specific files
    if [ -f "$service_path/pom.xml" ]; then
        # Java service checks
        echo "  ☕ Java service detected"
        
        # Check sonar-project.properties
        max_score=$((max_score + 1))
        if [ -f "$service_path/sonar-project.properties" ]; then
            compliance_score=$((compliance_score + 1))
            echo -e "    ✅ sonar-project.properties found"
        else
            issues+=("Missing sonar-project.properties")
            echo -e "    ❌ sonar-project.properties missing"
            missing_files=$((missing_files + 1))
        fi
        
        # Check pom.xml standardization
        max_score=$((max_score + 1))
        if grep -q "com.exalt" "$service_path/pom.xml" 2>/dev/null; then
            compliance_score=$((compliance_score + 1))
            echo -e "    ✅ pom.xml uses standardized groupId"
        else
            issues+=("pom.xml groupId not standardized")
            echo -e "    ❌ pom.xml groupId not standardized"
        fi
        
    elif [ -f "$service_path/package.json" ]; then
        # Node.js service checks
        echo "  📦 Node.js service detected"
        
        # Check package-lock.json
        max_score=$((max_score + 1))
        if [ -f "$service_path/package-lock.json" ]; then
            compliance_score=$((compliance_score + 1))
            echo -e "    ✅ package-lock.json found"
        else
            issues+=("Missing package-lock.json")
            echo -e "    ❌ package-lock.json missing"
            missing_files=$((missing_files + 1))
        fi
        
        # Check package.json standardization
        max_score=$((max_score + 1))
        if grep -q "\"name\":" "$service_path/package.json" 2>/dev/null; then
            compliance_score=$((compliance_score + 1))
            echo -e "    ✅ package.json has name field"
        else
            issues+=("package.json missing name field")
            echo -e "    ❌ package.json missing name field"
        fi
    fi
    
    # Check CI/CD workflows
    echo "  🔄 Checking CI/CD workflows..."
    max_score=$((max_score + 5)) # 5 workflow files expected
    
    local workflows=("build.yml" "code-quality.yml" "deploy-development.yml" "security-scan.yml" "test.yml")
    local workflow_count=0
    
    for workflow in "${workflows[@]}"; do
        if [ -f "$service_path/.github/workflows/$workflow" ]; then
            workflow_count=$((workflow_count + 1))
            echo -e "    ✅ $workflow found"
        else
            issues+=("Missing .github/workflows/$workflow")
            echo -e "    ❌ $workflow missing"
            missing_files=$((missing_files + 1))
        fi
    done
    compliance_score=$((compliance_score + workflow_count))
    
    # Check Docker configuration
    echo "  🐳 Checking Docker configuration..."
    max_score=$((max_score + 2))
    
    if [ -f "$service_path/Dockerfile" ]; then
        compliance_score=$((compliance_score + 1))
        echo -e "    ✅ Dockerfile found"
    else
        issues+=("Missing Dockerfile")
        echo -e "    ❌ Dockerfile missing"
        missing_files=$((missing_files + 1))
    fi
    
    if [ -f "$service_path/docker-compose.yml" ]; then
        compliance_score=$((compliance_score + 1))
        echo -e "    ✅ docker-compose.yml found"
    else
        issues+=("Missing docker-compose.yml")
        echo -e "    ❌ docker-compose.yml missing"
        missing_files=$((missing_files + 1))
    fi
    
    # Check Kubernetes configuration
    echo "  ⚓ Checking Kubernetes configuration..."
    max_score=$((max_score + 3))
    
    local k8s_files=("deployment.yaml" "service.yaml" "hpa.yaml")
    local k8s_count=0
    
    for k8s_file in "${k8s_files[@]}"; do
        if [ -f "$service_path/k8s/$k8s_file" ]; then
            k8s_count=$((k8s_count + 1))
            echo -e "    ✅ k8s/$k8s_file found"
        else
            issues+=("Missing k8s/$k8s_file")
            echo -e "    ❌ k8s/$k8s_file missing"
            missing_files=$((missing_files + 1))
        fi
    done
    compliance_score=$((compliance_score + k8s_count))
    
    # Check scripts
    echo "  📜 Checking scripts..."
    max_score=$((max_score + 2))
    
    if [ -f "$service_path/scripts/dev.sh" ]; then
        compliance_score=$((compliance_score + 1))
        echo -e "    ✅ scripts/dev.sh found"
    else
        issues+=("Missing scripts/dev.sh")
        echo -e "    ❌ scripts/dev.sh missing"
        missing_files=$((missing_files + 1))
    fi
    
    if [ -f "$service_path/scripts/setup.sh" ]; then
        compliance_score=$((compliance_score + 1))
        echo -e "    ✅ scripts/setup.sh found"
    else
        issues+=("Missing scripts/setup.sh")
        echo -e "    ❌ scripts/setup.sh missing"
        missing_files=$((missing_files + 1))
    fi
    
    # Calculate compliance percentage
    local compliance_percentage=0
    if [ $max_score -gt 0 ]; then
        compliance_percentage=$((compliance_score * 100 / max_score))
    fi
    
    # Print compliance status
    if [ $compliance_percentage -ge 90 ]; then
        echo -e "  ${GREEN}✅ COMPLIANT ($compliance_percentage%) - $compliance_score/$max_score${NC}"
        compliant_services=$((compliant_services + 1))
    elif [ $compliance_percentage -ge 70 ]; then
        echo -e "  ${YELLOW}⚠️  PARTIALLY COMPLIANT ($compliance_percentage%) - $compliance_score/$max_score${NC}"
    else
        echo -e "  ${RED}❌ NON-COMPLIANT ($compliance_percentage%) - $compliance_score/$max_score${NC}"
    fi
    
    # Print issues
    if [ ${#issues[@]} -gt 0 ]; then
        echo -e "  ${RED}Issues found:${NC}"
        for issue in "${issues[@]}"; do
            echo -e "    - $issue"
        done
    fi
    
    echo ""
}

# Check social-commerce domain
echo -e "${BLUE}=== SOCIAL-COMMERCE DOMAIN ===${NC}"
social_commerce_path="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/social-commerce"

if [ -d "$social_commerce_path" ]; then
    for service_dir in "$social_commerce_path"/*; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" -o -f "$service_dir/package.json" ]; then
            check_service_compliance "$service_dir" "social-commerce"
        fi
    done
else
    echo -e "${RED}❌ Social-commerce domain not found at $social_commerce_path${NC}"
fi

# Check warehousing domain
echo -e "${BLUE}=== WAREHOUSING DOMAIN ===${NC}"
warehousing_path="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/warehousing"

if [ -d "$warehousing_path" ]; then
    for service_dir in "$warehousing_path"/*; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" -o -f "$service_dir/package.json" ]; then
            check_service_compliance "$service_dir" "warehousing"
        fi
    done
else
    echo -e "${RED}❌ Warehousing domain not found at $warehousing_path${NC}"
fi

# Check courier-services domain
echo -e "${BLUE}=== COURIER-SERVICES DOMAIN ===${NC}"
courier_services_path="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/courier-services"

if [ -d "$courier_services_path" ]; then
    for service_dir in "$courier_services_path"/*; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" -o -f "$service_dir/package.json" ]; then
            check_service_compliance "$service_dir" "courier-services"
        fi
    done
else
    echo -e "${RED}❌ Courier-services domain not found at $courier_services_path${NC}"
fi

# Generate summary report
echo -e "${BLUE}=== PHASE 2B COMPLIANCE SUMMARY ===${NC}"
echo ""
echo -e "📊 ${BLUE}Overall Statistics:${NC}"
echo -e "   Total Services Checked: $total_services"
echo -e "   Compliant Services: $compliant_services"
echo -e "   Non-Compliant Services: $((total_services - compliant_services))"
echo -e "   Total Missing Files: $missing_files"

compliance_rate=0
if [ $total_services -gt 0 ]; then
    compliance_rate=$((compliant_services * 100 / total_services))
fi

echo ""
echo -e "🎯 ${BLUE}Compliance Rate: $compliance_rate%${NC}"

if [ $compliance_rate -ge 90 ]; then
    echo -e "${GREEN}✅ PHASE 2B COMPLIANCE: EXCELLENT${NC}"
    echo -e "${GREEN}   The ecosystem is ready for production deployment!${NC}"
elif [ $compliance_rate -ge 70 ]; then
    echo -e "${YELLOW}⚠️  PHASE 2B COMPLIANCE: GOOD${NC}"
    echo -e "${YELLOW}   Minor improvements needed before production.${NC}"
else
    echo -e "${RED}❌ PHASE 2B COMPLIANCE: NEEDS IMPROVEMENT${NC}"
    echo -e "${RED}   Significant work needed before production readiness.${NC}"
fi

echo ""
echo -e "${BLUE}=== PHASE 2B STATUS ===${NC}"
if [ $compliance_rate -ge 90 ] && [ $missing_files -le 10 ]; then
    echo -e "${GREEN}🎉 PHASE 2B IS COMPLETE! 🎉${NC}"
    echo -e "${GREEN}   All domains are standardized and production-ready.${NC}"
else
    echo -e "${YELLOW}⏳ PHASE 2B IS NEARLY COMPLETE${NC}"
    echo -e "${YELLOW}   $missing_files files still need to be added for full compliance.${NC}"
fi

echo ""
echo "Verification completed at $(date)"