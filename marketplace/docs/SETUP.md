# Marketplace Service Setup Guide

## Prerequisites

### Development Environment Requirements

#### Java Development Kit
```bash
# Java 17 or later (Required)
java -version
# Expected: openjdk version "17.0.x" or later

# Set JAVA_HOME if not already configured
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

#### Apache Maven
```bash
# Maven 3.8.0 or later (Required)
mvn -version
# Expected: Apache Maven 3.8.x or later

# Install Maven if not present
sudo apt update
sudo apt install maven
```

#### Database Systems
```bash
# PostgreSQL 14+ (Primary Database)
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql

# H2 Database (Development Fallback - Embedded)
# Included in Maven dependencies - no separate installation needed

# Redis 6+ (Caching Layer)
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

#### Message Broker
```bash
# RabbitMQ (Event Processing)
sudo apt install rabbitmq-server
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server

# Enable management plugin
sudo rabbitmq-plugins enable rabbitmq_management
# Access at: http://localhost:15672 (guest/guest)
```

### Infrastructure Dependencies

#### Service Registry (Eureka)
```bash
# Ensure Eureka Server is running
curl -s http://localhost:8761/actuator/health
# Expected: {"status":"UP"}

# If not running, start from shared-infrastructure:
cd ../../../shared-infrastructure/service-registry
mvn spring-boot:run
```

#### Configuration Server
```bash
# Ensure Config Server is running
curl -s http://localhost:8888/actuator/health
# Expected: {"status":"UP"}

# If not running:
cd ../../../shared-infrastructure/config-server
mvn spring-boot:run
```

## Database Setup

### PostgreSQL Configuration

#### 1. Create Database and User
```sql
-- Connect as postgres user
sudo -u postgres psql

-- Create marketplace database
CREATE DATABASE marketplace_db;

-- Create marketplace user
CREATE USER marketplace_user WITH PASSWORD 'marketplace_password_2024';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE marketplace_db TO marketplace_user;
GRANT ALL ON SCHEMA public TO marketplace_user;

-- Exit psql
\q
```

#### 2. Database Schema Initialization
```bash
# Navigate to marketplace service directory
cd /path/to/social-commerce/marketplace

# Run schema migration (using Flyway)
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/marketplace_db \
                   -Dflyway.user=marketplace_user \
                   -Dflyway.password=marketplace_password_2024
```

#### 3. Verify Database Setup
```sql
-- Connect to marketplace database
psql -h localhost -d marketplace_db -U marketplace_user

-- Check created tables
\dt

-- Expected tables:
-- marketplace_products, marketplace_vendors, marketplace_orders,
-- marketplace_customers, marketplace_categories, marketplace_reviews,
-- marketplace_carts, marketplace_transactions, marketplace_configurations

-- Exit
\q
```

### H2 Database Setup (Development Alternative)

#### Configuration for H2
```yaml
# src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:marketplace_db
    driverClassName: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

#### Access H2 Console
```bash
# After starting the application with dev profile:
# Open browser: http://localhost:8106/h2-console
# JDBC URL: jdbc:h2:mem:marketplace_db
# Username: sa
# Password: (leave empty)
```

## Redis Cache Setup

### Redis Configuration
```bash
# Edit Redis configuration
sudo nano /etc/redis/redis.conf

# Key configurations:
# maxmemory 256mb
# maxmemory-policy allkeys-lru
# save 900 1
# save 300 10

# Restart Redis
sudo systemctl restart redis-server

# Test Redis connection
redis-cli ping
# Expected: PONG
```

### Redis Data Structure Setup
```bash
# Connect to Redis CLI
redis-cli

# Create sample cache structures for testing
HSET marketplace:config "default_currency" "EUR"
HSET marketplace:config "max_cart_items" "50"
HSET marketplace:config "session_timeout" "3600"

# Set product cache template
SETEX product:1001 3600 '{"id":1001,"name":"Sample Product","price":29.99}'

# Exit Redis CLI
exit
```

## Environment Configuration

### Application Properties

#### 1. Primary Configuration (application.yml)
```yaml
# src/main/resources/application.yml
server:
  port: 8106

spring:
  application:
    name: marketplace-service
  
  profiles:
    active: development
  
  datasource:
    url: jdbc:postgresql://localhost:5432/marketplace_db
    username: marketplace_user
    password: marketplace_password_2024
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 10
        min-idle: 1

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.gogidix.socialcommerce.marketplace: INFO
    org.springframework.cloud.netflix.eureka: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

#### 2. Development Profile (application-development.yml)
```yaml
# src/main/resources/application-development.yml
spring:
  datasource:
    url: jdbc:h2:mem:marketplace_db
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

logging:
  level:
    com.gogidix.socialcommerce.marketplace: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 3. Production Profile (application-production.yml)
```yaml
# src/main/resources/application-production.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL}

logging:
  level:
    com.gogidix.socialcommerce.marketplace: WARN
    org.springframework.cloud.netflix.eureka: INFO
