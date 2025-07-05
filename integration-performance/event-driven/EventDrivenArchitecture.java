package com.gogidix.integration.eventdriven;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Week 15: Event-Driven Architecture Implementation
 * Core event handling for social commerce microservices
 */
@Service
public class EventDrivenArchitecture {
    
    @Autowired
    private KafkaTemplate<String, SocialCommerceEvent> kafkaTemplate;
    
    @Autowired
    private EventStore eventStore;
    
    @Autowired
    private CircuitBreaker circuitBreaker;
    
    /**
     * Social Commerce Event Types
     */
    public enum EventType {
        // Order Events
        ORDER_CREATED,
        ORDER_UPDATED,
        ORDER_CANCELLED,
        ORDER_COMPLETED,
        
        // User Events
        USER_REGISTERED,
        USER_UPDATED,
        USER_SUBSCRIPTION_CHANGED,
        
        // Vendor Events
        VENDOR_APPROVED,
        VENDOR_PRODUCT_ADDED,
        VENDOR_PAYOUT_PROCESSED,
        
        // Social Events
        SOCIAL_POST_CREATED,
        SOCIAL_REVIEW_ADDED,
        SOCIAL_SHARE_PERFORMED,
        
        // Payment Events
        PAYMENT_PROCESSED,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED,
        
        // Currency Events
        CURRENCY_RATE_UPDATED,
        CURRENCY_CONVERSION_COMPLETED,
        
        // System Events
        SYSTEM_ALERT,
        SYSTEM_MAINTENANCE,
        SYSTEM_BACKUP_COMPLETED
    }
    
    /**
     * Publish event to Kafka topic
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CompletableFuture<Void> publishEvent(SocialCommerceEvent event) {
        try {
            // Add event metadata
            event.setEventId(UUID.randomUUID().toString());
            event.setTimestamp(LocalDateTime.now());
            event.setSource(getServiceName());
            
            // Store event for audit
            eventStore.save(event);
            
            // Publish to appropriate topic
            String topic = getTopicForEventType(event.getType());
            
            return kafkaTemplate.send(topic, event.getKey(), event)
                .completable()
                .thenRun(() -> {
                    logEventPublished(event);
                })
                .exceptionally(throwable -> {
                    handlePublishError(event, throwable);
                    return null;
                });
            
        } catch (Exception e) {
            handleEventError(event, e);
            throw e;
        }
    }
    
    /**
     * Order Events Handling
     */
    @KafkaListener(topics = "order-events", groupId = "social-commerce-order-group")
    public void handleOrderEvent(SocialCommerceEvent event) {
        circuitBreaker.executeSupplier(() -> {
            switch (event.getType()) {
                case ORDER_CREATED:
                    handleOrderCreated(event);
                    break;
                case ORDER_UPDATED:
                    handleOrderUpdated(event);
                    break;
                case ORDER_CANCELLED:
                    handleOrderCancelled(event);
                    break;
                case ORDER_COMPLETED:
                    handleOrderCompleted(event);
                    break;
                default:
                    log.warn("Unhandled order event type: {}", event.getType());
            }
            return null;
        });
    }
    
    /**
     * User Events Handling
     */
    @KafkaListener(topics = "user-events", groupId = "social-commerce-user-group")
    public void handleUserEvent(SocialCommerceEvent event) {
        circuitBreaker.executeSupplier(() -> {
            switch (event.getType()) {
                case USER_REGISTERED:
                    handleUserRegistration(event);
                    break;
                case USER_UPDATED:
                    handleUserUpdate(event);
                    break;
                case USER_SUBSCRIPTION_CHANGED:
                    handleSubscriptionChange(event);
                    break;
                default:
                    log.warn("Unhandled user event type: {}", event.getType());
            }
            return null;
        });
    }
    
