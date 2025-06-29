# Analytics Service

## Overview

The Analytics Service is a core business intelligence service for the social commerce domain, providing comprehensive analytics, reporting, and data insights for social commerce operations. It aggregates data from all marketplace activities including vendor performance, product analytics, order trends, and customer behavior analysis.

### Service Details

- **Service Type**: Core
- **Domain**: Social Commerce
- **Port**: 8100
- **Health Check**: `http://localhost:8100/actuator/health`
- **API Documentation**: `http://localhost:8100/swagger-ui.html`

## Architecture

### Position in Ecosystem

The Analytics Service serves as the central data intelligence hub for social commerce operations, interfacing with all domain services to collect, process, and analyze business metrics. It provides real-time and historical analytics to support business decision-making.

### Key Responsibilities

- Collect and aggregate data from all social commerce services
- Generate real-time analytics dashboards for business intelligence
- Provide vendor performance metrics and marketplace insights
- Analyze customer behavior patterns and purchasing trends
- Generate automated reports for business stakeholders
- Support predictive analytics for inventory and demand forecasting

### Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.1.x
- **Database**: PostgreSQL (primary), MongoDB (analytics data)
- **Analytics Engine**: Apache Spark
- **Data Streaming**: Apache Kafka
- **Cache**: Redis
- **Build Tool**: Maven

## API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1/analytics/dashboard` | Get main analytics dashboard data |
| GET    | `/api/v1/analytics/vendors/{vendorId}` | Get vendor-specific analytics |
| GET    | `/api/v1/analytics/products/performance` | Get product performance metrics |
| GET    | `/api/v1/analytics/orders/trends` | Get order trend analysis |
| GET    | `/api/v1/analytics/customers/behavior` | Get customer behavior insights |
| POST   | `/api/v1/analytics/reports/generate` | Generate custom analytics report |
| GET    | `/api/v1/analytics/reports/{reportId}` | Retrieve generated report |

### Integration Endpoints

| Method | Endpoint | Description | Consumer Services |
|--------|----------|-------------|-------------------|
| POST | `/internal/analytics/events` | Process business events | All social commerce services |
| GET | `/internal/analytics/metrics/{serviceId}` | Get service-specific metrics | Global HQ Admin, Regional Admin |
| POST | `/internal/analytics/bulk-data` | Bulk data ingestion | Data aggregation services |

## Dependencies

### External Services

| Service | Purpose | Communication Method |
|---------|---------|---------------------|
| Product Service | Product data and metrics | REST |
| Order Service | Order analytics and trends | REST |
| Vendor Onboarding | Vendor performance data | REST |
| Commission Service | Financial analytics | REST |
| User Profile Service | Customer behavior data | REST |
| Centralized Data Aggregation | Global analytics integration | Kafka |

### Infrastructure Dependencies

- PostgreSQL Database (analytics metadata)
- MongoDB (time-series analytics data)
- Apache Kafka (real-time data streaming)
- Apache Spark (data processing)
- Redis Cache (dashboard performance)
- Elasticsearch (search analytics)

## Configuration

### Environment Variables

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=social_commerce_analytics
DB_USER=analytics_user
DB_PASSWORD=analytics_password

# MongoDB Configuration
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=analytics_data

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=analytics-service-group

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Spark Configuration
SPARK_MASTER_URL=local[*]
SPARK_APP_NAME=social-commerce-analytics

