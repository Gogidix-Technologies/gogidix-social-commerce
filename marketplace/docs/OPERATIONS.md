# Marketplace Service Operations Guide

## Production Deployment

### Deployment Architecture

#### Container Orchestration (Kubernetes)

```yaml
# marketplace-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: marketplace-service
  namespace: social-commerce
  labels:
    app: marketplace-service
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: marketplace-service
  template:
    metadata:
      labels:
        app: marketplace-service
        version: v1.0.0
    spec:
      containers:
      - name: marketplace-service
        image: marketplace-service:1.0.0
        ports:
        - containerPort: 8106
          name: http
        - containerPort: 8443
          name: https
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: marketplace-secrets
              key: database-url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: marketplace-secrets
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: marketplace-secrets
              key: database-password
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: marketplace-config
              key: redis-host
        - name: REDIS_PORT
          valueFrom:
            configMapKeyRef:
              name: marketplace-config
              key: redis-port
        - name: EUREKA_SERVER_URL
          valueFrom:
            configMapKeyRef:
              name: marketplace-config
              key: eureka-server-url
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8106
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8106
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health/startup
            port: 8106
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 10
        volumeMounts:
        - name: application-config
          mountPath: /config
          readOnly: true
        - name: logs
          mountPath: /logs
      volumes:
      - name: application-config
        configMap:
          name: marketplace-config
      - name: logs
        emptyDir: {}
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
```

#### Service Configuration

```yaml
# marketplace-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: marketplace-service
  namespace: social-commerce
  labels:
    app: marketplace-service
spec:
  type: ClusterIP
  ports:
  - port: 8106
    targetPort: 8106
    protocol: TCP
    name: http
  - port: 8443
    targetPort: 8443
    protocol: TCP
    name: https
  selector:
    app: marketplace-service
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: marketplace-config
  namespace: social-commerce
data:
  redis-host: "redis-cluster.redis.svc.cluster.local"
  redis-port: "6379"
  eureka-server-url: "http://eureka-server.infrastructure.svc.cluster.local:8761/eureka/"
  max-cart-items: "50"
  session-timeout: "3600"
  commission-rate: "0.05"
---
apiVersion: v1
kind: Secret
metadata:
  name: marketplace-secrets
  namespace: social-commerce
type: Opaque
data:
  database-url: <base64-encoded-database-url>
  database-username: <base64-encoded-database-username>
  database-password: <base64-encoded-database-password>
  jwt-secret: <base64-encoded-jwt-secret>
  redis-password: <base64-encoded-redis-password>
```

#### Horizontal Pod Autoscaler

```yaml
# marketplace-hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: marketplace-hpa
  namespace: social-commerce
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: marketplace-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 120
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
      - type: Pods
        value: 2
        periodSeconds: 60
      selectPolicy: Max
```

### Deployment Procedures

#### 1. Blue-Green Deployment

```bash
# Deploy new version (Green)
kubectl apply -f marketplace-deployment-green.yaml

# Wait for green deployment to be ready
kubectl rollout status deployment/marketplace-service-green -n social-commerce

# Run health checks
kubectl exec -n social-commerce deployment/marketplace-service-green -- \
  curl -s http://localhost:8106/actuator/health

# Switch traffic to green (update service selector)
kubectl patch service marketplace-service -n social-commerce \
  -p '{"spec":{"selector":{"version":"v1.1.0"}}}'

# Monitor metrics and logs
kubectl logs -f deployment/marketplace-service-green -n social-commerce

# If successful, remove blue deployment
kubectl delete deployment marketplace-service-blue -n social-commerce
```

#### 2. Rolling Update Deployment

```bash
# Update deployment image
kubectl set image deployment/marketplace-service \
  marketplace-service=marketplace-service:1.1.0 \
  -n social-commerce

# Monitor rollout progress
kubectl rollout status deployment/marketplace-service -n social-commerce

# Check rollout history
kubectl rollout history deployment/marketplace-service -n social-commerce

# Rollback if necessary
kubectl rollout undo deployment/marketplace-service -n social-commerce
```

