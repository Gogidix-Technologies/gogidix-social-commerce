# Commission Service Documentation

## Overview

The Commission Service is a critical financial component within the Social E-commerce Ecosystem that manages the calculation, tracking, and distribution of commissions for vendor transactions. This service implements sophisticated commission calculation algorithms, supports multi-tier rate structures, and provides comprehensive reporting capabilities for transparent financial operations.

## Business Context

In the social commerce ecosystem, commission management is fundamental to the platform's revenue model and vendor relationships:

- **Transaction-Based Revenue**: Calculate commissions on every completed transaction
- **Flexible Rate Structures**: Support vendor-specific, category-based, and promotional commission rates
- **Financial Transparency**: Provide clear commission breakdowns and reporting for vendors
- **Compliance Requirements**: Maintain audit trails and compliance with financial regulations
- **Performance Incentives**: Enable dynamic commission rates based on vendor performance
- **Multi-Currency Support**: Handle commission calculations across different currencies and regions

The Commission Service ensures accurate, transparent, and efficient commission processing while supporting the platform's growth and vendor satisfaction.

## Current Implementation Status

### ✅ Implemented Features
- **Basic Service Infrastructure**: Spring Boot application with health monitoring
- **Service Discovery Integration**: Eureka client registration
- **Health Monitoring**: Actuator endpoints for service health
- **Build Configuration**: Maven dependencies and Docker setup
- **Comprehensive Documentation**: Complete API specifications and operational guides

### 🚧 In Development
- **Commission Calculation Engine**: Core algorithm implementation for rate calculation
- **Database Schema**: Commission and rate entity models and migrations
- **REST API Controllers**: Implementation of documented API endpoints
- **Rate Management System**: Commission rate configuration and hierarchy
- **Integration Layer**: External service connectors for order and payment data

### 📋 Planned Features
- **Advanced Reporting**: Business intelligence and analytics dashboards
- **Real-Time Processing**: Event-driven commission calculations
- **Performance-Based Rates**: Dynamic commission adjustments based on metrics
- **Multi-Tenant Support**: Vendor-specific commission structures
- **Audit and Compliance**: Enhanced audit trails and regulatory reporting

## Components

### Core Components

- **CommissionServiceApplication**: Main Spring Boot application with microservices integration
- **HealthCheckController**: Service health monitoring and status reporting
- **Commission Calculator**: Core engine for commission calculation with business rules
- **Rate Manager**: Commission rate configuration and priority-based selection
- **Transaction Processor**: Commission transaction lifecycle management

### Feature Components

- **Commission Calculation**: Real-time commission computation with complex business rules
- **Rate Configuration**: Hierarchical commission rate management (vendor, category, promotional)
- **Transaction Management**: Complete commission transaction lifecycle tracking
- **Reporting Engine**: Comprehensive commission analytics and vendor reporting
- **Audit Service**: Transaction auditing, compliance tracking, and financial reconciliation
- **Performance Analytics**: Commission performance metrics and vendor insights

### Data Access Layer

- **Commission Repository**: Core commission transaction data access
- **Rate Repository**: Commission rate configuration and hierarchy management
- **Transaction Repository**: Financial transaction data and audit trails
- **Vendor Repository**: Vendor-specific commission configuration
- **Report Repository**: Pre-aggregated reporting data and analytics

### Utility Services

- **Rate Calculator**: Mathematical commission calculation utilities
- **Currency Converter**: Multi-currency commission calculation support
- **Audit Logger**: Comprehensive financial transaction logging
- **Notification Manager**: Commission-related notifications and alerts
- **Data Validator**: Financial data validation and compliance checking

### Integration Components

- **Order Service Client**: Integration for order data and transaction details
- **Payment Gateway Client**: Payment processing status and transaction verification
- **Vendor Service Client**: Vendor information and commission rate preferences
- **Notification Service Client**: Commission notifications and payment alerts
- **Analytics Service Client**: Commission data aggregation for business intelligence

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL database
- Redis (for caching commission rates)
- Maven 3.6+
- Spring Boot 3.1.5
- Access to order and payment services

### Quick Start
1. Configure database connection for commission data storage
2. Set up Redis connection for commission rate caching
3. Configure external service connections (order, payment, vendor services)
4. Set up commission rate hierarchy and default rates
5. Run `mvn spring-boot:run` to start the service
6. Access API documentation at `http://localhost:8084/swagger-ui.html`

