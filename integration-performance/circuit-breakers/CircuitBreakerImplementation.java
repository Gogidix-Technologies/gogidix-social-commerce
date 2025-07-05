package com.gogidix.integration.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Week 15: Circuit Breaker Implementation
 * Resilience patterns for social commerce microservices
 */
@Service
public class CircuitBreakerImplementation {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Autowired
    private HealthCheckService healthCheckService;
    
    /**
     * Create circuit breaker for specific service
     */
    public CircuitBreaker createServiceCircuitBreaker(String serviceName, CircuitBreakerConfig config) {
        return circuitBreakerRegistry.circuitBreaker(serviceName, config);
    }
    
    /**
     * Execute operation with circuit breaker protection
     */
    public <T> T executeWithCircuitBreaker(String serviceName, Supplier<T> operation) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceName);
        
        Supplier<T> decoratedOperation = CircuitBreaker
            .decorateSupplier(circuitBreaker, operation);
        
        return decoratedOperation.get();
    }
    
    /**
     * Async execution with circuit breaker
     */
    public <T> CompletableFuture<T> executeAsync(String serviceName, Supplier<CompletableFuture<T>> operation) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceName);
        
        return circuitBreaker.decorateCompletionStage(() -> operation.get()).get();
    }
    
    /**
     * Create default configuration for social commerce services
     */
    public CircuitBreakerConfig createDefaultConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(100)
            .minimumNumberOfCalls(20)
            .permittedNumberOfCallsInHalfOpenState(10)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .build();
    }
    
    /**
     * Create service-specific configurations
     */
    public Map<String, CircuitBreakerConfig> createServiceConfigs() {
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();
        
        // Payment service - More sensitive to failures
        configs.put("payment-service", CircuitBreakerConfig.custom()
            .failureRateThreshold(30)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowSize(50)
            .minimumNumberOfCalls(10)
            .build());
        
        // Order service - Balanced configuration
        configs.put("order-service", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(100)
            .minimumNumberOfCalls(20)
            .build());
        
        // Social service - Less critical
        configs.put("social-service", CircuitBreakerConfig.custom()
            .failureRateThreshold(70)
            .waitDurationInOpenState(Duration.ofSeconds(15))
            .slidingWindowSize(200)
            .minimumNumberOfCalls(50)
            .build());
        
        // Currency service - High availability required
        configs.put("currency-service", CircuitBreakerConfig.custom()
            .failureRateThreshold(20)
            .waitDurationInOpenState(Duration.ofSeconds(45))
            .slidingWindowSize(200)
            .minimumNumberOfCalls(30)
            .build());
        
        return configs;
    }
    
    private CircuitBreaker getOrCreateCircuitBreaker(String serviceName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find(serviceName).orElse(null);
        
        if (circuitBreaker == null) {
            CircuitBreakerConfig config = getConfigForService(serviceName);
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName, config);
            
            // Add event listeners
            addEventListeners(circuitBreaker);
        }
        
        return circuitBreaker;
    }
    
    private CircuitBreakerConfig getConfigForService(String serviceName) {
        Map<String, CircuitBreakerConfig> configs = createServiceConfigs();
        return configs.getOrDefault(serviceName, createDefaultConfig());
    }
    
    private void addEventListeners(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
            .onError(event -> logCircuitBreakerEvent("ERROR", event))
            .onSuccess(event -> logCircuitBreakerEvent("SUCCESS", event))
            .onCallNotPermitted(event -> logCircuitBreakerEvent("CALL_NOT_PERMITTED", event))
            .onStateTransition(event -> logStateTransition(event));
    }
    
    private void logCircuitBreakerEvent(String type, Object event) {
        log.info("Circuit Breaker Event [{}]: {}", type, event);
        
        // Send metrics
        meterRegistry.counter("circuit.breaker.events", 
            "type", type,
            "circuit", event.getCircuitBreakerName()
        ).increment();
    }
    
    private void logStateTransition(CircuitBreaker.StateTransitionEvent event) {
        log.info("Circuit Breaker State Transition: {} -> {}", 
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState());
        
        // Alert on open state
        if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
            alertService.sendCircuitBreakerAlert(event.getCircuitBreakerName());
        }
    }
}

// Service-specific circuit breakers
@Component
class SocialCommerceCircuitBreakers {
    
    @Autowired
    private CircuitBreakerImplementation circuitBreakerImpl;
    
    // Payment service circuit breaker
    public <T> T executePaymentOperation(Supplier<T> operation) {
        return circuitBreakerImpl.executeWithCircuitBreaker("payment-service", operation);
    }
    
    // Order service circuit breaker
    public <T> T executeOrderOperation(Supplier<T> operation) {
        return circuitBreakerImpl.executeWithCircuitBreaker("order-service", operation);
    }
    