# Service-Specific Configuration
ANALYTICS_BATCH_SIZE=1000
DASHBOARD_REFRESH_INTERVAL=30
REPORT_RETENTION_DAYS=90
```

### Configuration Files

- `application.yml` - Main configuration
- `application-dev.yml` - Development environment
- `application-prod.yml` - Production environment

## Database Schema

### PostgreSQL Tables

#### analytics_reports
| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | UUID | Primary key | PK, NOT NULL |
| report_name | VARCHAR(255) | Report identifier | NOT NULL |
| report_type | VARCHAR(100) | Type of report | NOT NULL |
| parameters | JSONB | Report parameters | NOT NULL |
| status | VARCHAR(50) | Generation status | NOT NULL |
| generated_at | TIMESTAMP | Generation timestamp | |
| file_path | VARCHAR(500) | Report file location | |

#### analytics_dashboards
| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | UUID | Primary key | PK, NOT NULL |
| dashboard_name | VARCHAR(255) | Dashboard identifier | NOT NULL |
| widget_config | JSONB | Dashboard configuration | NOT NULL |
| user_id | UUID | Dashboard owner | NOT NULL |
| is_default | BOOLEAN | Default dashboard flag | DEFAULT FALSE |
| created_at | TIMESTAMP | Creation timestamp | NOT NULL |

### MongoDB Collections

- `product_analytics` - Product performance metrics
- `vendor_analytics` - Vendor performance data
- `customer_behavior` - Customer interaction patterns
- `order_analytics` - Order trend analysis
- `financial_metrics` - Financial performance data

### Indexes

- `idx_analytics_reports_status` - For report status queries
- `idx_analytics_dashboards_user_id` - For user-specific dashboards
- `idx_product_analytics_date` - MongoDB index for time-series queries

## Message Events

### Published Events

| Event | Topic | Description | Schema |
|-------|-------|-------------|--------|
| AnalyticsReportGenerated | analytics.reports | When report generation completes | ReportGeneratedEvent |
| DashboardUpdated | analytics.dashboards | When dashboard data refreshes | DashboardUpdateEvent |
| AlertTriggered | analytics.alerts | When analytics threshold exceeded | AnalyticsAlertEvent |

### Consumed Events

| Event | Topic | Description | Handler |
|-------|-------|-------------|---------|
| OrderCompleted | orders.completed | Process order completion for analytics | OrderAnalyticsHandler |
| ProductViewed | products.viewed | Track product view analytics | ProductAnalyticsHandler |
| VendorRegistered | vendors.registered | Initialize vendor analytics | VendorAnalyticsHandler |
| PaymentProcessed | payments.processed | Financial analytics processing | FinancialAnalyticsHandler |

## Development

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+
- MongoDB 6.0+
- Apache Kafka 3.0+

### Local Setup

1. Clone the repository
```bash
git clone https://github.com/social-ecommerce-ecosystem/social-commerce.git
cd social-commerce/analytics-service
```

2. Set up environment variables
```bash
cp .env.template .env
# Edit .env with your local configuration
```

3. Start dependencies
```bash
docker-compose up -d postgres mongodb redis kafka
```

4. Run the service
```bash
mvn spring-boot:run
```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# With coverage
mvn test jacoco:report
```

## Deployment

### Docker

```bash
# Build image
docker build -t analytics-service:latest .

# Run container
docker run -p 8100:8100 \
  --env-file .env \
  analytics-service:latest
```

### Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=analytics-service
```

## Monitoring

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`

### Metrics

- **Prometheus**: `/actuator/prometheus`
- **Custom Metrics**:
  - `analytics_reports_generated_total` - Total reports generated
  - `analytics_dashboard_requests_total` - Dashboard request count
  - `analytics_data_processing_duration` - Data processing time

### Logging

- **Log Level**: Configurable via `LOG_LEVEL` environment variable
- **Log Format**: JSON structured logging
- **Key Log Patterns**:
  - Analytics query performance
  - Report generation status
  - Data processing metrics

## Security

### Authentication

- Method: JWT Bearer Token
- Token validation endpoint: `/internal/auth/validate`

### Authorization

- Role-based access control (RBAC)
- Vendor-specific data isolation
- Admin-level analytics access control

### API Rate Limiting

- Default: 100 requests per minute for dashboard endpoints
- 10 requests per hour for report generation
- Configurable via application properties

## Performance Considerations

- Data aggregation optimization with batch processing
- Dashboard caching with Redis
- Asynchronous report generation
- Connection pooling for database operations
- MongoDB sharding for large analytics datasets

## Integration Points

### Warehousing Domain

- Integrates with warehouse analytics for cross-domain insights
- Shares performance metrics with fulfillment analytics

### Courier Services Domain

- Receives delivery performance data for order completion analytics
- Provides vendor shipping analytics

### Shared Infrastructure

- Uses centralized analytics engine for data processing
- Integrates with monitoring service for performance tracking

## Related Documentation

- [Overall Architecture](/docs/architecture/README.md)
- [Data Analytics Strategy](/docs/analytics/data-strategy.md)
- [Business Intelligence Framework](/docs/operations/bi-framework.md)
- [Vendor Analytics Guide](/docs/vendor/analytics-guide.md)

## Contact

- **Team**: Social Commerce Analytics Team
- **Slack Channel**: #social-commerce-analytics
- **Email**: analytics-team@social-ecommerce.com

## License

Proprietary - Social E-commerce Ecosystem
