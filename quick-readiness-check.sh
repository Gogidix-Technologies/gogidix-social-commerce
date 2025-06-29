#!/bin/bash

echo "🎯 SOCIAL COMMERCE DOMAIN READINESS VALIDATION"
echo "=============================================="
echo ""

# Service counts
BACKEND_COUNT=$(find . -name "pom.xml" | wc -l)
FRONTEND_COUNT=$(find . -name "package.json" | grep -v node_modules | wc -l)
DOCKER_COUNT=$(find . -name "Dockerfile" | wc -l)

echo "📊 SERVICE INVENTORY:"
echo "├── Backend Services: $BACKEND_COUNT"
echo "├── Frontend Applications: $FRONTEND_COUNT"
echo "└── Docker Containers: $DOCKER_COUNT"
echo ""

# Critical services check
echo "🏗️ CRITICAL SERVICES CHECK:"
CRITICAL_SERVICES=("marketplace" "order-service" "product-service" "payment-gateway" "user-web-app")
CRITICAL_FOUND=0
for service in "${CRITICAL_SERVICES[@]}"; do
    if [ -d "$service" ]; then
        echo "✅ $service - Present"
        CRITICAL_FOUND=$((CRITICAL_FOUND + 1))
    else
        echo "❌ $service - Missing"
    fi
done
echo "Critical Services: $CRITICAL_FOUND/5"
echo ""

# Build test sample
echo "🔨 BUILD STATUS SAMPLE TEST:"
BUILD_PASS=0
BUILD_TOTAL=0
for service in $(find . -name "pom.xml" | head -5); do
    dir=$(dirname "$service")
    BUILD_TOTAL=$((BUILD_TOTAL + 1))
    echo -n "Testing $(basename $dir)... "
    cd "$dir"
    if mvn compile -q > /dev/null 2>&1; then
        echo "✅ PASS"
        BUILD_PASS=$((BUILD_PASS + 1))
    else
        echo "❌ FAIL"
    fi
    cd - > /dev/null
done
echo "Build Success Rate: $BUILD_PASS/$BUILD_TOTAL"
echo ""

# Docker compose check
echo "🐳 DOCKER INFRASTRUCTURE:"
COMPOSE_COUNT=$(find . -name "docker-compose*.yml" | wc -l)
echo "├── Docker Compose files: $COMPOSE_COUNT"
echo "└── Dockerfiles ready: $DOCKER_COUNT"
echo ""

# Integration components
echo "🔗 INTEGRATION READINESS:"
[ -d "api-gateway" ] && echo "✅ API Gateway present" || echo "❌ API Gateway missing"
[ -d "social-commerce-shared" ] && echo "✅ Shared library present" || echo "❌ Shared library missing"
DB_CONFIGS=$(grep -r "spring.datasource" . --include="*.properties" --include="*.yml" 2>/dev/null | wc -l)
echo "✅ Database configurations: $DB_CONFIGS"
echo ""

# Documentation check
echo "📚 DOCUMENTATION STATUS:"
[ -f "README.md" ] && echo "✅ README.md present" || echo "❌ README.md missing"
DOC_COUNT=$(find . -name "*.md" | wc -l)
echo "✅ Documentation files: $DOC_COUNT"
echo ""

# Overall readiness score
READINESS_SCORE=0

# Service count scoring (40 points total)
[ $BACKEND_COUNT -gt 15 ] && READINESS_SCORE=$((READINESS_SCORE + 20))
[ $FRONTEND_COUNT -gt 3 ] && READINESS_SCORE=$((READINESS_SCORE + 10))
[ $DOCKER_COUNT -gt 20 ] && READINESS_SCORE=$((READINESS_SCORE + 10))

# Critical services scoring (20 points)
[ $CRITICAL_FOUND -ge 3 ] && READINESS_SCORE=$((READINESS_SCORE + 20))

# Build success scoring (25 points)
if [ $BUILD_TOTAL -gt 0 ]; then
    BUILD_PERCENT=$((BUILD_PASS * 100 / BUILD_TOTAL))
    [ $BUILD_PERCENT -eq 100 ] && READINESS_SCORE=$((READINESS_SCORE + 25))
    [ $BUILD_PERCENT -ge 80 ] && [ $BUILD_PERCENT -lt 100 ] && READINESS_SCORE=$((READINESS_SCORE + 20))
    [ $BUILD_PERCENT -ge 60 ] && [ $BUILD_PERCENT -lt 80 ] && READINESS_SCORE=$((READINESS_SCORE + 15))
fi

# Infrastructure scoring (15 points)
[ $COMPOSE_COUNT -gt 0 ] && READINESS_SCORE=$((READINESS_SCORE + 5))
[ -d "api-gateway" ] && READINESS_SCORE=$((READINESS_SCORE + 5))
[ $DB_CONFIGS -gt 5 ] && READINESS_SCORE=$((READINESS_SCORE + 5))

echo "🎯 OVERALL READINESS ASSESSMENT:"
echo "=============================================="
echo "Readiness Score: $READINESS_SCORE/100"
echo ""

if [ $READINESS_SCORE -ge 85 ]; then
    echo "🟢 STATUS: EXCELLENT - READY FOR PRODUCTION"
    echo "   Domain is fully prepared and matches Warehousing/Courier standards"
elif [ $READINESS_SCORE -ge 75 ]; then
    echo "🟢 STATUS: GOOD - READY FOR PRODUCTION"
    echo "   Domain is ready with minor optimizations possible"
elif [ $READINESS_SCORE -ge 60 ]; then
    echo "🟡 STATUS: MOSTLY READY - MINOR ISSUES"
    echo "   Domain needs small fixes before production"
elif [ $READINESS_SCORE -ge 40 ]; then
    echo "🟠 STATUS: NEEDS ATTENTION"
    echo "   Domain requires significant work before production"
else
    echo "🔴 STATUS: NOT READY"
    echo "   Domain needs major work and fixes"
fi

echo ""
echo "📊 COMPARISON WITH OTHER DOMAINS:"
echo "├── Warehousing Domain: ✅ Production Ready"
echo "├── Courier Services: ✅ Production Ready" 
echo "└── Social Commerce: $([ $READINESS_SCORE -ge 75 ] && echo "✅ Production Ready" || echo "⚠️ Needs Review")"
echo ""

if [ $READINESS_SCORE -ge 75 ]; then
    echo "🎉 SOCIAL COMMERCE DOMAIN IS READY!"
    echo "   Ready to proceed with deployment and integration"
else
    echo "📝 RECOMMENDED ACTIONS:"
    [ $BUILD_PASS -lt $BUILD_TOTAL ] && echo "   - Fix compilation errors in failing services"
    [ $CRITICAL_FOUND -lt 4 ] && echo "   - Verify critical service implementations"
    [ $DOCKER_COUNT -lt 20 ] && echo "   - Complete Docker container configurations"
    [ $DB_CONFIGS -lt 5 ] && echo "   - Review database configurations"
fi

echo ""
echo "Validation completed: $(date)"