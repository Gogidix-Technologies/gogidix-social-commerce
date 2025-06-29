# API Gateway Service Documentation

## Overview

The API Gateway Service is the central entry point and traffic controller for the Social E-commerce Ecosystem. This service provides unified API management, routing, security enforcement, and cross-cutting concerns for all microservices within the social commerce domain. It implements the API Gateway pattern to simplify client interactions and centralize common functionality.

## Business Context

In a microservices architecture, the API Gateway serves as a crucial infrastructure component that:

- **Simplifies Client Integration**: Provides a single entry point for frontend applications and external clients
- **Centralizes Security**: Enforces authentication, authorization, and security policies across all services
- **Manages API Traffic**: Implements rate limiting, load balancing, and traffic management
- **Ensures Service Reliability**: Provides circuit breaker patterns and failover mechanisms
- **Standardizes Cross-Cutting Concerns**: Handles logging, monitoring, and request/response transformation
- **Enables API Evolution**: Supports versioning, backward compatibility, and gradual service updates

The API Gateway is essential for maintaining a clean separation between client applications and backend services while providing enterprise-grade reliability and security.

## Current Implementation Status

### ✅ Implemented Features
- **Basic Gateway Infrastructure**: Spring Cloud Gateway with route configuration
- **Service Discovery Integration**: Eureka client for dynamic service routing
- **Health Monitoring**: Actuator endpoints for service health and metrics
- **Container Deployment**: Docker and Kubernetes deployment configurations
- **Basic Routing**: Core routes to order, product, payment, and user services

### 🚧 In Development
- **Authentication Framework**: JWT-based authentication and authorization
- **Advanced Routing**: Dynamic route configuration and management APIs
- **Rate Limiting**: Request throttling and quota management
- **Circuit Breaker**: Resilience patterns for service failures
- **Request/Response Transformation**: Data transformation and enrichment

### 📋 Planned Features
- **Redis Integration**: Session storage and distributed caching
- **Advanced Security**: OAuth2, API key management, security headers
- **API Analytics**: Request tracking, performance metrics, usage analytics
- **Load Balancing**: Advanced load balancing strategies
- **API Versioning**: Comprehensive version management and deprecation

## Components

### Core Components

- **ApiGatewayApplication**: Main Spring Boot application with gateway configuration
- **Route Configuration**: Declarative and programmatic route definitions
- **Service Discovery Client**: Eureka integration for dynamic service resolution
- **Health Check Endpoint**: Actuator-based health monitoring
- **Request Routing Engine**: Spring Cloud Gateway routing mechanism

### Feature Components

- **Authentication Filter**: JWT token validation and user context injection
- **Authorization Filter**: Role-based access control and permission checking
- **Rate Limiting Filter**: Request throttling and quota enforcement
- **Logging Filter**: Request/response logging and audit trail
- **Circuit Breaker**: Service failure detection and recovery patterns
- **Request Transformation**: Data format conversion and enrichment

### Infrastructure Components

- **Load Balancer**: Client-side load balancing with health checks
- **Service Registry Integration**: Dynamic service discovery and registration
- **Configuration Management**: External configuration and feature flags
- **Monitoring Integration**: Metrics collection and observability
- **Security Headers**: CORS, CSRF, and security header management

### Integration Components

- **Backend Service Connectors**: Integration with social commerce microservices
- **External API Proxies**: Third-party API integration and proxying
- **Authentication Providers**: Integration with identity providers
- **Monitoring Systems**: Integration with observability platforms

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Spring Boot 3.1.5
- Spring Cloud Gateway
- Eureka Service Registry (running)
- Backend microservices (for routing)

### Quick Start
1. Ensure Eureka service registry is running
2. Configure service routes in `application.yml`
3. Set up authentication providers (if security is enabled)
4. Run `mvn spring-boot:run` to start the gateway
5. Access gateway at `http://localhost:8080`
6. Test routing to backend services

