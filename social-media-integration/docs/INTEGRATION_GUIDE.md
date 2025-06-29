# Social Media Integration Service - Integration Guide

This guide explains how to integrate with the Social Media Integration Service from other microservices in the ecosystem.

## Integration Methods

There are three main ways to integrate with the Social Media Integration Service:

1. **REST API Calls**: Direct HTTP requests to the service's REST endpoints
2. **Client Library**: Using the provided client library (if available)
3. **Event-based Integration**: Subscribing to events published by the service

## REST API Integration

### Authentication

All API requests must include a valid JWT token in the Authorization header:

```
Authorization: Bearer <token>
```

### Service Discovery

The Social Media Integration Service can be discovered using the Service Registry:

```
GET /service-registry/services?name=social-media-integration
```

### Common API Endpoints

See the [API Documentation](./API_DOCUMENTATION.md) for detailed endpoint information.

### Sample Integration Code (Node.js)

```javascript
const axios = require('axios');

class SocialMediaClient {
  constructor(baseUrl, authToken) {
    this.baseUrl = baseUrl;
    this.authToken = authToken;
  }
  
  async getUserSocialAccounts() {
    try {
      const response = await axios.get(`${this.baseUrl}/auth/accounts`, {
        headers: {
          'Authorization': `Bearer ${this.authToken}`
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error getting social accounts:', error.message);
      throw error;
    }
  }
  
  async shareProductToSocial(userId, platform, product, message) {
    try {
      const contentData = {
        type: 'product',
        id: product.id,
        name: product.name,
        description: product.description,
        image: product.imageUrl,
        url: product.productUrl
      };
      
      const response = await axios.post(`${this.baseUrl}/share/content`, {
        platform,
        contentData,
        message
      }, {
        headers: {
          'Authorization': `Bearer ${this.authToken}`
        }
      });
      
      return response.data;
    } catch (error) {
      console.error(`Error sharing to ${platform}:`, error.message);
      throw error;
    }
  }
  
  async addActivityToFeed(userId, type, targetData, platform = 'app') {
    try {
      const response = await axios.post(`${this.baseUrl}/activity`, {
        type,
        targetData,
        platform,
        visibility: 'public'
      }, {
        headers: {
          'Authorization': `Bearer ${this.authToken}`
        }
      });
      
      return response.data;
    } catch (error) {
      console.error('Error adding activity:', error.message);
      throw error;
    }
  }
  
  async trackEngagement(engagementData) {
    try {
      const response = await axios.post(`${this.baseUrl}/analytics/engagement`, 
        engagementData,
        {
          headers: {
            'Authorization': `Bearer ${this.authToken}`
          }
        }
      );
      
      return response.data;
    } catch (error) {
      console.error('Error tracking engagement:', error.message);
      throw error;
    }
  }
}

// Usage example
async function exampleUsage() {
  const client = new SocialMediaClient(
    'http://social-media-integration:8080',
    'user-jwt-token'
  );
  
  // Get user's connected social accounts
  const accounts = await client.getUserSocialAccounts();
  console.log('Connected accounts:', accounts);
  
  // Share a product to Facebook
  const product = {
    id: '123',
    name: 'Awesome Product',
    description: 'This is an amazing product',
    imageUrl: 'https://example.com/product123.jpg',
    productUrl: 'https://example.com/products/123'
  };
  
  const shareResult = await client.shareProductToSocial(
    'user123',
    'facebook',
    product,
    'Check out this awesome product!'
  );
  console.log('Share result:', shareResult);
  
  // Add product view to activity feed
  const activity = await client.addActivityToFeed(
    'user123',
    'product_view',
    {
      type: 'product',
      id: product.id,
      name: product.name,
      image: product.imageUrl
    }
  );
  console.log('Activity added:', activity);
  
  // Track engagement
  await client.trackEngagement({
    entityType: 'product',
    entityId: product.id,
    metricType: 'view',
    userId: 'user123',
    platform: 'app'
  });
}
```

## Event-based Integration

The Social Media Integration Service publishes events to a message broker that other services can subscribe to.

