# Social Media Integration Service

## Overview

The Social Media Integration Service provides a comprehensive solution for integrating social media platforms into the Micro-Social-Ecommerce ecosystem. This service enables social login capabilities, content sharing across platforms, activity feed management, and user engagement analytics.

## Features

- **OAuth2 Integration**: Connect with Facebook, Twitter, Instagram, Pinterest, and TikTok
- **Social Sharing**: Share products and content across multiple social platforms
- **Activity Feed**: Track and display user activities in a personalized feed
- **User Engagement Analytics**: Measure and analyze social interactions and engagement

## Tech Stack

- **Node.js**: Runtime environment
- **Express**: Web framework
- **MongoDB**: Database for storing social accounts and activity data
- **Redis**: Optional caching layer
- **JWT**: Authentication and token handling
- **REST API**: Standard interface for service integration

## Getting Started

### Prerequisites

- Node.js (v14+)
- MongoDB
- Redis (optional)
- Social platform developer accounts with API keys

### Installation

1. Clone the repository
2. Install dependencies:
   ```
   npm install
   ```
3. Copy the `.env.example` file to `.env` and update with your configuration
4. Start the service:
   ```
   npm start
   ```

### Development Mode

```
npm run dev
```

## API Documentation

API documentation can be found in [docs/API_DOCUMENTATION.md](./docs/API_DOCUMENTATION.md).

## Integration Guide

For information on how to integrate with this service, see [docs/INTEGRATION_GUIDE.md](./docs/INTEGRATION_GUIDE.md).

## Service Architecture

### Models

- **SocialAccount**: Stores user social media connections
- **Activity**: Tracks user activities in the feed
- **EngagementMetric**: Tracks user engagement analytics
- **SocialShare**: Tracks content sharing across platforms

### Services

- **OAuthService**: Handles social login integrations
- **SharingService**: Manages sharing content across social platforms
- **ActivityFeedService**: Manages user activity feeds
- **AnalyticsService**: Analyzes user engagement metrics

### Controllers

- **OAuthController**: Endpoints for social login and account management
- **SharingController**: Endpoints for content sharing functionality
- **ActivityFeedController**: Endpoints for activity feed operations
- **AnalyticsController**: Endpoints for engagement analytics
- **WebhookController**: Endpoints for receiving social platform events
- **HealthController**: Service health check endpoints

## Configuration

The service is configured through environment variables. See `.env.example` for all available options.

Key configuration areas:

- **Platform OAuth Credentials**: Client IDs and secrets for each social platform
- **Database Connection**: MongoDB URI for data storage
- **Security Settings**: JWT settings and token verification
- **Service Discovery**: URLs for other services in the ecosystem

## Testing

Run the test suite:

```
npm test
```

Run tests with coverage:

```
npm run test:coverage
```

## Docker Support

Build the Docker image:

```
docker build -t social-media-integration .
```

Run with Docker Compose:

```
docker-compose up
```

## Kubernetes Deployment

Kubernetes manifests are available in the `k8s` directory.

Deploy to Kubernetes:

```
kubectl apply -f k8s/
```

## Contributing

Please read [CONTRIBUTING.md](../../CONTRIBUTING.md) for details on our code of conduct and contribution guidelines.

## License

This project is licensed under the MIT License.

## Support

For any questions or issues, please contact the development team.