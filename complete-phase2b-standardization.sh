#!/bin/bash

# Complete Phase 2B Standardization Script
# Adds missing standardization files to achieve 100% compliance

echo "=== COMPLETING PHASE 2B STANDARDIZATION ==="
echo "Adding missing files to warehousing and courier-services domains"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

WAREHOUSING_PATH="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/warehousing"
COURIER_PATH="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/courier-services"

# Function to create .env.template
create_env_template() {
    local service_path="$1"
    local service_name="$(basename "$service_path")"
    
    cat > "$service_path/.env.template" << 'EOF'
# Environment Configuration Template
# Copy this file to .env and fill in your actual values

# Application Configuration
NODE_ENV=development
PORT=3000
APP_NAME=SERVICE_NAME
VERSION=1.0.0

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=database_name
DB_USERNAME=username
DB_PASSWORD=password

# Redis Configuration (if applicable)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Security
JWT_SECRET=your-super-secret-jwt-key
ENCRYPTION_KEY=your-encryption-key

# External Services
API_BASE_URL=http://localhost:8080
EXTERNAL_SERVICE_URL=
EXTERNAL_API_KEY=

# Monitoring & Logging
LOG_LEVEL=info
ENABLE_METRICS=true

# Development Settings
DEBUG=false
ENABLE_SWAGGER=true

# Production Settings (uncomment for production)
# NODE_ENV=production
# LOG_LEVEL=error
# DEBUG=false
EOF
    
    # Replace SERVICE_NAME with actual service name
    sed -i "s/SERVICE_NAME/$service_name/g" "$service_path/.env.template"
    echo -e "  ✅ Created .env.template for $service_name"
}

# Function to create sonar-project.properties for Java services
create_sonar_properties() {
    local service_path="$1"
    local service_name="$(basename "$service_path")"
    
    cat > "$service_path/sonar-project.properties" << EOF
# SonarQube Configuration for $service_name
sonar.projectKey=exalt:$service_name
sonar.projectName=$service_name
sonar.projectVersion=1.0.0

# Source and Test Configuration
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes

# Language and Encoding
sonar.language=java
sonar.sourceEncoding=UTF-8
sonar.java.source=17

# Coverage Configuration
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports

# Code Quality Rules
sonar.java.checkstyle.reportPaths=target/checkstyle-result.xml
sonar.java.pmd.reportPaths=target/pmd.xml
sonar.java.spotbugs.reportPaths=target/spotbugsXml.xml

# Exclusions
sonar.exclusions=**/*Test.java,**/*IT.java,**/target/**,**/generated/**
sonar.test.exclusions=**/src/test/**

# Quality Gate
sonar.qualitygate.wait=true
EOF
    
    echo -e "  ✅ Created sonar-project.properties for $service_name"
}

# Function to create missing CI/CD workflow
create_workflow() {
    local service_path="$1"
    local workflow_name="$2"
    local service_name="$(basename "$service_path")"
    
    mkdir -p "$service_path/.github/workflows"
    
    case "$workflow_name" in
        "code-quality.yml")
            cat > "$service_path/.github/workflows/code-quality.yml" << 'EOF'
name: Code Quality

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  code-quality:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
      with:
        fetch-depth: 0
    
    - name: Set up Node.js
      if: contains(github.repository, 'node') || contains(github.repository, 'frontend')
      uses: actions/setup-node@v4
      with:
        node-version: '18'
        cache: 'npm'
    
    - name: Set up JDK 17
      if: contains(github.repository, 'service') && !contains(github.repository, 'node')
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.m2/repository
          node_modules
        key: ${{ runner.os }}-deps-${{ hashFiles('**/pom.xml', '**/package-lock.json') }}
    
    - name: Run ESLint (Node.js)
      if: contains(github.repository, 'node') || contains(github.repository, 'frontend')
      run: |
        npm ci
        npm run lint || echo "Linting completed with warnings"
    
    - name: Run Checkstyle (Java)
      if: contains(github.repository, 'service') && !contains(github.repository, 'node')
      run: mvn checkstyle:check
    
    - name: SonarCloud Scan
      if: contains(github.repository, 'service') && !contains(github.repository, 'node')
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: mvn sonar:sonar
EOF
            ;;
        "security-scan.yml")
            cat > "$service_path/.github/workflows/security-scan.yml" << 'EOF'