### Common Events

1. **social.account.connected**: When a user connects a social media account
2. **social.content.shared**: When content is shared to a social platform
3. **social.activity.added**: When a new activity is added to the feed
4. **social.engagement.tracked**: When engagement metrics are recorded

### Sample Event Consumer (Node.js with Kafka)

```javascript
const { Kafka } = require('kafkajs');

const kafka = new Kafka({
  clientId: 'your-service',
  brokers: ['kafka:9092']
});

const consumer = kafka.consumer({ groupId: 'social-events-group' });

async function startConsumer() {
  await consumer.connect();
  
  // Subscribe to social media events
  await consumer.subscribe({ 
    topics: [
      'social.account.connected',
      'social.content.shared',
      'social.activity.added'
    ] 
  });
  
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      const eventData = JSON.parse(message.value.toString());
      
      switch (topic) {
        case 'social.account.connected':
          handleSocialAccountConnected(eventData);
          break;
        case 'social.content.shared':
          handleContentShared(eventData);
          break;
        case 'social.activity.added':
          handleActivityAdded(eventData);
          break;
      }
    },
  });
}

function handleSocialAccountConnected(eventData) {
  const { userId, platform, timestamp } = eventData;
  console.log(`User ${userId} connected ${platform} account at ${timestamp}`);
  // Update user profile, send notification, etc.
}

function handleContentShared(eventData) {
  const { userId, platform, contentType, contentId, timestamp } = eventData;
  console.log(`User ${userId} shared ${contentType}:${contentId} on ${platform}`);
  // Update share counter, trigger reward, etc.
}

function handleActivityAdded(eventData) {
  const { userId, type, target, timestamp } = eventData;
  console.log(`User ${userId} activity: ${type} - ${target.type}:${target.id}`);
  // Update notification feed, process activity, etc.
}

startConsumer().catch(console.error);
```

## OAuth Integration

To add social login buttons to your frontend application:

### Frontend Integration (React Example)

```jsx
import React from 'react';

function SocialLoginButtons() {
  const socialMediaService = 'http://localhost:8080';
  
  return (
    <div className="social-login-buttons">
      <h3>Connect with Social Media</h3>
      
      <button 
        onClick={() => window.location.href = `${socialMediaService}/auth/facebook`}
        className="facebook-button"
      >
        Connect with Facebook
      </button>
      
      <button 
        onClick={() => window.location.href = `${socialMediaService}/auth/twitter`}
        className="twitter-button"
      >
        Connect with Twitter
      </button>
      
      <button 
        onClick={() => window.location.href = `${socialMediaService}/auth/instagram`}
        className="instagram-button"
      >
        Connect with Instagram
      </button>
      
      <button 
        onClick={() => window.location.href = `${socialMediaService}/auth/pinterest`}
        className="pinterest-button"
      >
        Connect with Pinterest
      </button>
    </div>
  );
}

export default SocialLoginButtons;
```

## Service Configuration

Ensure the following environment variables are set in your service to enable integration:

```
SOCIAL_MEDIA_SERVICE_URL=http://social-media-integration:8080
SOCIAL_MEDIA_KAFKA_TOPIC_PREFIX=social
```

## Best Practices

1. **Handle Rate Limiting**: The Social Media Integration Service has rate limits. Implement exponential backoff in case of rate limit errors.

2. **Graceful Degradation**: Design your service to function if the Social Media Integration Service is unavailable.

3. **Use Server-Side Tokens**: Never expose social media access tokens to frontend clients.

4. **Secure Token Management**: Always store and transmit tokens securely.

5. **Respect User Privacy**: Only share content that the user has explicitly approved for sharing.

## Troubleshooting

Common issues and solutions:

1. **Authentication Errors**: Ensure your JWT token is valid and not expired.

2. **Rate Limit Exceeded**: Reduce request frequency or spread requests over time.

3. **Integration Timeout**: Check the Social Media Service health endpoint to verify it's operational.

4. **Missing OAuth Credentials**: Ensure platform-specific client IDs and secrets are configured in the service environment. 