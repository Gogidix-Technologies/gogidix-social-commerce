package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CurrencyRevenueDashboard {
    private Map<String, BigDecimal> revenuePerCurrency;
    private BigDecimal totalRevenueUSD;
    private Map<String, Double> growthRates;
    private Map<String, Integer> activeUsers;
}