### Basic Configuration Example

```yaml
# application.yml
server:
  port: 8084

commission:
  calculation:
    default-rate: 0.05
    minimum-commission: 0.01
    maximum-commission: 50.00
  rates:
    cache-ttl: 3600
    refresh-interval: 300
  
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/commission_db
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
  
external-services:
  order-service:
    url: http://localhost:8108
  payment-gateway:
    url: http://localhost:8109
  vendor-service:
    url: http://localhost:8113
```

## Examples

### Commission Calculation

```bash
# Calculate commission for a transaction
curl -X POST http://localhost:8084/api/v1/commissions/calculate \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN_12345",
    "vendorId": "VENDOR_789",
    "categoryId": "FASHION",
    "amount": 150.00,
    "currency": "USD"
  }'

# Get commission details
curl -X GET http://localhost:8084/api/v1/commissions/COMM_67890 \
  -H "Authorization: Bearer <jwt-token>"
```

### Rate Management

```bash
# Get vendor-specific commission rates
curl -X GET http://localhost:8084/api/v1/rates/vendor/VENDOR_789 \
  -H "Authorization: Bearer <jwt-token>"

# Update commission rate
curl -X PUT http://localhost:8084/api/v1/rates/RATE_456 \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "rate": 0.06,
    "effectiveDate": "2024-02-01T00:00:00Z",
    "expiryDate": "2024-12-31T23:59:59Z"
  }'
```

### Commission Reporting

```java
// Example: Commission calculation service
@Service
public class CommissionCalculationService {
    
    public CommissionResult calculateCommission(CommissionRequest request) {
        // Get applicable commission rate
        CommissionRate rate = rateService.getApplicableRate(
            request.getVendorId(), 
            request.getCategoryId(), 
            request.getTransactionDate()
        );
        
        // Calculate base commission
        BigDecimal baseCommission = request.getAmount()
            .multiply(rate.getRate())
            .setScale(2, RoundingMode.HALF_UP);
        
        // Apply performance bonuses
        BigDecimal performanceBonus = performanceService
            .calculatePerformanceBonus(request.getVendorId(), baseCommission);
        
        // Apply promotions
        BigDecimal promotionalAdjustment = promotionService
            .calculatePromotionalAdjustment(request, baseCommission);
        
        BigDecimal finalCommission = baseCommission
            .add(performanceBonus)
            .add(promotionalAdjustment);
        
        return CommissionResult.builder()
            .baseCommission(baseCommission)
            .performanceBonus(performanceBonus)
            .promotionalAdjustment(promotionalAdjustment)
            .finalCommission(finalCommission)
            .rate(rate)
            .calculatedAt(Instant.now())
            .build();
    }
}
```

### Rate Hierarchy Management

```java
// Example: Commission rate hierarchy
@Entity
public class CommissionRate {
    
    public enum RateType {
        GLOBAL_DEFAULT,
        CATEGORY_SPECIFIC,
        VENDOR_SPECIFIC,
        PROMOTIONAL,
        PERFORMANCE_TIER
    }
    
    public CommissionRate getEffectiveRate(String vendorId, String categoryId, LocalDateTime date) {
        // Priority order: Promotional > Vendor-specific > Category-specific > Global default
        return rateRepository.findEffectiveRate(vendorId, categoryId, date)
            .stream()
            .sorted(Comparator.comparing(CommissionRate::getPriority).reversed())
            .findFirst()
            .orElse(getGlobalDefaultRate());
    }
}
```

### Real-Time Commission Processing

```java
// Example: Event-driven commission processing
@Component
public class CommissionEventProcessor {
    
    @EventListener
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        try {
            CommissionRequest request = CommissionRequest.builder()
                .transactionId(event.getOrderId())
                .vendorId(event.getVendorId())
                .categoryId(event.getCategoryId())
                .amount(event.getTotalAmount())
                .currency(event.getCurrency())
                .transactionDate(event.getCompletedAt())
                .build();
            
            CommissionResult result = commissionCalculationService
                .calculateCommission(request);
            
            commissionTransactionService
                .createCommissionTransaction(result);
            
            notificationService
                .notifyVendorCommissionEarned(event.getVendorId(), result);
                
        } catch (Exception ex) {
            log.error("Failed to process commission for order: {}", event.getOrderId(), ex);
            commissionEventService.scheduleRetry(event);
        }
    }
}
```

