package com.exalt.ecosystem.socialcommerce.paymentgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Payment Methods Response DTO
 * 
 * REGIONAL PAYMENT METHOD SUPPORT
 * - Returns available payment methods based on country/region
 * - Includes gateway routing information
 * - Supports all domains with regional optimization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodsResponse {
    
    private String countryCode;
    private String gateway;
    private String region;
    private Set<String> paymentMethods;
    private Set<String> currencies;
    private List<PaymentMethodDetail> methodDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodDetail {
        private String method;
        private String displayName;
        private String description;
        private boolean requiresAuthentication;
        private List<String> supportedCurrencies;
        private Double minAmount;
        private Double maxAmount;
        private String processingTime;
        private List<String> supportedCountries;
    }
}