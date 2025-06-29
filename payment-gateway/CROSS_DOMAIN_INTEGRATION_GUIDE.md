# Cross-Domain Payment Integration Guide

## Overview
The payment gateway now supports unified payment processing across all domains:
- **Social Commerce**: Product purchases, vendor payouts
- **Warehousing**: Billing, shipping, storage, subscriptions  
- **Courier Services**: Driver payouts, commissions, walk-in payments

## Integration Components

### 1. UnifiedPaymentClient.java
**Location**: `src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/client/`
**Purpose**: Single payment client for all domains
**Usage**: Inject this client into any service requiring payment functionality

```java
@Autowired
private UnifiedPaymentClient paymentClient;

// Process payment
PaymentResponse response = paymentClient.processPayment(request);

// Get payment metrics
PaymentMetrics metrics = paymentClient.getPaymentMetrics(entityId, entityType, dateRange);
```

### 2. WarehouseBillingIntegration.java
**Location**: `src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/integration/`
**Purpose**: Warehouse-specific payment operations
**Methods**:
- `processStorageBilling()` - Warehouse storage billing
- `processSubscriptionPayment()` - Warehouse subscriptions
- `processShippingPayment()` - Cross-region shipping
- `processSelfStoragePayment()` - Vendor self-storage
- `getWarehouseBillingMetrics()` - Analytics

### 3. CourierPayoutIntegration.java
**Location**: `src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/integration/`
**Purpose**: Courier-specific payment operations
**Methods**:
- `processDriverPayout()` - Driver earnings
- `processPartnerCommission()` - Partner commissions
- `processWalkInPayment()` - Walk-in location payments
- `processInternationalShipping()` - International shipping
- `processFarePayment()` - Fare calculations
- `getDriverEarningsMetrics()` - Analytics

### 4. PaymentMetricsController.java
**Location**: `src/main/java/com/exalt/ecosystem/socialcommerce/paymentgateway/controller/`
**Purpose**: Cross-domain analytics API
**Endpoints**:
- `GET /api/v1/payments/metrics` - Unified metrics
- `GET /api/v1/payments/metrics/warehouse/{warehouseId}` - Warehouse metrics
- `GET /api/v1/payments/metrics/courier/{entityId}` - Courier metrics
- `GET /api/v1/payments/metrics/dashboard` - Multi-domain dashboard

## Integration for Warehousing Services

### billing-service Integration
```java
@Autowired
private WarehouseBillingIntegration warehouseBilling;

public void processInvoicePayment(String warehouseId, String invoiceId, Double amount) {
    PaymentResponse response = warehouseBilling.processStorageBilling(
        warehouseId, invoiceId, amount, "USD", "customer@example.com"
    );
    // Handle response
}
```

### warehouse-subscription Integration
```java
public void processSubscription(String warehouseId, String plan) {
    PaymentResponse response = warehouseBilling.processSubscriptionPayment(
        warehouseId, subscriptionId, amount, "USD", "customer@example.com", plan
    );
}
```

## Integration for Courier Services

### payout-service Integration
```java
@Autowired
private CourierPayoutIntegration courierPayout;

public void payDriver(String driverId, Double earnings) {
    PayoutResponse response = courierPayout.processDriverPayout(
        driverId, earnings, "USD", "WEEKLY", "driver@example.com"
    );
    // Handle response
}
```

### courier-network-locations Integration
```java
public void processWalkInPayment(String locationId, String customerId, Double amount) {
    PaymentResponse response = courierPayout.processWalkInPayment(
        locationId, customerId, amount, "USD", "cash", "PACKAGE_DROPOFF"
    );
}
```

## Analytics Integration

### Warehouse Analytics
```java
// Get warehouse metrics
PaymentMetrics metrics = warehouseBilling.getWarehouseBillingMetrics(warehouseId, "MONTHLY");

// Get revenue breakdown
PaymentMetrics breakdown = warehouseBilling.getRevenueBreakdown(warehouseId, "MONTHLY");
```

### Courier Analytics
```java
// Driver earnings
PaymentMetrics driverMetrics = courierPayout.getDriverEarningsMetrics(driverId, "MONTHLY");

// Location revenue
PaymentMetrics locationMetrics = courierPayout.getLocationRevenueMetrics(locationId, "MONTHLY");
```

## Regional Payment Support

The system automatically routes payments based on country:
- **Africa**: Paystack (54 countries)
- **Europe & Rest of World**: Stripe

### Supported Payment Methods by Region

#### Africa (Paystack)
- Mobile Money (MTN, Airtel, Vodafone)
- Bank Transfer
- USSD
- Card payments
- QR Code payments

#### Europe & Rest of World (Stripe)
- Credit/Debit Cards
- SEPA Direct Debit
- iDEAL
- Klarna
- Apple Pay / Google Pay

## Configuration

### Environment Variables Required
```bash
# Stripe Configuration
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Paystack Configuration  
PAYSTACK_SECRET_KEY=sk_live_...
PAYSTACK_WEBHOOK_SECRET=...

# Payment Gateway Configuration
PAYMENT_GATEWAY_BASE_URL=https://payments.yourcompany.com
```

### Spring Boot Configuration
```yaml
payment:
  gateway:
    base-url: ${PAYMENT_GATEWAY_BASE_URL:http://localhost:8086}
    timeout: 30000
  stripe:
    secret-key: ${STRIPE_SECRET_KEY:#{null}}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET:#{null}}
  paystack:
    secret-key: ${PAYSTACK_SECRET_KEY:#{null}}
    webhook-secret: ${PAYSTACK_WEBHOOK_SECRET:#{null}}
```

## Benefits of Cross-Domain Integration

### Business Benefits
- **Single Payment Infrastructure**: One system serves all domains
- **Unified Analytics**: Cross-domain financial insights
- **Consistent Security**: Same security standards across all services
- **Reduced Costs**: Shared infrastructure and development

### Technical Benefits
- **Centralized Payment Logic**: No code duplication
- **Regional Optimization**: Automatic gateway routing
- **Unified Error Handling**: Consistent error responses
- **Scalable Architecture**: Easy to add new domains

## Testing

### Unit Tests
```bash
mvn test -Dtest="*Integration*Test"
```

### Integration Tests
```bash
# Test warehouse billing
curl -X POST /api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId":"WAREHOUSE_BILLING_12345","amount":100.00,"currency":"USD"}'

# Test courier payout  
curl -X POST /api/v1/payments/payout \
  -H "Content-Type: application/json" \
  -d '{"vendorId":"DRIVER_456","amount":50.00,"currency":"USD"}'
```

## Deployment

1. **Build with cross-domain support**:
   ```bash
   mvn clean package -DskipTests
   ```

2. **Deploy payment gateway**:
   ```bash
   docker build -t payment-gateway:latest .
   docker run -p 8086:8086 payment-gateway:latest
   ```

3. **Verify integration**:
   ```bash
   curl http://localhost:8086/actuator/health
   ```

## Next Steps

1. **Implement in Warehousing Services**: Add payment client to billing-service
2. **Implement in Courier Services**: Add payment client to payout-service
3. **Configure Analytics**: Set up cross-domain dashboard
4. **Production Deployment**: Configure environment variables and deploy

## Support

For questions or issues with cross-domain payment integration:
- Review the PaymentMetricsController API documentation
- Check the UnifiedPaymentClient method signatures
- Verify regional payment method support for your target countries