#### 3. Canary Deployment

```bash
# Deploy canary with 10% traffic
kubectl apply -f marketplace-canary-deployment.yaml

# Monitor canary metrics
kubectl get pods -l version=canary -n social-commerce

# Gradually increase canary traffic
kubectl patch virtualservice marketplace-vs -n social-commerce \
  --type='json' \
  -p='[{"op": "replace", "path": "/spec/http/0/match/0/headers/canary/exact", "value": "20"}]'

# Promote canary to stable if metrics look good
kubectl patch deployment marketplace-service -n social-commerce \
  -p '{"spec":{"template":{"metadata":{"labels":{"version":"v1.1.0"}}}}}'
```

## Monitoring and Alerting

### Application Metrics

#### Business Metrics Dashboard

```yaml
# marketplace-business-metrics.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: marketplace-business-metrics
data:
  dashboard.json: |
    {
      "dashboard": {
        "title": "Marketplace Business Metrics",
        "panels": [
          {
            "title": "Order Conversion Rate",
            "type": "stat",
            "targets": [
              {
                "expr": "rate(marketplace_orders_completed_total[5m]) / rate(marketplace_sessions_total[5m]) * 100",
                "legendFormat": "Conversion Rate %"
              }
            ]
          },
          {
            "title": "Revenue per Hour",
            "type": "graph",
            "targets": [
              {
                "expr": "sum(rate(marketplace_revenue_total[1h])) by (currency)",
                "legendFormat": "{{currency}}"
              }
            ]
          },
          {
            "title": "Active Vendors",
            "type": "stat",
            "targets": [
              {
                "expr": "marketplace_active_vendors_total",
                "legendFormat": "Active Vendors"
              }
            ]
          },
          {
            "title": "Product Views vs Purchases",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(marketplace_product_views_total[5m])",
                "legendFormat": "Product Views"
              },
              {
                "expr": "rate(marketplace_product_purchases_total[5m])",
                "legendFormat": "Purchases"
              }
            ]
          }
        ]
      }
    }
```

#### Technical Metrics Collection

```yaml
# marketplace-technical-metrics.yaml
apiVersion: v1
kind: ServiceMonitor
metadata:
  name: marketplace-service-monitor
  namespace: social-commerce
spec:
  selector:
    matchLabels:
      app: marketplace-service
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

### Alerting Rules

#### Critical Alerts

```yaml
# marketplace-alerts.yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: marketplace-alerts
  namespace: social-commerce
spec:
  groups:
  - name: marketplace.critical
    rules:
    - alert: MarketplaceServiceDown
      expr: up{job="marketplace-service"} == 0
      for: 1m
      labels:
        severity: critical
        service: marketplace
      annotations:
        summary: "Marketplace service is down"
        description: "Marketplace service has been down for more than 1 minute"
        runbook_url: "https://wiki.company.com/runbooks/marketplace-service-down"
    
    - alert: MarketplaceHighErrorRate
      expr: rate(http_requests_total{job="marketplace-service",status=~"5.."}[5m]) > 0.05
      for: 2m
      labels:
        severity: critical
        service: marketplace
      annotations:
        summary: "High error rate in marketplace service"
        description: "Error rate is {{ $value | humanizePercentage }} for more than 2 minutes"
    
    - alert: MarketplaceDatabaseConnectionsHigh
      expr: hikaricp_connections_active{job="marketplace-service"} / hikaricp_connections_max{job="marketplace-service"} > 0.8
      for: 5m
      labels:
        severity: warning
        service: marketplace
      annotations:
        summary: "High database connection usage"
        description: "Database connection pool is {{ $value | humanizePercentage }} full"
    
    - alert: MarketplaceMemoryUsageHigh
      expr: jvm_memory_used_bytes{job="marketplace-service",area="heap"} / jvm_memory_max_bytes{job="marketplace-service",area="heap"} > 0.85
      for: 10m
      labels:
        severity: warning
        service: marketplace
      annotations:
        summary: "High memory usage in marketplace service"
        description: "Memory usage is {{ $value | humanizePercentage }} of available heap"

  - name: marketplace.business
    rules:
    - alert: MarketplaceConversionRateDrop
      expr: rate(marketplace_orders_completed_total[1h]) / rate(marketplace_sessions_total[1h]) < 0.02
      for: 30m
      labels:
        severity: warning
        service: marketplace
        type: business
      annotations:
        summary: "Marketplace conversion rate below threshold"
        description: "Conversion rate has been below 2% for 30 minutes"
    
    - alert: MarketplaceNoOrdersReceived
      expr: increase(marketplace_orders_total[10m]) == 0
      for: 10m
      labels:
        severity: warning
        service: marketplace
        type: business
      annotations:
        summary: "No orders received in marketplace"
        description: "No orders have been received for 10 minutes"
    
    - alert: MarketplaceVendorOffline
      expr: marketplace_offline_vendors_total > 5
      for: 5m
      labels:
        severity: warning
        service: marketplace
        type: business
      annotations:
        summary: "Multiple vendors offline"
        description: "{{ $value }} vendors are currently offline"
