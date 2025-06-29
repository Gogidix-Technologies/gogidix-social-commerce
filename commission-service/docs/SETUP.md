# Commission Service - Setup Guide

## Prerequisites

### System Requirements
- **Java**: OpenJDK 17 or higher
- **Maven**: 3.8.0 or higher
- **Docker**: 20.10+ with Docker Compose
- **PostgreSQL**: 15+ (for production)
- **Redis**: 7.0+ (for caching)
- **Memory**: Minimum 2GB RAM available
- **Storage**: 10GB free disk space

### Development Tools (Recommended)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**: For version control
- **Postman**: For API testing
- **DBeaver**: For database management

## Environment Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd commission-service
```

### 2. Environment Configuration

Create environment-specific configuration files:

#### Development Environment (`.env.dev`)
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/commission_dev
SPRING_DATASOURCE_USERNAME=commission_user
SPRING_DATASOURCE_PASSWORD=dev_password

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=

# Service Configuration
SERVER_PORT=8102
SPRING_PROFILES_ACTIVE=development

# Commission Business Rules
COMMISSION_DEFAULT_RATE=0.05
COMMISSION_MAX_RATE=0.30
COMMISSION_MIN_THRESHOLD=10.00
COMMISSION_PROCESSING_BATCH_SIZE=100

# External Service URLs
ORDER_SERVICE_URL=http://localhost:8108
PAYMENT_SERVICE_URL=http://localhost:8109
VENDOR_SERVICE_URL=http://localhost:8113
NOTIFICATION_SERVICE_URL=http://localhost:8201

# Security Configuration
JWT_SECRET=your-jwt-secret-key-here
JWT_EXPIRATION=86400

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_EXALT=DEBUG

# Actuator Configuration
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

#### Test Environment (`.env.test`)
```bash
# Test Database (H2 In-Memory)
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# Test Configuration
SPRING_PROFILES_ACTIVE=test
SERVER_PORT=0

# Test-specific Settings
COMMISSION_ENABLE_ASYNC_PROCESSING=false
COMMISSION_ENABLE_CACHING=false
```

### 3. Database Setup

#### PostgreSQL Installation (Development)
```bash
# Using Docker
docker run --name commission-postgres \
  -e POSTGRES_DB=commission_dev \
  -e POSTGRES_USER=commission_user \
  -e POSTGRES_PASSWORD=dev_password \
  -p 5432:5432 \
  -d postgres:15

# Or using Docker Compose
docker-compose up -d postgres
```

#### Database Schema Creation
```sql
-- Connect to PostgreSQL and create database
CREATE DATABASE commission_dev;
CREATE USER commission_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE commission_dev TO commission_user;

-- Switch to commission_dev database
\c commission_dev

-- Create schema (will be auto-created by Flyway migrations)
```

### 4. Redis Setup
```bash
# Using Docker
docker run --name commission-redis \
  -p 6379:6379 \
  -d redis:7-alpine

# Or using Docker Compose
docker-compose up -d redis
```

### 5. Application Configuration

#### application.yml (Main Configuration)
```yaml
spring:
  application:
    name: commission-service
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    open-in-view: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

server:
  port: ${SERVER_PORT:8102}
  servlet:
    context-path: /
  error:
    include-message: always
    include-binding-errors: always

# Commission Service Configuration
commission:
  business-rules:
    default-rate: ${COMMISSION_DEFAULT_RATE:0.05}
    max-rate: ${COMMISSION_MAX_RATE:0.30}
    min-threshold: ${COMMISSION_MIN_THRESHOLD:10.00}
  
  processing:
    batch-size: ${COMMISSION_PROCESSING_BATCH_SIZE:100}
    async-enabled: ${COMMISSION_ENABLE_ASYNC_PROCESSING:true}
    retry-attempts: 3
    retry-delay: 1000
  
  caching:
    enabled: ${COMMISSION_ENABLE_CACHING:true}
    rate-ttl: 1800 # 30 minutes
    calculation-ttl: 300 # 5 minutes

