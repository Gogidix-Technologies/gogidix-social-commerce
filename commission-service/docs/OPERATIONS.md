# Commission Service - Operations Guide

## Production Deployment

### Infrastructure Requirements

#### Minimum Production Requirements
- **CPU**: 2 vCPUs per instance
- **Memory**: 2GB RAM per instance
- **Storage**: 50GB SSD storage
- **Network**: 1Gbps bandwidth
- **Instances**: Minimum 2 instances for high availability

#### Recommended Production Requirements
- **CPU**: 4 vCPUs per instance
- **Memory**: 4GB RAM per instance
- **Storage**: 100GB SSD storage
- **Database**: Dedicated PostgreSQL cluster
- **Cache**: Dedicated Redis cluster
- **Load Balancer**: Application load balancer with SSL termination

### Kubernetes Deployment

#### Namespace and RBAC
```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: commission-service
  labels:
    name: commission-service

---
# service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: commission-service
  namespace: commission-service

---
# rbac.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: commission-service
  name: commission-service-role
rules:
- apiGroups: [""]
  resources: ["secrets", "configmaps"]
  verbs: ["get", "list"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: commission-service-binding
  namespace: commission-service
subjects:
- kind: ServiceAccount
  name: commission-service
  namespace: commission-service
roleRef:
  kind: Role
  name: commission-service-role
  apiGroup: rbac.authorization.k8s.io
```

#### ConfigMap and Secrets
```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: commission-service-config
  namespace: commission-service
data:
  application.yml: |
    spring:
      profiles:
        active: production
      datasource:
        url: jdbc:postgresql://postgres-service:5432/commission_prod
        hikari:
          maximum-pool-size: 20
          minimum-idle: 5
      redis:
        host: redis-service
        port: 6379
    
    commission:
      business-rules:
        default-rate: 0.05
        max-rate: 0.30
        min-threshold: 10.00
      processing:
        batch-size: 200
        async-enabled: true
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus

---
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: commission-service-secrets
  namespace: commission-service
type: Opaque
data:
  database-username: Y29tbWlzc2lvbl91c2Vy  # commission_user
  database-password: c2VjdXJlX3Bhc3N3b3Jk  # secure_password
  jwt-secret: eW91ci1qd3Qtc2VjcmV0LWtleQ==    # your-jwt-secret-key
  redis-password: cmVkaXNfcGFzc3dvcmQ=        # redis_password
```

#### Deployment Configuration
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: commission-service
  namespace: commission-service
  labels:
    app: commission-service
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: commission-service
  template:
    metadata:
      labels:
        app: commission-service
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8102"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: commission-service
      containers:
      - name: commission-service
        image: commission-service:1.0.0
        ports:
        - containerPort: 8102
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: commission-service-secrets
              key: database-username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: commission-service-secrets
              key: database-password
        - name: SPRING_REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: commission-service-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: commission-service-secrets
              key: jwt-secret
        - name: JAVA_OPTS
          value: "-Xms1g -Xmx2g -XX:+UseG1GC -XX:G1HeapRegionSize=16m"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8102
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8102
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: config
          mountPath: /app/config
          readOnly: true
        - name: logs
          mountPath: /app/logs
      volumes:
      - name: config
        configMap:
          name: commission-service-config
      - name: logs
        emptyDir: {}
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      restartPolicy: Always

---
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: commission-service
  namespace: commission-service
  labels:
    app: commission-service
spec:
  type: ClusterIP
  ports:
  - port: 8102
    targetPort: 8102
    protocol: TCP
    name: http
  selector:
    app: commission-service

---
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: commission-service-hpa
  namespace: commission-service
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: commission-service
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
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 60
```

### Database Operations

#### Production Database Setup
```sql
-- Create production database and user
CREATE DATABASE commission_prod;
CREATE USER commission_prod_user WITH PASSWORD 'secure_production_password';
GRANT ALL PRIVILEGES ON DATABASE commission_prod TO commission_prod_user;

-- Connect to commission_prod database
\c commission_prod

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO commission_prod_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO commission_prod_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO commission_prod_user;