### Basic Configuration Example

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=2
        - id: payment-gateway
          uri: lb://payment-gateway
          predicates:
            - Path=/api/payments/**
          filters:
            - StripPrefix=2

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Examples

### Basic Request Routing

```bash
# Route to order service
curl -X GET http://localhost:8080/api/orders/12345 \
  -H "Authorization: Bearer <jwt-token>"

# Route to product service
curl -X GET http://localhost:8080/api/products/search?query=laptop \
  -H "Authorization: Bearer <jwt-token>"

# Route to payment gateway
curl -X POST http://localhost:8080/api/payments/process \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 99.99, "currency": "USD"}'
```

### Gateway Management

```bash
# Check gateway health
curl -X GET http://localhost:8080/actuator/health

# View configured routes
curl -X GET http://localhost:8080/actuator/gateway/routes

# Check gateway metrics
curl -X GET http://localhost:8080/actuator/metrics
```

### Custom Route Configuration

```java
// Example: Programmatic route configuration
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("analytics-service", r -> r
                .path("/api/analytics/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .circuitBreaker(config -> config
                        .setName("analytics-circuit-breaker")
                        .setFallbackUri("forward:/fallback/analytics")))
                .uri("lb://analytics-service"))
            .route("vendor-service", r -> r
                .path("/api/vendors/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://vendor-service"))
            .build();
    }
}
```

### Security Filter Implementation

```java
// Example: JWT authentication filter
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for health checks
        if (request.getPath().value().startsWith("/actuator")) {
            return chain.filter(exchange);
        }
        
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange);
        }
        
        String token = authHeader.substring(7);
        return validateToken(token)
            .flatMap(userContext -> {
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(request.mutate()
                        .header("X-User-Id", userContext.getUserId())
                        .header("X-User-Role", userContext.getRole())
                        .build())
                    .build();
                return chain.filter(modifiedExchange);
            })
            .onErrorResume(ex -> handleUnauthorized(exchange));
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority filter
    }
}
```

### Rate Limiting Configuration

```java
// Example: Redis-based rate limiting
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1); // 10 requests per second, burst of 20
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getRequest()
            .getHeaders()
            .getFirst("X-User-Id") != null 
                ? Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                : Mono.just("anonymous");
    }
    
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> exchange.getRequest()
            .getHeaders()
            .getFirst("X-API-Key") != null
                ? Mono.just(exchange.getRequest().getHeaders().getFirst("X-API-Key"))
                : Mono.just("default");
    }
}
```

## Best Practices

### Security
1. **Authentication First**: Validate all requests before routing to backend services
2. **Least Privilege**: Apply minimal required permissions for each route
3. **Security Headers**: Add appropriate security headers to all responses
4. **Input Validation**: Validate and sanitize all input parameters
5. **Rate Limiting**: Implement rate limiting to prevent abuse and DDoS attacks

### Performance
1. **Connection Pooling**: Configure optimal connection pools for backend services
2. **Caching Strategy**: Cache authentication tokens and frequently accessed data
3. **Request Timeout**: Set appropriate timeouts for backend service calls
4. **Load Balancing**: Distribute traffic evenly across service instances
5. **Circuit Breaker**: Implement circuit breakers for resilience

### Routing
1. **Path Design**: Use consistent and intuitive path structures
2. **Version Management**: Support multiple API versions simultaneously
3. **Service Discovery**: Use service discovery for dynamic routing
4. **Route Prioritization**: Configure route precedence appropriately
5. **Fallback Handling**: Provide meaningful fallback responses

### Monitoring
1. **Request Logging**: Log all gateway requests and responses
2. **Performance Metrics**: Track response times and error rates
3. **Health Checks**: Monitor backend service health continuously
4. **Alert Configuration**: Set up alerts for critical gateway issues
5. **Trace Correlation**: Implement distributed tracing across services

### Configuration
1. **External Configuration**: Use external configuration for route management
2. **Environment Separation**: Maintain separate configurations for each environment
3. **Feature Flags**: Use feature flags for gradual rollouts
4. **Hot Reloading**: Support configuration changes without restarts
5. **Validation**: Validate all configuration changes before deployment

### Error Handling
1. **Graceful Degradation**: Provide fallback responses when services are unavailable
2. **Error Standardization**: Use consistent error response formats
3. **Circuit Breaker**: Fail fast when backend services are down
4. **Retry Logic**: Implement intelligent retry mechanisms
5. **Client Communication**: Provide clear error messages to clients

## Route Configuration

### Current Routes

| Path Pattern | Target Service | Port | Strip Prefix | Description |
|-------------|----------------|------|--------------|-------------|
| `/api/orders/**` | order-service | 8080 | 2 | Order management operations |
| `/api/products/**` | product-service | 8080 | 2 | Product catalog and search |
| `/api/payments/**` | payment-gateway | 8080 | 2 | Payment processing |
| `/api/users/**` | user-profile-service | Auto | 2 | User profile management |

### Planned Routes

| Path Pattern | Target Service | Description |
|-------------|----------------|-------------|
| `/api/analytics/**` | analytics-service | Business intelligence and reporting |
| `/api/vendors/**` | vendor-service | Vendor management and onboarding |
| `/api/admin/**` | admin-interfaces | Administrative operations |
| `/api/subscriptions/**` | subscription-service | Subscription management |
| `/api/commissions/**` | commission-service | Commission calculations |

## Monitoring and Observability

### Health Checks
- **Gateway Health**: Overall gateway status and component health
- **Backend Services**: Health status of all routed services
- **Service Discovery**: Eureka connection and service registration status
- **Database Connections**: Connection pool status and performance

### Metrics Collection
- **Request Metrics**: Request count, response times, error rates
- **Route Metrics**: Per-route performance and usage statistics
- **Security Metrics**: Authentication success/failure rates
- **System Metrics**: JVM metrics, memory usage, CPU utilization

### Logging Strategy
- **Access Logs**: Complete request/response logging with correlation IDs
- **Error Logs**: Detailed error information for troubleshooting
- **Security Logs**: Authentication and authorization events
- **Performance Logs**: Slow request identification and optimization

## Deployment Considerations

### Infrastructure Requirements
- **CPU**: 2+ cores recommended for production
- **Memory**: 2GB+ RAM for optimal performance
- **Network**: Low latency connection to backend services
- **Storage**: Minimal storage requirements (logs and temporary data)

### Scaling Strategy
- **Horizontal Scaling**: Multiple gateway instances behind load balancer
- **Session Affinity**: Stateless design for easy scaling
- **Health Checks**: Kubernetes-compatible health endpoints
- **Rolling Updates**: Zero-downtime deployment support

### Security Considerations
- **Network Security**: Deploy in secure network with proper firewall rules
- **Certificate Management**: TLS termination and certificate rotation
- **Secrets Management**: Secure handling of authentication keys and tokens
- **Audit Compliance**: Comprehensive audit trails for compliance requirements