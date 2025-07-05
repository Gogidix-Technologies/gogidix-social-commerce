# Admin Finalization Service Setup Guide

## Prerequisites

### Development Environment Requirements

#### Java Development Kit
```bash
# Java 17 or later (Required)
java -version
# Expected output: openjdk version "17.0.x" or higher

# Set JAVA_HOME if not already set
export JAVA_HOME=/path/to/java17
echo $JAVA_HOME
```

#### Build Tools
```bash
# Maven 3.6.3 or later
mvn --version
# Expected output: Apache Maven 3.6.x or higher

# Alternative: Gradle 7.x+ (if using Gradle)
gradle --version
```

#### Database Requirements
```bash
# PostgreSQL 12+ (Primary Database)
psql --version
# Expected output: psql (PostgreSQL) 12.x or higher

# H2 Database (Development/Testing)
# Embedded - no separate installation required
```

#### Message Queue (Optional - for event publishing)
```bash
# RabbitMQ 3.8+ (Production)
rabbitmq-diagnostics server_version
# Expected output: 3.8.x or higher

# Redis 6.0+ (Caching)
redis-server --version
# Expected output: Redis server v=6.0.x or higher
```

#### Development Tools
```bash
# Git
git --version

# Docker & Docker Compose (for containerized development)
docker --version
docker-compose --version

# IDE (IntelliJ IDEA, Eclipse, or VS Code)
```

## Quick Start Guide

### 1. Clone Repository
```bash
# Clone the main repository
git clone https://github.com/gogidix/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/social-commerce/admin-finalization

# Switch to development branch
git checkout develop
```

### 2. Database Setup

#### Option A: PostgreSQL (Recommended for Production)
```bash
# Install PostgreSQL (Ubuntu/Debian)
sudo apt update
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create database and user
sudo -u postgres psql
```

```sql
-- Create database
CREATE DATABASE admin_finalization_db;

-- Create user
CREATE USER admin_fin_user WITH ENCRYPTED PASSWORD 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE admin_finalization_db TO admin_fin_user;

-- Connect to database and create schema
\c admin_finalization_db;

-- Create schema (optional - using public by default)
CREATE SCHEMA IF NOT EXISTS admin_workflows;

-- Exit
\q
```

#### Option B: H2 Database (Development)
```bash
# H2 is embedded - no separate setup required
# Database will be created automatically on first run
```

### 3. Environment Configuration
```bash
# Copy environment template
cp src/main/resources/application-template.yml src/main/resources/application-local.yml

# Edit configuration
nano src/main/resources/application-local.yml
```

#### Essential Configuration Properties
```yaml
# application-local.yml
server:
  port: 8081

spring:
  application:
    name: admin-finalization-service
  
  # Database Configuration (PostgreSQL)
  datasource:
    url: jdbc:postgresql://localhost:5432/admin_finalization_db
    username: admin_fin_user
    password: secure_password
    driver-class-name: org.postgresql.Driver
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate  # Use 'create-drop' for development
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  # H2 Configuration (Alternative for development)
  # datasource:
  #   url: jdbc:h2:mem:testdb
  #   driver-class-name: org.h2.Driver
  #   username: sa
  #   password: password
  # h2:
  #   console:
  #     enabled: true
  #     path: /h2-console

  # Eureka Configuration
  eureka:
    client:
      service-url:
        defaultZone: http://localhost:8761/eureka/
      register-with-eureka: true
      fetch-registry: true
    instance:
      prefer-ip-address: true
      hostname: localhost

  # Redis Configuration (Optional)
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  # RabbitMQ Configuration (Optional)
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# Logging Configuration
logging:
  level:
    com.gogidix.adminfinalization: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/admin-finalization.log

# Custom Application Properties
admin-finalization:
  workflow:
    default-timeout: 24h
    max-retries: 3
    auto-approve-threshold: 1000
  security:
    jwt:
      secret: your-jwt-secret-key-here
      expiration: 86400 # 24 hours
  audit:
    enabled: true
    retention-days: 90
```

### 4. Build and Install Dependencies
```bash
# Clean and install dependencies
mvn clean install

# Skip tests for faster build (optional)
mvn clean install -DskipTests

# Download dependencies only
mvn dependency:resolve
```

