# Social Media Integration Service Operations Guide

## Production Deployment

### Container Orchestration (Kubernetes)

#### Deployment Configuration
```yaml
# social-media-integration-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: social-media-integration
  namespace: social-commerce
  labels:
    app: social-media-integration
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: social-media-integration
  template:
    metadata:
      labels:
        app: social-media-integration
    spec:
      containers:
      - name: social-media-integration
        image: gogidix/social-media-integration:1.0.0
        ports:
        - containerPort: 3004
        env:
        - name: NODE_ENV
          value: "production"
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: social-secrets
              key: mongodb-uri
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: social-config
              key: redis-host
        resources:
          limits:
            cpu: 1000m
            memory: 1Gi
          requests:
            cpu: 500m
            memory: 512Mi
        livenessProbe:
          httpGet:
            path: /health
            port: 3004
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: 3004
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### Service Configuration
```yaml
# social-media-integration-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: social-media-integration-service
  namespace: social-commerce
spec:
  selector:
    app: social-media-integration
  ports:
  - protocol: TCP
    port: 80
    targetPort: 3004
  type: ClusterIP
```

#### Horizontal Pod Autoscaler
```yaml
# social-media-integration-hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: social-media-integration-hpa
  namespace: social-commerce
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: social-media-integration
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
```

### Database Operations

#### MongoDB Management
```bash
# Database backup
mongodump --uri="mongodb://production-cluster:27017/social_media_integration" \
  --out=/backups/social-media-$(date +%Y%m%d_%H%M%S)

# Database restore
mongorestore --uri="mongodb://production-cluster:27017/social_media_integration" \
  /backups/social-media-20241226_143000

# Index optimization
mongo social_media_integration --eval "
  db.social_posts.createIndex({platformId: 1, platform: 1}, {background: true});
  db.social_posts.createIndex({createdAt: -1}, {background: true});
  db.analytics.createIndex({postId: 1, timestamp: -1}, {background: true});
"

# Collection statistics
mongo social_media_integration --eval "db.stats()"
```

#### Redis Operations
```bash
# Memory usage analysis
redis-cli info memory

# Key space analysis
redis-cli info keyspace

# Performance monitoring
redis-cli monitor

# Backup Redis data
redis-cli --rdb /backups/redis-dump-$(date +%Y%m%d).rdb

# Clear cache (use with caution)
redis-cli flushdb
```

#### PostgreSQL Analytics Database
```sql
-- Performance monitoring
SELECT 
  schemaname,
  tablename,
  attname,
  n_distinct,
  correlation
FROM pg_stats 
WHERE tablename = 'social_analytics';

-- Index usage analysis
SELECT 
  schemaname,
  tablename,
  indexname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch
FROM pg_stat_user_indexes;

-- Database size monitoring
SELECT 
  pg_size_pretty(pg_database_size('social_analytics')) as database_size;
```

## Monitoring and Alerting

### Application Metrics

#### Prometheus Configuration
```yaml
# prometheus-config.yml
global:
  scrape_interval: 15s

scrape_configs:
- job_name: 'social-media-integration'
  static_configs:
  - targets: ['social-media-integration:9090']
  metrics_path: /metrics
  scrape_interval: 10s
```

#### Custom Metrics
```javascript
// metrics.js
const promClient = require('prom-client');

// Business metrics
const socialPostsTotal = new promClient.Counter({
  name: 'social_posts_total',
  help: 'Total number of social media posts',
  labelNames: ['platform', 'status']
});

const socialEngagementGauge = new promClient.Gauge({
  name: 'social_engagement_rate',
  help: 'Social media engagement rate',
  labelNames: ['platform', 'post_type']
});

const apiRequestDuration = new promClient.Histogram({
  name: 'api_request_duration_seconds',
  help: 'Duration of API requests in seconds',
  labelNames: ['method', 'route', 'status_code'],
  buckets: [0.1, 0.5, 1, 2, 5]
});

// Platform-specific metrics
const platformApiCalls = new promClient.Counter({
  name: 'platform_api_calls_total',
  help: 'Total API calls to social platforms',
  labelNames: ['platform', 'endpoint', 'status']
});

