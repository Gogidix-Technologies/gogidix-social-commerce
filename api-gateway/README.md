# API Gateway

## Overview

The API Gateway is the central entry point for all social commerce domain services, providing unified API management, security, routing, and monitoring. It handles authentication, authorization, rate limiting, and request/response transformation for all microservices in the social commerce ecosystem.

### Service Details

- **Service Type**: Core Infrastructure
- **Domain**: Social Commerce
- **Port**: 8101
- **Health Check**: `http://localhost:8101/actuator/health`
- **API Documentation**: `http://localhost:8101/swagger-ui.html`

## Architecture

### Position in Ecosystem

The API Gateway acts as the single point of entry for all client applications (web, mobile, and admin interfaces) accessing social commerce services. It provides a unified API interface, handles cross-cutting concerns, and routes requests to appropriate backend services.

### Key Responsibilities

- Centralized API entry point for all social commerce services
- Authentication and authorization enforcement
- Request routing and load balancing
- Rate limiting and throttling
- API versioning and request/response transformation
- Security policies enforcement
- Monitoring and analytics collection
- Circuit breaker pattern implementation

### Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.1.x + Spring Cloud Gateway
- **Database**: Redis (session storage, rate limiting)
- **Service Discovery**: Eureka
- **Security**: Spring Security + JWT
- **Monitoring**: Micrometer + Prometheus
- **Build Tool**: Maven

## API Endpoints

### Gateway Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1/gateway/routes` | List all configured routes |
| GET    | `/api/v1/gateway/health` | Gateway health and service status |
| GET    | `/api/v1/gateway/metrics` | Gateway performance metrics |
| POST   | `/api/v1/gateway/routes/refresh` | Refresh route configurations |

### Routed Services

| Service | Route Prefix | Target Port | Description |
|---------|-------------|-------------|-------------|
| Analytics Service | `/api/v1/analytics/**` | 8100 | Analytics and reporting endpoints |
| Product Service | `/api/v1/products/**` | 8111 | Product catalog and management |
| Order Service | `/api/v1/orders/**` | 8108 | Order processing and management |
| Payment Gateway | `/api/v1/payments/**` | 8109 | Payment processing endpoints |
| Vendor Onboarding | `/api/v1/vendors/**` | 8113 | Vendor registration and management |
| Commission Service | `/api/v1/commissions/**` | 8102 | Commission calculation endpoints |
| Subscription Service | `/api/v1/subscriptions/**` | 8112 | Subscription management |
| Marketplace | `/api/v1/marketplace/**` | 8106 | Main marketplace endpoints |

## Dependencies

### External Services

| Service | Purpose | Communication Method |
|---------|---------|---------------------|
| Auth Service | Authentication and JWT validation | REST |
| Service Registry | Service discovery and health checks | Eureka |
| All Social Commerce Services | Request routing and proxying | HTTP/REST |
| Redis | Session storage and rate limiting | Redis Protocol |

### Infrastructure Dependencies

- Redis (session storage, rate limiting cache)
- Eureka Service Registry (service discovery)
- Load Balancer (production deployment)
- SSL/TLS Certificates (HTTPS termination)

## Configuration

### Environment Variables

```bash
# Server Configuration
SERVER_PORT=8101
GATEWAY_HOST=0.0.0.0

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka
SERVICE_REGISTRY_ENABLED=true

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=gateway_redis_password

# Security Configuration
JWT_SECRET_KEY=your_jwt_secret_key_here
JWT_EXPIRATION_TIME=86400000
AUTH_SERVICE_URL=http://localhost:8080

# Rate Limiting
RATE_LIMIT_ENABLED=true
DEFAULT_RATE_LIMIT=100
DEFAULT_RATE_LIMIT_WINDOW=60

# Circuit Breaker
CIRCUIT_BREAKER_ENABLED=true
CIRCUIT_BREAKER_FAILURE_THRESHOLD=5
CIRCUIT_BREAKER_TIMEOUT=30000

# SSL/TLS Configuration
SSL_ENABLED=false
SSL_KEYSTORE_PATH=/etc/ssl/gateway-keystore.p12
SSL_KEYSTORE_PASSWORD=keystore_password
```

