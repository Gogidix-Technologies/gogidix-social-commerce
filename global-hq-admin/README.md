# Global HQ Admin Dashboard

## Overview

The Global HQ Admin Dashboard is a comprehensive administrative interface for global headquarters management of the social commerce ecosystem. It provides executive-level insights, cross-regional analytics, and strategic decision-making tools for managing the entire social commerce platform across multiple regions.

### Service Details

- **Service Type**: Frontend Application
- **Domain**: Social Commerce
- **Port**: 3100
- **Health Check**: `http://localhost:3100/health`
- **Technology**: React.js + Node.js

## Architecture

### Position in Ecosystem

The Global HQ Admin Dashboard serves as the primary command center for global executives and administrators to monitor, manage, and optimize the entire social commerce ecosystem. It aggregates data from all regions and provides high-level strategic insights.

### Key Responsibilities

- Global performance monitoring and KPI tracking
- Cross-regional analytics and comparison reports
- Strategic decision support through advanced visualizations
- Global vendor and marketplace oversight
- Financial performance tracking across all regions
- Compliance monitoring and regulatory reporting
- Executive dashboard for stakeholder presentations

### Technology Stack

- **Frontend**: React 18.x + TypeScript
- **State Management**: Redux Toolkit + RTK Query
- **UI Framework**: Material-UI v5 + Custom Design System
- **Charts**: Chart.js + D3.js for advanced visualizations
- **Backend**: Node.js 18.x + Express.js
- **Authentication**: Auth0 + JWT
- **Build Tool**: Webpack 5 + Vite

## Features

### Executive Dashboard

- **Global KPIs**: Revenue, growth rates, customer acquisition
- **Regional Performance**: Comparative analysis across Europe and Africa
- **Real-time Metrics**: Live updates of critical business metrics
- **Trend Analysis**: Historical trends and predictive analytics
- **Executive Reports**: Automated executive summary generation

### Multi-Regional Management

- **Regional Comparison**: Side-by-side regional performance analysis
- **Market Penetration**: Geographic market analysis and opportunities
- **Localization Status**: Multi-language and currency adoption metrics
- **Regulatory Compliance**: Region-specific compliance monitoring

### Vendor & Marketplace Oversight

- **Top Performer Analysis**: Global vendor performance rankings
- **Category Performance**: Product category analysis across regions
- **Marketplace Health**: Platform utilization and engagement metrics
- **Quality Metrics**: Customer satisfaction and service quality tracking

### Financial Analytics

- **Revenue Streams**: Commission, subscription, and service revenue tracking
- **Profitability Analysis**: Margin analysis by region and service
- **Financial Forecasting**: Predictive financial modeling
- **Cost Center Analysis**: Operational cost breakdown and optimization

## API Endpoints

### Dashboard Data

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1/dashboard/overview` | Global overview metrics |
| GET    | `/api/v1/dashboard/regions` | Regional performance data |
| GET    | `/api/v1/dashboard/financial` | Financial analytics dashboard |
| GET    | `/api/v1/dashboard/vendors` | Global vendor performance |
| GET    | `/api/v1/dashboard/compliance` | Compliance status overview |

### Reports and Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1/reports/executive` | Executive summary reports |
| POST   | `/api/v1/reports/generate` | Generate custom reports |
| GET    | `/api/v1/analytics/trends` | Trend analysis data |
| GET    | `/api/v1/analytics/predictions` | Predictive analytics |

## Dependencies

### External Services

| Service | Purpose | Communication Method |
|---------|---------|---------------------|
| Analytics Service | Global analytics data | REST API |
| Regional Admin Services | Regional performance data | REST API |
| Centralized Data Aggregation | Cross-domain data aggregation | WebSocket + REST |
| Auth Service | Authentication and authorization | OAuth2/JWT |
| User Profile Service | User management | REST API |

### Infrastructure Dependencies

- **CDN**: Content delivery for global performance
- **Load Balancer**: Traffic distribution and failover
- **Redis**: Session storage and caching
- **Elasticsearch**: Search and analytics indexing

## Configuration

### Environment Variables

