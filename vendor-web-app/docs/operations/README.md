# Operations Guide - Vendor Web App

## Deployment Overview

The Vendor Web App supports multiple deployment strategies for different environments and requirements.

## Environment Configuration

### Development
- **Purpose**: Local development and testing
- **URL**: http://localhost:3000
- **API**: http://localhost:8080/api/v1
- **Database**: Local PostgreSQL instance
- **Caching**: Local Redis instance

### Staging
- **Purpose**: Pre-production testing
- **URL**: https://vendor-staging.gogidix-ecosystem.com
- **API**: https://api-staging.gogidix-ecosystem.com/vendor-web/api/v1
- **Database**: AWS RDS (staging instance)
- **CDN**: CloudFront distribution

### Production
- **Purpose**: Live production environment
- **URL**: https://vendor.gogidix-ecosystem.com
- **API**: https://api.gogidix-ecosystem.com/vendor-web/api/v1
- **Database**: AWS RDS (production cluster)
- **CDN**: CloudFront with multiple edge locations

## Docker Deployment

### Building the Image
```bash
# Build production image
docker build -t vendor-web-app:latest .

# Tag for registry
docker tag vendor-web-app:latest registry.gogidix.com/vendor-web-app:1.0.0

# Push to registry
docker push registry.gogidix.com/vendor-web-app:1.0.0
```

### Docker Compose Deployment
```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f vendor-web-app

# Update service
docker-compose pull vendor-web-app
docker-compose up -d vendor-web-app
```

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (version 1.20+)
- kubectl configured
- Helm 3.x installed
- Ingress controller deployed

### Deployment Steps
```bash
# Apply namespace
kubectl apply -f k8s/namespace.yaml

# Deploy application
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n vendor-web-app

# Check service status
kubectl get svc -n vendor-web-app

# View logs
kubectl logs -f deployment/vendor-web-app -n vendor-web-app
```

### Rolling Updates
```bash
# Update image
kubectl set image deployment/vendor-web-app \
  vendor-web-app=registry.gogidix.com/vendor-web-app:1.1.0 \
  -n vendor-web-app

# Check rollout status
kubectl rollout status deployment/vendor-web-app -n vendor-web-app

# Rollback if needed
kubectl rollout undo deployment/vendor-web-app -n vendor-web-app
```

## CDN and Static Asset Deployment

### AWS CloudFront Setup
```bash
# Build production assets
npm run build

# Sync to S3 bucket
aws s3 sync build/ s3://vendor-web-app-assets --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id E1234567890123 \
  --paths "/*"
```

### Asset Optimization
- Enable gzip compression
- Set proper cache headers
- Use WebP images where supported
- Implement lazy loading
- Minify CSS and JavaScript

## SSL/TLS Configuration

### Certificate Management
```bash
# Using Let's Encrypt with cert-manager
kubectl apply -f k8s/certificate.yaml

# Check certificate status
kubectl get certificate -n vendor-web-app

# Verify SSL configuration
openssl s_client -connect vendor.gogidix-ecosystem.com:443
```

## Monitoring and Observability

### Health Checks
```bash
# Application health endpoint
curl https://vendor.gogidix-ecosystem.com/health

# Kubernetes liveness probe
kubectl describe pod <pod-name> -n vendor-web-app
```

### Metrics Collection
- **Prometheus**: Application metrics
- **Grafana**: Visualization dashboards
- **Jaeger**: Distributed tracing
- **ELK Stack**: Log aggregation

### Key Metrics to Monitor
- Response time and throughput
- Error rates and types
- Resource utilization (CPU, Memory)
- Database connection pool status
- Cache hit/miss ratios

## Backup and Recovery

### Database Backups
```bash
# Create database backup
kubectl exec -it postgres-pod -n vendor-web-app -- \
  pg_dump -U postgres vendor_web_app > backup-$(date +%Y%m%d).sql

# Restore from backup
kubectl exec -i postgres-pod -n vendor-web-app -- \
  psql -U postgres vendor_web_app < backup-20231201.sql
```

### Application State Backup
- Redux state persistence
- Local storage backup
- Session management
- User preferences

## Security Operations

### Security Headers
```nginx
# Nginx configuration
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;
add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
```

### Vulnerability Scanning
```bash
# NPM audit
npm audit

# Fix vulnerabilities
npm audit fix

# Docker image scanning
docker scan vendor-web-app:latest

# Kubernetes security scanning
kubectl run --rm -it kube-bench --image=aquasec/kube-bench:latest
```

## Performance Optimization

### Build Optimization
```bash
# Analyze bundle size
npm run analyze

# Enable production optimizations
NODE_ENV=production npm run build

# Enable gzip compression
gzip -9 build/static/js/*.js
gzip -9 build/static/css/*.css
```

### Runtime Optimization
- Code splitting and lazy loading
- Service worker caching
- HTTP/2 server push
- Resource preloading
- Image optimization

## Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check environment variables
printenv | grep REACT_APP

# Verify API connectivity
curl -I https://api.gogidix-ecosystem.com/vendor-web/api/v1/health

# Check Docker logs
docker logs vendor-web-app
```

#### High Memory Usage
```bash
# Monitor memory usage
kubectl top pods -n vendor-web-app

# Check for memory leaks
node --inspect build/static/js/main.js

# Optimize bundle size
npm run analyze
```

#### Slow Performance
```bash
# Enable profiling
REACT_APP_PROFILE=true npm start

# Check network requests
# Use browser DevTools Network tab

# Monitor Core Web Vitals
# Use Lighthouse audit
```

### Emergency Procedures

#### Rollback Deployment
```bash
# Kubernetes rollback
kubectl rollout undo deployment/vendor-web-app -n vendor-web-app

# Docker Compose rollback
docker-compose down
docker-compose up -d
```

#### Scale Down/Up
```bash
# Scale down for maintenance
kubectl scale deployment vendor-web-app --replicas=0 -n vendor-web-app

# Scale up after maintenance
kubectl scale deployment vendor-web-app --replicas=3 -n vendor-web-app
```

## Maintenance

### Regular Tasks
- Update dependencies monthly
- Review security vulnerabilities
- Monitor performance metrics
- Clean up old Docker images
- Rotate SSL certificates
- Update documentation

### Automated Tasks
- Daily backup creation
- Weekly security scans
- Monthly dependency updates
- Quarterly performance reviews
- Bi-annual disaster recovery tests

## Runbooks

### Deployment Runbook
1. Verify staging deployment
2. Create production backup
3. Deploy to production
4. Run smoke tests
5. Monitor for issues
6. Rollback if necessary

### Incident Response
1. Assess impact and severity
2. Notify stakeholders
3. Implement temporary fix
4. Root cause analysis
5. Permanent fix deployment
6. Post-incident review

## Support Contacts

- **DevOps Team**: devops@gogidix.com
- **Security Team**: security@gogidix.com
- **On-call Engineer**: +1-555-0123
- **Incident Management**: incidents@gogidix.com