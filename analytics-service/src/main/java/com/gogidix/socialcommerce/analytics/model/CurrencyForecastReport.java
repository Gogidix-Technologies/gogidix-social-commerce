package com.gogidix.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CurrencyForecastReport {
    private Map<String, BigDecimal> projectedRevenue;
    private Map<String, Double> growthPredictions;
    private Map<String, String> recommendations;
}
