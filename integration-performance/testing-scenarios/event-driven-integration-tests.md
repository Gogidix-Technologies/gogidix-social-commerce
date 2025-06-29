# Event-Driven Integration Testing Scenarios

## Test Suite Overview
This test suite covers end-to-end scenarios for the event-driven architecture implementation in Week 15.

## Test Environment Setup
```bash
# Start test environment
docker-compose -f docker-compose-test.yml up -d

# Verify all services are running
docker-compose ps

# Run database migrations
./gradlew flywayMigrate -Penvironment=test
```

## Scenario 1: Order Processing Flow
**Objective**: Test complete order processing with event propagation

### 1.1 Happy Path
```gherkin
Given a valid user is logged in
And products are available in inventory
When the user places an order
Then OrderPlacedEvent is published to Kafka
And Payment processing is initiated
When payment is successful
Then PaymentCompletedEvent is published
And Commission calculation is triggered
And CommissionCalculatedEvent is published
And Order status is updated to CONFIRMED
And Inventory is decremented
```

### 1.2 Payment Failure
```gherkin
Given a valid user places an order
When payment processing fails
Then PaymentFailedEvent is published
And OrderStatusUpdatedEvent with PAYMENT_FAILED status is published
And Inventory reservation is released
And Order status is updated to PAYMENT_FAILED
```

### 1.3 Circuit Breaker Activation
```gherkin
Given payment service is experiencing high latency
When payment failure rate exceeds 30%
Then payment-service circuit breaker opens
And PaymentFallbackEvent is triggered
And Alternative payment processing is initiated
```

## Scenario 2: Performance Testing
**Objective**: Validate system performance under load

### 2.1 Load Test Configuration
```yaml
load_test:
  concurrent_users: 100
  duration: 5m
  scenarios:
    - name: product_search
      weight: 40%
      rps: 100
    - name: order_creation
      weight: 30%
      rps: 50
    - name: payment_processing
      weight: 30%
      rps: 30
```

### 2.2 Performance Assertions
```yaml
assertions:
  response_time:
    p95: <500ms
    p99: <1500ms
  error_rate: <1%
  throughput:
    orders_per_minute: >300
    payments_per_minute: >200
  resource_utilization:
    cpu: <70%
    memory: <80%
    database_connections: <80%
```

### 2.3 Kafka Performance
```yaml
kafka_tests:
  producer_throughput:
    min_messages_per_second: 10000
    max_latency_ms: 100
  consumer_lag:
    max_lag: 1000
    catch_up_time: <30s
```

## Scenario 3: Circuit Breaker Testing
**Objective**: Validate resilience patterns

### 3.1 Circuit Breaker States
```gherkin
Given all services are running normally
When payment service experiences 5 consecutive failures
Then circuit breaker transitions to OPEN state
When configured wait duration passes
Then circuit breaker transitions to HALF_OPEN state
When test request succeeds
Then circuit breaker transitions to CLOSED state
```

### 3.2 Bulkhead Pattern
```gherkin
Given bulkhead configured for payment service
When concurrent payment requests exceed limit
Then excess requests are queued
And queue timeout prevents system overload
```

## Scenario 4: Database Performance
**Objective**: Validate database optimization

### 4.1 Query Performance
```sql
-- Test slow query log
SET log_min_duration_statement = 100;

-- Verify index usage
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM products 
WHERE name ILIKE '%electronics%' 
AND status = 'ACTIVE' 
ORDER BY created_at DESC 
LIMIT 20;
```

### 4.2 Connection Pooling
```yaml
hikari_tests:
  pool_size: 20
  max_connections: 200
  connection_timeout: <5s
  leak_detection: enabled
```

## Scenario 5: Cache Testing
**Objective**: Validate caching behavior

### 5.1 Cache Hit Rates
```yaml
redis_tests:
  product_cache:
    hit_rate: >85%
    ttl: 3600s
  user_session_cache:
    hit_rate: >95%
    ttl: 1800s
  currency_rate_cache:
    hit_rate: >99%
    ttl: 86400s
```

### 5.2 Cache Invalidation
```gherkin
Given product data is cached
When product is updated
Then cache is invalidated
And ProductUpdatedEvent propagates
And dependent caches are refreshed
```

## Scenario 6: Event Sourcing Validation
**Objective**: Ensure event consistency

### 6.1 Event Ordering
```gherkin
Given multiple events for same order
When events are processed
Then events maintain correct order
And aggregate state is consistent
```

### 6.2 Replay Capability
```gherkin
Given event store contains historical events
When replay is initiated
Then aggregate state can be reconstructed
And state matches current database state
```

## Scenario 7: Monitoring and Alerting
**Objective**: Validate observability

### 7.1 Metrics Collection
```yaml
prometheus_metrics:
  business_metrics:
    - orders_per_minute
    - revenue_per_hour
    - conversion_rate
  technical_metrics:
    - api_latency_p99
    - error_rate
    - circuit_breaker_state
    - kafka_consumer_lag
```

### 7.2 Alert Triggers
```yaml
alerts:
  - name: HighErrorRate
    condition: error_rate > 5%
    duration: 5m
    severity: critical
  - name: CircuitBreakerOpen
    condition: circuit_breaker_state == OPEN
    duration: 1m
    severity: warning
```

## Test Execution Plan

### Phase 1: Unit Tests
- Event publisher/consumer tests
- Circuit breaker configuration tests
- Cache configuration tests

### Phase 2: Integration Tests
- Service-to-service communication
- Database transaction tests
- Kafka event flow tests

### Phase 3: Performance Tests
- Load testing with Gatling/JMeter
- Stress testing
- Soak testing (24 hours)

### Phase 4: Chaos Engineering
- Network latency injection
- Service failure simulation
- Database connection drops

## Reporting Structure

### Daily Reports
- Performance metrics dashboard
- Error rate trends
- Circuit breaker activation log

### Weekly Reports
- Capacity planning recommendations
- Performance optimization suggestions
- Architecture refinement proposals

## Success Criteria
1. All event flows complete successfully
2. 99.9% uptime during load tests
3. P99 response time < 2 seconds
4. Error rate < 1%
5. Circuit breakers function as designed
6. Cache hit rate > 85%
7. Kafka consumer lag < 1000 messages
8. Database query performance meets targets

## Test Data Management
```yaml
test_data:
  products: 10000
  users: 5000
  orders_per_day: 1000
  data_generation:
    tool: TestContainers
    seed_file: test-data-seed.sql
```

## Cleanup Procedures
```bash
# Clean test environment
docker-compose -f docker-compose-test.yml down -v

# Reset test database
./gradlew flywayClean flywayMigrate -Penvironment=test

# Clear Kafka topics
kafka-console-consumer --bootstrap-server localhost:9092 --from-beginning --topic test-topic --delete
```
