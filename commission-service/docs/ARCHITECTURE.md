# Commission Service Architecture

## Overview

The Commission Service implements a sophisticated commission calculation and management system designed to handle complex business rules, multi-tier commission structures, and real-time processing requirements within the social e-commerce ecosystem.

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Commission Service                        │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   API       │ │ Commission  │ │    Rate     │ │ Audit   │ │
│  │ Gateway     │ │ Calculator  │ │  Manager    │ │Service  │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
│         │              │              │              │      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │ Transaction │ │ Reporting   │ │ Validation  │ │ Cache   │ │
│  │ Processor   │ │ Engine      │ │ Service     │ │Manager  │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ PostgreSQL  │      │   Redis     │      │  Message    │
│ Database    │      │   Cache     │      │  Queue      │
└─────────────┘      └─────────────┘      └─────────────┘
```

### Component Architecture

#### 1. API Gateway Layer
```java
@RestController
@RequestMapping("/api/v1")
@Validated
public class CommissionController {
    
    @Autowired
    private CommissionService commissionService;
    
    @PostMapping("/commissions/calculate")
    public ResponseEntity<CommissionResponse> calculateCommission(
        @Valid @RequestBody CommissionRequest request) {
        // Commission calculation endpoint
    }
    
    @GetMapping("/commissions/{id}")
    public ResponseEntity<CommissionDetails> getCommission(@PathVariable Long id) {
        // Get commission details
    }
}
```

#### 2. Commission Calculator Core
```java
@Service
@Transactional
public class CommissionCalculationService {
    
    public CommissionResult calculateCommission(CommissionRequest request) {
        // 1. Validate input parameters
        validateRequest(request);
        
        // 2. Retrieve applicable rates
        CommissionRate rate = rateManager.getApplicableRate(request);
        
        // 3. Apply business rules
        CommissionCalculation calculation = applyBusinessRules(request, rate);
        
        // 4. Process splits and allocations
        List<CommissionAllocation> allocations = processAllocations(calculation);
        
        // 5. Create commission transaction
        CommissionTransaction transaction = createTransaction(calculation, allocations);
        
        // 6. Publish event
        eventPublisher.publishCommissionCalculated(transaction);
        
        return CommissionResult.builder()
            .transactionId(transaction.getId())
            .totalCommission(calculation.getTotalAmount())
            .allocations(allocations)
            .build();
    }
}
```

#### 3. Rate Management Engine
```java
@Service
public class CommissionRateManager {
    
    @Cacheable(value = "commission-rates", key = "#vendorId + '-' + #categoryId")
    public CommissionRate getApplicableRate(Long vendorId, Long categoryId, 
                                          LocalDateTime transactionDate) {
        // Priority-based rate selection:
        // 1. Vendor-specific promotional rates
        // 2. Category-specific promotional rates
        // 3. Vendor-specific standard rates
        // 4. Category-specific standard rates
        // 5. Default platform rates
        
        return rateRepository.findApplicableRate(vendorId, categoryId, transactionDate)
            .orElse(getDefaultRate());
    }
    
    private CommissionRate getDefaultRate() {
        return CommissionRate.builder()
            .rateType(RateType.STANDARD)
            .rateValue(BigDecimal.valueOf(0.05)) // 5% default
            .build();
    }
}
```

## Data Architecture

### Database Schema Design

#### Commission Rates Table
```sql
CREATE TABLE commission_rates (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT,
    category_id BIGINT,
    subcategory_id BIGINT,
    rate_type VARCHAR(50) NOT NULL,
    rate_value DECIMAL(5,4) NOT NULL,
    min_amount DECIMAL(10,2),
    max_amount DECIMAL(10,2),
    volume_tier VARCHAR(20),
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    priority INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT ck_rate_value CHECK (rate_value >= 0 AND rate_value <= 1),
    CONSTRAINT ck_date_range CHECK (effective_to IS NULL OR effective_to > effective_from)
);
```

#### Commission Transactions Table
```sql
CREATE TABLE commission_transactions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    affiliate_id BIGINT,
    transaction_type VARCHAR(50) NOT NULL,
    gross_amount DECIMAL(10,2) NOT NULL,
    net_amount DECIMAL(10,2) NOT NULL,
    commission_rate DECIMAL(5,4) NOT NULL,
    commission_amount DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    calculation_details JSONB,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT ck_amounts CHECK (gross_amount >= 0 AND net_amount >= 0 AND commission_amount >= 0)
);
```

#### Commission Allocations Table
```sql
CREATE TABLE commission_allocations (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES commission_transactions(id),
    recipient_type VARCHAR(50) NOT NULL,
    recipient_id BIGINT NOT NULL,
    allocation_percentage DECIMAL(5,4) NOT NULL,
    allocation_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT ck_allocation_percentage CHECK (allocation_percentage > 0 AND allocation_percentage <= 1)
);
```

### Indexes and Performance
```sql
-- Performance indexes
CREATE INDEX idx_commission_rates_vendor_category ON commission_rates(vendor_id, category_id, effective_from, effective_to);
CREATE INDEX idx_commission_rates_effective_dates ON commission_rates(effective_from, effective_to) WHERE is_active = true;
CREATE INDEX idx_commission_transactions_order ON commission_transactions(order_id);
CREATE INDEX idx_commission_transactions_vendor ON commission_transactions(vendor_id);
CREATE INDEX idx_commission_transactions_status ON commission_transactions(status);
CREATE INDEX idx_commission_transactions_created_at ON commission_transactions(created_at);
```

## Service Architecture Patterns

### 1. Command Query Responsibility Segregation (CQRS)
```java
// Command Side - Commission Calculation
@Component
public class CommissionCommandHandler {
    