-- Create indexes for performance
CREATE INDEX CONCURRENTLY idx_commission_rates_lookup ON commission_rates(vendor_id, category_id, effective_from, effective_to) WHERE is_active = true;
CREATE INDEX CONCURRENTLY idx_commission_transactions_vendor_status ON commission_transactions(vendor_id, status, created_at);
CREATE INDEX CONCURRENTLY idx_commission_transactions_order_lookup ON commission_transactions(order_id);
CREATE INDEX CONCURRENTLY idx_commission_allocations_transaction ON commission_allocations(transaction_id, status);
```

#### Database Backup Strategy
```bash
#!/bin/bash
# backup-commission-db.sh

DATABASE_NAME="commission_prod"
BACKUP_DIR="/backup/commission"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/commission_backup_${DATE}.sql"

# Create backup directory
mkdir -p ${BACKUP_DIR}

# Perform backup
pg_dump -h ${DB_HOST} -U ${DB_USER} -d ${DATABASE_NAME} \
  --no-password \
  --format=custom \
  --compress=9 \
  --file=${BACKUP_FILE}

# Verify backup
if [ $? -eq 0 ]; then
  echo "Backup completed successfully: ${BACKUP_FILE}"
  
  # Remove backups older than 7 days
  find ${BACKUP_DIR} -name "commission_backup_*.sql" -mtime +7 -delete
  
  # Upload to cloud storage (optional)
  # aws s3 cp ${BACKUP_FILE} s3://your-backup-bucket/commission/
else
  echo "Backup failed!"
  exit 1
fi
```

#### Database Monitoring
```sql
-- Monitor database performance
SELECT 
  schemaname,
  tablename,
  n_tup_ins AS inserts,
  n_tup_upd AS updates,
  n_tup_del AS deletes,
  n_live_tup AS live_rows,
  n_dead_tup AS dead_rows
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY n_live_tup DESC;

-- Monitor slow queries
SELECT 
  query,
  calls,
  total_time,
  mean_time,
  rows
FROM pg_stat_statements
WHERE query LIKE '%commission%'
ORDER BY mean_time DESC
LIMIT 10;

-- Monitor index usage
SELECT 
  schemaname,
  tablename,
  indexname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
```

## Monitoring and Alerting

### Prometheus Configuration
```yaml
# prometheus-config.yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "commission-service-rules.yaml"

scrape_configs:
  - job_name: 'commission-service'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names:
            - commission-service
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Alert Rules
```yaml
# commission-service-rules.yaml
groups:
  - name: commission-service-alerts
    rules:
      - alert: CommissionServiceDown
        expr: up{job="commission-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Commission Service is down"
          description: "Commission Service has been down for more than 1 minute."

      - alert: CommissionServiceHighErrorRate
        expr: |
          (
            rate(http_server_requests_seconds_count{job="commission-service", status=~"5.."}[5m]) /
            rate(http_server_requests_seconds_count{job="commission-service"}[5m])
          ) > 0.05
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate in Commission Service"
          description: "Commission Service error rate is {{ $value | humanizePercentage }}"

      - alert: CommissionServiceHighLatency
        expr: |
          histogram_quantile(0.95, 
            rate(http_server_requests_seconds_bucket{job="commission-service"}[5m])
          ) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High latency in Commission Service"
          description: "95th percentile latency is {{ $value }}s"

      - alert: CommissionServiceDatabaseConnections
        expr: |
          hikaricp_connections_active{job="commission-service"} / 
          hikaricp_connections_max{job="commission-service"} > 0.8
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "Commission Service database connection pool nearly exhausted"
          description: "Database connection pool is {{ $value | humanizePercentage }} full"

      - alert: CommissionServicePendingTransactions
        expr: commission_transactions_pending_total{job="commission-service"} > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High number of pending commission transactions"
          description: "{{ $value }} pending commission transactions"

      - alert: CommissionCalculationFailures
        expr: |
          rate(commission_calculations_failed_total{job="commission-service"}[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High commission calculation failure rate"
          description: "Commission calculation failures: {{ $value }}/second"
```

### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "Commission Service Monitoring",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"commission-service\"}[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job=\"commission-service\"}[5m]))",
            "legendFormat": "95th percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(http_server_requests_seconds_bucket{job=\"commission-service\"}[5m]))",
            "legendFormat": "50th percentile"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"commission-service\", status=~\"5..\"}[5m])",
            "legendFormat": "5xx errors"
          },
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"commission-service\", status=~\"4..\"}[5m])",
            "legendFormat": "4xx errors"
          }
        ]
      },
      {
        "title": "Commission Calculations",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(commission_calculations_total{job=\"commission-service\"}[5m])",
            "legendFormat": "Successful"
          },
          {
            "expr": "rate(commission_calculations_failed_total{job=\"commission-service\"}[5m])",
            "legendFormat": "Failed"
          }
        ]
      },
      {
        "title": "Pending Transactions",
        "type": "singlestat",
        "targets": [
          {
            "expr": "commission_transactions_pending_total{job=\"commission-service\"}",
            "legendFormat": "Pending"
          }
        ]
      },
      {
        "title": "Database Connections",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{job=\"commission-service\"}",
            "legendFormat": "Active"
          },
          {
            "expr": "hikaricp_connections_idle{job=\"commission-service\"}",
            "legendFormat": "Idle"
          },
          {
            "expr": "hikaricp_connections_max{job=\"commission-service\"}",
            "legendFormat": "Max"
          }
        ]
      }
    ]
  }
}
```

## Log Management

### Logging Configuration
```yaml
# logback-spring.xml
<configuration>
  <springProfile name="production">
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
      <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
        <providers>
          <timestamp/>
          <logLevel/>
          <loggerName/>
          <message/>
          <mdc/>
          <stackTrace/>
        </providers>
      </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
      <file>logs/commission-service.log</file>
      <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/commission-service.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
          <maxFileSize>100MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
        <maxHistory>30</maxHistory>
        <totalSizeCap>10GB</totalSizeCap>
      </rollingPolicy>
      <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
        <providers>
          <timestamp/>
          <logLevel/>
          <loggerName/>
          <message/>
          <mdc/>
          <stackTrace/>
        </providers>
      </encoder>
    </appender>
    
    <logger name="com.gogidix.socialcommerce.commission" level="INFO"/>
    <logger name="org.springframework.web" level="WARN"/>
    <logger name="org.hibernate" level="WARN"/>
    
    <root level="INFO">
      <appender-ref ref="STDOUT"/>
      <appender-ref ref="FILE"/>
    </root>
  </springProfile>
</configuration>
```

### Log Aggregation with ELK Stack
```yaml
# filebeat.yml
filebeat.inputs:
- type: container
  paths:
    - '/var/log/containers/*commission-service*.log'
  processors:
  - add_kubernetes_metadata:
      host: ${NODE_NAME}
      matchers:
      - logs_path:
          logs_path: "/var/log/containers/"

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "commission-service-logs-%{+yyyy.MM.dd}"

setup.template.name: "commission-service"
setup.template.pattern: "commission-service-*"
setup.template.settings:
  index.number_of_shards: 1
  index.number_of_replicas: 1
```

## Security Operations

### SSL/TLS Configuration
```yaml
# ssl-config.yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: commission-service
  port: 8443

management:
  server:
    ssl:
      enabled: true
    port: 8444
```

### Network Security
```yaml
# network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: commission-service-network-policy
  namespace: commission-service
spec:
  podSelector:
    matchLabels:
      app: commission-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: api-gateway
    - namespaceSelector:
        matchLabels:
          name: order-service
    ports:
    - protocol: TCP
      port: 8102
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - namespaceSelector:
        matchLabels:
          name: redis
    ports:
    - protocol: TCP
      port: 6379
```

### Secret Management
```bash
#!/bin/bash
# rotate-secrets.sh

# Rotate database password
NEW_DB_PASSWORD=$(openssl rand -base64 32)
kubectl patch secret commission-service-secrets \
  -n commission-service \
  -p='{"data":{"database-password":"'$(echo -n $NEW_DB_PASSWORD | base64)'"}}'

# Rotate JWT secret
NEW_JWT_SECRET=$(openssl rand -base64 64)
kubectl patch secret commission-service-secrets \
  -n commission-service \
  -p='{"data":{"jwt-secret":"'$(echo -n $NEW_JWT_SECRET | base64)'"}}'

# Restart deployment to use new secrets
kubectl rollout restart deployment/commission-service -n commission-service

echo "Secrets rotated successfully"
```

## Maintenance Operations

### Database Maintenance
```sql
-- Vacuum and analyze tables
VACUUM ANALYZE commission_transactions;
VACUUM ANALYZE commission_rates;
VACUUM ANALYZE commission_allocations;