## Best Practices

### Financial Accuracy
1. **Precision Handling**: Use BigDecimal for all monetary calculations to avoid floating-point errors
2. **Rounding Standards**: Apply consistent rounding rules across all calculations
3. **Currency Handling**: Implement proper currency conversion and precision management
4. **Audit Trails**: Maintain complete audit logs for all commission calculations
5. **Data Validation**: Validate all financial data inputs for accuracy and compliance

### Performance Optimization
1. **Rate Caching**: Cache commission rates in Redis for fast lookup
2. **Batch Processing**: Process commission calculations in optimized batches
3. **Database Optimization**: Use appropriate indexes for commission queries
4. **Async Processing**: Use asynchronous processing for non-critical commission operations
5. **Connection Pooling**: Optimize database connections for high-throughput operations

### Security
1. **Financial Data Protection**: Encrypt sensitive commission and rate data
2. **Access Control**: Implement strict role-based access for commission management
3. **Audit Logging**: Log all access and modifications to commission data
4. **Input Validation**: Validate all commission calculation inputs
5. **Rate Tampering Protection**: Implement safeguards against unauthorized rate changes

### Compliance
1. **Regulatory Compliance**: Ensure compliance with financial regulations
2. **Data Retention**: Implement appropriate data retention policies for commission records
3. **Reporting Standards**: Maintain standardized reporting for auditing purposes
4. **Transaction Traceability**: Ensure complete traceability of all commission transactions
5. **Documentation**: Maintain comprehensive documentation for compliance audits

### Integration
1. **Service Resilience**: Implement circuit breakers for external service calls
2. **Data Consistency**: Ensure data consistency across commission-related services
3. **Event Processing**: Handle commission events reliably with retry mechanisms
4. **API Versioning**: Maintain backward compatibility for commission APIs
5. **Error Handling**: Implement comprehensive error handling for financial operations

### Monitoring
1. **Financial Metrics**: Monitor commission calculation accuracy and performance
2. **Rate Monitoring**: Track commission rate changes and their impacts
3. **Transaction Monitoring**: Monitor commission transaction volumes and patterns
4. **Error Tracking**: Track and alert on commission calculation errors
5. **Performance Monitoring**: Monitor service performance and response times

## Commission Rate Hierarchy

### Rate Priority System
1. **Promotional Rates** (Highest Priority) - Limited-time promotional commission rates
2. **Performance-Based Rates** - Rates based on vendor performance metrics
3. **Vendor-Specific Rates** - Negotiated rates for individual vendors
4. **Category-Specific Rates** - Rates specific to product categories
5. **Global Default Rates** (Lowest Priority) - Platform-wide default commission rates

### Rate Configuration Examples

```yaml
# Commission rate examples
commission-rates:
  global-default: 0.05  # 5%
  categories:
    FASHION: 0.06       # 6%
    ELECTRONICS: 0.04   # 4%
    BOOKS: 0.08         # 8%
  vendor-tiers:
    PREMIUM: 0.03       # 3%
    STANDARD: 0.05      # 5%
    STARTER: 0.07       # 7%
  promotional:
    NEW_VENDOR_PROMO: 0.02  # 2% for first 3 months
    HIGH_VOLUME_PROMO: 0.04 # 4% for orders >$1000
```

## Development Roadmap

### Phase 1: Core Foundation (In Progress)
- ✅ Basic service infrastructure
- 🚧 Database schema and entity models
- 🚧 Core commission calculation engine
- 🚧 Basic rate management system
- 📋 REST API implementation

### Phase 2: Business Logic
- 📋 Advanced commission calculation algorithms
- 📋 Rate hierarchy and priority system
- 📋 Transaction lifecycle management
- 📋 Basic reporting capabilities
- 📋 Integration with external services

### Phase 3: Advanced Features
- 📋 Performance-based commission adjustments
- 📋 Real-time event processing
- 📋 Advanced analytics and reporting
- 📋 Multi-currency support
- 📋 Compliance and audit enhancements

### Phase 4: Enterprise Features
- 📋 Machine learning for rate optimization
- 📋 Advanced fraud detection
- 📋 Comprehensive business intelligence
- 📋 Multi-tenant architecture
- 📋 Global compliance framework