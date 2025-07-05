# Fulfillment Options Service

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-green.svg)
![Maven](https://img.shields.io/badge/Maven-3.8+-red.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)

Part of the Social E-commerce Ecosystem - Vendor warehouse selection and fulfillment optimization service

## Overview

The Fulfillment Options Service is a sophisticated logistics optimization component that intelligently selects the best fulfillment centers and shipping methods for orders. It considers multiple factors including vendor warehouse locations, inventory availability, shipping costs, delivery times, and customer preferences to provide optimal fulfillment strategies.

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL (Production), H2 (Testing)
- **Build Tool**: Maven
- **Documentation**: OpenAPI 3.0
- **Containerization**: Docker
- **Messaging**: RabbitMQ/Kafka

### Key Components
- **Warehouse Optimizer**: Intelligent warehouse selection algorithm
- **Shipping Calculator**: Cost and time estimation engine
- **Inventory Checker**: Real-time inventory validation
- **Route Planner**: Delivery route optimization
- **Zone Manager**: Geographic zone and coverage management

## Features

### Core Functionality
- ✅ **Intelligent Warehouse Selection**: AI-powered fulfillment center selection
- ✅ **Multi-carrier Shipping**: Integration with multiple shipping providers
- ✅ **Cost Optimization**: Minimum cost fulfillment strategies
- ✅ **Delivery Time Optimization**: Fastest delivery route calculation
- ✅ **Inventory-aware Selection**: Real-time inventory consideration
- ✅ **Geographic Zone Management**: Location-based fulfillment rules

### Business Rules
- **Proximity Priority**: Prefer closest warehouse to customer
- **Inventory Availability**: Only select warehouses with sufficient stock
- **Cost Optimization**: Balance shipping cost vs delivery time
- **Vendor Preferences**: Honor vendor-specific fulfillment rules
- **Service Level Agreements**: Meet promised delivery commitments

## API Endpoints

### Fulfillment Options
```
POST   /api/v1/fulfillment/options           # Get fulfillment options
POST   /api/v1/fulfillment/optimize          # Optimize fulfillment strategy
GET    /api/v1/fulfillment/estimate          # Get cost/time estimates
POST   /api/v1/fulfillment/validate          # Validate fulfillment option
```

### Warehouse Management
```
GET    /api/v1/warehouses                    # Get all warehouses
GET    /api/v1/warehouses/{id}               # Get warehouse details
POST   /api/v1/warehouses                    # Create warehouse
PUT    /api/v1/warehouses/{id}               # Update warehouse

GET    /api/v1/warehouses/vendor/{vendorId}  # Get vendor warehouses
GET    /api/v1/warehouses/zone/{zoneId}      # Get warehouses by zone
```

### Shipping Methods
```
GET    /api/v1/shipping/methods              # Get shipping methods
GET    /api/v1/shipping/carriers             # Get available carriers
POST   /api/v1/shipping/calculate            # Calculate shipping cost
GET    /api/v1/shipping/zones                # Get shipping zones
```

### Analytics and Reporting
```
GET    /api/v1/analytics/performance         # Fulfillment performance
GET    /api/v1/analytics/costs               # Cost analysis
GET    /api/v1/reports/efficiency            # Efficiency reports
```

## Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fulfillment_db
SPRING_DATASOURCE_USERNAME=fulfillment_user
SPRING_DATASOURCE_PASSWORD=fulfillment_password

# Service Configuration
SERVER_PORT=8103
SERVICE_NAME=fulfillment-options

# Fulfillment Settings
FULFILLMENT_MAX_WAREHOUSES=5
FULFILLMENT_DEFAULT_ZONE=DOMESTIC
FULFILLMENT_OPTIMIZATION_MODE=COST_FIRST

# External Service URLs
INVENTORY_SERVICE_URL=http://inventory-service:8015
SHIPPING_SERVICE_URL=http://shipping-service:8200
WAREHOUSE_SERVICE_URL=http://warehouse-service:8014

# Geographic Services
GEOCODING_API_KEY=your_geocoding_api_key
MAP_SERVICE_URL=https://api.mapservice.com
```

### Database Schema
```sql
-- Warehouses Table
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    address JSONB NOT NULL,
    coordinates POINT,
    capacity INTEGER DEFAULT 0,
    current_utilization DECIMAL(5,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    operating_hours JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fulfillment Zones Table
CREATE TABLE fulfillment_zones (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    zone_type VARCHAR(50) NOT NULL,
    geographic_boundary POLYGON,
    coverage_countries TEXT[],
    coverage_states TEXT[],
    coverage_cities TEXT[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fulfillment Options History Table
CREATE TABLE fulfillment_options_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_address JSONB NOT NULL,
    selected_warehouse_id BIGINT,
    selected_shipping_method VARCHAR(100),
    optimization_criteria VARCHAR(50),
    calculated_cost DECIMAL(10,2),
    estimated_delivery_time INTERVAL,
    options_generated JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Development

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+ (for local development)
- Redis (for caching)

### Setup
```bash
# Clone repository
git clone <repository-url>
cd fulfillment-options

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
  "type": "FulfillmentOptionSelected",
  "data": {
    "orderId": "12345",
    "warehouseId": "789",
    "shippingMethod": "EXPRESS",
    "estimatedCost": 15.99,
    "estimatedDelivery": "2024-01-17T10:00:00Z",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

#### Subscribed Events
```json
{
  "type": "OrderCreated",
  "data": {
    "orderId": "12345",
    "customerId": "456",
    "vendorId": "789",
    "deliveryAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "US"
    },
    "items": [
      {
        "productId": "101",
        "quantity": 2,
        "weight": 1.5,
        "dimensions": {
          "length": 10,
          "width": 8,
          "height": 6
        }
      }
    ]
  }
}
```

### External Dependencies
- **Inventory Service**: Stock availability validation
- **Warehouse Service**: Warehouse data and management
- **Shipping Service**: Carrier rates and transit times
- **Geocoding Service**: Address validation and coordinates

## Business Logic

### Fulfillment Optimization Algorithm

1. **Input Validation**
   - Validate delivery address
   - Confirm item availability
   - Check order constraints

2. **Warehouse Filtering**
   - Filter by inventory availability
   - Check geographic coverage
   - Validate operational status

3. **Option Generation**
   - Calculate shipping costs for each warehouse
   - Estimate delivery times
   - Apply business rules and constraints

4. **Optimization Scoring**
   - Cost-based scoring (30%)
   - Time-based scoring (40%)
   - Reliability scoring (20%)
   - Vendor preference scoring (10%)

5. **Result Ranking**
   - Sort by composite score
   - Apply tie-breaking rules
   - Return top N options

### Cost Calculation
```java
public FulfillmentCost calculateCost(Warehouse warehouse, 
                                   ShippingAddress address, 
                                   List<OrderItem> items) {
    // Base shipping cost
    BigDecimal shippingCost = shippingCalculator.calculate(
        warehouse.getLocation(), 
        address, 
        getTotalWeight(items)
    );
    
    // Handling and processing fees
    BigDecimal handlingFee = calculateHandlingFee(items);
    
    // Distance-based adjustments
    BigDecimal distanceAdjustment = calculateDistanceAdjustment(
        warehouse.getLocation(), 
        address
    );
    
    // Zone-based surcharges
    BigDecimal zoneSurcharge = getZoneSurcharge(address);
    
    return FulfillmentCost.builder()
        .baseShipping(shippingCost)
        .handlingFee(handlingFee)
        .distanceAdjustment(distanceAdjustment)
        .zoneSurcharge(zoneSurcharge)
        .totalCost(shippingCost.add(handlingFee)
                                .add(distanceAdjustment)
                                .add(zoneSurcharge))
        .build();
}
```

## Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: PostgreSQL connectivity check
- **External Service Health**: Dependent service availability
- **Cache Health**: Redis connectivity and performance

### Metrics
- **Option Generation Rate**: Requests per second
- **Selection Accuracy**: Prediction vs actual performance
- **Cost Optimization**: Average cost savings
- **Delivery Time Accuracy**: Estimated vs actual delivery times

## Deployment

### Docker
```bash
# Build image
docker build -t fulfillment-options:latest .

# Run container
docker run -p 8103:8103 fulfillment-options:latest
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fulfillment-options
spec:
  replicas: 3
  selector:
    matchLabels:
      app: fulfillment-options
  template:
    metadata:
      labels:
        app: fulfillment-options
    spec:
      containers:
      - name: fulfillment-options
        image: fulfillment-options:latest
        ports:
        - containerPort: 8103
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

## Security

- **Authentication**: JWT token validation
- **Authorization**: Role-based access control
- **Data Encryption**: Sensitive logistics data encryption
- **Audit Logging**: Complete fulfillment decision audit trail
- **Rate Limiting**: API rate limiting protection

## Performance

- **Caching Strategy**: Redis caching for warehouse data and zones
- **Database Optimization**: Spatial indexes for geographic queries
- **Async Processing**: Non-blocking fulfillment calculations
- **Batch Processing**: Bulk fulfillment option generation

## Troubleshooting

### Common Issues
1. **Geographic Calculation Errors**: Verify geocoding service availability
2. **Inventory Sync Issues**: Check inventory service integration
3. **Shipping Rate Errors**: Validate carrier API connections
4. **Zone Configuration**: Review geographic zone boundaries

### Support
- **Documentation**: `/docs` directory
- **API Docs**: `/swagger-ui.html`
- **Logs**: Application logs in `/logs`
- **Metrics**: Actuator endpoints for monitoring

## License

Proprietary - Gogidix Application Limited
