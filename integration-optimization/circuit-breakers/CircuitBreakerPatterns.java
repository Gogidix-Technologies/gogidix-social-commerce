package com.gogidix.integration.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Week 15: Circuit Breaker Patterns for Resilient Integration
 * Implements fault tolerance patterns for service communication
 */
@Service
public class CircuitBreakerPatterns {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Autowired
    private RetryRegistry retryRegistry;
    
    @Autowired
    private BulkheadRegistry bulkheadRegistry;
    
    @Autowired
    private FallbackService fallbackService;
    
    /**
     * Configure circuit breakers for different services
     */
    public void configureCircuitBreakers() {
        // Payment service circuit breaker
        CircuitBreaker paymentBreaker = circuitBreakerRegistry.circuitBreaker("payment-service",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build()
        );
        
        // Inventory service circuit breaker
        CircuitBreaker inventoryBreaker = circuitBreakerRegistry.circuitBreaker("inventory-service",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(30)
                .minimumNumberOfCalls(15)
                .failureRateThreshold(60.0f)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .build()
        );
        
        // Shipping service circuit breaker
        CircuitBreaker shippingBreaker = circuitBreakerRegistry.circuitBreaker("shipping-service",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(25)
                .minimumNumberOfCalls(12)
                .failureRateThreshold(45.0f)
                .waitDurationInOpenState(Duration.ofSeconds(25))
                .build()
        );
        
        // Social media API circuit breaker
        CircuitBreaker socialBreaker = circuitBreakerRegistry.circuitBreaker("social-media-api",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(50)
                .minimumNumberOfCalls(20)
                .failureRateThreshold(70.0f)
                .waitDurationInOpenState(Duration.ofMinutes(5))
                .build()
        );
        
        // Analytics service circuit breaker
        CircuitBreaker analyticsBreaker = circuitBreakerRegistry.circuitBreaker("analytics-service",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(40)
                .minimumNumberOfCalls(18)
                .failureRateThreshold(65.0f)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .build()
        );
    }
    
    /**
     * Combined resilience patterns for critical operations
     */
    public <T> T executeWithResilience(String serviceName, Supplier<T> operation) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        Retry retry = retryRegistry.retry(serviceName);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(serviceName);
        TimeLimiter timeLimiter = TimeLimiter.of(Duration.ofSeconds(10));
        
        // Chain resilience patterns
        Supplier<T> decoratedSupplier = Decorators.ofSupplier(operation)
            .withCircuitBreaker(circuitBreaker)
            .withRetry(retry)
            .withBulkhead(bulkhead)
            .withTimeLimiter(timeLimiter)
            .withFallback(
                Arrays.asList(
                    Exception.class
                ),
                exception -> getFallback(serviceName, exception)
            )
            .decorate();
        
