# Social Media Integration Service API Documentation

## Overview

The Social Media Integration Service provides a unified API for integrating with various social media platforms. It supports OAuth authentication, social sharing, activity feeds, and engagement analytics.

## Base URL

```
http://localhost:8080
```

## Authentication

All API endpoints (except OAuth endpoints and webhooks) require authentication via JWT token.

Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Health Check

```
GET /health
```

Returns the current status of the service.

```
GET /health/details
```

Returns detailed information about the service, including system resources and connected components.

### OAuth Authentication

#### Initiate OAuth Flow

```
GET /auth/:platform
```

Redirects the user to the OAuth authorization page for the specified platform.

Supported platforms:
- facebook
- twitter
- instagram
- pinterest
- tiktok

#### OAuth Callback

```
GET /auth/:platform/callback
```

Callback endpoint for OAuth providers to return authorization codes.

#### Get Connected Accounts

```
GET /auth/accounts
```

Returns a list of the user's connected social media accounts.

#### Disconnect an Account

```
DELETE /auth/accounts/:platform
```

Disconnects a social media account.

#### Validate Token

```
GET /auth/validate/:platform
```

Validates if the OAuth token for a platform is still valid.

#### Refresh Token

```
POST /auth/refresh/:platform
```

Refreshes an expired OAuth token.

### Social Sharing

#### Share Content

```
POST /share/content
```

Shares content to a social media platform.

**Request Body:**
```json
{
  "platform": "facebook",
  "contentData": {
    "type": "product",
    "id": "123",
    "name": "Product Name",
    "description": "Product description",
    "image": "https://example.com/image.jpg",
    "url": "https://example.com/product/123"
  },
  "message": "Check out this product!"
}
```

#### Generate Shareable Link

```
POST /share/link
```

Generates a tracking link for content sharing.

**Request Body:**
```json
{
  "contentData": {
    "type": "product",
    "id": "123",
    "name": "Product Name",
    "description": "Product description",
    "image": "https://example.com/image.jpg"
  },
  "platform": "facebook"
}
```

#### Get Share Statistics

```
GET /share/stats/:contentType/:contentId
```

Returns sharing statistics for specific content.

### Activity Feed

#### Add Activity

```
POST /activity
```

Adds an activity to the user's feed.

**Request Body:**
```json
{
  "type": "product_view",
  "targetData": {
    "type": "product",
    "id": "123",
    "name": "Product Name",
    "image": "https://example.com/image.jpg"
  },
  "platform": "app",
  "visibility": "public",
  "metadata": {
    "additionalInfo": "value"
  }
}
```

#### Get User Activities

```
GET /activity/user
```

Returns activities for the authenticated user.

**Query Parameters:**
- `limit`: Number of results (default: 20)
- `skip`: Number of results to skip (default: 0)
- `types`: Comma-separated list of activity types
- `platforms`: Comma-separated list of platforms
- `startDate`: Start date filter (ISO format)
- `endDate`: End date filter (ISO format)
- `visibility`: Comma-separated list of visibility settings

#### Get Target Activities

```
GET /activity/target/:targetType/:targetId
```

Returns activities related to a specific target (e.g., product, vendor).

#### Get User Feed

```
GET /activity/feed
```

Returns a personalized feed of activities for the user.

#### Delete Activity

```
DELETE /activity/:activityId
```

Deletes an activity from the feed.

#### Update Activity Visibility

```
PATCH /activity/:activityId/visibility
```

Updates the visibility of an activity.

**Request Body:**
```json
{
  "visibility": "private"
}
```

### Analytics

#### Track Engagement

```
POST /analytics/engagement
```

Tracks a user engagement event.

**Request Body:**
```json
{
  "entityType": "product",
  "entityId": "123",
  "metricType": "view",
  "platform": "app",
  "duration": 30,
  "device": {
    "type": "mobile",
    "browser": "Chrome",
    "os": "Android"
  }
}
```

#### Get Entity Metrics

```
GET /analytics/entity/:entityType/:entityId
```

Returns engagement metrics for a specific entity.

**Query Parameters:**
- `startDate`: Start date (ISO format)
- `endDate`: End date (ISO format)

#### Get Metrics by Platform

```
GET /analytics/platform/:entityType/:entityId
```

Returns metrics broken down by platform.

#### Get User Activity Metrics

```
GET /analytics/user
```

Returns metrics for the authenticated user's activities.

#### Get Trending Entities

```
GET /analytics/trending/:entityType
```

Returns trending entities based on engagement metrics.

**Query Parameters:**
- `limit`: Number of results (default: 10)
- `period`: Number of days to analyze (default: 7)
- `metricTypes`: Comma-separated list of metric types

#### Get Platform Performance

```
GET /analytics/performance
```

Returns performance comparison across social platforms (admin only).

### Webhooks

The service provides webhook endpoints for each social platform to receive real-time updates:

```
GET/POST /webhook/:platform
```

Where `:platform` is one of:
- facebook
- instagram
- twitter
- pinterest
- tiktok
- whatsapp

## Error Handling

All API endpoints return errors in a consistent format:

```json
{
  "success": false,
  "message": "Error message"
}
```

HTTP status codes:
- 200: Success
- 400: Bad request
- 401: Unauthorized
- 403: Forbidden
- 404: Not found
- 500: Internal server error

## Rate Limiting

API endpoints are rate limited to protect the service from abuse. The limits are:
- 100 requests per minute for authenticated users
- 20 requests per minute for unauthenticated users 