```

### Environment Variables

#### Development Environment
```bash
# Create .env file in project root
cat > .env << EOF
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/marketplace_db
DATABASE_USERNAME=marketplace_user
DATABASE_PASSWORD=marketplace_password_2024

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# Application Configuration
SPRING_PROFILES_ACTIVE=development
SERVER_PORT=8106

# External Service URLs
PRODUCT_SERVICE_URL=http://localhost:8111
COMMISSION_SERVICE_URL=http://localhost:8102
PAYMENT_GATEWAY_URL=http://localhost:8086
ANALYTICS_SERVICE_URL=http://localhost:8101

# Security Configuration
JWT_SECRET=marketplace_jwt_secret_key_2024
JWT_EXPIRATION=86400

# Business Configuration
DEFAULT_CURRENCY=EUR
MAX_CART_ITEMS=50
SESSION_TIMEOUT=3600
COMMISSION_RATE=0.05

# Feature Flags
ENABLE_PRODUCT_RECOMMENDATIONS=true
ENABLE_VENDOR_ANALYTICS=true
ENABLE_REAL_TIME_INVENTORY=true
EOF

# Source environment variables
source .env
```

## Service Dependencies Setup

### Required Services Configuration

#### 1. Product Service Integration
```yaml
# Configure Product Service client
product-service:
  url: http://localhost:8111
  timeout: 5000
  retry:
    max-attempts: 3
    delay: 1000
```

#### 2. Commission Service Integration
```yaml
# Configure Commission Service client
commission-service:
  url: http://localhost:8102
  timeout: 3000
  commission-rate: 0.05
```

#### 3. Payment Gateway Integration
```yaml
# Configure Payment Gateway client
payment-gateway:
  url: http://localhost:8086
  timeout: 10000
  supported-methods:
    - CREDIT_CARD
    - PAYPAL
    - BANK_TRANSFER
```

#### 4. Analytics Service Integration
```yaml
# Configure Analytics Service client
analytics-service:
  url: http://localhost:8101
  timeout: 2000
  batch-size: 100
```

## Security Setup

### SSL/TLS Configuration

#### 1. Generate Development Certificates
```bash
# Create keystore for HTTPS (development only)
keytool -genkeypair -alias marketplace-service \
        -keyalg RSA -keysize 2048 \
        -storetype PKCS12 \
        -keystore marketplace-keystore.p12 \
        -validity 365 \
        -dname "CN=localhost, OU=Development, O=Gogidix, L=Dublin, ST=Leinster, C=IE"

# Move to resources directory
mv marketplace-keystore.p12 src/main/resources/
```

#### 2. SSL Configuration
```yaml
# Add to application-production.yml
server:
  ssl:
    key-store: classpath:marketplace-keystore.p12
    key-store-password: marketplace_keystore_password
    key-store-type: PKCS12
    key-alias: marketplace-service
  port: 8443

# HTTP to HTTPS redirect
security:
  require-ssl: true
```

### JWT Configuration

#### 1. JWT Secret Generation
```bash
# Generate secure JWT secret
openssl rand -base64 64

# Add to environment variables
export JWT_SECRET="generated_jwt_secret_here"
export JWT_EXPIRATION=86400
```

## Development Workspace Setup

### IDE Configuration

#### 1. IntelliJ IDEA Setup
```bash
# Import project as Maven project
# File -> Open -> Select marketplace/pom.xml

# Configure JDK 17
# File -> Project Structure -> Project -> Project SDK

# Install required plugins:
# - Spring Boot
# - Lombok
# - Database Navigator
```

#### 2. VS Code Setup
```json
// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.checkstyle.on": true,
  "files.exclude": {
    "**/target": true,
    "**/.mvn": true
  }
}
```

#### 3. Required Extensions (VS Code)
- Extension Pack for Java
- Spring Boot Extension Pack
- REST Client
- Database Client

### Git Hooks Setup

#### 1. Pre-commit Hook
```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
echo "Running pre-commit checks..."

# Run Maven tests
mvn test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Run checkstyle
mvn checkstyle:check
if [ $? -ne 0 ]; then
    echo "Checkstyle violations found. Commit aborted."
    exit 1
fi

echo "Pre-commit checks passed."
EOF

# Make executable
chmod +x .git/hooks/pre-commit
```

## Build and Compilation

### Maven Build

#### 1. Clean Build
```bash
# Navigate to marketplace service directory
cd social-commerce/marketplace

# Clean previous builds
mvn clean

# Compile source code
mvn compile

# Run tests
mvn test

# Package application
mvn package

# Install to local repository
mvn install
```

#### 2. Build Profiles
```bash
# Development build
mvn clean package -Pdevelopment

# Production build
mvn clean package -Pproduction

# Build with tests
mvn clean package -Dspring.profiles.active=test

# Skip tests (not recommended)
mvn clean package -DskipTests
```

### Docker Build

#### 1. Docker Configuration
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

ARG JAR_FILE=target/marketplace-service-*.jar
COPY ${JAR_FILE} marketplace-service.jar

EXPOSE 8106

ENTRYPOINT ["java", "-jar", "/marketplace-service.jar"]
```

