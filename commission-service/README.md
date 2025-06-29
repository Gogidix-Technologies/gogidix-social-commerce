# Commission Service

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-green.svg)
![Maven](https://img.shields.io/badge/Maven-3.8+-red.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)

Part of the Social E-commerce Ecosystem - Commission calculation and processing service

## Overview

The Commission Service is a critical component of the social e-commerce ecosystem responsible for calculating, processing, and managing commission structures for vendors, affiliates, and platform operations. It handles complex commission scenarios including tiered structures, promotional rates, and multi-party splits.

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL (Production), H2 (Testing)
- **Build Tool**: Maven
- **Documentation**: OpenAPI 3.0
- **Containerization**: Docker

### Key Components
- **Commission Calculator**: Core calculation engine
- **Rate Manager**: Commission rate configuration
- **Transaction Processor**: Commission transaction handling
- **Reporting Engine**: Commission analytics and reporting
- **Audit Service**: Transaction auditing and compliance

## Features

### Core Functionality
- ✅ **Dynamic Commission Calculation**: Real-time commission computation
- ✅ **Multi-tier Commission Structures**: Support for complex hierarchical rates
- ✅ **Promotional Rate Management**: Temporary and seasonal commission adjustments
- ✅ **Multi-party Commission Splits**: Revenue sharing between multiple stakeholders
- ✅ **Real-time Processing**: Instant commission calculations
- ✅ **Audit Trail**: Complete transaction history and compliance tracking

### Business Rules
- **Vendor Commissions**: Standard rates based on category and volume
- **Affiliate Commissions**: Performance-based compensation
- **Platform Fees**: Service charges and processing fees
- **Promotional Rates**: Time-limited special commission rates
- **Minimum Thresholds**: Minimum transaction amounts for commission eligibility

## API Endpoints

### Commission Management
```
POST   /api/v1/commissions/calculate          # Calculate commission
GET    /api/v1/commissions/{id}              # Get commission details
PUT    /api/v1/commissions/{id}              # Update commission
DELETE /api/v1/commissions/{id}              # Cancel commission

GET    /api/v1/commissions/vendor/{vendorId}  # Get vendor commissions
GET    /api/v1/commissions/order/{orderId}    # Get order commissions
```

### Rate Management
```
GET    /api/v1/rates                         # Get all commission rates
POST   /api/v1/rates                         # Create commission rate
PUT    /api/v1/rates/{id}                    # Update commission rate
DELETE /api/v1/rates/{id}                    # Delete commission rate

GET    /api/v1/rates/vendor/{vendorId}       # Get vendor-specific rates
GET    /api/v1/rates/category/{category}     # Get category-based rates
```

### Reporting
```
GET    /api/v1/reports/commissions           # Commission summary report
GET    /api/v1/reports/vendor/{vendorId}     # Vendor commission report
GET    /api/v1/reports/period/{startDate}/{endDate} # Period-based report
```

## Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/commission_db
SPRING_DATASOURCE_USERNAME=commission_user
SPRING_DATASOURCE_PASSWORD=commission_password

# Service Configuration
SERVER_PORT=8102
SERVICE_NAME=commission-service

# Commission Settings
COMMISSION_DEFAULT_RATE=0.05
COMMISSION_MAX_RATE=0.30
COMMISSION_MIN_THRESHOLD=10.00

# External Service URLs
ORDER_SERVICE_URL=http://order-service:8108
PAYMENT_SERVICE_URL=http://payment-gateway:8109
VENDOR_SERVICE_URL=http://vendor-onboarding:8113
```

### Database Schema
```sql
-- Commission Rates Table
CREATE TABLE commission_rates (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT,
    category_id BIGINT,
    rate_type VARCHAR(50) NOT NULL,
    rate_value DECIMAL(5,4) NOT NULL,
    min_amount DECIMAL(10,2),
    max_amount DECIMAL(10,2),
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Commission Transactions Table
CREATE TABLE commission_transactions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    gross_amount DECIMAL(10,2) NOT NULL,
    commission_rate DECIMAL(5,4) NOT NULL,
    commission_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Development

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+ (for local development)

### Setup
```bash
# Clone repository
git clone <repository-url>
cd commission-service

# Install dependencies
mvn clean install

# Start dependencies
docker-compose up -d postgres redis

# Run application
mvn spring-boot:run
```

### Testing
```bash
# Unit tests
mvn test

# Integration tests
mvn test -Dspring.profiles.active=test

# Test with coverage
mvn test jacoco:report
```

## Integration

### Message Events

#### Published Events
```json
{
  "type": "CommissionCalculated",
  "data": {
    "commissionId": "123",
    "orderId": "456",
    "vendorId": "789",
    "amount": 25.50,
    "rate": 0.05,
    "timestamp": "2024-01-01T10:00:00Z"
  }
}
```

#### Subscribed Events
```json
{
  "type": "OrderCompleted",
  "data": {
    "orderId": "456",
    "vendorId": "789",
    "totalAmount": 510.00,
    "status": "COMPLETED"
  }
}
```

### External Dependencies
- **Order Service**: Order data retrieval
- **Payment Gateway**: Payment processing status
- **Vendor Service**: Vendor information and rates
- **Notification Service**: Commission notifications

## Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: Checks PostgreSQL connectivity
- **External Service Health**: Validates dependent service availability

### Metrics
- **Commission Processing Rate**: Transactions per second
- **Commission Accuracy**: Calculation precision metrics
- **Service Response Time**: API endpoint performance
- **Error Rates**: Failed transaction percentages

## Deployment

### Docker
```bash
# Build image
docker build -t commission-service:latest .

# Run container
docker run -p 8102:8102 commission-service:latest
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: commission-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: commission-service
  template:
    metadata:
      labels:
        app: commission-service
    spec:
      containers:
      - name: commission-service
        image: commission-service:latest
        ports:
        - containerPort: 8102
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

## Security

- **Authentication**: JWT token validation
- **Authorization**: Role-based access control
- **Data Encryption**: Sensitive data encryption at rest
- **Audit Logging**: Complete transaction audit trail
- **Rate Limiting**: API rate limiting protection

## Performance

- **Caching**: Redis caching for commission rates
- **Database Optimization**: Indexed queries and connection pooling
- **Async Processing**: Non-blocking commission calculations
- **Batch Processing**: Bulk commission processing capabilities

## Troubleshooting

### Common Issues
1. **Database Connection**: Verify PostgreSQL connectivity
2. **Rate Calculation**: Check commission rate configurations
3. **External Service**: Validate dependent service availability
4. **Memory Issues**: Monitor JVM heap usage

### Support
- **Documentation**: `/docs` directory
- **API Docs**: `/swagger-ui.html`
- **Logs**: Application logs in `/logs`
- **Metrics**: Actuator endpoints for monitoring

## License

Proprietary - Exalt Application Limited
