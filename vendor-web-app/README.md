# vendor-web-app

Part of the Social E-commerce Ecosystem - social-commerce domain

## Description

Vendor Web Application frontend service for the Social E-commerce Ecosystem. A React-based web interface for vendors to manage their marketplace presence, products, orders, and analytics.

## Technology Stack

- **Frontend**: React 18 + TypeScript
- **Build Tool**: Vite/Webpack
- **UI Framework**: Material-UI / Tailwind CSS
- **State Management**: Redux Toolkit / Zustand
- **HTTP Client**: Axios
- **Testing**: Jest + React Testing Library

## Development

### Prerequisites
- Node.js 18+
- npm or yarn
- Docker (optional)

### Setup
1. Clone this repository
2. Install dependencies: `npm install`
3. Start development server: `npm start`
4. Or run with Docker: `docker-compose up`

### Available Scripts
- `npm start` - Start development server
- `npm run build` - Build for production
- `npm test` - Run unit tests
- `npm run test:e2e` - Run end-to-end tests
- `npm run lint` - Run linting
- `npm run type-check` - TypeScript type checking

### Testing
- Unit tests: `npm test`
- Integration tests: `npm run test:integration`
- E2E tests: `npm run test:e2e`
- Performance tests: `npm run test:performance`

## Features

- **Vendor Dashboard**: Sales metrics, order statistics, performance analytics
- **Product Management**: Add, edit, delete products with rich media support
- **Order Processing**: View and manage customer orders, fulfillment status
- **Analytics & Reporting**: Revenue tracking, customer insights, market trends
- **Profile Management**: Vendor account settings, store configuration
- **Responsive Design**: Mobile-first responsive web interface

## API Documentation

API documentation can be found in the `api-docs` directory.

## Deployment

Deployment instructions can be found in the `docs/operations` directory.

## Environment Variables

Create a `.env` file with the following variables:
```
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_ENVIRONMENT=development
REACT_APP_VENDOR_PORTAL_URL=https://vendor.gogidix-ecosystem.com
```

## Architecture

This application follows a component-based architecture with:
- Feature-based folder structure
- Reusable UI components
- Centralized state management
- API service layer abstraction
- Route-based code splitting

## Contributing

1. Follow the existing code style
2. Write tests for new features
3. Update documentation as needed
4. Ensure all tests pass before submitting PR