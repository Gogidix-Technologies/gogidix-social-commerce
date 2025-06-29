# Social Media Integration Service Setup Guide

## Prerequisites

### Development Environment Requirements

#### Node.js Runtime
```bash
# Node.js 18.x or later (Required)
node --version
# Expected output: v18.x.x or higher

# npm 9.x or later
npm --version
# Expected output: 9.x.x or higher

# Alternative: Using Yarn
yarn --version
# Expected output: 1.22.x or higher
```

#### Database Requirements
```bash
# MongoDB 6.0+ (Primary Database)
mongod --version
# Expected output: db version v6.0.x

# Redis 7.0+ (Caching and Session Management)
redis-server --version
# Expected output: Redis server v=7.0.x

# PostgreSQL 15+ (Analytics and Reporting)
psql --version
# Expected output: psql (PostgreSQL) 15.x
```

#### Message Queue
```bash
# RabbitMQ 3.12+ or Kafka 3.5+
rabbitmq-diagnostics server_version
# OR
kafka-server-start.sh --version
```

#### Development Tools
```bash
# Git
git --version

# Docker & Docker Compose
docker --version
docker-compose --version

# Visual Studio Code or preferred IDE
code --version
```

## Quick Start Guide

### 1. Clone Repository
```bash
# Clone the repository
git clone https://github.com/exalt/social-media-integration-service.git
cd social-media-integration-service

# Switch to development branch
git checkout develop
```

### 2. Environment Configuration
```bash
# Copy environment template
cp .env.example .env

# Edit configuration
nano .env
```

#### Essential Environment Variables
```env
# Application Configuration
NODE_ENV=development
PORT=3004
SERVICE_NAME=social-media-integration
API_VERSION=v1

# Service Discovery
EUREKA_URL=http://localhost:8761/eureka
SERVICE_REGISTRY_ENABLED=true

# Database Configuration
MONGODB_URI=mongodb://localhost:27017/social_media_integration
MONGODB_DB_NAME=social_media_integration
MONGODB_POOL_SIZE=10

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
REDIS_PASSWORD=

# PostgreSQL Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=social_analytics
POSTGRES_USER=social_user
POSTGRES_PASSWORD=secure_password

# Message Queue
RABBITMQ_URL=amqp://localhost:5672
QUEUE_EXCHANGE=social_media_events
QUEUE_ROUTING_KEY=social.#

# Social Media Platform APIs
FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_APP_SECRET=your_facebook_app_secret
FACEBOOK_WEBHOOK_VERIFY_TOKEN=your_verify_token

INSTAGRAM_CLIENT_ID=your_instagram_client_id
INSTAGRAM_CLIENT_SECRET=your_instagram_client_secret

TWITTER_API_KEY=your_twitter_api_key
TWITTER_API_SECRET=your_twitter_api_secret
TWITTER_BEARER_TOKEN=your_twitter_bearer_token

TIKTOK_APP_ID=your_tiktok_app_id
TIKTOK_APP_SECRET=your_tiktok_app_secret

# Authentication
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRY=24h
REFRESH_TOKEN_EXPIRY=7d

# Security
ENCRYPTION_KEY=your_32_char_encryption_key
CORS_ORIGINS=http://localhost:3000,http://localhost:3001

# Monitoring
ENABLE_METRICS=true
METRICS_PORT=9090
LOG_LEVEL=debug
```

### 3. Install Dependencies
```bash
# Using npm
npm install

# Or using Yarn
yarn install

# Install global dependencies
npm install -g nodemon pm2 typescript
```

### 4. Database Setup
```bash
# Start MongoDB
docker run -d -p 27017:27017 --name social-mongo \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:6

# Start Redis
docker run -d -p 6379:6379 --name social-redis redis:7-alpine

# Start PostgreSQL
docker run -d -p 5432:5432 --name social-postgres \
  -e POSTGRES_USER=social_user \
  -e POSTGRES_PASSWORD=secure_password \
  -e POSTGRES_DB=social_analytics \
  postgres:15-alpine

# Run database migrations
npm run migrate

# Seed initial data
npm run seed
```

### 5. Start Development Server
```bash
# Development mode with hot reload
npm run dev

# Or using nodemon directly
nodemon src/index.js

# Production mode
npm start
```

## Detailed Setup Instructions

### Platform-Specific Configuration

