# Vendor Web App API Documentation

## Overview

The Vendor Web App API provides endpoints for vendor management, product operations, order processing, and analytics within the Social E-commerce Ecosystem.

## Base URL

```
Development: http://localhost:8080/api/v1
Production: https://api.gogidix-ecosystem.com/vendor-web/api/v1
```

## Authentication

All API requests require authentication using JWT tokens in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Endpoints

### Authentication

#### POST /auth/login
Login vendor user

**Request Body:**
```json
{
  "email": "vendor@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "jwt_token_here",
  "user": {
    "id": "vendor_id",
    "email": "vendor@example.com",
    "name": "Vendor Name",
    "storeId": "store_id"
  }
}
```

#### POST /auth/logout
Logout current user

### Dashboard

#### GET /dashboard/metrics
Retrieve vendor dashboard metrics

**Response:**
```json
{
  "totalSales": 15420.50,
  "totalOrders": 148,
  "conversionRate": 3.2,
  "averageOrderValue": 104.19,
  "topProducts": [...],
  "recentOrders": [...]
}
```

### Products

#### GET /products
List vendor products

**Query Parameters:**
- `page` (number): Page number
- `limit` (number): Items per page
- `category` (string): Filter by category
- `status` (string): Filter by status

**Response:**
```json
{
  "products": [...],
  "total": 45,
  "page": 1,
  "totalPages": 5
}
```

#### POST /products
Create new product

**Request Body:**
```json
{
  "name": "Product Name",
  "description": "Product description",
  "price": 29.99,
  "category": "electronics",
  "images": ["image1.jpg", "image2.jpg"],
  "inventory": 100
}
```

#### PUT /products/:id
Update existing product

#### DELETE /products/:id
Delete product

### Orders

#### GET /orders
List vendor orders

**Query Parameters:**
- `status` (string): Filter by order status
- `dateFrom` (string): Start date filter
- `dateTo` (string): End date filter

**Response:**
```json
{
  "orders": [...],
  "total": 148,
  "pagination": {...}
}
```

#### GET /orders/:id
Get order details

#### PUT /orders/:id/status
Update order status

### Analytics

#### GET /analytics/revenue
Get revenue analytics

**Query Parameters:**
- `period` (string): daily, weekly, monthly, yearly
- `startDate` (string): Start date
- `endDate` (string): End date

**Response:**
```json
{
  "revenue": {
    "total": 15420.50,
    "growth": 12.5,
    "breakdown": [...]
  }
}
```

#### GET /analytics/customers
Get customer analytics

#### GET /analytics/products
Get product performance analytics

## Error Handling

All endpoints return consistent error responses:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "field": "email",
      "reason": "Invalid email format"
    }
  }
}
```

## HTTP Status Codes

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `422` - Unprocessable Entity
- `500` - Internal Server Error

## Rate Limiting

API requests are limited to 1000 requests per hour per vendor.

## Webhooks

The API supports webhooks for real-time notifications:

- Order status changes
- Payment confirmations
- Inventory alerts
- Customer reviews

Configure webhooks in the vendor portal settings.