### 5. Database Migration
```bash
# Run Flyway migrations (if using Flyway)
mvn flyway:migrate

# Or run Liquibase migrations (if using Liquibase)
mvn liquibase:update

# For development, you can use Hibernate auto DDL
# Set spring.jpa.hibernate.ddl-auto=create-drop in application-local.yml
```

### 6. Start Development Server
```bash
# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or with specific configuration
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.config.location=classpath:/application-local.yml"

# Run with debugging enabled
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## Detailed Setup Instructions

### Service Registry Integration

#### 1. Eureka Server Setup
```bash
# Start Eureka Server (if not already running)
cd ../../shared-infrastructure/service-registry
mvn spring-boot:run

# Verify Eureka is running
curl -s http://localhost:8761/actuator/health
```

#### 2. Service Registration Verification
```bash
# Check if service is registered
curl -s http://localhost:8761/eureka/apps | grep ADMIN-FINALIZATION-SERVICE

# View service instances
curl -s http://localhost:8761/eureka/apps/ADMIN-FINALIZATION-SERVICE
```

### Database Setup Details

#### PostgreSQL Advanced Configuration
```bash
# Optimize PostgreSQL for development
sudo -u postgres psql
```

```sql
-- Performance tuning for development
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

-- Reload configuration
SELECT pg_reload_conf();

-- Create indexes for performance
\c admin_finalization_db;

CREATE INDEX IF NOT EXISTS idx_workflows_status ON admin_workflows(status);
CREATE INDEX IF NOT EXISTS idx_workflows_created_at ON admin_workflows(created_at);
CREATE INDEX IF NOT EXISTS idx_workflows_priority ON admin_workflows(priority);
CREATE INDEX IF NOT EXISTS idx_workflows_created_by ON admin_workflows(created_by);
```

#### Sample Data Setup
```sql
-- Insert sample workflow data for testing
INSERT INTO admin_workflows (
    workflow_id, title, description, status, priority, 
    workflow_type, request_data, created_by, created_at
) VALUES 
(
    'WF-001', 
    'Vendor Onboarding Review',
    'Review new vendor application for compliance',
    'PENDING',
    'HIGH',
    'VENDOR_ONBOARDING',
    '{"vendorId": "V123", "businessType": "RETAIL", "documents": ["license.pdf", "tax_cert.pdf"]}',
    'admin@gogidix.com',
    NOW()
),
(
    'WF-002',
    'Product Approval Request',
    'Review product listing for policy compliance',
    'IN_PROGRESS',
    'MEDIUM',
    'PRODUCT_APPROVAL',
    '{"productId": "P456", "category": "ELECTRONICS", "price": 299.99}',
    'moderator@gogidix.com',
    NOW() - INTERVAL '2 hours'
);
```

### Redis Setup (Optional)
```bash
# Install Redis (Ubuntu/Debian)
sudo apt update
sudo apt install redis-server

# Start Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Test Redis connection
redis-cli ping
# Expected response: PONG

# Configure Redis for development
sudo nano /etc/redis/redis.conf
```

```conf
# Redis configuration for development
maxmemory 256mb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

### RabbitMQ Setup (Optional)
```bash
# Install RabbitMQ (Ubuntu/Debian)
sudo apt update
sudo apt install rabbitmq-server

# Start RabbitMQ
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server

# Enable management plugin
sudo rabbitmq-plugins enable rabbitmq_management

# Create admin user
sudo rabbitmqctl add_user admin secure_password
sudo rabbitmqctl set_user_tags admin administrator
sudo rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# Access management UI
# http://localhost:15672 (admin/secure_password)
```

### IDE Configuration

#### IntelliJ IDEA Setup
1. **Import Project**
   ```
   File -> Open -> Select admin-finalization directory
   Import as Maven project
   ```

2. **Configure Project SDK**
   ```
   File -> Project Structure -> Project Settings -> Project
   Set Project SDK to Java 17
   Set Project language level to 17
   ```

3. **Configure Run Configuration**
   ```
   Run -> Edit Configurations -> Add New -> Spring Boot
   Main class: com.gogidix.adminfinalization.AdminFinalizationApplication
   VM options: -Dspring.profiles.active=local
   Environment variables: Add any required variables
   ```