    @EventHandler
    public void handle(OrderCompletedEvent event) {
        CommissionCalculationCommand command = CommissionCalculationCommand.builder()
            .orderId(event.getOrderId())
            .vendorId(event.getVendorId())
            .amount(event.getTotalAmount())
            .build();
            
        commissionService.calculateCommission(command);
    }
}

// Query Side - Commission Reporting
@Component
public class CommissionQueryHandler {
    
    @QueryHandler
    public CommissionReport handle(CommissionReportQuery query) {
        return commissionReportRepository.generateReport(query);
    }
}
```

### 2. Event-Driven Architecture
```java
@Component
public class CommissionEventHandler {
    
    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Processing commission for order: {}", event.getOrderId());
        
        // Asynchronous commission calculation
        commissionCalculationService.processCommissionAsync(event);
    }
    
    @EventListener
    public void handleVendorRateChanged(VendorRateChangedEvent event) {
        log.info("Updating commission rates for vendor: {}", event.getVendorId());
        
        // Clear cached rates
        cacheManager.evictCommissionRates(event.getVendorId());
        
        // Recalculate pending transactions
        commissionRecalculationService.recalculatePendingTransactions(event.getVendorId());
    }
}
```

### 3. Circuit Breaker Pattern
```java
@Component
public class ExternalServiceClient {
    
    @CircuitBreaker(name = "order-service", fallbackMethod = "fallbackOrderInfo")
    @Retry(name = "order-service")
    public OrderInfo getOrderInfo(Long orderId) {
        return orderServiceClient.getOrder(orderId);
    }
    
    public OrderInfo fallbackOrderInfo(Long orderId, Exception ex) {
        log.warn("Fallback for order info: {}", orderId, ex);
        return OrderInfo.builder()
            .orderId(orderId)
            .status("UNKNOWN")
            .build();
    }
}
```

## Integration Architecture

### Message Queue Integration
```java
@Component
@Slf4j
public class CommissionMessageProducer {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${commission.events.exchange}")
    private String exchangeName;
    
    public void publishCommissionCalculated(CommissionTransaction transaction) {
        CommissionCalculatedEvent event = CommissionCalculatedEvent.builder()
            .transactionId(transaction.getId())
            .orderId(transaction.getOrderId())
            .vendorId(transaction.getVendorId())
            .commissionAmount(transaction.getCommissionAmount())
            .status(transaction.getStatus())
            .timestamp(Instant.now())
            .build();
            
        rabbitTemplate.convertAndSend(exchangeName, "commission.calculated", event);
        log.info("Published commission calculated event for transaction: {}", transaction.getId());
    }
}
```

### REST API Integration
```java
@FeignClient(name = "vendor-service", url = "${vendor.service.url}")
public interface VendorServiceClient {
    
    @GetMapping("/api/v1/vendors/{vendorId}")
    VendorDetails getVendorDetails(@PathVariable Long vendorId);
    
    @GetMapping("/api/v1/vendors/{vendorId}/commission-rates")
    List<CommissionRate> getVendorCommissionRates(@PathVariable Long vendorId);
}
```

## Caching Strategy

### Multi-Level Caching
```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
            
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