```

### Log Management

#### Centralized Logging Configuration

```yaml
# marketplace-logging.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: marketplace-logging-config
  namespace: social-commerce
data:
  logback-spring.xml: |
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
      <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
      
      <!-- Console appender for local development -->
      <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
          <pattern>${CONSOLE_LOG_PATTERN}</pattern>
          <charset>utf8</charset>
        </encoder>
      </appender>
      
      <!-- File appender for production -->
      <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/logs/marketplace-service.log</file>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
          <providers>
            <timestamp/>
            <logLevel/>
            <loggerName/>
            <mdc/>
            <message/>
            <stackTrace/>
          </providers>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
          <fileNamePattern>/logs/marketplace-service-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
          <maxFileSize>100MB</maxFileSize>
          <maxHistory>30</maxHistory>
          <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
      </appender>
      
      <!-- Logstash appender for centralized logging -->
      <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash.logging.svc.cluster.local:5044</destination>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
          <providers>
            <timestamp/>
            <logLevel/>
            <loggerName/>
            <mdc/>
            <arguments/>
            <message/>
            <stackTrace/>
          </providers>
        </encoder>
      </appender>
      
      <!-- Application loggers -->
      <logger name="com.gogidix.socialcommerce.marketplace" level="INFO"/>
      <logger name="org.springframework.security" level="WARN"/>
      <logger name="org.hibernate.SQL" level="WARN"/>
      
      <!-- Root logger -->
      <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="LOGSTASH"/>
      </root>
    </configuration>
```

## Performance Management

### Performance Monitoring

#### JVM Performance Tuning

```bash
# Production JVM options
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -XX:+UseCompressedOops \
  -XX:+UseCompressedClassPointers \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=production"

# Memory analysis options (for troubleshooting)
DEBUG_OPTS="-XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Xloggc:/logs/gc.log"
```

#### Database Performance Optimization

```yaml
# HikariCP Configuration
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      validation-timeout: 5000
      leak-detection-threshold: 60000
      connection-test-query: "SELECT 1"
      auto-commit: true
      
# JPA Performance Configuration
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
        generate_statistics: false
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region.factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

#### Redis Performance Configuration

```yaml
# Redis performance settings
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: -1ms
      cluster:
        refresh:
          adaptive: true
          period: 30s
    timeout: 2000ms
    cluster:
      max-redirects: 3
    
# Cache configuration
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "marketplace:"
```

### Load Testing

#### Performance Test Scenarios

