# Social Commerce Folder Classification Analysis

## Summary
- **Total Folders Analyzed**: 29
- **Java Backend Services**: 20
- **Frontend Services**: 5
- **Utility/Shared/Documentation**: 4

## Detailed Classification

### Java Backend Services (20 folders)
Services with `pom.xml` files indicating Spring Boot/Maven-based Java microservices:

1. **admin-finalization** - Java Backend Service
2. **admin-interfaces** - Java Backend Service
3. **analytics-service** - Java Backend Service
4. **api-gateway** - Java Backend Service
5. **commission-service** - Java Backend Service
6. **fulfillment-options** - Java Backend Service
7. **integration-optimization** - Java Backend Service
8. **integration-performance** - Java Backend Service
9. **invoice-service** - Java Backend Service
10. **localization-service** - Java Backend Service
11. **marketplace** - Java Backend Service
12. **multi-currency-service** - Java Backend Service
13. **order-service** - Java Backend Service
14. **payment-gateway** - Java Backend Service
15. **payout-service** - Java Backend Service
16. **product-service** - Java Backend Service
17. **regional-admin** - Java Backend Service
18. **social-commerce-production** - Java Backend Service
19. **social-commerce-shared** - Java Backend Service
20. **social-commerce-staging** - Java Backend Service
21. **subscription-service** - Java Backend Service
22. **vendor-onboarding** - Java Backend Service

### Frontend Services (5 folders)
Services with `package.json` files indicating Node.js/React/TypeScript applications:

1. **global-hq-admin** - Frontend Service (React/TypeScript admin dashboard)
2. **social-media-integration** - Frontend Service (Node.js service)
3. **user-mobile-app** - Frontend Service (React Native mobile app)
4. **user-web-app** - Frontend Service (React web application)
5. **vendor-app** - Frontend Service (React vendor application)

### Utility/Shared/Documentation (4 folders)
Folders without build configuration files, containing documentation, models, or utility code:

1. **audit** - Documentation/reports folder
2. **domain-model** - Shared domain models and entities
3. **DOC archive** - Archived documentation
4. **Doc** - Documentation folder
5. **REMEDIATION-DOCS** - Documentation for remediation processes
6. **REMEDIATION-SCRIPTS** - Utility scripts for remediation
7. **tools archives** - Archived tools and utilities

## Architecture Insights

### Backend Services Pattern
- All Java services follow Maven structure with `pom.xml`
- Standard Spring Boot application structure in `src/main/java`
- Comprehensive testing structure in `src/test/java`
- Docker support with `Dockerfile` in most services
- Kubernetes manifests in `k8s/` directories
- API documentation in `api-docs/` folders

### Frontend Services Pattern
- React/TypeScript applications with `package.json`
- Modern frontend build tooling (Webpack, Create React App, etc.)
- Mobile app built with React Native
- Node.js services for integration layers

### Service Categorization by Business Function

**Core Commerce Services:**
- marketplace, order-service, product-service, payment-gateway

**Financial Services:**
- commission-service, payout-service, invoice-service, multi-currency-service

**Administrative Services:**
- admin-finalization, admin-interfaces, regional-admin, global-hq-admin

**Integration & Performance:**
- api-gateway, integration-optimization, integration-performance, social-media-integration

**Support Services:**
- analytics-service, localization-service, fulfillment-options, subscription-service, vendor-onboarding

**Infrastructure:**
- social-commerce-shared, social-commerce-production, social-commerce-staging

## Recommendations

1. **Service Consolidation**: Consider if all 20 Java services are necessary or if some can be merged
2. **Shared Libraries**: Leverage `social-commerce-shared` for common functionality
3. **Frontend Architecture**: Ensure proper API integration between frontend and backend services
4. **Documentation**: Organize utility folders for better maintainability
5. **Testing Strategy**: Implement comprehensive testing across all service types