#### Facebook & Instagram Setup
1. **Create Facebook App**
   ```
   1. Visit https://developers.facebook.com
   2. Create new app (Business type)
   3. Add Instagram Basic Display product
   4. Configure OAuth redirect URIs
   5. Generate access tokens
   ```

2. **Configure Webhooks**
   ```javascript
   // webhook-config.js
   module.exports = {
     facebook: {
       verifyToken: process.env.FACEBOOK_WEBHOOK_VERIFY_TOKEN,
       events: ['messages', 'messaging_postbacks', 'feed'],
       callbackUrl: `${process.env.BASE_URL}/webhooks/facebook`
     }
   };
   ```

3. **Test Connection**
   ```bash
   npm run test:facebook-connection
   ```

#### Twitter/X Setup
1. **Create Twitter App**
   ```
   1. Visit https://developer.twitter.com
   2. Apply for Elevated access
   3. Create project and app
   4. Generate API keys and tokens
   5. Configure OAuth 2.0 settings
   ```

2. **Configure Streaming API**
   ```javascript
   // twitter-stream-config.js
   module.exports = {
     rules: [
       { value: 'from:your_brand_handle' },
       { value: '#yourbrand' },
       { value: '@yourbrand' }
     ]
   };
   ```

#### TikTok Setup
1. **Register TikTok Developer Account**
   ```
   1. Visit https://developers.tiktok.com
   2. Create app with required scopes
   3. Configure redirect URIs
   4. Submit for review (production)
   ```

2. **Configure TikTok Integration**
   ```javascript
   // tiktok-config.js
   module.exports = {
     scopes: ['user.info.basic', 'video.list', 'video.upload'],
     redirectUri: `${process.env.BASE_URL}/auth/tiktok/callback`
   };
   ```

### Service Integration Setup

#### Eureka Service Discovery
```javascript
// eureka-config.js
const Eureka = require('eureka-js-client').Eureka;

const client = new Eureka({
  instance: {
    app: 'SOCIAL-MEDIA-INTEGRATION',
    hostName: 'localhost',
    ipAddr: '127.0.0.1',
    port: {
      '$': 3004,
      '@enabled': 'true',
    },
    vipAddress: 'social-media-integration',
    dataCenterInfo: {
      '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
      name: 'MyOwn',
    },
  },
  eureka: {
    host: 'localhost',
    port: 8761,
    servicePath: '/eureka/apps/',
  },
});
```

#### API Gateway Registration
```yaml
# api-gateway-routes.yml
routes:
  - id: social-media-integration
    uri: lb://SOCIAL-MEDIA-INTEGRATION
    predicates:
      - Path=/api/v1/social/**
    filters:
      - RewritePath=/api/v1/social/(?<segment>.*), /${segment}
      - name: CircuitBreaker
        args:
          name: socialMediaCircuitBreaker
          fallbackUri: forward:/fallback/social
```

### Development Workflow

#### 1. Branch Strategy
```bash
# Create feature branch
git checkout -b feature/social-post-scheduler

# Create bugfix branch
git checkout -b bugfix/instagram-auth-fix

# Create hotfix branch
git checkout -b hotfix/rate-limit-issue
```

#### 2. Code Style Setup
```bash
# Install ESLint and Prettier
npm install --save-dev eslint prettier eslint-config-prettier

# Initialize ESLint
npx eslint --init

# Create .prettierrc
echo '{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2
}' > .prettierrc
```

#### 3. Pre-commit Hooks
```bash
# Install Husky
npm install --save-dev husky lint-staged

# Initialize Husky
npx husky install

# Add pre-commit hook
npx husky add .husky/pre-commit "npx lint-staged"

# Configure lint-staged
echo '{
  "*.js": ["eslint --fix", "prettier --write"],
  "*.json": ["prettier --write"]
}' > .lintstagedrc
```

### Testing Setup

#### Unit Testing
```bash
# Install testing dependencies
npm install --save-dev jest @types/jest supertest

# Configure Jest
echo 'module.exports = {
  testEnvironment: "node",
  coverageDirectory: "coverage",
  collectCoverageFrom: [
    "src/**/*.js",
    "!src/index.js"
  ],
  testMatch: ["**/__tests__/**/*.test.js"]
};' > jest.config.js

# Run tests
npm test

# Run with coverage
npm run test:coverage
```

