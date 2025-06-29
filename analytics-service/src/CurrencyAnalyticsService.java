package com.exalt.ecosystem.socialcommerce.analytics.currency;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Week 11 Day 4: Currency-Based Analytics Service
 * Provides insights and reporting on currency usage and performance
 */
@Service
public class CurrencyAnalyticsService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get currency usage analytics
     */
    public CurrencyUsageReport getCurrencyUsageReport(LocalDate startDate, LocalDate endDate) {
        CurrencyUsageReport report = new CurrencyUsageReport();
        report.setPeriod(startDate, endDate);
        
        // Get orders by currency
        Map<String, List<Order>> ordersByCurrency = orderRepository
            .findByDateRange(startDate, endDate)
            .stream()
            .collect(Collectors.groupingBy(order -> order.getCurrency().getOrderCurrency()));
        
        Map<String, CurrencyMetrics> currencyMetrics = new HashMap<>();
        
        for (Map.Entry<String, List<Order>> entry : ordersByCurrency.entrySet()) {
            String currency = entry.getKey();
            List<Order> orders = entry.getValue();
            
            CurrencyMetrics metrics = new CurrencyMetrics();
            metrics.setCurrency(currency);
            metrics.setOrderCount(orders.size());
            metrics.setTotalRevenue(calculateTotalRevenue(orders));
            metrics.setAverageOrderValue(calculateAverageOrderValue(orders));
            metrics.setCustomerCount(getUniqueCustomerCount(orders));
            metrics.setConversionRate(calculateCurrencyConversionRate(currency, startDate, endDate));
            
            currencyMetrics.put(currency, metrics);
        }
        
        report.setCurrencyMetrics(currencyMetrics);
        
        // Calculate cross-currency analysis
        report.setTopCurrencyPairs(getTopCurrencyPairs(startDate, endDate));
        report.setCurrencyAdoptionRate(calculateCurrencyAdoptionRate(startDate, endDate));
        report.setExchangeRateImpact(analyzeExchangeRateImpact(startDate, endDate));
        
        return report;
    }
    
    /**
     * Get revenue by currency dashboard
     */
    public CurrencyRevenueDashboard getRevenueDashboard(LocalDate startDate, LocalDate endDate) {
        CurrencyRevenueDashboard dashboard = new CurrencyRevenueDashboard();
        
        // Revenue trend by currency
        Map<LocalDate, Map<String, BigDecimal>> revenueByDate = orderRepository
            .getRevenueByDateAndCurrency(startDate, endDate);
        
        dashboard.setRevenueTimeSeries(revenueByDate);
        
        // Revenue distribution pie chart
        Map<String, BigDecimal> revenueDistribution = calculateRevenueDistribution(startDate, endDate);
        dashboard.setRevenueDistribution(revenueDistribution);
        
        // Currency performance comparison
        List<CurrencyPerformance> performance = compareCurrencyPerformance(startDate, endDate);
        dashboard.setCurrencyPerformance(performance);
        
        // Exchange rate impact on revenue
        BigDecimal exchangeRateImpact = calculateExchangeRateRevenueCurrency(startDate, endDate);
        dashboard.setExchangeRateImpact(exchangeRateImpact);
        
        return dashboard;
    }
    
    /**
     * Get currency forecasting report
     */
    public CurrencyForecastReport generateForecast(String currency, int daysAhead) {
        CurrencyForecastReport forecast = new CurrencyForecastReport();
        forecast.setCurrency(currency);
        forecast.setForecastDays(daysAhead);
        
        // Historical data for ML model
        List<CurrencyDataPoint> historicalData = getHistoricalCurrencyData(currency, 90);
        
        // Revenue forecast
        List<RevenueForecast> revenueForecast = forecastRevenue(currency, historicalData, daysAhead);
        forecast.setRevenueForecast(revenueForecast);
        
        // Exchange rate forecast
        List<ExchangeRateForecast> rateForecast = forecastExchangeRates(currency, daysAhead);
        forecast.setRateForecast(rateForecast);
        
        // Currency adoption forecast
        List<AdoptionForecast> adoptionForecast = forecastCurrencyAdoption(currency, daysAhead);
        forecast.setAdoptionForecast(adoptionForecast);
        
        // Generate recommendations
        List<String> recommendations = generateCurrencyRecommendations(forecast);
        forecast.setRecommendations(recommendations);
        
        return forecast;
    }
    
    /**
     * Get currency performance alerts
     */
    public List<CurrencyAlert> getCurrencyAlerts() {
        List<CurrencyAlert> alerts = new ArrayList<>();
        
        // Alert for significant rate changes
        checkExchangeRateAlerts(alerts);
        
        // Alert for unusual transaction patterns
        checkTransactionPatternAlerts(alerts);
        
        // Alert for currency performance issues
        checkPerformanceAlerts(alerts);
        
        return alerts;
    }
    
    private void checkExchangeRateAlerts(List<CurrencyAlert> alerts) {
        Map<String, BigDecimal> currentRates = currencyRateService.getCurrentRates();
        Map<String, BigDecimal> yesterdayRates = currencyRateService.getRatesForDate(LocalDate.now().minusDays(1));
        
        for (Map.Entry<String, BigDecimal> entry : currentRates.entrySet()) {
            String currency = entry.getKey();
            BigDecimal currentRate = entry.getValue();
            BigDecimal yesterdayRate = yesterdayRates.get(currency);
            
            if (yesterdayRate != null) {
                BigDecimal change = currentRate.subtract(yesterdayRate)
                    .divide(yesterdayRate, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                
                if (change.abs().compareTo(new BigDecimal("5")) > 0) { // 5% change threshold
                    CurrencyAlert alert = new CurrencyAlert();
                    alert.setType(AlertType.RATE_CHANGE);
                    alert.setCurrency(currency);
                    alert.setSeverity(calculateSeverity(change));
                    alert.setMessage(String.format("Significant rate change: %s changed by %s%%", 
                        currency, change.toString()));
                    
                    alerts.add(alert);
                }
            }
        }
    }
}