        // Execute with retry and circuit breaker
        return decoratedSupplier.get();
    }
    
    /**
     * Payment processing with circuit breaker
     */
    public CompletableFuture<PaymentResult> processPaymentWithResilience(PaymentRequest request) {
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("payment-service");
        
        return CompletableFuture.supplyAsync(
            CircuitBreaker.decorateSupplier(breaker, () -> {
                try {
                    return paymentService.processPayment(request);
                } catch (PaymentException e) {
                    // Handle payment-specific exceptions
                    if (e.isRetryable()) {
                        throw new RetryableException(e);
                    }
                    throw e;
                }
            })
        )
        .exceptionally(throwable -> {
            // Fallback to alternative payment method
            log.warn("Payment failed, attempting fallback: {}", throwable.getMessage());
            return fallbackService.processPaymentFallback(request);
        });
    }
    
    /**
     * Inventory checking with bulkhead and circuit breaker
     */
    public InventoryStatus checkInventoryWithResilience(String productId, int quantity) {
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("inventory-check");
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("inventory-service");
        
        Supplier<InventoryStatus> inventoryCheck = () -> {
            return inventoryService.checkAvailability(productId, quantity);
        };
        
        // Apply bulkhead and circuit breaker
        Supplier<InventoryStatus> decoratedCheck = Decorators.ofSupplier(inventoryCheck)
            .withBulkhead(bulkhead)
            .withCircuitBreaker(breaker)
            .withFallback(Arrays.asList(Exception.class), e -> 
                fallbackService.getCachedInventoryStatus(productId))
            .decorate();
        
        return decoratedCheck.get();
    }
    
    /**
     * External API calls with rate limiting
     */
    public <T> T callExternalApiWithResilience(String apiName, Supplier<T> apiCall) {
        RateLimiter rateLimiter = RateLimiterRegistry.ofDefaults()
            .rateLimiter(apiName, RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100)
                .build());
        
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker(apiName);
        
        Supplier<T> decoratedCall = Decorators.ofSupplier(apiCall)
            .withRateLimiter(rateLimiter)
            .withCircuitBreaker(breaker)
            .withRetry(retryRegistry.retry(apiName))
            .decorate();
        
        return decoratedCall.get();
    }
    
    /**
     * Distributed transaction with circuit breakers
     */
    public OrderResult createOrderWithResilience(OrderCreateRequest request) {
        // Create order with multiple service calls
        return TransactionTemplate.execute(transactionId -> {
            try {
                // Step 1: Create order (critical)
                Order order = executeWithResilience("order-service", 
                    () -> orderService.createOrder(request));
                
                // Step 2: Process payment (critical)
                PaymentResult payment = executeWithResilience("payment-service",
                    () -> paymentService.processPayment(order.getPaymentRequest()));
                
                // Step 3: Update inventory (critical)
                InventoryResult inventory = executeWithResilience("inventory-service",
                    () -> inventoryService.updateInventory(order.getItems()));
                
                // Step 4: Schedule shipping (non-critical)
                ShippingResult shipping = executeWithResilience("shipping-service",
                    () -> shippingService.scheduleShipping(order));
                
                // Step 5: Send notifications (non-critical)
                executeWithResilience("notification-service",
                    () -> notificationService.sendOrderConfirmation(order));
                
                return OrderResult.success(order, payment, inventory, shipping);
                
            } catch (Exception e) {
                // Compensating transactions
                compensateOrderCreation(transactionId, e);
                throw new OrderCreationException("Order creation failed", e);
            }
        });
    }
    
    /**
     * Circuit breaker health check and metrics
     */
    @EventListener
    public void handleCircuitBreakerEvent(CircuitBreakerEvent event) {
        switch (event.getEventType()) {
            case STATE_TRANSITION:
                log.info("Circuit breaker '{}' changed state to {}",
                    event.getCircuitBreakerName(),
                    event.getStateTransition().getToState());
                
                // Update metrics
                metricsService.updateCircuitBreakerState(
                    event.getCircuitBreakerName(),
                    event.getStateTransition().getToState()
                );
                
                // Notify if circuit is open
                if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                    alertService.notifyCircuitBreakerOpen(event.getCircuitBreakerName());
                }
                break;
                
            case ERROR:
                log.warn("Circuit breaker '{}' error: {}",
                    event.getCircuitBreakerName(),
                    event.getThrowable().getMessage());
                break;
                
            case SUCCESS:
                metricsService.incrementSuccessCount(event.getCircuitBreakerName());
                break;
            
            case FAILURE_RATE_EXCEEDED:
                log.error("Circuit breaker '{}' failure rate exceeded threshold",
                    event.getCircuitBreakerName());
                alertService.notifyFailureRateExceeded(event.getCircuitBreakerName());
                break;
        }
    }
    
    /**
     * Adaptive circuit breaker configuration
     */
    public class AdaptiveCircuitBreaker {
        
        public void adjustCircuitBreakerConfig(String serviceName, ServiceHealth health) {
            CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            
            // Adjust based on service health metrics
            if (health.getAverageResponseTime() > 5000) {
                // Increase sensitivity for slow services
                breaker.transitionToForcedOpenState();
            } else if (health.getErrorRate() < 5.0) {
                // Decrease sensitivity for stable services
                breaker.transitionToClosedState();
            }
            
            // Dynamic threshold adjustment
            CircuitBreakerConfig newConfig = CircuitBreakerConfig.from(breaker.getCircuitBreakerConfig())
                .failureRateThreshold(calculateOptimalThreshold(health))
                .waitDurationInOpenState(calculateOptimalWaitDuration(health))
                .build();
            
            circuitBreakerRegistry.replace(serviceName, newConfig);
        }
        
        private float calculateOptimalThreshold(ServiceHealth health) {
            // Dynamic calculation based on historical performance
            if (health.getStandardDeviation() > 1000) {
                return 30.0f; // More sensitive for volatile services
            } else if (health.getAverageErrorRate() < 2.0) {
                return 70.0f; // Less sensitive for stable services
            }
            return 50.0f; // Default
        }
        
        private Duration calculateOptimalWaitDuration(ServiceHealth health) {
            // Adjust wait duration based on recovery patterns
            long avgRecoveryTime = health.getAverageRecoveryTime();
            return Duration.ofSeconds(Math.max(10, avgRecoveryTime / 2));
        }
    }
    
    /**
     * Fallback strategies for different services
     */
    private <T> T getFallback(String serviceName, Exception exception) {
        switch (serviceName) {
            case "payment-service":
                return (T) fallbackService.getPaymentFallback(exception);
            case "inventory-service":
                return (T) fallbackService.getInventoryFallback(exception);
            case "shipping-service":
                return (T) fallbackService.getShippingFallback(exception);
            case "social-media-api":
                return (T) fallbackService.getSocialMediaFallback(exception);
            default:
                throw new FallbackNotAvailableException(serviceName, exception);
        }
    }
}

// Supporting classes
class RetryableException extends RuntimeException {
    public RetryableException(Throwable cause) {
        super(cause);
    }
}

class FallbackNotAvailableException extends RuntimeException {
    public FallbackNotAvailableException(String serviceName, Exception cause) {
        super(String.format("No fallback available for service: %s", serviceName), cause);
    }
}

class CircuitBreakerConfig {
    // Configuration wrapper for cleaner code
    public static CircuitBreakerConfig.Builder custom() {
        return new CircuitBreakerConfig.Builder();
    }
}