const rateLimitStatus = new promClient.Gauge({
  name: 'platform_rate_limit_remaining',
  help: 'Remaining API calls for platform rate limits',
  labelNames: ['platform']
});
```

#### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "Social Media Integration Service",
    "panels": [
      {
        "title": "API Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(api_request_duration_seconds_count[5m])",
            "legendFormat": "{{method}} {{route}}"
          }
        ]
      },
      {
        "title": "Social Posts by Platform",
        "type": "stat",
        "targets": [
          {
            "expr": "sum by (platform) (social_posts_total)",
            "legendFormat": "{{platform}}"
          }
        ]
      },
      {
        "title": "Rate Limit Status",
        "type": "gauge",
        "targets": [
          {
            "expr": "platform_rate_limit_remaining",
            "legendFormat": "{{platform}}"
          }
        ]
      }
    ]
  }
}
```

### Health Checks

#### Application Health Endpoints
```javascript
// health.js
const express = require('express');
const mongoose = require('mongoose');
const redis = require('./config/redis');

const router = express.Router();

// Basic health check
router.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    service: 'social-media-integration',
    version: process.env.npm_package_version
  });
});

// Detailed readiness check
router.get('/ready', async (req, res) => {
  const checks = {};
  let allHealthy = true;

  // MongoDB check
  try {
    await mongoose.connection.db.admin().ping();
    checks.mongodb = { status: 'healthy' };
  } catch (error) {
    checks.mongodb = { status: 'unhealthy', error: error.message };
    allHealthy = false;
  }

  // Redis check
  try {
    await redis.ping();
    checks.redis = { status: 'healthy' };
  } catch (error) {
    checks.redis = { status: 'unhealthy', error: error.message };
    allHealthy = false;
  }

  // Platform connectivity check
  const platforms = ['facebook', 'twitter', 'instagram', 'tiktok'];
  for (const platform of platforms) {
    try {
      const status = await checkPlatformHealth(platform);
      checks[platform] = { status: status ? 'healthy' : 'degraded' };
    } catch (error) {
      checks[platform] = { status: 'unhealthy', error: error.message };
    }
  }

  res.status(allHealthy ? 200 : 503).json({
    status: allHealthy ? 'ready' : 'not ready',
    checks,
    timestamp: new Date().toISOString()
  });
});
```

### Alerting Rules

#### Prometheus Alerting Rules
```yaml
# social-media-alerts.yml
groups:
- name: social-media-integration
  rules:
  - alert: HighErrorRate
    expr: rate(api_request_duration_seconds_count{status_code=~"5.."}[5m]) > 0.1
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "High error rate in Social Media Integration Service"
      description: "Error rate is {{ $value }} errors per second"

  - alert: HighResponseTime
    expr: histogram_quantile(0.95, rate(api_request_duration_seconds_bucket[5m])) > 2
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High response time in Social Media Integration Service"
      description: "95th percentile response time is {{ $value }}s"

  - alert: PlatformRateLimitExceeded
    expr: platform_rate_limit_remaining < 10
    for: 1m
    labels:
      severity: warning
    annotations:
      summary: "Platform rate limit nearly exceeded"
      description: "{{ $labels.platform }} has only {{ $value }} API calls remaining"

  - alert: DatabaseConnectionFailed
    expr: up{job="social-media-integration"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Social Media Integration Service is down"
      description: "Service has been down for more than 1 minute"
```

#### Slack/Email Notifications
```javascript
// alerting.js
const { WebClient } = require('@slack/web-api');
const nodemailer = require('nodemailer');

class AlertManager {
  constructor() {
    this.slack = new WebClient(process.env.SLACK_BOT_TOKEN);
    this.emailTransporter = nodemailer.createTransporter({
      host: process.env.SMTP_HOST,
      port: process.env.SMTP_PORT,
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS
      }
    });
  }

  async sendAlert(alert) {
    if (alert.severity === 'critical') {
      await this.sendSlackAlert(alert);
      await this.sendEmailAlert(alert);
    } else {
      await this.sendSlackAlert(alert);
    }
  }

  async sendSlackAlert(alert) {
    await this.slack.chat.postMessage({
      channel: '#social-media-alerts',
      text: `🚨 *${alert.severity.toUpperCase()}*: ${alert.summary}`,
      blocks: [
        {
          type: 'section',
          text: {
            type: 'mrkdwn',
            text: `*Alert*: ${alert.summary}\n*Description*: ${alert.description}\n*Severity*: ${alert.severity}`
          }
        }
      ]
    });
  }
}
```