```bash
# Load testing with Apache Bench
# Scenario 1: Product catalog browsing
ab -n 10000 -c 100 -k \
  -H "Accept: application/json" \
  http://marketplace-service:8106/api/v1/marketplace/products

# Scenario 2: Search functionality
ab -n 5000 -c 50 -k \
  -H "Accept: application/json" \
  "http://marketplace-service:8106/api/v1/marketplace/search?q=electronics"

# Scenario 3: Shopping cart operations
ab -n 2000 -c 20 -k \
  -H "Content-Type: application/json" \
  -p cart-payload.json \
  http://marketplace-service:8106/api/v1/marketplace/cart/add

# Scenario 4: Order placement
ab -n 1000 -c 10 -k \
  -H "Content-Type: application/json" \
  -p order-payload.json \
  http://marketplace-service:8106/api/v1/marketplace/orders
```

#### Performance Benchmarks

```yaml
# Expected performance benchmarks
performance_targets:
  response_times:
    product_listing: "< 200ms (95th percentile)"
    product_search: "< 500ms (95th percentile)"
    cart_operations: "< 300ms (95th percentile)"
    order_placement: "< 1000ms (95th percentile)"
    
  throughput:
    product_views: "> 1000 req/sec"
    search_queries: "> 500 req/sec"
    cart_updates: "> 200 req/sec"
    order_creation: "> 50 req/sec"
    
  resource_usage:
    cpu_utilization: "< 70% under normal load"
    memory_usage: "< 80% of allocated heap"
    database_connections: "< 80% of pool size"
    
  availability:
    uptime: "> 99.9%"
    error_rate: "< 0.1%"
```

## Backup and Recovery

### Database Backup Procedures

#### Automated Backup Configuration

```yaml
# marketplace-backup-cronjob.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: marketplace-db-backup
  namespace: social-commerce
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: postgres-backup
            image: postgres:14
            command:
            - /bin/bash
            - -c
            - |
              export PGPASSWORD=$DATABASE_PASSWORD
              pg_dump -h $DATABASE_HOST -U $DATABASE_USERNAME -d marketplace_db \
                --format=custom --compress=9 --verbose \
                --file=/backup/marketplace_db_$(date +%Y%m%d_%H%M%S).dump
              
              # Upload to cloud storage
              aws s3 cp /backup/marketplace_db_$(date +%Y%m%d_%H%M%S).dump \
                s3://marketplace-backups/database/
              
              # Keep only last 30 days of local backups
              find /backup -name "marketplace_db_*.dump" -mtime +30 -delete
            env:
            - name: DATABASE_HOST
              valueFrom:
                secretKeyRef:
                  name: marketplace-secrets
                  key: database-host
            - name: DATABASE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: marketplace-secrets
                  key: database-username
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: marketplace-secrets
                  key: database-password
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: marketplace-backup-pvc
          restartPolicy: OnFailure
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 1
```

#### Manual Backup Procedures

```bash
# Create manual database backup
kubectl exec -n social-commerce deployment/postgres-master -- \
  pg_dump -U marketplace_user marketplace_db \
  --format=custom --compress=9 > marketplace_backup_$(date +%Y%m%d).dump

# Restore from backup
kubectl exec -i -n social-commerce deployment/postgres-master -- \
  pg_restore -U marketplace_user -d marketplace_db --clean --if-exists \
  < marketplace_backup_20240626.dump

# Verify backup integrity
kubectl exec -n social-commerce deployment/postgres-master -- \
  pg_restore --list marketplace_backup_20240626.dump | head -20
```

### Redis Backup Procedures

```bash
# Redis backup (RDB snapshots)
kubectl exec -n social-commerce deployment/redis-master -- \
  redis-cli BGSAVE

# Monitor backup progress
kubectl exec -n social-commerce deployment/redis-master -- \
  redis-cli LASTSAVE

# Copy backup file
kubectl cp social-commerce/redis-master-xxx:/data/dump.rdb \
  ./redis_backup_$(date +%Y%m%d).rdb

# Restore Redis from backup
kubectl cp redis_backup_20240626.rdb \
  social-commerce/redis-master-xxx:/data/dump.rdb

kubectl exec -n social-commerce deployment/redis-master -- \
  redis-cli SHUTDOWN NOSAVE

# Redis will automatically load the dump.rdb on restart
```