-- Update table statistics
ANALYZE commission_transactions;
ANALYZE commission_rates;
ANALYZE commission_allocations;

-- Check for unused indexes
SELECT 
  schemaname,
  tablename,
  indexname,
  idx_scan,
  idx_tup_read
FROM pg_stat_user_indexes
WHERE idx_scan < 100
ORDER BY idx_scan;

-- Archive old commission data
DELETE FROM commission_transactions 
WHERE created_at < NOW() - INTERVAL '1 year'
  AND status = 'COMPLETED';
```

### Cache Maintenance
```bash
#!/bin/bash
# cache-maintenance.sh

# Clear expired cache entries
redis-cli --scan --pattern "commission:*" | xargs -L 1000 redis-cli DEL

# Check cache hit ratio
redis-cli info stats | grep keyspace_hits
redis-cli info stats | grep keyspace_misses

# Monitor memory usage
redis-cli info memory | grep used_memory_human

# Set cache eviction policy
redis-cli config set maxmemory-policy allkeys-lru
```

### Application Maintenance
```bash
#!/bin/bash
# maintenance-tasks.sh

# Health check before maintenance
curl -f http://commission-service:8102/actuator/health || exit 1

# Graceful shutdown
kubectl scale deployment commission-service --replicas=0 -n commission-service

# Wait for pods to terminate
kubectl wait --for=delete pod -l app=commission-service -n commission-service --timeout=300s

# Perform maintenance tasks
echo "Performing maintenance tasks..."

# Scale back up
kubectl scale deployment commission-service --replicas=3 -n commission-service

# Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app=commission-service -n commission-service --timeout=300s

# Verify service is healthy
curl -f http://commission-service:8102/actuator/health

echo "Maintenance completed successfully"
```

## Disaster Recovery

### Backup Procedures
```bash
#!/bin/bash
# full-backup.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/commission/${BACKUP_DATE}"

mkdir -p ${BACKUP_DIR}

# Database backup
pg_dump -h ${DB_HOST} -U ${DB_USER} -d commission_prod \
  --format=custom --compress=9 \
  --file=${BACKUP_DIR}/database.dump

# Configuration backup
kubectl get configmap commission-service-config -n commission-service -o yaml > ${BACKUP_DIR}/configmap.yaml
kubectl get secret commission-service-secrets -n commission-service -o yaml > ${BACKUP_DIR}/secrets.yaml

# Application state backup (if applicable)
kubectl get deployment commission-service -n commission-service -o yaml > ${BACKUP_DIR}/deployment.yaml

echo "Backup completed: ${BACKUP_DIR}"
```

### Recovery Procedures
```bash
#!/bin/bash
# disaster-recovery.sh

BACKUP_DIR=$1

if [ -z "$BACKUP_DIR" ]; then
  echo "Usage: $0 <backup-directory>"
  exit 1
fi

echo "Starting disaster recovery from: ${BACKUP_DIR}"

# Restore database
pg_restore -h ${DB_HOST} -U ${DB_USER} -d commission_prod \
  --clean --if-exists \
  ${BACKUP_DIR}/database.dump

# Restore configuration
kubectl apply -f ${BACKUP_DIR}/configmap.yaml
kubectl apply -f ${BACKUP_DIR}/secrets.yaml

# Restore application
kubectl apply -f ${BACKUP_DIR}/deployment.yaml

# Wait for service to be ready
kubectl wait --for=condition=available deployment/commission-service -n commission-service --timeout=300s

# Verify service health
curl -f http://commission-service:8102/actuator/health

echo "Disaster recovery completed successfully"
```

## Performance Tuning

### JVM Tuning
```bash
# Production JVM settings
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:G1HeapRegionSize=16m \
  -XX:+UseStringDeduplication \
  -XX:+UseCompressedOops \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseCGroupMemoryLimitForHeap \
  -Djava.security.egd=file:/dev/./urandom"
```

### Database Tuning
```sql
-- PostgreSQL performance tuning
ALTER SYSTEM SET shared_buffers = '512MB';
ALTER SYSTEM SET effective_cache_size = '2GB';
ALTER SYSTEM SET maintenance_work_mem = '128MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '32MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Reload configuration
SELECT pg_reload_conf();
```

This comprehensive operations guide provides all the necessary information for running the Commission Service in production, including deployment, monitoring, maintenance, and disaster recovery procedures.