### Route Configuration

Routes are configured dynamically through service discovery and can be customized via application configuration:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: analytics-service
          uri: lb://analytics-service
          predicates:
            - Path=/api/v1/analytics/**
          filters:
            - StripPrefix=3
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Security Features

### Authentication

- JWT Bearer Token validation
- OAuth2 integration support
- API key authentication for service-to-service communication
- Session management with Redis

### Authorization

- Role-based access control (RBAC)
- Endpoint-level permission checking
- Vendor data isolation
- Admin privilege escalation

### Rate Limiting

- Per-user rate limiting
- Per-API endpoint rate limiting
- Burst capacity management
- Distributed rate limiting with Redis

### Security Headers

- CORS policy enforcement
- Security headers injection (HSTS, CSP, etc.)
- Request/response sanitization
- IP whitelisting/blacklisting

## Circuit Breaker Configuration

The gateway implements circuit breaker patterns to handle service failures gracefully:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      default:
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
```

## Development

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Redis 6.0+
- Running Eureka Service Registry

### Local Setup

1. Clone the repository
```bash
git clone https://github.com/social-ecommerce-ecosystem/social-commerce.git
cd social-commerce/api-gateway
```

2. Set up environment variables
```bash
cp .env.template .env
# Edit .env with your local configuration
```

3. Start dependencies
```bash
docker-compose up -d redis eureka
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

# Load testing
mvn gatling:test
```

## Deployment

### Docker

```bash
# Build image
docker build -t api-gateway:latest .

# Run container
docker run -p 8101:8101 \
  --env-file .env \
  api-gateway:latest
```

### Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=api-gateway
```

## Monitoring

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Service Health**: `/actuator/health/services`

### Metrics

- **Prometheus**: `/actuator/prometheus`
- **Custom Metrics**:
  - `gateway_requests_total` - Total requests processed
  - `gateway_request_duration` - Request processing time
  - `gateway_circuit_breaker_state` - Circuit breaker states
  - `gateway_rate_limit_exceeded` - Rate limit violations

### Logging

- **Log Level**: Configurable via `LOG_LEVEL` environment variable
- **Log Format**: JSON structured logging
- **Key Log Patterns**:
  - Request/response logging with correlation IDs
  - Authentication and authorization events
  - Rate limiting and circuit breaker events
  - Service routing and load balancing

## Performance Considerations

- Connection pooling for backend services
- Response caching for static data
- Async request processing
- JVM tuning for high throughput
- Redis connection pooling

## Integration Points

### Warehousing Domain

- Routes warehouse-related API calls to warehousing services
- Handles cross-domain authentication for warehouse operations

### Courier Services Domain

- Integrates courier service APIs into unified gateway
- Manages delivery tracking and logistics endpoints

### Shared Infrastructure

- Integrates with centralized authentication service
- Uses shared monitoring and logging infrastructure
- Connects to centralized configuration management

## Troubleshooting

### Common Issues

1. **Service Discovery Problems**
   - Check Eureka connectivity
   - Verify service registration
   - Review service health endpoints

2. **Rate Limiting Issues**
   - Monitor Redis connectivity
   - Check rate limit configurations
   - Review client request patterns

3. **Circuit Breaker Triggering**
   - Monitor downstream service health
   - Adjust circuit breaker thresholds
   - Review service timeout configurations

## Related Documentation

- [Overall Architecture](/docs/architecture/README.md)
- [Security Guidelines](/docs/security/gateway-security.md)
- [Rate Limiting Configuration](/docs/operations/rate-limiting.md)
- [Service Discovery Setup](/docs/infrastructure/service-discovery.md)

## Contact

- **Team**: Social Commerce Platform Team
- **Slack Channel**: #social-commerce-platform
- **Email**: platform-team@social-ecommerce.com

## License

Proprietary - Social E-commerce Ecosystem