## Security Operations

### Secrets Management

#### Kubernetes Secrets
```yaml
# social-secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: social-secrets
  namespace: social-commerce
type: Opaque
data:
  mongodb-uri: <base64-encoded-uri>
  facebook-app-secret: <base64-encoded-secret>
  twitter-api-secret: <base64-encoded-secret>
  instagram-client-secret: <base64-encoded-secret>
  tiktok-app-secret: <base64-encoded-secret>
  jwt-secret: <base64-encoded-secret>
  encryption-key: <base64-encoded-key>
```

#### Vault Integration
```javascript
// vault-client.js
const vault = require('node-vault')({
  apiVersion: 'v1',
  endpoint: process.env.VAULT_ENDPOINT,
  token: process.env.VAULT_TOKEN
});

class SecretsManager {
  async getSecret(path) {
    try {
      const result = await vault.read(`secret/data/${path}`);
      return result.data.data;
    } catch (error) {
      throw new Error(`Failed to retrieve secret: ${error.message}`);
    }
  }

  async rotateApiKeys() {
    const platforms = ['facebook', 'twitter', 'instagram', 'tiktok'];
    
    for (const platform of platforms) {
      const newKeys = await this.generateNewKeys(platform);
      await vault.write(`secret/data/social-media/${platform}`, newKeys);
      await this.updateApplicationConfig(platform, newKeys);
    }
  }
}
```

### Access Control

#### JWT Token Management
```javascript
// auth-middleware.js
const jwt = require('jsonwebtoken');
const redis = require('./config/redis');

const authMiddleware = async (req, res, next) => {
  try {
    const token = req.header('Authorization')?.replace('Bearer ', '');
    
    if (!token) {
      return res.status(401).json({ error: 'Access denied. No token provided.' });
    }

    // Check if token is blacklisted
    const blacklisted = await redis.get(`blacklist:${token}`);
    if (blacklisted) {
      return res.status(401).json({ error: 'Token has been revoked.' });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (error) {
    res.status(400).json({ error: 'Invalid token.' });
  }
};

// Token blacklisting on logout
const blacklistToken = async (token) => {
  const decoded = jwt.decode(token);
  const ttl = decoded.exp - Math.floor(Date.now() / 1000);
  await redis.setex(`blacklist:${token}`, ttl, 'true');
};
```

### Rate Limiting

#### Platform-Specific Rate Limiting
```javascript
// rate-limiter.js
const RateLimiter = require('bottleneck');

class PlatformRateLimiter {
  constructor() {
    this.limiters = {
      facebook: new RateLimiter({
        reservoir: 200,
        reservoirRefreshAmount: 200,
        reservoirRefreshInterval: 60 * 60 * 1000, // 1 hour
      }),
      twitter: new RateLimiter({
        reservoir: 300,
        reservoirRefreshAmount: 300,
        reservoirRefreshInterval: 15 * 60 * 1000, // 15 minutes
      }),
      instagram: new RateLimiter({
        reservoir: 200,
        reservoirRefreshAmount: 200,
        reservoirRefreshInterval: 60 * 60 * 1000, // 1 hour
      }),
      tiktok: new RateLimiter({
        reservoir: 100,
        reservoirRefreshAmount: 100,
        reservoirRefreshInterval: 10 * 60 * 1000, // 10 minutes
      })
    };
  }

  async makeRequest(platform, requestFn) {
    return this.limiters[platform].schedule(requestFn);
  }
}
```

## Performance Optimization

### Caching Strategy