    // Social service circuit breaker
    public <T> T executeSocialOperation(Supplier<T> operation) {
        return circuitBreakerImpl.executeWithCircuitBreaker("social-service", operation);
    }
    
    // Currency service circuit breaker
    public <T> T executeCurrencyOperation(Supplier<T> operation) {
        return circuitBreakerImpl.executeWithCircuitBreaker("currency-service", operation);
    }
}

// Fallback implementations
@Component
class FallbackStrategies {
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * Payment fallback - Queue for later processing
     */
    public PaymentResult handlePaymentFallback(PaymentRequest request) {
        // Queue payment for retry
        paymentQueue.add(request);
        
        // Return pending status
        return PaymentResult.builder()
            .status("PENDING")
            .message("Payment queued for processing")
            .transactionId(UUID.randomUUID().toString())
            .build();
    }
    
    /**
     * Order fallback - Use cached data
     */
    public Order handleOrderFallback(String orderId) {
        // Try cache first
        Order cachedOrder = cacheService.getOrder(orderId);
        if (cachedOrder != null) {
            return cachedOrder;
        }
        
        // Return minimal order data
        return Order.builder()
            .id(orderId)
            .status("UNKNOWN")
            .message("Order service temporarily unavailable")
            .build();
    }
    
    /**
     * Social service fallback - Disable features
     */
    public SocialResponse handleSocialFallback(String operation) {
        return SocialResponse.builder()
            .success(false)
            .message("Social features temporarily unavailable")
            .fallbackMode(true)
            .build();
    }
    
    /**
     * Currency fallback - Use stored rates
     */
    public CurrencyRate handleCurrencyFallback(String fromCurrency, String toCurrency) {
        // Use last known rate from cache
        CurrencyRate cachedRate = cacheService.getCurrencyRate(fromCurrency, toCurrency);
        if (cachedRate != null) {
            cachedRate.setSource("CACHED");
            return cachedRate;
        }
        
        // Return fallback rate if no cache
        return CurrencyRate.builder()
            .fromCurrency(fromCurrency)
            .toCurrency(toCurrency)
            .rate(1.0) // Default to 1:1
            .source("FALLBACK")
            .timestamp(LocalDateTime.now())
            .build();
    }
}

// Circuit breaker health checks
@Component
class CircuitBreakerHealthCheck {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    /**
     * Check health of all circuit breakers
     */
    public Map<String, HealthStatus> checkCircuitBreakerHealth() {
        Map<String, HealthStatus> healthMap = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            HealthStatus status = new HealthStatus();
            status.setName(circuitBreaker.getName());
            status.setState(circuitBreaker.getState().toString());
            status.setMetrics(getCircuitBreakerMetrics(circuitBreaker));
            status.setHealthy(circuitBreaker.getState() != CircuitBreaker.State.OPEN);
            
            healthMap.put(circuitBreaker.getName(), status);
        });
        
        return healthMap;
    }
    
    private Map<String, Object> getCircuitBreakerMetrics(CircuitBreaker circuitBreaker) {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        
        Map<String, Object> metricsMap = new HashMap<>();
        metricsMap.put("failureRate", metrics.getFailureRate());
        metricsMap.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        metricsMap.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        metricsMap.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        
        return metricsMap;
    }
}

// Integration with monitoring
@Component
class CircuitBreakerMonitoring {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @EventListener
    public void onCircuitBreakerEvent(CircuitBreaker.CircuitBreakerEvent event) {
        // Record metrics
        String circuitName = event.getCircuitBreakerName();
        String eventType = event.getEventType().toString();
        
        meterRegistry.counter("circuit.breaker.events",
            "circuit", circuitName,
            "type", eventType
        ).increment();
        
        // State duration tracking
        if (event instanceof CircuitBreaker.StateTransitionEvent) {
            CircuitBreaker.StateTransitionEvent stateEvent = (CircuitBreaker.StateTransitionEvent) event;
            
            meterRegistry.timer("circuit.breaker.state.duration",
                "circuit", circuitName,
                "from", stateEvent.getStateTransition().getFromState().toString(),
                "to", stateEvent.getStateTransition().getToState().toString()
            ).record(stateEvent.getElapsedDuration());
        }
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void recordCircuitBreakerMetrics() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            String name = circuitBreaker.getName();
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            
            // Record current state
            meterRegistry.gauge("circuit.breaker.failure.rate", Tags.of("circuit", name), 
                metrics.getFailureRate());
            
            meterRegistry.gauge("circuit.breaker.calls.successful", Tags.of("circuit", name), 
                metrics.getNumberOfSuccessfulCalls());
            
            meterRegistry.gauge("circuit.breaker.calls.failed", Tags.of("circuit", name), 
                metrics.getNumberOfFailedCalls());
        });
    }
}
