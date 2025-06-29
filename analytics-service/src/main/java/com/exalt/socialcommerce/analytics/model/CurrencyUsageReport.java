package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class CurrencyUsageReport {
    private String reportId;
    private LocalDateTime generatedAt;
    private Map<String, Long> currencyTransactionCounts;
    private Map<String, BigDecimal> currencyTotalAmounts;
    private Map<String, Double> currencyPercentages;
    private String mostUsedCurrency;
    private String leastUsedCurrency;
}