#### Multi-Layer Caching
```javascript
// cache-manager.js
const NodeCache = require('node-cache');
const redis = require('./config/redis');

class CacheManager {
  constructor() {
    this.memoryCache = new NodeCache({ stdTTL: 300 }); // 5 minutes
  }

  async get(key, options = {}) {
    // L1: Memory cache
    let value = this.memoryCache.get(key);
    if (value) return value;

    // L2: Redis cache
    value = await redis.get(key);
    if (value) {
      const parsed = JSON.parse(value);
      this.memoryCache.set(key, parsed, options.ttl || 300);
      return parsed;
    }

    return null;
  }

  async set(key, value, ttl = 3600) {
    // Set in both layers
    this.memoryCache.set(key, value, ttl);
    await redis.setex(key, ttl, JSON.stringify(value));
  }

  async invalidate(pattern) {
    // Clear memory cache
    this.memoryCache.flushAll();
    
    // Clear Redis cache by pattern
    const keys = await redis.keys(pattern);
    if (keys.length > 0) {
      await redis.del(keys);
    }
  }
}
```

### Database Optimization

#### Connection Pool Management
```javascript
// database.js
const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGODB_URI, {
      maxPoolSize: 10,
      serverSelectionTimeoutMS: 5000,
      socketTimeoutMS: 45000,
      bufferMaxEntries: 0,
      useNewUrlParser: true,
      useUnifiedTopology: true
    });

    console.log(`MongoDB Connected: ${conn.connection.host}`);
  } catch (error) {
    console.error('Database connection failed:', error);
    process.exit(1);
  }
};
```

#### Query Optimization
```javascript
// optimized-queries.js
class SocialPostService {
  async getRecentPosts(userId, platform, limit = 20) {
    return await SocialPost.find({
      userId,
      platform,
      createdAt: { $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) }
    })
    .sort({ createdAt: -1 })
    .limit(limit)
    .lean() // Return plain objects instead of Mongoose documents
    .select('content mediaUrls engagement createdAt') // Only select needed fields
    .hint({ userId: 1, platform: 1, createdAt: -1 }); // Use specific index
  }
}
```

## Disaster Recovery

### Backup Procedures

#### Automated Backup Script
```bash
#!/bin/bash
# backup-social-media.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/social-media/$DATE"
LOG_FILE="/logs/backup-$DATE.log"

mkdir -p $BACKUP_DIR

echo "Starting backup at $(date)" >> $LOG_FILE

# MongoDB backup
echo "Backing up MongoDB..." >> $LOG_FILE
mongodump --uri="$MONGODB_URI" --out="$BACKUP_DIR/mongodb" 2>> $LOG_FILE

# Redis backup
echo "Backing up Redis..." >> $LOG_FILE
redis-cli --rdb "$BACKUP_DIR/redis-dump.rdb" 2>> $LOG_FILE

# PostgreSQL backup
echo "Backing up PostgreSQL..." >> $LOG_FILE
pg_dump "$POSTGRES_URI" > "$BACKUP_DIR/postgres-dump.sql" 2>> $LOG_FILE

# Application configuration backup
echo "Backing up configurations..." >> $LOG_FILE
kubectl get configmaps,secrets -n social-commerce -o yaml > "$BACKUP_DIR/k8s-config.yaml"

# Compress backups
tar -czf "/backups/social-media-$DATE.tar.gz" "$BACKUP_DIR"
rm -rf "$BACKUP_DIR"

echo "Backup completed at $(date)" >> $LOG_FILE

# Clean old backups (keep last 7 days)
find /backups -name "social-media-*.tar.gz" -mtime +7 -delete

# Upload to cloud storage
aws s3 cp "/backups/social-media-$DATE.tar.gz" "s3://gogidix-backups/social-media/"
```

### Recovery Procedures

#### Service Recovery Checklist
1. **Assess Impact**
   ```bash
   # Check service status
   kubectl get pods -n social-commerce
   
   # Check recent logs
   kubectl logs -f deployment/social-media-integration -n social-commerce --tail=100
   
   # Check metrics dashboard
   # Access Grafana dashboard to assess impact
   ```

2. **Database Recovery**
   ```bash
   # Restore MongoDB
   mongorestore --uri="$MONGODB_URI" /backups/latest/mongodb/
   
   # Restore Redis
   redis-cli --rdb /backups/latest/redis-dump.rdb
   
   # Restore PostgreSQL
   psql "$POSTGRES_URI" < /backups/latest/postgres-dump.sql
   ```

