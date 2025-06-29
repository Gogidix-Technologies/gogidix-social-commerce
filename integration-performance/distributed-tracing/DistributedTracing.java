package com.exalt.integration.tracing;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Week 15: Distributed Tracing Implementation
 * Comprehensive tracing for social commerce microservices
 */
@Service
public class DistributedTracing {
    
    @Autowired
    private Tracer tracer;
    
    @Autowired
    private TracingConfiguration tracingConfig;
    
    private final Map<String, Span> activeSpans = new ConcurrentHashMap<>();
    
    /**
     * Create a new trace for business operations
     */
    public Span startTrace(String operationName, Map<String, String> attributes) {
        Span span = tracer.createSpan(operationName);
        
        // Add business context
        span.tag("service", "social-commerce");
        span.tag("operation", operationName);
        span.tag("trace.id", generateTraceId());
        
        // Add custom attributes
        if (attributes != null) {
            attributes.forEach(span::tag);
        }
        
        // Store for reference
        activeSpans.put(span.context().traceId(), span);
        
        return span;
    }
    
    /**
     * Create child span for sub-operations
     */
    public Span startChildSpan(String spanName, Span parentSpan) {
        Span childSpan = tracer.createSpan(spanName, parentSpan);
        
        // Inherit parent context
        childSpan.tag("parent.span.id", parentSpan.context().spanId());
        childSpan.tag("operation.type", "child");
        
        return childSpan;
    }
    
    /**
     * Trace cross-service communication
     */
    public TraceContext createCrossServiceContext(String serviceName, String operation) {
        Span span = tracer.createSpan("cross-service-call");
        span.tag("target.service", serviceName);
        span.tag("target.operation", operation);
        span.tag("call.direction", "outbound");
        
        return span.context();
    }
    
    /**
     * End span with success
     */
    public void endSpan(Span span, boolean success) {
        if (span != null) {
            span.tag("status", success ? "SUCCESS" : "ERROR");
            span.end();
            
            // Remove from active spans
            activeSpans.remove(span.context().traceId());
        }
    }
    
    /**
     * End span with error details
     */
    public void endSpanWithError(Span span, Exception error) {
        if (span != null) {
            span.tag("error", "true");
            span.tag("error.class", error.getClass().getName());
            span.tag("error.message", error.getMessage());
            span.error(error);
            span.end();
            
            activeSpans.remove(span.context().traceId());
        }
    }
    
    /**
     * Add business context to active span
     */
    public void addBusinessContext(String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("business." + key, value);
        }
    }
    
    /**
     * Create correlation ID for request tracking
     */
    public String createCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

// HTTP Request Tracing Filter
@Component
class TracingRequestFilter extends OncePerRequestFilter {
    
    @Autowired
    private Tracer tracer;
    
    @Autowired
    private DistributedTracing distributedTracing;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = distributedTracing.createCorrelationId();
        }
        
        // Create span for the request
        Span requestSpan = tracer.createSpan("http-request");
        requestSpan.tag("http.method", request.getMethod());
        requestSpan.tag("http.url", request.getRequestURI());
        requestSpan.tag("http.trace.id", traceId);
        
        // Set correlation ID in response header
        response.setHeader("X-Trace-Id", traceId);
        
        try {
            filterChain.doFilter(request, response);
            requestSpan.tag("http.status", String.valueOf(response.getStatus()));
            distributedTracing.endSpan(requestSpan, response.getStatus() < 400);
        } catch (Exception e) {
            distributedTracing.endSpanWithError(requestSpan, e);
            throw e;
        }
    }
}

// Service-specific tracing aspects
@Aspect
@Component
class ServiceTracingAspect {
    
    @Autowired
    private DistributedTracing distributedTracing;
    
    @Around("execution(* com.socialecommerceecosystem.*.service.*.*(..))")
    public Object traceServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Map<String, String> attributes = new HashMap<>();
        attributes.put("class", className);
        attributes.put("method", methodName);
        
        Span span = distributedTracing.startTrace(className + "." + methodName, attributes);
        
        try {
            Object result = joinPoint.proceed();
            distributedTracing.endSpan(span, true);
            return result;
        } catch (Exception e) {
            distributedTracing.endSpanWithError(span, e);
            throw e;
        }
    }
    
    @Around("execution(* com.socialecommerceecosystem.*.repository.*.*(..))")
    public Object traceDatabaseOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        Span databaseSpan = tracer.createSpan("database-operation");
        databaseSpan.tag("db.operation", methodName);
        databaseSpan.tag("db.system", "postgresql");
        
        try {
            Object result = joinPoint.proceed();
            distributedTracing.endSpan(databaseSpan, true);
            return result;
        } catch (Exception e) {
            distributedTracing.endSpanWithError(databaseSpan, e);
            throw e;
        }
    }
}