4. **Install Useful Plugins**
   - Spring Boot Assistant
   - Database Navigator
   - Docker
   - SonarLint

#### VS Code Setup
```json
// .vscode/settings.json
{
  "java.home": "/path/to/java17",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/path/to/java17"
    }
  ],
  "spring-boot.ls.java.home": "/path/to/java17",
  "java.compile.nullAnalysis.mode": "automatic"
}
```

```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Admin Finalization Service",
      "request": "launch",
      "mainClass": "com.gogidix.adminfinalization.AdminFinalizationApplication",
      "projectName": "admin-finalization-service",
      "args": "--spring.profiles.active=local",
      "vmArgs": "-Dserver.port=8081"
    }
  ]
}
```

### Docker Development Setup

#### Dockerfile
```dockerfile
# Multi-stage build for development
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appuser /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose for Development
```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  admin-finalization:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/admin_finalization_db
      - SPRING_REDIS_HOST=redis
      - SPRING_RABBITMQ_HOST=rabbitmq
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka/
    depends_on:
      - postgres
      - redis
      - rabbitmq
    volumes:
      - ./logs:/app/logs

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: admin_finalization_db
      POSTGRES_USER: admin_fin_user
      POSTGRES_PASSWORD: secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: secure_password
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  postgres_data:
  redis_data:
  rabbitmq_data:
```

### Testing Setup

#### Unit Testing Configuration
```bash
# Run unit tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AdminWorkflowServiceTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

#### Integration Testing Setup
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  
  h2:
    console:
      enabled: true

# Disable external services for testing
eureka:
  client:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

#### Test Database Setup
```java
// TestDataSetup.java
@TestConfiguration
public class TestDataSetup {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .addScript("classpath:test-data.sql")
            .build();
    }
}
```

### Troubleshooting

#### Common Setup Issues

1. **Port Already in Use**
   ```bash
   # Check what's using port 8081
   lsof -i :8081
   
   # Kill process using the port
   sudo kill -9 $(lsof -t -i:8081)
   
   # Or use different port
   mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
   ```

2. **Database Connection Failed**
   ```bash
   # Check PostgreSQL status
   sudo systemctl status postgresql
   
   # Test database connection
   psql -h localhost -U admin_fin_user -d admin_finalization_db
   
   # Check connection in application
   curl http://localhost:8081/actuator/health
   ```

3. **Eureka Registration Failed**
   ```bash
   # Check Eureka server status
   curl http://localhost:8761/actuator/health
   
   # Check network connectivity
   telnet localhost 8761
   
   # Verify service configuration
   curl http://localhost:8081/actuator/configprops
   ```

4. **OutOfMemoryError**
   ```bash
   # Increase heap size
   export MAVEN_OPTS="-Xmx1024m -Xms512m"
   mvn spring-boot:run
   
   # Or set in run configuration
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1024m"
   ```

### Performance Optimization

#### JVM Tuning for Development
```bash
# Set JVM options for development
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Enable JMX for monitoring
export JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
```

#### Database Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # Adjust based on expected load
      minimum-idle: 2
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
```

## Next Steps

1. **Verify Installation**
   ```bash
   # Check service health
   curl http://localhost:8081/actuator/health
   
   # Check API documentation
   open http://localhost:8081/swagger-ui.html
   
   # Test basic endpoint
   curl http://localhost:8081/api/v1/admin-finalization/workflows
   ```

2. **Development Workflow**
   - Set up IDE with proper configuration
   - Configure code formatting and linting
   - Set up pre-commit hooks
   - Review API documentation

3. **Integration Setup**
   - Configure with API Gateway
   - Set up monitoring and logging
   - Configure security and authentication
   - Test inter-service communication

## Support Resources

- **Documentation**: `/docs/README.md`, `/docs/API.md`
- **Architecture Guide**: `/docs/ARCHITECTURE.md`
- **Operations Manual**: `/docs/OPERATIONS.md`
- **Support Email**: admin-finalization-team@gogidix.com
- **Slack Channel**: #admin-finalization-service