3. **Application Recovery**
   ```bash
   # Rollback to previous version
   kubectl rollout undo deployment/social-media-integration -n social-commerce
   
   # Scale up replicas
   kubectl scale deployment social-media-integration --replicas=5 -n social-commerce
   
   # Verify health
   kubectl get pods -n social-commerce -w
   ```

## Maintenance Procedures

### Regular Maintenance Tasks

#### Weekly Maintenance
```bash
#!/bin/bash
# weekly-maintenance.sh

# Update dependencies
npm audit
npm update

# Database maintenance
mongo social_media_integration --eval "db.runCommand({compact: 'social_posts'})"
mongo social_media_integration --eval "db.runCommand({reIndex: 'social_posts'})"

# Clear old logs
find /logs -name "*.log" -mtime +30 -delete

# Update platform tokens
npm run refresh:tokens

# Run health checks
npm run health:check

# Generate weekly report
npm run report:weekly
```

#### Monthly Maintenance
```bash
#!/bin/bash
# monthly-maintenance.sh

# Full system backup
./backup-social-media.sh

# Security updates
npm audit fix
docker pull gogidix/social-media-integration:latest

# Performance analysis
npm run analyze:performance

# Capacity planning review
kubectl top pods -n social-commerce
kubectl top nodes

# Clean up old data
mongo social_media_integration --eval "
  db.social_posts.deleteMany({
    createdAt: { \$lt: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000) }
  })
"
```

### Configuration Management

#### Environment-Specific Configurations
```yaml
# config/production.yaml
database:
  mongodb:
    uri: ${MONGODB_URI}
    poolSize: 20
    timeout: 30000
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    cluster: true
    
logging:
  level: info
  format: json
  destinations:
    - console
    - file: /logs/social-media.log
    - elasticsearch: ${ELASTICSEARCH_URL}

monitoring:
  metrics:
    enabled: true
    port: 9090
  tracing:
    enabled: true
    jaeger: ${JAEGER_ENDPOINT}

platforms:
  facebook:
    rateLimit: 200
    timeout: 10000
  twitter:
    rateLimit: 300
    timeout: 8000
  instagram:
    rateLimit: 200
    timeout: 12000
  tiktok:
    rateLimit: 100
    timeout: 15000
```

## Support and Troubleshooting

### Common Issues and Solutions

#### 1. High Memory Usage
```bash
# Check memory usage
kubectl top pods -n social-commerce

# Analyze heap dump
node --max-old-space-size=4096 --inspect src/index.js

# Optimize garbage collection
NODE_OPTIONS="--max-old-space-size=2048 --gc-interval=100"
```

#### 2. Database Connection Issues
```javascript
// Connection monitoring
mongoose.connection.on('connected', () => {
  console.log('MongoDB connected');
});

mongoose.connection.on('error', (err) => {
  console.error('MongoDB connection error:', err);
  // Implement reconnection logic
});

mongoose.connection.on('disconnected', () => {
  console.log('MongoDB disconnected. Attempting to reconnect...');
  setTimeout(() => {
    mongoose.connect(process.env.MONGODB_URI);
  }, 5000);
});
```

#### 3. Platform API Rate Limits
```javascript
// Rate limit handling
const handleRateLimit = async (platform, error) => {
  if (error.response?.status === 429) {
    const resetTime = error.response.headers['x-rate-limit-reset'];
    const waitTime = (resetTime * 1000) - Date.now();
    
    console.log(`Rate limited on ${platform}. Waiting ${waitTime}ms`);
    await new Promise(resolve => setTimeout(resolve, waitTime));
    
    // Retry the request
    return true;
  }
  return false;
};
```

### Emergency Contacts

- **Primary On-Call**: +1-555-SOCIAL (24/7)
- **Technical Lead**: social-lead@gogidix.com
- **DevOps Team**: devops@gogidix.com
- **Security Team**: security@gogidix.com

### Escalation Procedures

1. **P1 (Critical)**: Immediate notification to on-call engineer
2. **P2 (High)**: Notification within 15 minutes
3. **P3 (Medium)**: Notification within 1 hour
4. **P4 (Low)**: Notification within 4 hours

Each incident should be logged in the incident management system with appropriate severity level and assigned to the relevant team for resolution.