# External Service Configuration
external-services:
  order-service:
    url: ${ORDER_SERVICE_URL:http://order-service:8108}
    timeout: 5000
    retry-attempts: 3
  
  payment-service:
    url: ${PAYMENT_SERVICE_URL:http://payment-gateway:8109}
    timeout: 5000
    retry-attempts: 3
  
  vendor-service:
    url: ${VENDOR_SERVICE_URL:http://vendor-onboarding:8113}
    timeout: 5000
    retry-attempts: 3
  
  notification-service:
    url: ${NOTIFICATION_SERVICE_URL:http://notification-service:8201}
    timeout: 3000
    retry-attempts: 2

# Security Configuration
security:
  jwt:
    secret: ${JWT_SECRET:default-secret-key}
    expiration: ${JWT_EXPIRATION:86400}

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics}
      base-path: /actuator
  
  endpoint:
    health:
      show-details: always
      show-components: always
  
  health:
    circuitbreakers:
      enabled: true
    diskspace:
      enabled: true
    redis:
      enabled: true

# Logging Configuration
logging:
  level:
    root: ${LOGGING_LEVEL_ROOT:INFO}
    com.exalt.socialcommerce.commission: ${LOGGING_LEVEL_COM_EXALT:DEBUG}
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  
  file:
    name: logs/commission-service.log
    max-size: 10MB
    max-history: 10

# Resilience4j Configuration
resilience4j:
  circuitbreaker:
    instances:
      order-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
      
      payment-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 15s
        failure-rate-threshold: 60
  
  retry:
    instances:
      order-service:
        max-attempts: 3
        wait-duration: 1s
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
      
      payment-service:
        max-attempts: 2
        wait-duration: 2s

# OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
  
  info:
    title: Commission Service API
    description: Commission calculation and management service
    version: 1.0.0
    contact:
      name: Development Team
      email: dev@exalt.com
```

## Build and Run

### 1. Maven Build
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package -DskipTests

# Install dependencies
mvn clean install
```

### 2. Database Migration
```bash
# Run Flyway migrations manually (optional - runs automatically on startup)
mvn flyway:migrate
```

### 3. Run Application

#### Development Mode
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Using Java
java -jar target/commission-service-1.0.0.jar --spring.profiles.active=development

# With custom port
java -jar target/commission-service-1.0.0.jar --server.port=8102
```

#### Production Mode
```bash
java -jar commission-service-1.0.0.jar \
  --spring.profiles.active=production \
  --spring.datasource.url=jdbc:postgresql://prod-db:5432/commission_prod \
  --spring.datasource.username=commission_prod_user \
  --spring.datasource.password=secure_password
```

### 4. Docker Deployment

#### Build Docker Image
```bash
# Build image
docker build -t commission-service:latest .

# Build with specific tag
docker build -t commission-service:1.0.0 .
```

#### Run with Docker
```bash
# Run single container
docker run -d \
  --name commission-service \
  -p 8102:8102 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/commission_prod \
  commission-service:latest
```

#### Docker Compose Setup
```yaml
# docker-compose.yml
version: '3.8'

services:
  commission-service:
    build: .
    ports:
      - "8102:8102"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/commission_dev
      - SPRING_REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    networks:
      - commission-network

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=commission_dev
      - POSTGRES_USER=commission_user
      - POSTGRES_PASSWORD=dev_password
    ports:
      - "5432:5432"
    volumes:
      - commission_db_data:/var/lib/postgresql/data
    networks:
      - commission-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - commission-network

volumes:
  commission_db_data:

networks:
  commission-network:
    driver: bridge
```

#### Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up commission-service

# View logs
docker-compose logs -f commission-service

# Stop services
docker-compose down
```

## Verification

### 1. Health Check
```bash
# Application health
curl http://localhost:8102/actuator/health

# Detailed health
curl http://localhost:8102/actuator/health | jq
```

### 2. API Testing
```bash
# Test commission calculation
curl -X POST http://localhost:8102/api/v1/commissions/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 12345,
    "vendorId": 67890,
    "grossAmount": 100.00,
    "categoryId": 1
  }'

# Get commission rates
curl http://localhost:8102/api/v1/rates

# Get vendor commissions
curl http://localhost:8102/api/v1/commissions/vendor/67890
```

### 3. Database Verification
```sql
-- Connect to database and verify tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';

-- Check sample data
SELECT * FROM commission_rates LIMIT 5;
SELECT * FROM commission_transactions LIMIT 5;
```

### 4. Metrics and Monitoring
```bash
# Application metrics
curl http://localhost:8102/actuator/metrics

# Prometheus metrics
curl http://localhost:8102/actuator/prometheus

# Application info
curl http://localhost:8102/actuator/info
```

## Development Workflow

### 1. Code Development
```bash
# Create feature branch
git checkout -b feature/commission-enhancement

# Make changes and test
mvn test

# Run integration tests
mvn test -Dspring.profiles.active=test

# Check code coverage
mvn jacoco:report
```

### 2. Database Changes
```bash
# Create new migration
mkdir -p src/main/resources/db/migration
# Create file: V2__Add_new_commission_features.sql

# Test migration
mvn flyway:migrate
```

### 3. API Documentation
```bash
# Generate OpenAPI docs
mvn spring-boot:run

# Access Swagger UI
open http://localhost:8102/swagger-ui.html

# Download API specification
curl http://localhost:8102/api-docs > api-docs/openapi.yaml
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check PostgreSQL status
docker ps | grep postgres

# Check database connectivity
psql -h localhost -U commission_user -d commission_dev

# View application logs
tail -f logs/commission-service.log | grep -i error
```

#### 2. Redis Connection Issues
```bash
# Check Redis status
docker ps | grep redis

# Test Redis connection
redis-cli -h localhost -p 6379 ping

# Clear Redis cache
redis-cli -h localhost -p 6379 flushall
```

#### 3. Application Startup Issues
```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Run with debug logging
java -jar target/commission-service-1.0.0.jar \
  --logging.level.root=DEBUG \
  --logging.level.com.exalt=TRACE
```

#### 4. Port Conflicts
```bash
# Check port usage
lsof -i :8102

# Kill process using port
kill -9 $(lsof -t -i:8102)

# Use different port
java -jar commission-service-1.0.0.jar --server.port=8103
```

### Performance Tuning

#### JVM Settings
```bash
# Production JVM settings
java -Xms512m -Xmx1g \
  -XX:+UseG1GC \
  -XX:G1HeapRegionSize=16m \
  -XX:+UseStringDeduplication \
  -jar commission-service-1.0.0.jar
```

#### Database Tuning
```sql
-- PostgreSQL performance settings
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
```

### Monitoring Setup

#### Application Metrics
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'commission-service'
    static_configs:
      - targets: ['localhost:8102']
    metrics_path: /actuator/prometheus
    scrape_interval: 10s
```

#### Log Aggregation
```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /app/logs/commission-service.log
  fields:
    service: commission-service
    environment: development

output.elasticsearch:
  hosts: ["elasticsearch:9200"]

setup.kibana:
  host: "kibana:5601"
```

This comprehensive setup guide provides all the necessary information to get the Commission Service running in any environment, from local development to production deployment.