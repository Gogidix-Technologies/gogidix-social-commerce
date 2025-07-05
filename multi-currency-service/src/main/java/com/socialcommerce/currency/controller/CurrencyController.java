// FILE MOVED TO: com.gogidix.ecosystem.socialcommerce.multicurrency.controller.CurrencyController
// This file has been relocated to follow the new package naming convention
// Please use the new file location for all future development

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Currency Controller for Multi-Currency Service
 * Handles currency conversion and exchange rate management
 */
@RestController
@RequestMapping("/api/v1/currency")
@Tag(name = "Multi-Currency Service", description = "Currency conversion and exchange rate operations")
@Slf4j
public class CurrencyController {

    @Operation(summary = "Health check for Multi-Currency Service")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Multi-Currency Service health check requested");
        return ResponseEntity.ok("Multi-Currency Service is running - " + LocalDateTime.now());
    }

    @Operation(summary = "Convert currency amount")
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertCurrency(@RequestBody Map<String, Object> request) {
        String fromCurrency = request.getOrDefault("from", "USD").toString();
        String toCurrency = request.getOrDefault("to", "EUR").toString();
        BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "100.00").toString());
        
        // Mock exchange rate (in production, would fetch from external API)
        BigDecimal exchangeRate = getExchangeRate(fromCurrency, toCurrency);
        BigDecimal convertedAmount = amount.multiply(exchangeRate);
        
        Map<String, Object> response = Map.of(
            "fromCurrency", fromCurrency,
            "toCurrency", toCurrency,
            "originalAmount", amount,
            "exchangeRate", exchangeRate,
            "convertedAmount", convertedAmount,
            "timestamp", LocalDateTime.now()
        );
        
        log.info("Currency conversion: {} {} = {} {}", amount, fromCurrency, convertedAmount, toCurrency);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current exchange rates")
    @GetMapping("/rates/{baseCurrency}")
    public ResponseEntity<Map<String, Object>> getExchangeRates(@PathVariable String baseCurrency) {
        log.info("Fetching exchange rates for base currency: {}", baseCurrency);
        
        Map<String, Object> rates = Map.of(
            "baseCurrency", baseCurrency,
            "rates", Map.of(
                "USD", new BigDecimal("1.00"),
                "EUR", new BigDecimal("0.85"),
                "GBP", new BigDecimal("0.75"),
                "CAD", new BigDecimal("1.25"),
                "JPY", new BigDecimal("110.00")
            ),
            "lastUpdated", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(rates);
    }

    private BigDecimal getExchangeRate(String from, String to) {
        // Mock exchange rates
        if (from.equals("USD") && to.equals("EUR")) return new BigDecimal("0.85");
        if (from.equals("USD") && to.equals("GBP")) return new BigDecimal("0.75");
        if (from.equals("EUR") && to.equals("USD")) return new BigDecimal("1.18");
        return new BigDecimal("1.00"); // Default 1:1 rate
    }
}