    /**
     * Social Events Handling
     */
    @KafkaListener(topics = "social-events", groupId = "social-commerce-social-group")
    public void handleSocialEvent(SocialCommerceEvent event) {
        circuitBreaker.executeSupplier(() -> {
            switch (event.getType()) {
                case SOCIAL_POST_CREATED:
                    handleSocialPostCreated(event);
                    break;
                case SOCIAL_REVIEW_ADDED:
                    handleReviewAdded(event);
                    break;
                case SOCIAL_SHARE_PERFORMED:
                    handleSocialShare(event);
                    break;
                default:
                    log.warn("Unhandled social event type: {}", event.getType());
            }
            return null;
        });
    }
    
    /**
     * Payment Events Handling
     */
    @KafkaListener(topics = "payment-events", groupId = "social-commerce-payment-group")
    public void handlePaymentEvent(SocialCommerceEvent event) {
        circuitBreaker.executeSupplier(() -> {
            switch (event.getType()) {
                case PAYMENT_PROCESSED:
                    handlePaymentProcessed(event);
                    break;
                case PAYMENT_FAILED:
                    handlePaymentFailed(event);
                    break;
                case PAYMENT_REFUNDED:
                    handlePaymentRefunded(event);
                    break;
                default:
                    log.warn("Unhandled payment event type: {}", event.getType());
            }
            return null;
        });
    }
    
    /**
     * Currency Events Handling
     */
    @KafkaListener(topics = "currency-events", groupId = "social-commerce-currency-group")
    public void handleCurrencyEvent(SocialCommerceEvent event) {
        circuitBreaker.executeSupplier(() -> {
            switch (event.getType()) {
                case CURRENCY_RATE_UPDATED:
                    handleCurrencyRateUpdate(event);
                    break;
                case CURRENCY_CONVERSION_COMPLETED:
                    handleCurrencyConversion(event);
                    break;
                default:
                    log.warn("Unhandled currency event type: {}", event.getType());
            }
            return null;
        });
    }
    
    // Specific Event Handlers
    private void handleOrderCreated(SocialCommerceEvent event) {
        Map<String, Object> data = event.getData();
        String orderId = (String) data.get("orderId");
        String userId = (String) data.get("userId");
        
        // Trigger dependent processes
        triggerInventoryCheck(orderId);
        triggerPaymentProcessing(orderId);
        triggerVendorNotification(orderId);
        triggerShippingPreparation(orderId);
        
        // Update analytics
        publishEvent(createEvent(EventType.SYSTEM_ALERT, 
            Map.of("type", "order_analytics", "orderId", orderId)));
    }
    
    private void handleUserRegistration(SocialCommerceEvent event) {
        Map<String, Object> data = event.getData();
        String userId = (String) data.get("userId");
        String region = (String) data.get("region");
        
        // Set up user services
        initializeUserSubscription(userId);
        assignUserCurrency(userId, region);
        createSocialProfile(userId);
        
        // Send welcome communications
        publishEvent(createEvent(EventType.USER_UPDATED,
            Map.of("userId", userId, "action", "welcome_sent")));
    }
    
    private void handlePaymentProcessed(SocialCommerceEvent event) {
        Map<String, Object> data = event.getData();
        String orderId = (String) data.get("orderId");
        String paymentId = (String) data.get("paymentId");
        
        // Update order status
        publishEvent(createEvent(EventType.ORDER_UPDATED,
            Map.of("orderId", orderId, "status", "PAID")));
        
        // Process vendor commissions
        procesVendorCommissions(orderId);
        
        // Trigger fulfillment
        publishEvent(createEvent(EventType.ORDER_COMPLETED,
            Map.of("orderId", orderId, "paymentId", paymentId)));
    }
    
    private void handleSocialPostCreated(SocialCommerceEvent event) {
        Map<String, Object> data = event.getData();
        String postId = (String) data.get("postId");
        String userId = (String) data.get("userId");
        
        // Content moderation
        moderateContent(postId);
        
        // Update social metrics
        updateSocialMetrics(userId, "post_created");
        
        // Trigger engagement tracking
        initializeEngagementTracking(postId);
    }
    
