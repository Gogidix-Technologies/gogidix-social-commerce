package com.gogidix.integration.tests;

import com.socialecommerceecosystem.shared.event.*;
import com.socialecommerceecosystem.shared.config.KafkaConfig;
import com.socialecommerceecosystem.shared.config.KafkaTopicConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {KafkaConfig.class, KafkaTopicConfig.class, EventPublisher.class})
@EmbeddedKafka(
    partitions = 1, 
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"},
    topics = {"order-placed-event", "payment-completed-event", "product-stock-updated-event", "commission-calculated-event"}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventPublishingIntegrationTest {
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    private Consumer<String, Object> testConsumer;
    
    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        testConsumer = consumerFactory.createConsumer();
    }
    
    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
    }
    
    @Test
    @DisplayName("Should publish and consume OrderPlacedEvent")
    void shouldPublishAndConsumeOrderPlacedEvent() throws InterruptedException {
        // Arrange
        testConsumer.subscribe(Collections.singletonList("order-placed-event"));
        
        OrderPlacedEvent event = createTestOrderPlacedEvent();
        
        // Act
        eventPublisher.publish(event);
        
        // Assert
        ConsumerRecords<String, Object> records = testConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());
        
        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("order-placed-event", record.topic());
        assertEquals(event.getOrderId(), record.key());
        
        // Verify the event content
        assertNotNull(record.value());
        assertTrue(record.value() instanceof OrderPlacedEvent);
        
        OrderPlacedEvent receivedEvent = (OrderPlacedEvent) record.value();
        assertEquals(event.getOrderId(), receivedEvent.getOrderId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getTotalAmount(), receivedEvent.getTotalAmount());
    }
    
    @Test
    @DisplayName("Should publish and consume PaymentCompletedEvent")
    void shouldPublishAndConsumePaymentCompletedEvent() throws InterruptedException {
        // Arrange
        testConsumer.subscribe(Collections.singletonList("payment-completed-event"));
        
        PaymentCompletedEvent event = createTestPaymentCompletedEvent();
        
        // Act
        eventPublisher.publish(event);
        
        // Assert
        ConsumerRecords<String, Object> records = testConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());
        
        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("payment-completed-event", record.topic());
        
        PaymentCompletedEvent receivedEvent = (PaymentCompletedEvent) record.value();
        assertEquals(event.getPaymentId(), receivedEvent.getPaymentId());
        assertEquals(event.getStatus(), receivedEvent.getStatus());
    }
    
    @Test
    @DisplayName("Should publish synchronously with timeout")
    void shouldPublishSynchronouslyWithTimeout() {
        // Arrange
        testConsumer.subscribe(Collections.singletonList("product-stock-updated-event"));
        
        ProductStockUpdatedEvent event = createTestProductStockUpdatedEvent();
        
        // Act & Assert
        assertDoesNotThrow(() -> eventPublisher.publishSync(event, 5000));
        
        // Verify it was published
        ConsumerRecords<String, Object> records = testConsumer.poll(Duration.ofSeconds(5));
        assertFalse(records.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle multiple events in sequence")
    void shouldHandleMultipleEventsInSequence() throws InterruptedException {
        // Arrange
        testConsumer.subscribe(Collections.singletonList("commission-calculated-event"));
        
        // Act
        for (int i = 0; i < 5; i++) {
            CommissionCalculatedEvent event = createTestCommissionCalculatedEvent("commission-" + i);
            eventPublisher.publish(event);
        }
        
        // Wait for all events to be published
        Thread.sleep(2000);
        
        // Assert
        ConsumerRecords<String, Object> records = testConsumer.poll(Duration.ofSeconds(10));
        assertEquals(5, records.count());
    }
    
    @Test
    @DisplayName("Should maintain event order for same partition key")
    void shouldMaintainEventOrderForSamePartitionKey() throws InterruptedException {
        // Arrange
        testConsumer.subscribe(Collections.singletonList("order-placed-event"));
        String sameUserId = "user-123";
        
        // Act - publish multiple events with same user ID (partition key)
        for (int i = 0; i < 3; i++) {
            OrderPlacedEvent event = createTestOrderPlacedEvent();
            event.setUserId(sameUserId);
            event.setOrderId("order-" + i);
            eventPublisher.publishSync(event, 1000);
        }
        
        // Assert
        ConsumerRecords<String, Object> records = testConsumer.poll(Duration.ofSeconds(10));
        assertEquals(3, records.count());
        
        // Verify order
        int i = 0;
        for (ConsumerRecord<String, Object> record : records) {
            OrderPlacedEvent event = (OrderPlacedEvent) record.value();
            assertEquals("order-" + i, event.getOrderId());
            i++;
        }
    }
    
    // Helper methods to create test events
    private OrderPlacedEvent createTestOrderPlacedEvent() {
        OrderPlacedEvent.ShippingDetails shipping = new OrderPlacedEvent.ShippingDetails(
            "123 Test St", "Test City", "Test Country", "12345"
        );
        
        OrderPlacedEvent.OrderItem item = new OrderPlacedEvent.OrderItem(
            "prod-123", "Test Product", 2, new BigDecimal("50.00"), new BigDecimal("100.00")
        );
        
        return new OrderPlacedEvent(
            "order-123", "user-456", "vendor-789", 
            new BigDecimal("100.00"), "USD", "pm-321", 
            shipping, Collections.singletonList(item)
        );
    }
    
    private PaymentCompletedEvent createTestPaymentCompletedEvent() {
        return new PaymentCompletedEvent(
            "payment-123", "order-456", "user-789",
            new BigDecimal("100.00"), "USD", 
            PaymentCompletedEvent.PaymentStatus.COMPLETED,
            "CREDIT_CARD", "trans-987"
        );
    }
    
    private ProductStockUpdatedEvent createTestProductStockUpdatedEvent() {
        return new ProductStockUpdatedEvent(
            "prod-123", "vendor-456", 50, 10,
            ProductStockUpdatedEvent.StockStatus.IN_STOCK,
            100, "SALE_COMPLETED"
        );
    }
    
    private CommissionCalculatedEvent createTestCommissionCalculatedEvent(String commissionId) {
        CommissionCalculatedEvent.CommissionRate vendorRate = 
            new CommissionCalculatedEvent.CommissionRate(
                new BigDecimal("85.0"), new BigDecimal("85.00"), 
                CommissionCalculatedEvent.CommissionType.PERCENTAGE
            );
        
        CommissionCalculatedEvent.CommissionRate platformRate = 
            new CommissionCalculatedEvent.CommissionRate(
                new BigDecimal("10.0"), new BigDecimal("10.00"), 
                CommissionCalculatedEvent.CommissionType.PERCENTAGE
            );
        
        CommissionCalculatedEvent.CommissionRate influencerRate = 
            new CommissionCalculatedEvent.CommissionRate(
                new BigDecimal("5.0"), new BigDecimal("5.00"), 
                CommissionCalculatedEvent.CommissionType.PERCENTAGE
            );
        
        return new CommissionCalculatedEvent(
            commissionId, "order-789", "trans-456",
            "vendor-123", "influencer-789", new BigDecimal("100.00"),
            "USD", vendorRate, platformRate, influencerRate,
            new BigDecimal("85.00"), new BigDecimal("10.00"), new BigDecimal("5.00")
        );
    }
}