// Custom tracing annotations
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Traced {
    String value() default "";
    String description() default "";
}

// Trace processor for business events
@Component
class BusinessEventTracer {
    
    @Autowired
    private DistributedTracing distributedTracing;
    
    @EventListener
    public void traceBusinessEvent(SocialCommerceEvent event) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("event.type", event.getType().name());
        attributes.put("event.id", event.getEventId());
        attributes.put("event.source", event.getSource());
        
        Span eventSpan = distributedTracing.startTrace("business-event", attributes);
        
        try {
            // Process event tracing
            processEventTrace(event, eventSpan);
            distributedTracing.endSpan(eventSpan, true);
        } catch (Exception e) {
            distributedTracing.endSpanWithError(eventSpan, e);
        }
    }
    
    private void processEventTrace(SocialCommerceEvent event, Span span) {
        // Add event-specific tracing
        switch (event.getType()) {
            case ORDER_CREATED:
                traceOrderCreation(event, span);
                break;
            case USER_REGISTERED:
                traceUserRegistration(event, span);
                break;
            case PAYMENT_PROCESSED:
                tracePaymentProcessing(event, span);
                break;
            default:
                traceGenericEvent(event, span);
        }
    }
    
    private void traceOrderCreation(SocialCommerceEvent event, Span span) {
        Map<String, Object> data = event.getData();
        span.tag("order.id", (String) data.get("orderId"));
        span.tag("order.user", (String) data.get("userId"));
        span.tag("order.total", String.valueOf(data.get("total")));
        span.tag("order.currency", (String) data.get("currency"));
    }
    
    private void traceUserRegistration(SocialCommerceEvent event, Span span) {
        Map<String, Object> data = event.getData();
        span.tag("user.id", (String) data.get("userId"));
        span.tag("user.region", (String) data.get("region"));
        span.tag("user.type", (String) data.get("userType"));
    }
    
    private void tracePaymentProcessing(SocialCommerceEvent event, Span span) {
        Map<String, Object> data = event.getData();
        span.tag("payment.id", (String) data.get("paymentId"));
        span.tag("payment.order", (String) data.get("orderId"));
        span.tag("payment.method", (String) data.get("paymentMethod"));
        span.tag("payment.amount", String.valueOf(data.get("amount")));
    }
    
    private void traceGenericEvent(SocialCommerceEvent event, Span span) {
        // Add generic event data as tags
        event.getData().forEach((key, value) -> 
            span.tag("event.data." + key, String.valueOf(value)));
    }
}

// Configuration for tracing
@Configuration
@EnableAutoConfiguration
class TracingConfiguration {
    
    @Bean
    public Sampler alwaysSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
    
    @Bean
    public Tracer tracer() {
        return Tracing.newBuilder()
            .localServiceName("social-commerce")
            .spanReporter(reporter())
            .sampler(sampler())
            .build()
            .tracer();
    }
    
    @Bean
    public Reporter<Span> reporter() {
        return zipkin2.reporter.AsyncReporter.create(
            URLConnectionSender.create("http://zipkin:9411/api/v2/spans")
        );
    }
    
    @Bean
    public Sampler sampler() {
        // Sample 10% of requests in production
        return Sampler.create(0.1f);
    }
    
    @Bean
    public BaggageField correlationIdField() {
        return BaggageField.create("correlation-id");
    }
    
    @Bean
    public BaggageField userIdField() {
        return BaggageField.create("user-id");
    }
}

// Metrics collection for tracing
@Component
class TracingMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter spanCounter;
    private final Timer spanDuration;
    
    public TracingMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.spanCounter = Counter.builder("tracing.spans.total")
            .register(meterRegistry);
        this.spanDuration = Timer.builder("tracing.spans.duration")
            .register(meterRegistry);
    }
    
    @EventListener
    public void onSpanEnded(SpanEndedEvent event) {
        spanCounter.increment();
        spanDuration.record(event.getDuration());
        
        // Add service-specific metrics
        meterRegistry.counter("tracing.spans", 
            "service", event.getSpan().tag("service"),
            "operation", event.getSpan().tag("operation")
        ).increment();
    }
}
