package com.gogidix.ecosystem.socialcommerce.paymentgateway;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.service.*;
import com.gogidix.ecosystem.socialcommerce.paymentgateway.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple Payment Gateway Test
 * 
 * Basic tests to verify payment gateway functionality
 */
class SimplePaymentTest {
    
    private RegionalPaymentRouter router;
    
    @BeforeEach
    void setUp() {
        router = new RegionalPaymentRouter();
    }
    
    @Test
    void testAfricanCountryDetection() {
        assertTrue(router.isAfricanCountry("NG")); // Nigeria
        assertTrue(router.isAfricanCountry("ZA")); // South Africa
        assertTrue(router.isAfricanCountry("KE")); // Kenya
        assertTrue(router.isAfricanCountry("GH")); // Ghana
        
        assertFalse(router.isAfricanCountry("US")); // United States
        assertFalse(router.isAfricanCountry("GB")); // United Kingdom
        assertFalse(router.isAfricanCountry("DE")); // Germany
    }
    
    @Test
    void testPaystackCurrencySupport() {
        assertTrue(router.isPaystackSupportedCurrency("NGN"));
        assertTrue(router.isPaystackSupportedCurrency("GHS"));
        assertTrue(router.isPaystackSupportedCurrency("ZAR"));
        assertTrue(router.isPaystackSupportedCurrency("USD"));
        
        assertFalse(router.isPaystackSupportedCurrency("EUR"));
        assertFalse(router.isPaystackSupportedCurrency("GBP"));
    }
    
    @Test
    void testGatewayTypeEnumeration() {
        assertEquals("Stripe", PaymentGatewayType.STRIPE.getDisplayName());
        assertEquals("Paystack", PaymentGatewayType.PAYSTACK.getDisplayName());
        assertEquals("Europe & Rest of World", PaymentGatewayType.STRIPE.getRegion());
        assertEquals("Africa", PaymentGatewayType.PAYSTACK.getRegion());
    }
    
    @Test
    void testPaymentRequestValidation() {
        PaymentRequest validRequest = PaymentRequest.builder()
            .amount(100.0)
            .currency("USD")
            .orderId("order123")
            .customerId("customer123")
            .customerEmail("test@example.com")
            .build();
        
        assertNotNull(validRequest);
        assertEquals(100.0, validRequest.getAmount());
        assertEquals("USD", validRequest.getCurrency());
        assertEquals("order123", validRequest.getOrderId());
    }
    
    @Test
    void testPaymentResponseBuilder() {
        PaymentResponse response = PaymentResponse.builder()
            .transactionId("tx_123")
            .status("COMPLETED")
            .amount(100.0)
            .currency("USD")
            .message("Payment successful")
            .build();
        
        assertNotNull(response);
        assertEquals("tx_123", response.getTransactionId());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(100.0, response.getAmount());
    }
    
    @Test
    void testAllAfricanCountriesRecognized() {
        String[] majorAfricanCountries = {
            "DZ", "AO", "BJ", "BW", "BF", "BI", "CM", "CV", "CF", "TD",
            "KM", "CG", "CD", "CI", "DJ", "EG", "GQ", "ER", "ET", "GA",
            "GM", "GH", "GN", "GW", "KE", "LS", "LR", "LY", "MG", "MW",
            "ML", "MR", "MU", "MA", "MZ", "NA", "NE", "NG", "RW", "ST",
            "SN", "SC", "SL", "SO", "ZA", "SS", "SD", "SZ", "TZ", "TG",
            "TN", "UG", "ZM", "ZW"
        };
        
        for (String country : majorAfricanCountries) {
            assertTrue(router.isAfricanCountry(country), 
                "Country " + country + " should be recognized as African");
        }
        
        assertEquals(54, majorAfricanCountries.length, "Should have all 54 African countries");
    }
}