#### Integration Testing
```javascript
// __tests__/integration/social-post.test.js
const request = require('supertest');
const app = require('../../src/app');

describe('Social Media Post Integration', () => {
  test('POST /api/posts/multi-platform', async () => {
    const response = await request(app)
      .post('/api/posts/multi-platform')
      .send({
        content: 'Test post',
        platforms: ['facebook', 'twitter'],
        mediaUrls: ['https://example.com/image.jpg']
      })
      .expect(201);

    expect(response.body).toHaveProperty('postIds');
    expect(response.body.postIds).toHaveProperty('facebook');
    expect(response.body.postIds).toHaveProperty('twitter');
  });
});
```

### IDE Configuration

#### VS Code Settings
```json
// .vscode/settings.json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "eslint.validate": ["javascript"],
  "files.exclude": {
    "node_modules": true,
    "coverage": true
  }
}
```

#### Recommended Extensions
```json
// .vscode/extensions.json
{
  "recommendations": [
    "dbaeumer.vscode-eslint",
    "esbenp.prettier-vscode",
    "christian-kohler.npm-intellisense",
    "eg2.vscode-npm-script",
    "mongodb.mongodb-vscode",
    "ms-azuretools.vscode-docker"
  ]
}
```

### Docker Development Setup

#### Dockerfile
```dockerfile
FROM node:18-alpine

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm ci --only=production

# Copy application
COPY . .

# Create non-root user
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nodejs -u 1001
USER nodejs

# Expose port
EXPOSE 3004

# Start application
CMD ["node", "src/index.js"]
```

#### Docker Compose
```yaml
# docker-compose.yml
version: '3.8'

services:
  social-media-integration:
    build: .
    ports:
      - "3004:3004"
    environment:
      NODE_ENV: development
      MONGODB_URI: mongodb://mongo:27017/social_media
      REDIS_HOST: redis
    depends_on:
      - mongo
      - redis
      - postgres
    volumes:
      - ./src:/app/src
      - ./logs:/app/logs

  mongo:
    image: mongo:6
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  postgres:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: social_analytics
      POSTGRES_USER: social_user
      POSTGRES_PASSWORD: secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  mongo_data:
  postgres_data:
```

### Troubleshooting

#### Common Issues

1. **MongoDB Connection Failed**
   ```bash
   # Check MongoDB status
   docker ps | grep mongo
   
   # View MongoDB logs
   docker logs social-mongo
   
   # Test connection
   mongosh mongodb://localhost:27017
   ```

2. **Social Platform Authentication Errors**
   ```bash
   # Verify API credentials
   npm run verify:credentials
   
   # Check token expiration
   npm run check:tokens
   
   # Refresh tokens
   npm run refresh:tokens
   ```

3. **Rate Limiting Issues**
   ```javascript
   // config/rate-limits.js
   module.exports = {
     facebook: { requests: 200, window: 3600 },
     twitter: { requests: 300, window: 900 },
     instagram: { requests: 200, window: 3600 },
     tiktok: { requests: 100, window: 600 }
   };
   ```

### Performance Optimization

#### Caching Strategy
```javascript
// cache-config.js
module.exports = {
  redis: {
    ttl: {
      userProfile: 3600,      // 1 hour
      platformPosts: 300,     // 5 minutes
      analytics: 1800,        // 30 minutes
      accessTokens: 86400     // 24 hours
    }
  }
};
```

#### Database Indexes
```javascript
// migrations/create-indexes.js
module.exports = {
  up: async (db) => {
    // Social posts indexes
    await db.collection('social_posts').createIndex({ platformId: 1, platform: 1 });
    await db.collection('social_posts').createIndex({ createdAt: -1 });
    await db.collection('social_posts').createIndex({ userId: 1, platform: 1 });
    
    // Analytics indexes
    await db.collection('analytics').createIndex({ postId: 1, metric: 1 });
    await db.collection('analytics').createIndex({ timestamp: -1 });
  }
};
```

## Next Steps

1. **Complete Platform Setup**
   - Obtain all required API credentials
   - Configure webhook endpoints
   - Test platform connections

2. **Configure Monitoring**
   - Set up application metrics
   - Configure log aggregation
   - Enable performance monitoring

3. **Security Hardening**
   - Enable rate limiting
   - Configure CORS properly
   - Set up API authentication

4. **Integration Testing**
   - Test service discovery
   - Verify API gateway routing
   - Test inter-service communication

## Support Resources

- **Documentation**: `/docs/API.md`
- **Architecture Guide**: `/docs/ARCHITECTURE.md`
- **Operations Manual**: `/docs/OPERATIONS.md`
- **Support Email**: social-media-team@exalt.com
- **Slack Channel**: #social-media-integration