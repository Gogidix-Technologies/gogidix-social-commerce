# Vendor Onboarding Service

The Vendor Onboarding Service is a critical component of the Micro-Social-Ecommerce-Ecosystem, responsible for managing the entire vendor onboarding lifecycle, including registration, document verification, KYC processing, and subscription management.

## Features

- **Vendor Registration Workflow**: Comprehensive process for new vendors to register on the platform
- **KYC/Documentation Processing**: Secure verification of vendor identity and business credentials
- **Subscription Management**: Flexible subscription tiers with support for various payment options
- **Vendor Dashboard API**: Centralized dashboard for vendors to track their onboarding progress

## Tech Stack

- Java 11
- Spring Boot 2.6.x
- Spring Security with OAuth2 Resource Server
- Spring Data JPA
- MySQL Database
- Flyway Migration
- Resilience4j for Circuit Breaking
- Spring Cloud for Service Discovery
- Lombok for reducing boilerplate code

## Getting Started

### Prerequisites

- JDK 11 or higher
- Maven 3.6.x or higher
- MySQL 8.0 or higher

### Local Development

1. Clone the repository
2. Configure your local database in `application.yml` or use environment variables
3. Run the application using Maven:

```bash
mvn spring-boot:run
```

### Building

```bash
mvn clean package
```

### Running with Docker

```bash
docker-compose up -d
```

## API Documentation

The API documentation is available at `/swagger-ui/index.html` when the application is running.

### Key Endpoints

- `/api/vendors` - Vendor registration and management
- `/api/documents` - Document upload and verification
- `/api/kyc` - KYC information submission and verification
- `/api/subscriptions` - Subscription management
- `/api/vendor-dashboard` - Vendor dashboard API

## Architecture

The service follows a layered architecture:

- **Controllers**: REST endpoints for client interaction
- **Services**: Business logic layer
- **Repositories**: Data access layer
- **DTOs**: Data Transfer Objects for client communication
- **Models**: Domain entities for persistence
- **Security**: Components for authorization and authentication

## Deployment

The service can be deployed as a standalone Spring Boot application or as a containerized service in a Kubernetes cluster. Deployment configurations are available in the `k8s` directory.

## Integrations

This service integrates with:

- **Auth Service**: For authentication and authorization
- **Marketplace Service**: For vendor marketplace listings
- **Payment Gateway**: For subscription payments
- **Notification Service**: For alerts and notifications

## Contributing

Please see the [CONTRIBUTING.md](../../CONTRIBUTING.md) file for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
