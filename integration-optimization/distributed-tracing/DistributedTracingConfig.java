package com.exalt.integration.tracing;

import brave.Tracing;
import brave.Span;
import brave.Tracer;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.sampler.Sampler;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Week 15: Distributed Tracing Configuration
 * Comprehensive tracing for cross-service communication
 */
@Component
public class DistributedTracingConfig {
    
    /**
     * Configure Zipkin tracing
     */
    @Bean
    public Tracing tracing() {
        // Setup Zipkin reporter
        OkHttpSender sender = OkHttpSender.create("http://zipkin:9411/api/v2/spans");
        AsyncReporter<Span> zipkinReporter = AsyncReporter.builder(sender)
            .closeTimeout(10, TimeUnit.SECONDS)
            .messageTimeout(1, TimeUnit.SECONDS)
            .build();
            
        // Configure service name from environment
        String serviceName = System.getenv("SERVICE_NAME");
        if (serviceName == null) {
            serviceName = "social-commerce-service";
        }
        
        // Setup custom sampler
        Sampler sampler = new AdaptiveSampler();
        
        // Configure tracing
        return Tracing.newBuilder()
            .localServiceName(serviceName)
            .spanReporter(zipkinReporter)
            .propagationFactory(B3Propagation.newFactoryBuilder()
                .injectFormat(B3Propagation.Format.MULTI)
                .build())
            .sampler(sampler)
            .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.create())
                .build())
            .build();
    }
    
    /**
     * Custom adaptive sampler for intelligent trace sampling
     */
    public class AdaptiveSampler extends Sampler {
        private final RateLimiter traceSampler = RateLimiter.create(100.0); // 100 traces/sec max
        
        @Override
        public Boolean isSampled(long traceId) {
            // Always sample errors and slow requests
            if (isErrorTrace(traceId) || isSlowTrace(traceId)) {
                return true;
            }
            
            // Sample important operations
            if (isImportantOperation(traceId)) {
                return true;
            }
            
            // Rate-limited sampling for other requests
            return traceSampler.tryAcquire();
        }
        
        private boolean isErrorTrace(long traceId) {
            // Check if this trace has errors
            return traceContext.hasErrors();
        }
        
        private boolean isSlowTrace(long traceId) {
            // Check if this trace exceeds latency threshold
            return traceContext.getDuration() > 1000; // 1 second
        }
        
        private boolean isImportantOperation(long traceId) {
            // Check if this is a critical business operation
            String operation = traceContext.getOperation();
            return operation != null && (
                operation.contains("payment") ||
                operation.contains("order") ||
                operation.contains("checkout")
            );
        }
    }
    
    /**
     * Trace context propagation interceptor
     */
    @Component
    public class TraceInterceptor implements HandlerInterceptor {
        
        private final Tracer tracer;
        
        public TraceInterceptor(Tracer tracer) {
            this.tracer = tracer;
        }
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // Extract trace context from headers
            TraceContext.Extractor<HttpServletRequest> extractor = 
                tracing.propagation().extractor(HttpServletRequest::getHeader);
            
            TraceContextOrSamplingFlags context = extractor.extract(request);
            
            // Start new span
            Span span = tracer.newTrace(context).start();
            
            // Add standard tags
            span.tag("http.method", request.getMethod());
            span.tag("http.path", request.getRequestURI());
            span.tag("user.id", getUserId(request));
            span.tag("tenant.id", getTenantId(request));
            
            // Add custom business context
            span.tag("operation.type", getOperationType(request));
            span.tag("region", getRegion(request));
            
            return true;
        }
        
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) {
            Span span = tracer.currentSpan();
            if (span != null) {
                span.tag("http.status_code", String.valueOf(response.getStatus()));
                
                if (ex != null) {
                    span.tag("error", ex.getClass().getSimpleName());
                    span.tag("error.message", ex.getMessage());
                }
                
                span.end();
            }
        }
    }
    
    /**
     * Service layer tracing aspects
     */
    @Component
    @Aspect
    public class ServiceTracingAspect {
        
        private final Tracer tracer;
        
        public ServiceTracingAspect(Tracer tracer) {
            this.tracer = tracer;
        }
        
        @Around("@annotation(org.springframework.stereotype.Service)")
        public Object traceServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
            String serviceName = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            Span span = tracer.newTrace().name(serviceName + "." + methodName).start();
            
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                // Add method parameters as tags
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null) {
                        span.tag("arg." + i, args[i].toString());
                    }
                }
                
                // Execute method
                Object result = joinPoint.proceed();
                
                // Add result metadata
                if (result != null) {
                    span.tag("result.type", result.getClass().getSimpleName());
                    if (result instanceof Collection) {
                        span.tag("result.size", String.valueOf(((Collection<?>) result).size()));
                    }
                }
                
                return result;
                
            } catch (Exception e) {
                span.tag("error", e.getClass().getSimpleName());
                span.tag("error.message", e.getMessage());
                throw e;
            } finally {
                span.end();
            }
        }
    }
    
    /**
     * Async operation tracing
     */
    public class AsyncTracing {
        
        private final Tracer tracer;
        
        public AsyncTracing(Tracer tracer) {
            this.tracer = tracer;
        }
        
        public <T> CompletableFuture<T> traceAsync(String operationName, Supplier<CompletableFuture<T>> supplier) {
            // Capture current span
            Span currentSpan = tracer.currentSpan();
            
            // Create child span for async operation
            Span asyncSpan = tracer.newChild(currentSpan.context()).name(operationName).start();
            
            return supplier.get()
                .whenComplete((result, throwable) -> {
                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(asyncSpan)) {
                        if (throwable != null) {
                            asyncSpan.tag("error", throwable.getClass().getSimpleName());
                            asyncSpan.tag("error.message", throwable.getMessage());
                        }
                        
                        if (result != null) {
                            asyncSpan.tag("result.type", result.getClass().getSimpleName());
                        }
                    } finally {
                        asyncSpan.end();
                    }
                });
        }
    }
    
    /**
     * Cross-service tracing for microservices communication
     */
    @Component
    public class MicroserviceTracingFilter {
        
        private final Tracer tracer;
        private final RestTemplate restTemplate;
        
        public MicroserviceTracingFilter(Tracer tracer, RestTemplate restTemplate) {
            this.tracer = tracer;
            this.restTemplate = restTemplate;
            
            // Add tracing interceptor to RestTemplate
            List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
            interceptors.add(new TracingClientHttpRequestInterceptor(tracer));
            restTemplate.setInterceptors(interceptors);
        }
        
        public <T> T callService(String serviceName, String url, Class<T> responseType) {
            Span span = tracer.newTrace().name("call." + serviceName).start();
            
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                span.tag("http.url", url);
                span.tag("service.name", serviceName);
                
                // Make service call
                ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
                
                span.tag("http.status_code", String.valueOf(response.getStatusCodeValue()));
                
                return response.getBody();
                
            } catch (Exception e) {
                span.tag("error", e.getClass().getSimpleName());
                span.tag("error.message", e.getMessage());
                throw e;
            } finally {
                span.end();
            }
        }
    }
    
    /**
     * Business transaction tracing
     */
    @Service
    public class BusinessTransactionTracer {
        
        private final Tracer tracer;
        
        public BusinessTransactionTracer(Tracer tracer) {
            this.tracer = tracer;
        }
        
        @NewSpan("business.order.checkout")
        public OrderResult processCheckout(
            @SpanTag("user.id") String userId,
            @SpanTag("order.id") String orderId,
            @SpanTag("order.total") BigDecimal total) {
            
            Span span = tracer.currentSpan();
            
            // Add business context
            span.tag("business.flow", "checkout");
            span.tag("order.currency", getCurrency(orderId));
            span.tag("user.tier", getUserTier(userId));
            
            try {
                // Step 1: Validate order
                span.tag("step.current", "validation");
                validateOrder(orderId);
                
                // Step 2: Process payment
                span.tag("step.current", "payment");
                PaymentResult payment = processPayment(orderId, total);
                span.tag("payment.method", payment.getMethod());
                span.tag("payment.provider", payment.getProvider());
                
                // Step 3: Update inventory
                span.tag("step.current", "inventory");
                updateInventory(orderId);
                
                // Step 4: Schedule fulfillment
                span.tag("step.current", "fulfillment");
                FulfillmentResult fulfillment = scheduleFulfillment(orderId);
                span.tag("fulfillment.method", fulfillment.getMethod());
                
                // Step 5: Send confirmation
                span.tag("step.current", "notification");
                sendOrderConfirmation(userId, orderId);
                
                span.tag("checkout.status", "success");
                return OrderResult.success(orderId, payment, fulfillment);
                
            } catch (Exception e) {
                span.tag("checkout.status", "failed");
                span.tag("failure.step", span.getTag("step.current"));
                throw e;
            }
        }
    }
    
    /**
     * Trace aggregation and analysis
     */
    @Component
    public class TraceAnalytics {
        
        public TraceAnalytics performanceAnalysis(String serviceName, Duration timeWindow) {
            // Fetch traces for analysis
            List<Trace> traces = zipkinClient.getTraces(serviceName, timeWindow);
            
            // Calculate performance metrics
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setAvgLatency(calculateAverageLatency(traces));
            metrics.setP95Latency(calculatePercentileLatency(traces, 95));
            metrics.setP99Latency(calculatePercentileLatency(traces, 99));
            metrics.setErrorRate(calculateErrorRate(traces));
            metrics.setThroughput(calculateThroughput(traces, timeWindow));
            
            // Identify bottlenecks
            List<Bottleneck> bottlenecks = identifyBottlenecks(traces);
            
            // Generate recommendations
            List<Recommendation> recommendations = generateRecommendations(metrics, bottlenecks);
            
            return new TraceAnalytics(metrics, bottlenecks, recommendations);
        }
        
        private List<Bottleneck> identifyBottlenecks(List<Trace> traces) {
            return traces.stream()
                .flatMap(trace -> trace.getSpans().stream())
                .collect(Collectors.groupingBy(Span::getServiceName))
                .entrySet().stream()
                .map(entry -> {
                    String service = entry.getKey();
                    List<Span> spans = entry.getValue();
                    
                    long avgDuration = spans.stream()
                        .mapToLong(Span::getDuration)
                        .average()
                        .orElse(0);
                    
                    if (avgDuration > 1000000) { // 1 second in microseconds
                        return new Bottleneck(service, avgDuration, spans.size());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }
}

// Supporting classes
class TraceContext {
    private final String traceId;
    private final String spanId;
    private final Map<String, String> baggage;
    private boolean hasErrors;
    private long duration;
    private String operation;
    
    // constructors, getters, setters
}

class PerformanceMetrics {
    private double avgLatency;
    private double p95Latency;
    private double p99Latency;
    private double errorRate;
    private double throughput;
    
    // getters, setters
}

class Bottleneck {
    private String serviceName;
    private long avgDuration;
    private int callCount;
    
    // constructors, getters, setters
}

class Recommendation {
    private String service;
    private String issue;
    private String suggestion;
    private Priority priority;
    
    // constructors, getters, setters
}