### Disaster Recovery Procedures

#### RTO/RPO Targets

```yaml
disaster_recovery_targets:
  recovery_time_objective: "4 hours"
  recovery_point_objective: "1 hour"
  
  tier_1_services:  # Critical business functions
    - marketplace-api
    - payment-processing
    - order-management
    rto: "30 minutes"
    rpo: "15 minutes"
    
  tier_2_services:  # Important but not critical
    - analytics
    - recommendations
    - notifications
    rto: "2 hours"
    rpo: "1 hour"
```

#### Recovery Runbook

```bash
# 1. Assess the situation
kubectl get pods -n social-commerce | grep marketplace
kubectl get events -n social-commerce --sort-by='.lastTimestamp'

# 2. Check data integrity
kubectl exec -n social-commerce deployment/postgres-master -- \
  psql -U marketplace_user -d marketplace_db -c "SELECT COUNT(*) FROM marketplace_orders;"

# 3. Restore from backup if necessary
# - Follow backup restoration procedures above
# - Verify data consistency after restore

# 4. Restart services in correct order
kubectl delete pod -l app=marketplace-service -n social-commerce
kubectl rollout status deployment/marketplace-service -n social-commerce

# 5. Verify service functionality
curl -s http://marketplace-service:8106/actuator/health
curl -s http://marketplace-service:8106/api/v1/marketplace/products?limit=1

# 6. Monitor for issues
kubectl logs -f deployment/marketplace-service -n social-commerce
```

## Security Operations

### Security Monitoring

#### Security Event Detection

```yaml
# marketplace-security-alerts.yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: marketplace-security-alerts
  namespace: social-commerce
spec:
  groups:
  - name: marketplace.security
    rules:
    - alert: MarketplaceUnauthorizedAccess
      expr: rate(http_requests_total{job="marketplace-service",status="401"}[5m]) > 0.1
      for: 2m
      labels:
        severity: warning
        service: marketplace
        type: security
      annotations:
        summary: "High number of unauthorized access attempts"
        description: "{{ $value }} unauthorized requests per second for 2 minutes"
    
    - alert: MarketplaceSQLInjectionAttempt
      expr: increase(marketplace_sql_injection_attempts_total[5m]) > 5
      for: 1m
      labels:
        severity: critical
        service: marketplace
        type: security
      annotations:
        summary: "Potential SQL injection attack detected"
        description: "{{ $value }} SQL injection attempts detected in 5 minutes"
    
    - alert: MarketplaceRateLimitExceeded
      expr: rate(marketplace_rate_limit_exceeded_total[1m]) > 1
      for: 30s
      labels:
        severity: warning
        service: marketplace
        type: security
      annotations:
        summary: "Rate limiting activated frequently"
        description: "Rate limit exceeded {{ $value }} times per second"
```

#### Security Hardening Checklist

```bash
# 1. Review and update dependencies
mvn dependency:tree
mvn org.owasp:dependency-check-maven:check

# 2. Scan for vulnerabilities
docker run --rm -v $(pwd):/workspace \
  registry.aquasec.com/scanner:latest \
  scan --image marketplace-service:latest

# 3. Check security headers
curl -I http://marketplace-service:8106/api/v1/marketplace/health

# Expected headers:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# X-XSS-Protection: 1; mode=block
# Strict-Transport-Security: max-age=31536000

# 4. Verify HTTPS configuration
openssl s_client -connect marketplace-service:8443 -servername marketplace.example.com

# 5. Test authentication and authorization
curl -H "Authorization: Bearer invalid_token" \
  http://marketplace-service:8106/api/v1/marketplace/admin/vendors
# Expected: 401 Unauthorized
```

### Incident Response

#### Security Incident Runbook