### Cache Strategy Implementation
```java
@Service
public class CommissionCacheService {
    
    @Cacheable(value = "commission-rates", key = "#vendorId + '-' + #categoryId")
    public CommissionRate getCommissionRate(Long vendorId, Long categoryId) {
        return rateRepository.findByVendorIdAndCategoryId(vendorId, categoryId);
    }
    
    @CacheEvict(value = "commission-rates", key = "#vendorId + '-*'")
    public void evictVendorRates(Long vendorId) {
        log.info("Evicted commission rates cache for vendor: {}", vendorId);
    }
    
    @Cacheable(value = "commission-calculations", key = "#orderId")
    public CommissionResult getCommissionCalculation(Long orderId) {
        return commissionRepository.findByOrderId(orderId);
    }
}
```

## Security Architecture

### Authentication & Authorization
```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/commissions/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/rates/**").hasRole("RATE_MANAGER")
                .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "REPORTER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
            
        return http.build();
    }
}
```

### Data Protection
```java
@Component
public class CommissionDataProtectionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    public String encryptSensitiveData(String data) {
        return AESEncryption.encrypt(data, encryptionKey);
    }
    
    public String decryptSensitiveData(String encryptedData) {
        return AESEncryption.decrypt(encryptedData, encryptionKey);
    }
}
```

## Monitoring & Observability

### Metrics Collection
```java
@Component
public class CommissionMetrics {
    
    private final Counter calculationCounter;
    private final Timer calculationTimer;
    private final Gauge pendingTransactions;
    
    public CommissionMetrics(MeterRegistry meterRegistry) {
        this.calculationCounter = Counter.builder("commission.calculations.total")
            .description("Total commission calculations")
            .register(meterRegistry);
            
        this.calculationTimer = Timer.builder("commission.calculation.duration")
            .description("Commission calculation duration")
            .register(meterRegistry);
            
        this.pendingTransactions = Gauge.builder("commission.transactions.pending")
            .description("Pending commission transactions")
            .register(meterRegistry, this, CommissionMetrics::getPendingTransactionCount);
    }
    
    public void recordCalculation() {
        calculationCounter.increment();
    }
    
    public Timer.Sample startCalculationTimer() {
        return Timer.start(calculationTimer);
    }
    
    private double getPendingTransactionCount() {
        return commissionRepository.countByStatus(TransactionStatus.PENDING);
    }
}
```

### Health Checks
```java
@Component
public class CommissionHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Health health() {
        Health.Builder healthBuilder = Health.up();
        
        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                healthBuilder.down().withDetail("database", "Connection invalid");
            }
        } catch (SQLException e) {
            healthBuilder.down().withDetail("database", "Connection failed: " + e.getMessage());
        }
        
        // Check Redis connectivity
        try {
            redisTemplate.opsForValue().get("health-check");
            healthBuilder.withDetail("redis", "Connected");
        } catch (Exception e) {
            healthBuilder.down().withDetail("redis", "Connection failed: " + e.getMessage());
        }
        
        return healthBuilder.build();
    }
}
```

## Deployment Architecture

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: commission-service
  labels:
    app: commission-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: commission-service
  template:
    metadata:
      labels:
        app: commission-service
        version: v1
    spec:
      containers:
      - name: commission-service
        image: commission-service:latest
        ports:
        - containerPort: 8102
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: commission-secrets
              key: database-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8102
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8102
          initialDelaySeconds: 5
          periodSeconds: 5
```

### Service Mesh Integration
```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: commission-service
spec:
  hosts:
  - commission-service
  http:
  - match:
    - uri:
        prefix: /api/v1/commissions
    route:
    - destination:
        host: commission-service
        port:
          number: 8102
    timeout: 30s
    retries:
      attempts: 3
      perTryTimeout: 10s
```

## Performance Optimization

### Database Optimization
- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: Indexed queries and pagination
- **Batch Processing**: Bulk operations for high-volume scenarios
- **Read Replicas**: Separate read/write operations

### Caching Strategy
- **Rate Caching**: Commission rates cached for 30 minutes
- **Calculation Caching**: Recent calculations cached for 5 minutes
- **Vendor Data Caching**: Vendor information cached for 1 hour
- **Cache Warming**: Proactive cache population

### Asynchronous Processing
- **Event-Driven**: Non-blocking commission calculations
- **Queue Processing**: Batch processing for bulk operations
- **Circuit Breakers**: Fault tolerance for external services
- **Retry Mechanisms**: Exponential backoff for failed operations

This architecture provides a robust, scalable, and maintainable commission processing system that can handle complex business requirements while maintaining high performance and reliability.