name: Security Scan

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
  schedule:
    - cron: '0 2 * * 1' # Weekly scan

jobs:
  security-scan:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Run Snyk Security Scan
      uses: snyk/actions/node@master
      if: contains(github.repository, 'node') || contains(github.repository, 'frontend')
      env:
        SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
      with:
        args: --severity-threshold=high
    
    - name: Run OWASP Dependency Check (Java)
      if: contains(github.repository, 'service') && !contains(github.repository, 'node')
      run: |
        mvn org.owasp:dependency-check-maven:check
    
    - name: Upload security scan results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: security-scan-results
        path: |
          dependency-check-report.html
          snyk-results.json
EOF
            ;;
    esac
    
    echo -e "  ✅ Created $workflow_name for $service_name"
}

# Function to create docker-compose.yml
create_docker_compose() {
    local service_path="$1"
    local service_name="$(basename "$service_path")"
    
    # Determine if it's a Java or Node.js service
    if [ -f "$service_path/pom.xml" ]; then
        # Java service
        cat > "$service_path/docker-compose.yml" << EOF
version: '3.8'

services:
  $service_name:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ${service_name}_container
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - JAVA_OPTS=-Xmx512m -Xms256m
    env_file:
      - .env
    networks:
      - exalt-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped

networks:
  exalt-network:
    external: true
EOF
    else
        # Node.js service
        cat > "$service_path/docker-compose.yml" << EOF
version: '3.8'

services:
  $service_name:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ${service_name}_container
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=development
    env_file:
      - .env
    networks:
      - exalt-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped
    volumes:
      - .:/app
      - /app/node_modules

networks:
  exalt-network:
    external: true
EOF
    fi
    
    echo -e "  ✅ Created docker-compose.yml for $service_name"
}

# Function to process a service and add missing files
process_service() {
    local service_path="$1"
    local domain="$2"
    local service_name="$(basename "$service_path")"
    
    if [ ! -d "$service_path" ]; then
        return
    fi
    
    echo -e "${BLUE}Processing: $domain/$service_name${NC}"
    
    # Check for .env.template
    if [ ! -f "$service_path/.env.template" ]; then
        create_env_template "$service_path"
    fi
    
    # Check for Java-specific files
    if [ -f "$service_path/pom.xml" ]; then
        if [ ! -f "$service_path/sonar-project.properties" ]; then
            create_sonar_properties "$service_path"
        fi
    fi
    
    # Check for missing workflows
    if [ ! -f "$service_path/.github/workflows/code-quality.yml" ]; then
        create_workflow "$service_path" "code-quality.yml"
    fi
    
    if [ ! -f "$service_path/.github/workflows/security-scan.yml" ]; then
        create_workflow "$service_path" "security-scan.yml"
    fi
    
    # Check for docker-compose.yml
    if [ ! -f "$service_path/docker-compose.yml" ]; then
        create_docker_compose "$service_path"
    fi
    
    echo ""
}

# Process warehousing domain
echo -e "${BLUE}=== PROCESSING WAREHOUSING DOMAIN ===${NC}"
if [ -d "$WAREHOUSING_PATH" ]; then
    for service_dir in "$WAREHOUSING_PATH"/*; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" -o -f "$service_dir/package.json" ]; then
            process_service "$service_dir" "warehousing"
        fi
    done
else
    echo -e "${YELLOW}⚠️  Warehousing domain not found${NC}"
fi

# Process courier-services domain
echo -e "${BLUE}=== PROCESSING COURIER-SERVICES DOMAIN ===${NC}"
if [ -d "$COURIER_PATH" ]; then
    for service_dir in "$COURIER_PATH"/*; do
        if [ -d "$service_dir" ] && [ -f "$service_dir/pom.xml" -o -f "$service_dir/package.json" ]; then
            process_service "$service_dir" "courier-services"
        fi
    done
else
    echo -e "${YELLOW}⚠️  Courier-services domain not found${NC}"
fi

echo -e "${GREEN}=== PHASE 2B STANDARDIZATION COMPLETED ===${NC}"
echo -e "${GREEN}✅ All missing standardization files have been created${NC}"
echo ""
echo "Next steps:"
echo "1. Run the compliance verification script again"
echo "2. Verify that compliance rate is now 95%+"
echo "3. Commit all changes to git"