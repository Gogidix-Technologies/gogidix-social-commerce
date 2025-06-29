# Marketplace Setup Documentation

## Development Setup

### Prerequisites
- **Java 17+**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL 14+**
- **Redis 6.2+**
- **Elasticsearch 8.x**
- **IDE**: IntelliJ IDEA recommended

### Local Development

#### 1. Clone Repository
```bash
git clone <repository-url>
cd marketplace
```

#### 2. Start Dependencies
```bash
docker-compose up -d postgres redis elasticsearch
```

#### 3. Configure Environment
```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=marketplace_db
export DB_USERNAME=marketplace_user
export DB_PASSWORD=marketplace_pass

# Redis
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Elasticsearch
export ELASTICSEARCH_URL=http://localhost:9200

# Security
export JWT_SECRET=your-jwt-secret-key
export JWT_EXPIRATION=86400

# External Services
export PAYMENT_SERVICE_URL=http://localhost:8081
export INVENTORY_SERVICE_URL=http://localhost:8082
export VENDOR_SERVICE_URL=http://localhost:8083
```

#### 4. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Database Setup

#### Schema Creation
```sql
CREATE DATABASE marketplace_db;
CREATE USER marketplace_user WITH PASSWORD 'marketplace_pass';
GRANT ALL PRIVILEGES ON DATABASE marketplace_db TO marketplace_user;
```

#### Sample Data
```bash
mvn flyway:migrate
mvn exec:java -Dexec.mainClass="com.exalt.marketplace.util.DataLoader"
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/products

# Using k6
k6 run scripts/load-test.js
```

### API Testing
```bash
# Health check
curl http://localhost:8080/actuator/health

# Product search
curl "http://localhost:8080/api/products/search?q=laptop"

# Add to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"productId": "uuid", "quantity": 1}'
```

## Production Setup

### Infrastructure Requirements
- **CPU**: 4+ cores per instance
- **Memory**: 8+ GB per instance
- **Storage**: 100+ GB SSD
- **Network**: 1+ Gbps bandwidth
- **Load Balancer**: Nginx/HAProxy

### Database Configuration
```yaml
# PostgreSQL Production Settings
max_connections: 200
shared_buffers: 2GB
effective_cache_size: 6GB
work_mem: 32MB
maintenance_work_mem: 512MB
```

### Redis Configuration
```yaml
# Redis Production Settings
maxmemory: 4gb
maxmemory-policy: allkeys-lru
save: 900 1 300 10 60 10000
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: marketplace
spec:
  replicas: 3
  selector:
    matchLabels:
      app: marketplace
  template:
    spec:
      containers:
      - name: marketplace
        image: marketplace:latest
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

### Security Configuration
- Enable SSL/TLS encryption
- Configure firewall rules
- Set up monitoring and alerting
- Implement backup strategy
- Configure log rotation