    private void handleCurrencyRateUpdate(SocialCommerceEvent event) {
        Map<String, Object> data = event.getData();
        String baseCurrency = (String) data.get("baseCurrency");
        String targetCurrency = (String) data.get("targetCurrency");
        
        // Update all affected services
        updateProductPrices(baseCurrency, targetCurrency);
        updateActiveOrders(baseCurrency, targetCurrency);
        updateAnalytics(baseCurrency, targetCurrency);
        
        // Notify affected users
        notifyRateChange(baseCurrency, targetCurrency);
    }
    
    // Helper methods
    private String getTopicForEventType(EventType type) {
        return switch (type) {
            case ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED, ORDER_COMPLETED -> "order-events";
            case USER_REGISTERED, USER_UPDATED, USER_SUBSCRIPTION_CHANGED -> "user-events";
            case VENDOR_APPROVED, VENDOR_PRODUCT_ADDED, VENDOR_PAYOUT_PROCESSED -> "vendor-events";
            case SOCIAL_POST_CREATED, SOCIAL_REVIEW_ADDED, SOCIAL_SHARE_PERFORMED -> "social-events";
            case PAYMENT_PROCESSED, PAYMENT_FAILED, PAYMENT_REFUNDED -> "payment-events";
            case CURRENCY_RATE_UPDATED, CURRENCY_CONVERSION_COMPLETED -> "currency-events";
            case SYSTEM_ALERT, SYSTEM_MAINTENANCE, SYSTEM_BACKUP_COMPLETED -> "system-events";
            default -> "general-events";
        };
    }
    
    private SocialCommerceEvent createEvent(EventType type, Map<String, Object> data) {
        SocialCommerceEvent event = new SocialCommerceEvent();
        event.setType(type);
        event.setData(data);
        event.setKey(generateEventKey(type, data));
        return event;
    }
    
    private String generateEventKey(EventType type, Map<String, Object> data) {
        // Generate partition key based on event type and data
        return switch (type) {
            case ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED, ORDER_COMPLETED -> 
                "order:" + data.get("orderId");
            case USER_REGISTERED, USER_UPDATED, USER_SUBSCRIPTION_CHANGED -> 
                "user:" + data.get("userId");
            case VENDOR_APPROVED, VENDOR_PRODUCT_ADDED, VENDOR_PAYOUT_PROCESSED -> 
                "vendor:" + data.get("vendorId");
            default -> UUID.randomUUID().toString();
        };
    }
}

// Event classes
class SocialCommerceEvent {
    private String eventId;
    private EventType type;
    private String key;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
    private String source;
    private Map<String, String> metadata;
    
    // getters, setters
}

// Event Store for audit
@Service
class EventStore {
    @Autowired
    private EventRepository eventRepository;
    
    public void save(SocialCommerceEvent event) {
        EventAuditLog log = new EventAuditLog();
        log.setEventId(event.getEventId());
        log.setEventType(event.getType().name());
        log.setEventData(JsonUtils.toJson(event.getData()));
        log.setCreatedAt(event.getTimestamp());
        log.setSource(event.getSource());
        
        eventRepository.save(log);
    }
    
    public List<EventAuditLog> getEventHistory(String key, LocalDateTime from, LocalDateTime to) {
        return eventRepository.findByKeyAndTimestampBetween(key, from, to);
    }
}

// Circuit Breaker for resilience
@Component
class EventCircuitBreaker {
    
    public <T> T executeSupplier(Supplier<T> supplier) {
        return CircuitBreaker.ofDefaults("eventHandler")
            .decorateSupplier(supplier)
            .get();
    }
    
    public void executeRunnable(Runnable runnable) {
        CircuitBreaker.ofDefaults("eventHandler")
            .decorateRunnable(runnable)
            .run();
    }
}
