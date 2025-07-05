package com.gogidix.ecosystem.socialcommerce.paymentgateway.service;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.exception.UnsupportedGatewayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Payment Gateway Factory Test
 * 
 * Tests the payment gateway factory functionality
 */
@ExtendWith(MockitoExtension.class)
class PaymentGatewayFactoryTest {
    
    @Mock
    private StripePaymentService stripePaymentService;
    
    @Mock
    private PaystackPaymentService paystackPaymentService;
    
    @InjectMocks
    private PaymentGatewayFactory paymentGatewayFactory;
    
    @BeforeEach
    void setUp() {
        // Initialize factory
        paymentGatewayFactory.initializeGateways();
    }
    
    @Test
    void testGetStripeGateway() {
        when(stripePaymentService.isAvailable()).thenReturn(true);
        
        PaymentGateway gateway = paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        assertNotNull(gateway);
        assertEquals(stripePaymentService, gateway);
    }
    
    @Test
    void testGetPaystackGateway() {
        when(paystackPaymentService.isAvailable()).thenReturn(true);
        
        PaymentGateway gateway = paymentGatewayFactory.getGateway(PaymentGatewayType.PAYSTACK);
        assertNotNull(gateway);
        assertEquals(paystackPaymentService, gateway);
    }
    
    @Test
    void testGetGatewayByName() {
        when(stripePaymentService.isAvailable()).thenReturn(true);
        when(paystackPaymentService.isAvailable()).thenReturn(true);
        
        PaymentGateway stripeGateway = paymentGatewayFactory.getGateway("STRIPE");
        PaymentGateway paystackGateway = paymentGatewayFactory.getGateway("PAYSTACK");
        
        assertNotNull(stripeGateway);
        assertNotNull(paystackGateway);
        assertEquals(stripePaymentService, stripeGateway);
        assertEquals(paystackPaymentService, paystackGateway);
    }
    
    @Test
    void testUnsupportedGateway() {
        assertThrows(UnsupportedGatewayException.class, () -> {
            paymentGatewayFactory.getGateway("INVALID_GATEWAY");
        });
    }
    
    @Test
    void testGatewayUnavailable() {
        when(stripePaymentService.isAvailable()).thenReturn(false);
        
        assertThrows(UnsupportedGatewayException.class, () -> {
            paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        });
    }
    
    @Test
    void testNullGatewayType() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentGatewayFactory.getGateway((PaymentGatewayType) null);
        });
    }
    
    @Test
    void testIsGatewaySupported() {
        assertTrue(paymentGatewayFactory.isGatewaySupported(PaymentGatewayType.STRIPE));
        assertTrue(paymentGatewayFactory.isGatewaySupported(PaymentGatewayType.PAYSTACK));
    }
    
    @Test
    void testGetAvailableGateways() {
        when(stripePaymentService.isAvailable()).thenReturn(true);
        when(paystackPaymentService.isAvailable()).thenReturn(true);
        
        var availability = paymentGatewayFactory.getAvailableGateways();
        
        assertNotNull(availability);
        assertEquals(2, availability.size());
        assertTrue(availability.get(PaymentGatewayType.STRIPE));
        assertTrue(availability.get(PaymentGatewayType.PAYSTACK));
    }
}