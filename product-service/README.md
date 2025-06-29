# product-service

## Overview
product-service is a microservice component of the Social Commerce Ecosystem that handles [brief description needed].

## Technology Stack
- Java 17
- Spring Boot 3.x
- Maven
- MySQL/PostgreSQL (as applicable)
- Docker

## Prerequisites
- JDK 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- MySQL/PostgreSQL (for local development)

## Getting Started

### Local Development
1. Clone the repository
2. Navigate to the service directory:
   ```bash
   cd product-service
   ```

3. Install dependencies:
   ```bash
   mvn clean install
   ```

4. Run the service locally:
   ```bash
   mvn spring-boot:run
   ```

### Docker Development
Build and run with Docker:
```bash
docker-compose up --build
```

## API Documentation
API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html (when running locally)
- OpenAPI spec: [api-docs/openapi.yaml](api-docs/openapi.yaml)

## Configuration
Configuration properties can be found in:
- `src/main/resources/application.yml` - Default configuration
- `src/main/resources/application-dev.yml` - Development configuration
- `src/main/resources/application-prod.yml` - Production configuration

Key configuration properties:
- `server.port` - Service port (default: 8080)
- `spring.datasource.*` - Database connection settings
- `app.*` - Application-specific settings

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Code Coverage
```bash
mvn jacoco:report
```

## Build
```bash
mvn clean package
```

## Deployment
The service is deployed using Kubernetes. Deployment configurations are in the `k8s/` directory.

## Monitoring
- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## Contributing
Please read the main project's contributing guidelines before submitting pull requests.

## License
This project is part of the Social Commerce Ecosystem and follows the same license terms.
