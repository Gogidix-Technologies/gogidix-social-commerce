package com.exalt.ecosystem.socialcommerce.paymentgateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Regional Payment Router
 * 
 * Routes payment requests to appropriate gateway based on region:
 * - Stripe: Europe, Americas, Asia-Pacific, Middle East (Rest of World)
 * - Paystack: Africa (All 54 African countries)
 * 
 * SECURITY IMPLEMENTATION: Regional payment gateway separation
 */
@Service
public class RegionalPaymentRouter {
    
    private static final Logger logger = Logger.getLogger(RegionalPaymentRouter.class.getName());
    
    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;
    
    // ISO 3166-1 alpha-2 codes for all 54 African countries
    private static final Set<String> AFRICAN_COUNTRY_CODES = Set.of(
        "DZ", // Algeria
        "AO", // Angola
        "BJ", // Benin
        "BW", // Botswana
        "BF", // Burkina Faso
        "BI", // Burundi
        "CM", // Cameroon
        "CV", // Cape Verde
        "CF", // Central African Republic
        "TD", // Chad
        "KM", // Comoros
        "CG", // Congo (Brazzaville)
        "CD", // Congo (Kinshasa/DRC)
        "CI", // Côte d'Ivoire
        "DJ", // Djibouti
        "EG", // Egypt
        "GQ", // Equatorial Guinea
        "ER", // Eritrea
        "ET", // Ethiopia
        "GA", // Gabon
        "GM", // Gambia
        "GH", // Ghana
        "GN", // Guinea
        "GW", // Guinea-Bissau
        "KE", // Kenya
        "LS", // Lesotho
        "LR", // Liberia
        "LY", // Libya
        "MG", // Madagascar
        "MW", // Malawi
        "ML", // Mali
        "MR", // Mauritania
        "MU", // Mauritius
        "MA", // Morocco
        "MZ", // Mozambique
        "NA", // Namibia
        "NE", // Niger
        "NG", // Nigeria
        "RW", // Rwanda
        "ST", // São Tomé and Príncipe
        "SN", // Senegal
        "SC", // Seychelles
        "SL", // Sierra Leone
        "SO", // Somalia
        "ZA", // South Africa
        "SS", // South Sudan
        "SD", // Sudan
        "SZ", // Swaziland (Eswatini)
        "TZ", // Tanzania
        "TG", // Togo
        "TN", // Tunisia
        "UG", // Uganda
        "ZM", // Zambia
        "ZW"  // Zimbabwe
    );
    
    // Supported African currencies by Paystack
    private static final Set<String> PAYSTACK_SUPPORTED_CURRENCIES = Set.of(
        "NGN", // Nigerian Naira
        "GHS", // Ghanaian Cedi
        "ZAR", // South African Rand
        "KES", // Kenyan Shilling
        "UGX", // Ugandan Shilling
        "USD"  // US Dollar (for international transactions)
    );
    
    /**
     * Select payment gateway based on country code
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return Appropriate payment gateway for the region
     */
    public PaymentGateway selectGateway(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            logger.warning("No country code provided, defaulting to Stripe");
            return paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        }
        
        String upperCountryCode = countryCode.toUpperCase().trim();
        
        if (isAfricanCountry(upperCountryCode)) {
            logger.info("Routing payment to Paystack for African country: " + upperCountryCode);
            return paymentGatewayFactory.getGateway(PaymentGatewayType.PAYSTACK);
        } else {
            logger.info("Routing payment to Stripe for country: " + upperCountryCode);
            return paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        }
    }
    
    /**
     * Select payment gateway based on currency
     * 
     * @param currencyCode ISO 4217 currency code
     * @return Appropriate payment gateway for the currency
     */
    public PaymentGateway selectGatewayByCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            logger.warning("No currency code provided, defaulting to Stripe");
            return paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        }
        
        String upperCurrencyCode = currencyCode.toUpperCase().trim();
        
        if (isPaystackSupportedCurrency(upperCurrencyCode)) {
            logger.info("Routing payment to Paystack for currency: " + upperCurrencyCode);
            return paymentGatewayFactory.getGateway(PaymentGatewayType.PAYSTACK);
        } else {
            logger.info("Routing payment to Stripe for currency: " + upperCurrencyCode);
            return paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
        }
    }
    
    /**
     * Check if country is in Africa
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return true if African country
     */
    public boolean isAfricanCountry(String countryCode) {
        return AFRICAN_COUNTRY_CODES.contains(countryCode);
    }
    
    /**
     * Check if currency is supported by Paystack
     * 
     * @param currencyCode ISO 4217 currency code
     * @return true if supported by Paystack
     */
    public boolean isPaystackSupportedCurrency(String currencyCode) {
        return PAYSTACK_SUPPORTED_CURRENCIES.contains(currencyCode);
    }
    
    /**
     * Get supported payment methods for a country
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return List of supported payment methods
     */
    public Set<String> getSupportedPaymentMethods(String countryCode) {
        PaymentGateway gateway = selectGateway(countryCode);
        return gateway.getSupportedPaymentMethods();
    }
    
    /**
     * Get supported currencies for a country
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return List of supported currencies
     */
    public Set<String> getSupportedCurrencies(String countryCode) {
        PaymentGateway gateway = selectGateway(countryCode);
        return gateway.getSupportedCurrencies();
    }
    
    /**
     * Get gateway name for a country
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return Gateway name (STRIPE or PAYSTACK)
     */
    public String getGatewayName(String countryCode) {
        if (isAfricanCountry(countryCode)) {
            return PaymentGatewayType.PAYSTACK.name();
        } else {
            return PaymentGatewayType.STRIPE.name();
        }
    }
    
    /**
     * Route payment to appropriate gateway (alias for selectGateway)
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return Appropriate payment gateway for the region
     */
    public PaymentGateway routePayment(String countryCode) {
        return selectGateway(countryCode);
    }
    
    /**
     * Route payout to appropriate gateway based on vendor location
     * For now, this determines the gateway based on vendor ID prefix or defaults to country-based routing
     * 
     * @param vendorId Vendor identifier
     * @return Appropriate payment gateway for payout
     */
    public PaymentGateway routePayout(String vendorId) {
        // For now, default to Stripe for global payouts
        // In future, this could be enhanced to check vendor profile for location
        logger.info("Routing payout to Stripe for vendor: " + vendorId);
        return paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE);
    }
}