```bash
# Application Configuration
REACT_APP_ENV=production
REACT_APP_API_BASE_URL=https://api.social-ecommerce.com
REACT_APP_VERSION=1.0.0

# Authentication
REACT_APP_AUTH0_DOMAIN=social-commerce.auth0.com
REACT_APP_AUTH0_CLIENT_ID=your_auth0_client_id
REACT_APP_AUTH0_AUDIENCE=social-commerce-api

# Backend Server Configuration
PORT=3100
NODE_ENV=production
SESSION_SECRET=your_session_secret
REDIS_URL=redis://localhost:6379

# External Service URLs
ANALYTICS_SERVICE_URL=http://analytics-service:8100
REGIONAL_ADMIN_URLS={"EU":"http://eu-admin:3200","AF":"http://af-admin:3201"}

# Feature Flags
ENABLE_PREDICTIVE_ANALYTICS=true
ENABLE_REAL_TIME_UPDATES=true
ENABLE_ADVANCED_REPORTING=true
```

### Configuration Files

- `src/config/environment.ts` - Environment-specific configuration
- `src/config/features.ts` - Feature flag configuration
- `src/config/regions.ts` - Regional configuration settings

## User Interface

### Dashboard Layout

```
Header (Global Navigation + User Menu)
├── Sidebar Navigation
│   ├── Executive Dashboard
│   ├── Regional Analysis
│   ├── Vendor Management
│   ├── Financial Analytics
│   ├── Compliance Reports
│   └── System Administration
└── Main Content Area
    ├── KPI Cards
    ├── Interactive Charts
    ├── Data Tables
    └── Action Panels
```

### Key Components

- **ExecutiveDashboard**: Main overview dashboard
- **RegionalComparison**: Multi-region analysis component
- **VendorPerformance**: Vendor analytics and management
- **FinancialMetrics**: Financial performance tracking
- **ComplianceMonitor**: Regulatory compliance dashboard
- **ReportBuilder**: Custom report generation tool

## Development

### Prerequisites

- Node.js 18+
- npm 8+ or yarn 1.22+
- React DevTools browser extension
- Git

### Local Setup

1. Clone the repository
```bash
git clone https://github.com/social-ecommerce-ecosystem/social-commerce.git
cd social-commerce/global-hq-admin
```

2. Install dependencies
```bash
npm install
```

3. Set up environment variables
```bash
cp .env.template .env.local
# Edit .env.local with your local configuration
```

4. Start the development server
```bash
npm start
```

### Running Tests

```bash
# Unit tests
npm test

# Integration tests
npm run test:integration

# End-to-end tests
npm run test:e2e

# Test coverage
npm run test:coverage
```

### Building for Production

```bash
# Production build
npm run build

# Analyze bundle size
npm run analyze

# Build with specific environment
npm run build:staging
```

## Deployment

### Docker

```bash
# Build image
docker build -t global-hq-admin:latest .

# Run container
docker run -p 3100:3100 \
  --env-file .env \
  global-hq-admin:latest
```

### Kubernetes

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=global-hq-admin
```

### CDN Deployment

The application uses CDN for global performance:

```bash
# Deploy static assets to CDN
npm run deploy:cdn

# Verify CDN deployment
npm run verify:cdn
```

## Monitoring

### Performance Metrics

- **Core Web Vitals**: LCP, FID, CLS monitoring
- **User Experience**: Page load times, interaction metrics
- **Error Tracking**: JavaScript error monitoring with Sentry
- **Analytics**: User behavior tracking with custom events

### Health Checks

- **Application Health**: `/health` endpoint
- **API Connectivity**: Backend service health checks
- **Authentication Status**: Auth service connectivity

## Security

### Authentication & Authorization

- **Multi-Factor Authentication**: Required for all admin access
- **Role-Based Access**: Executive, Regional Manager, Analyst roles
- **Session Management**: Secure session handling with Redis
- **API Security**: All API calls use JWT tokens

### Data Protection

- **Encryption**: All data encrypted in transit and at rest
- **Access Logging**: Complete audit trail of data access
- **Data Masking**: Sensitive data masked in development environments
- **GDPR Compliance**: Full GDPR compliance for European operations

## Integration Points

### Warehousing Domain

- Warehouse performance metrics integration
- Cross-domain inventory insights
- Fulfillment analytics and reporting

### Courier Services Domain

- Delivery performance tracking
- Logistics cost analysis
- Service quality monitoring

### Shared Infrastructure

- Centralized authentication and user management
- Global monitoring and alerting integration
- Shared configuration management

## Related Documentation

- [Overall Architecture](/docs/architecture/README.md)
- [Admin Framework Design System](/docs/ui-ux/admin-framework.md)
- [Global Analytics Strategy](/docs/analytics/global-strategy.md)
- [Executive Reporting Guide](/docs/operations/executive-reporting.md)

## Contact

- **Team**: Global HQ Platform Team
- **Slack Channel**: #global-hq-admin
- **Email**: global-hq-team@social-ecommerce.com

## License

Proprietary - Social E-commerce Ecosystem