#### 2. Build Docker Image
```bash
# Build image
docker build -t marketplace-service:latest .

# Run container
docker run -p 8106:8106 \
  -e SPRING_PROFILES_ACTIVE=development \
  marketplace-service:latest
```

## Application Startup

### Local Development Startup

#### 1. Manual Startup
```bash
# Start required services first:
# 1. PostgreSQL/H2
# 2. Redis
# 3. Eureka Server (port 8761)
# 4. Config Server (port 8888)

# Start marketplace service
cd social-commerce/marketplace
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Or run JAR directly
java -jar target/marketplace-service-1.0.0.jar
```

#### 2. Docker Compose Startup
```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: marketplace_db
      POSTGRES_USER: marketplace_user
      POSTGRES_PASSWORD: marketplace_password_2024
    ports:
      - "5432:5432"

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

  marketplace-service:
    build: .
    ports:
      - "8106:8106"
    depends_on:
      - postgres
      - redis
    environment:
      - SPRING_PROFILES_ACTIVE=development
```

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f marketplace-service
```

## Health Checks and Verification

### Service Health Verification

#### 1. Basic Health Check
```bash
# Check if service is running
curl -s http://localhost:8106/actuator/health
# Expected: {"status":"UP"}

# Detailed health information
curl -s http://localhost:8106/actuator/health | jq
```

#### 2. Service Registration Verification
```bash
# Check Eureka registration
curl -s http://localhost:8761/eureka/apps/MARKETPLACE-SERVICE | grep -A5 instanceId
# Should show marketplace-service instance

# Check service discovery
curl -s http://localhost:8106/actuator/info
```

#### 3. Database Connectivity Test
```bash
# Test database connection
curl -s http://localhost:8106/actuator/health/db
# Expected: {"status":"UP"}

# Test Redis connection
curl -s http://localhost:8106/actuator/health/redis
# Expected: {"status":"UP"}
```

### API Endpoint Testing

#### 1. Marketplace API Tests
```bash
# Test marketplace endpoints
curl -X GET http://localhost:8106/api/v1/marketplace/health
curl -X GET http://localhost:8106/api/v1/marketplace/products
curl -X GET http://localhost:8106/api/v1/marketplace/vendors
curl -X GET http://localhost:8106/api/v1/marketplace/categories
```

#### 2. Load Basic Test Data
```bash
# Load sample vendors
curl -X POST http://localhost:8106/api/v1/marketplace/vendors \
  -H "Content-Type: application/json" \
  -d '{"name":"Sample Vendor","email":"vendor@example.com"}'

# Load sample products
curl -X POST http://localhost:8106/api/v1/marketplace/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Sample Product","price":29.99,"vendorId":1}'
```

### Performance Testing

#### 1. Basic Load Test
```bash
# Install Apache Bench (if not available)
sudo apt install apache2-utils

# Run basic load test
ab -n 1000 -c 10 http://localhost:8106/api/v1/marketplace/products

# Monitor response times
ab -n 100 -c 5 -g marketplace_perf.dat http://localhost:8106/api/v1/marketplace/health
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Service Registration Issues
```bash
# Issue: Service not registering with Eureka
# Solution: Check Eureka server status and network connectivity
curl -s http://localhost:8761/actuator/health

# Verify application.yml eureka configuration
# Check logs for registration errors
tail -f logs/marketplace-service.log | grep -i eureka
```

#### 2. Database Connection Issues
```bash
# Issue: Cannot connect to PostgreSQL
# Solution: Verify database service and credentials
sudo systemctl status postgresql
psql -h localhost -d marketplace_db -U marketplace_user -c "SELECT 1;"

# Check connection string in application.yml
# Verify firewall settings if using remote database
```

#### 3. Redis Connection Issues
```bash
# Issue: Redis connection failed
# Solution: Check Redis service status
sudo systemctl status redis-server
redis-cli ping

# Check Redis configuration
redis-cli CONFIG GET maxmemory
redis-cli CONFIG GET maxmemory-policy
```

#### 4. Port Conflicts
```bash
# Issue: Port 8106 already in use
# Solution: Find and kill conflicting process
sudo netstat -tlnp | grep :8106
sudo kill -9 <PID>

# Or use different port
export SERVER_PORT=8107
mvn spring-boot:run
```

### Logging and Monitoring

#### 1. Enable Debug Logging
```yaml
# application-debug.yml
logging:
  level:
    com.gogidix.socialcommerce.marketplace: DEBUG
    org.springframework: DEBUG
    org.hibernate: DEBUG
```

#### 2. Log File Configuration
```yaml
# application.yml
logging:
  file:
    name: logs/marketplace-service.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

#### 3. Monitoring Setup
```bash
# View real-time logs
tail -f logs/marketplace-service.log

# Monitor JVM metrics
curl -s http://localhost:8106/actuator/metrics/jvm.memory.used

# Monitor custom business metrics
curl -s http://localhost:8106/actuator/metrics/marketplace.orders.total
```

---

**Document Version**: 1.0  
**Last Updated**: June 26, 2025  
**Next Review**: July 26, 2025  
**Maintainer**: Development Team