```bash
# 1. Immediate Response (First 15 minutes)
# - Assess the scope and impact
kubectl logs -n social-commerce deployment/marketplace-service --since=1h | grep -i "error\|exception\|unauthorized"

# - Check for data breaches
kubectl exec -n social-commerce deployment/postgres-master -- \
  psql -U marketplace_user -d marketplace_db -c \
  "SELECT COUNT(*) FROM marketplace_customers WHERE updated_at > NOW() - INTERVAL '1 hour';"

# - Isolate affected systems if necessary
kubectl scale deployment marketplace-service --replicas=0 -n social-commerce

# 2. Investigation (First hour)
# - Collect logs and evidence
kubectl logs deployment/marketplace-service -n social-commerce --since=24h > incident_logs.txt

# - Analyze access patterns
kubectl exec -n social-commerce deployment/redis-master -- \
  redis-cli KEYS "session:*" | wc -l

# - Check database for suspicious activity
kubectl exec -n social-commerce deployment/postgres-master -- \
  psql -U marketplace_user -d marketplace_db -c \
  "SELECT ip_address, COUNT(*) as attempts FROM marketplace_access_logs 
   WHERE created_at > NOW() - INTERVAL '1 hour' 
   GROUP BY ip_address ORDER BY attempts DESC LIMIT 10;"

# 3. Containment and Recovery
# - Apply security patches
kubectl set image deployment/marketplace-service \
  marketplace-service=marketplace-service:1.0.1-security-patch \
  -n social-commerce

# - Reset compromised credentials
kubectl delete secret marketplace-secrets -n social-commerce
kubectl create secret generic marketplace-secrets \
  --from-literal=jwt-secret="new_jwt_secret" \
  -n social-commerce

# - Restore service with enhanced monitoring
kubectl scale deployment marketplace-service --replicas=3 -n social-commerce
```

## Maintenance Procedures

### Scheduled Maintenance

#### Monthly Maintenance Tasks

```bash
# 1. Update dependencies and security patches
mvn versions:display-dependency-updates
mvn versions:use-latest-releases

# 2. Database maintenance
kubectl exec -n social-commerce deployment/postgres-master -- \
  psql -U marketplace_user -d marketplace_db -c "VACUUM ANALYZE;"

kubectl exec -n social-commerce deployment/postgres-master -- \
  psql -U marketplace_user -d marketplace_db -c "REINDEX DATABASE marketplace_db;"

# 3. Clear old logs and temporary data
kubectl exec -n social-commerce deployment/marketplace-service -- \
  find /logs -name "*.log" -mtime +30 -delete

# 4. Redis maintenance
kubectl exec -n social-commerce deployment/redis-master -- \
  redis-cli BGREWRITEAOF

# 5. Certificate renewal
certbot renew --dry-run

# 6. Security scans
docker run --rm -v $(pwd):/workspace \
  owasp/zap2docker-stable zap-baseline.py \
  -t http://marketplace-service:8106

# 7. Performance benchmarking
ab -n 1000 -c 10 http://marketplace-service:8106/api/v1/marketplace/products
```

#### Weekly Health Checks

```bash
# 1. Service health validation
curl -s http://marketplace-service:8106/actuator/health | jq '.status'

# 2. Database connection pool status
curl -s http://marketplace-service:8106/actuator/metrics/hikaricp.connections.active

# 3. Redis memory usage
kubectl exec -n social-commerce deployment/redis-master -- \
  redis-cli INFO memory | grep used_memory_human

# 4. Check error rates
curl -s http://marketplace-service:8106/actuator/metrics/http.server.requests | \
  jq '.measurements[] | select(.statistic=="COUNT" and .value > 0)'

# 5. Verify backups
aws s3 ls s3://marketplace-backups/database/ | tail -7

# 6. Check resource usage
kubectl top pods -n social-commerce | grep marketplace

# 7. Review recent alerts
curl -s http://alertmanager:9093/api/v1/alerts | \
  jq '.data[] | select(.labels.service=="marketplace")'
```

---

**Document Version**: 1.0  
**Last Updated**: June 26, 2025  
**Next Review**: July 26, 2025  
**Maintainer**: Operations Team