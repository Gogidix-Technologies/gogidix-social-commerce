package com.exalt.socialcommerce.analytics.model;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class CurrencyAlert {
    private String alertId;
    private String currency;
    private String alertType;
    private String message;
    private LocalDateTime timestamp;
    private String severity;
}
