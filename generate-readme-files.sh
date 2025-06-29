#!/bin/bash

# Script to generate README.md files for services missing documentation

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Function to generate README for Java service
generate_java_readme() {
    local service_name=$1
    local service_path=$2
    
    cat > "$service_path/README.md" << EOF
# ${service_name}

## Overview
${service_name} is a microservice component of the Social Commerce Ecosystem that handles [brief description needed].

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
   \`\`\`bash
   cd ${service_name}
   \`\`\`

3. Install dependencies:
   \`\`\`bash
   mvn clean install
   \`\`\`

4. Run the service locally:
   \`\`\`bash
   mvn spring-boot:run
   \`\`\`

### Docker Development
Build and run with Docker:
\`\`\`bash
docker-compose up --build
\`\`\`

## API Documentation
API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html (when running locally)
- OpenAPI spec: [api-docs/openapi.yaml](api-docs/openapi.yaml)

## Configuration
Configuration properties can be found in:
- \`src/main/resources/application.yml\` - Default configuration
- \`src/main/resources/application-dev.yml\` - Development configuration
- \`src/main/resources/application-prod.yml\` - Production configuration

Key configuration properties:
- \`server.port\` - Service port (default: 8080)
- \`spring.datasource.*\` - Database connection settings
- \`app.*\` - Application-specific settings

## Testing

### Unit Tests
\`\`\`bash
mvn test
\`\`\`

### Integration Tests
\`\`\`bash
mvn verify
\`\`\`

### Code Coverage
\`\`\`bash
mvn jacoco:report
\`\`\`

## Build
\`\`\`bash
mvn clean package
\`\`\`

## Deployment
The service is deployed using Kubernetes. Deployment configurations are in the \`k8s/\` directory.

## Monitoring
- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## Contributing
Please read the main project's contributing guidelines before submitting pull requests.

## License
This project is part of the Social Commerce Ecosystem and follows the same license terms.
EOF
}

# Function to generate README for Frontend service
generate_frontend_readme() {
    local service_name=$1
    local service_path=$2
    local is_react_native=$3
    
    if [ "$is_react_native" = "true" ]; then
        cat > "$service_path/README.md" << EOF
# ${service_name}

## Overview
${service_name} is a React Native mobile application for the Social Commerce Ecosystem.

## Technology Stack
- React Native 0.71.7
- React 18.2.0
- Redux Toolkit
- React Navigation
- TypeScript
- Formik & Yup for forms

## Prerequisites
- Node.js 18+
- npm or yarn
- React Native development environment setup
- Android Studio (for Android development)
- Xcode (for iOS development on macOS)

## Getting Started

### Installation
1. Clone the repository
2. Navigate to the app directory:
   \`\`\`bash
   cd ${service_name}
   \`\`\`

3. Install dependencies:
   \`\`\`bash
   npm install
   # or
   yarn install
   \`\`\`

4. Install iOS dependencies (macOS only):
   \`\`\`bash
   cd ios && pod install
   \`\`\`

### Running the App

#### Android
\`\`\`bash
npm run android
# or
yarn android
\`\`\`

#### iOS (macOS only)
\`\`\`bash
npm run ios
# or
yarn ios
\`\`\`

#### Metro Bundler
Start the Metro bundler separately:
\`\`\`bash
npm start
# or
yarn start
\`\`\`

## Development

### Code Style
\`\`\`bash
npm run lint
\`\`\`

### Testing
\`\`\`bash
npm test
\`\`\`

### Building for Production

#### Android
\`\`\`bash
npm run build:android
\`\`\`

The APK will be generated in \`android/app/build/outputs/apk/release/\`

#### iOS
Build through Xcode or use:
\`\`\`bash
cd ios && xcodebuild -workspace [WorkspaceName].xcworkspace -scheme [SchemeName] -configuration Release
\`\`\`

## Project Structure
\`\`\`
src/
├── components/     # Reusable components
├── screens/        # Screen components
├── navigation/     # Navigation configuration
├── store/          # Redux store and slices
├── services/       # API services
├── utils/          # Utility functions
└── types/          # TypeScript type definitions
\`\`\`

## Contributing
Please read the main project's contributing guidelines before submitting pull requests.

## License
This project is part of the Social Commerce Ecosystem and follows the same license terms.
EOF
    else
        cat > "$service_path/README.md" << EOF
# ${service_name}

## Overview
${service_name} is a web application component of the Social Commerce Ecosystem.

## Technology Stack
- React 18.x
- TypeScript
- Redux Toolkit (if applicable)
- React Router
- Axios for API calls
- Material-UI or Ant Design (as applicable)

## Prerequisites
- Node.js 18+
- npm or yarn

## Getting Started

### Installation
1. Clone the repository
2. Navigate to the app directory:
   \`\`\`bash
   cd ${service_name}
   \`\`\`

3. Install dependencies:
   \`\`\`bash
   npm install
   # or
   yarn install
   \`\`\`

### Development
Start the development server:
\`\`\`bash
npm start
# or
yarn start
\`\`\`

The application will be available at http://localhost:3000

## Available Scripts

- \`npm start\` - Runs the app in development mode
- \`npm test\` - Launches the test runner
- \`npm run build\` - Builds the app for production
- \`npm run lint\` - Runs the linter
- \`npm run format\` - Formats the code

## Build
Build for production:
\`\`\`bash
npm run build
\`\`\`

The build artifacts will be stored in the \`build/\` or \`dist/\` directory.

## Testing

### Unit Tests
\`\`\`bash
npm test
\`\`\`

### E2E Tests
\`\`\`bash
npm run test:e2e
\`\`\`

## Environment Variables
Create a \`.env\` file in the root directory:
\`\`\`
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_ENV=development
\`\`\`

## Project Structure
\`\`\`
src/
├── components/     # Reusable components
├── pages/          # Page components
├── routes/         # Routing configuration
├── services/       # API services
├── store/          # State management
├── utils/          # Utility functions
├── types/          # TypeScript types
└── styles/         # Global styles
\`\`\`

## Docker
Build and run with Docker:
\`\`\`bash
docker build -t ${service_name} .
docker run -p 3000:3000 ${service_name}
\`\`\`

## Contributing
Please read the main project's contributing guidelines before submitting pull requests.

## License
This project is part of the Social Commerce Ecosystem and follows the same license terms.
EOF
    fi
}

# Generate README files for Java services missing them
echo "Generating README files for Java services..."
for service in admin-finalization admin-interfaces api-gateway integration-optimization \
               integration-performance invoice-service multi-currency-service payment-gateway \
               payout-service product-service regional-admin social-commerce-production \
               social-commerce-staging subscription-service analytics-service; do
    if [ -d "$service" ] && [ ! -f "$service/README.md" ]; then
        echo "Creating README for $service"
        generate_java_readme "$service" "$service"
    fi
done

# Generate README files for Frontend services missing them
echo "Generating README files for frontend services..."
if [ -d "user-mobile-app" ] && [ ! -f "user-mobile-app/README.md" ]; then
    echo "Creating README for user-mobile-app"
    generate_frontend_readme "user-mobile-app" "user-mobile-app" "true"
fi

if [ -d "user-web-app" ] && [ ! -f "user-web-app/README.md" ]; then
    echo "Creating README for user-web-app"
    generate_frontend_readme "user-web-app" "user-web-app" "false"
fi

echo "README generation completed!"