# Admin Interfaces Service API Documentation

## Overview

The Admin Interfaces Service provides comprehensive REST APIs for administrative management across the Social E-commerce Ecosystem. These APIs enable regional administrators, global HQ staff, and content moderators to efficiently manage all aspects of the platform through programmatic interfaces and web-based dashboards.

## Base URL

```
http://localhost:8082/api/v1/admin-interfaces
```

## Authentication

All endpoints require JWT authentication via Authorization header:
```
Authorization: Bearer <jwt-token>
```

### Role-Based Access Control

Different endpoints require specific administrative roles:
- **GLOBAL_ADMIN**: Full platform access across all regions
- **REGIONAL_ADMIN**: Access limited to specific regions
- **CONTENT_MODERATOR**: Content review and moderation access
- **VENDOR_MANAGER**: Vendor management and onboarding access
- **USER_SUPPORT**: Customer support and user management access

## Core API Endpoints

### Health and Monitoring

#### GET /health
Returns service health status and component availability.

**Required Role**: Any authenticated admin

**Response (200 OK):**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00Z",
  "components": {
    "database": {"status": "UP", "responseTime": "12ms"},
    "cache": {"status": "UP", "responseTime": "2ms"},
    "externalServices": {"status": "UP", "responseTime": "45ms"}
  },
  "activeAdmins": 23,
  "pendingTasks": 15
}
```

#### GET /metrics
Returns administrative metrics and KPIs.

**Required Role**: GLOBAL_ADMIN, REGIONAL_ADMIN

## Regional Administration API

### Get Regional Overview

#### GET /regional/{regionCode}/overview

Retrieves comprehensive regional administrative overview.

**Required Role**: GLOBAL_ADMIN, REGIONAL_ADMIN (for own region)

**Path Parameters:**
- `regionCode` (string, required): Region identifier (e.g., "EU-WEST", "AFRICA-NORTH")

**Response (200 OK):**
```json
{
  "regionCode": "EU-WEST",
  "regionName": "Western Europe",
  "overview": {
    "totalVendors": 1250,
    "activeUsers": 45000,
    "pendingOrders": 320,
    "monthlyRevenue": 2500000.00,
    "growthRate": 15.2
  },
  "alerts": [
    {
      "type": "WARNING",
      "message": "High volume of pending vendor approvals",
      "count": 25,
      "priority": "MEDIUM"
    }
  ],
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

### Regional Vendor Management

#### GET /regional/{regionCode}/vendors

Retrieves vendors for a specific region with filtering and pagination.

**Query Parameters:**
- `status` (string, optional): Filter by vendor status
- `category` (string, optional): Filter by business category
- `page` (int, optional, default: 0): Page number
- `size` (int, optional, default: 20): Page size
- `sortBy` (string, optional, default: "createdAt"): Sort field
- `sortDirection` (string, optional, default: "DESC"): Sort direction

**Response (200 OK):**
```json
{
  "content": [
    {
      "vendorId": "VENDOR_12345",
      "businessName": "Fashion Plus Ltd",
      "status": "ACTIVE",
      "category": "FASHION",
      "registrationDate": "2024-01-10T09:00:00Z",
      "monthlyRevenue": 25000.00,
      "customerRating": 4.5,
      "complianceScore": 95,
      "lastActivity": "2024-01-15T08:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1250,
    "totalPages": 63
  }
}
```

#### POST /regional/{regionCode}/vendors/{vendorId}/approve

Approve a vendor application.

**Path Parameters:**
- `regionCode` (string, required): Region identifier
- `vendorId` (string, required): Vendor identifier

**Request Body:**
```json
{
  "approvalReason": "All documentation verified and compliant",
  "complianceNotes": "Meets all regional requirements",
  "approvedCategories": ["FASHION", "ACCESSORIES"],
  "restrictions": [],
  "approvedBy": "admin@example.com"
}
```

**Response (200 OK):**
```json
{
  "vendorId": "VENDOR_12345",
  "status": "APPROVED",
  "approvalDate": "2024-01-15T10:30:00Z",
  "approvedBy": "admin@example.com",
  "activationDate": "2024-01-16T00:00:00Z"
}
```

## Global HQ Administration API

### Global Dashboard

#### GET /global/dashboard

Retrieves comprehensive global platform dashboard data.

**Required Role**: GLOBAL_ADMIN

**Response (200 OK):**
```json
{
  "globalMetrics": {
    "totalRevenue": 50000000.00,
    "totalVendors": 15000,
    "totalUsers": 500000,
    "totalOrders": 1250000,
    "averageOrderValue": 85.50
  },
  "regionalBreakdown": [
    {
      "regionCode": "EU-WEST",
      "revenue": 20000000.00,
      "vendors": 6000,
      "users": 200000,
      "growthRate": 15.2
    },
    {
      "regionCode": "EU-EAST",
      "revenue": 15000000.00,
      "vendors": 4500,
      "users": 150000,
      "growthRate": 22.1
    }
  ],
  "trends": {
    "revenueGrowth": 18.5,
    "userGrowth": 25.3,
    "vendorGrowth": 12.8
  },
  "alerts": [
    {
      "severity": "HIGH",
      "message": "Unusual spike in chargebacks in AFRICA-NORTH region",
      "actionRequired": true
    }
  ]
}
```

### Cross-Regional Analytics

#### GET /global/analytics/comparison

Compare metrics across regions.

**Query Parameters:**
- `metric` (string, required): Metric to compare (revenue, users, vendors, orders)
- `period` (string, optional, default: "monthly"): Time period (daily, weekly, monthly)
- `months` (int, optional, default: 6): Number of months to include
- `regions` (string[], optional): Specific regions to include

**Response (200 OK):**
```json
{
  "metric": "revenue",
  "period": "monthly",
  "data": [
    {
      "month": "2023-12",
      "regions": {
        "EU-WEST": 2500000.00,
        "EU-EAST": 1800000.00,
        "AFRICA-NORTH": 950000.00,
        "AFRICA-SOUTH": 750000.00
      }
    }
  ],
  "insights": {
    "topPerformingRegion": "EU-WEST",
    "fastestGrowingRegion": "EU-EAST",
    "recommendations": ["Focus marketing in AFRICA regions", "Expand vendor base in EU-EAST"]
  }
}
```

## Content Moderation API

### Pending Content Review

#### GET /content/pending

Retrieve content items pending moderation review.

**Required Role**: CONTENT_MODERATOR, GLOBAL_ADMIN

**Query Parameters:**
- `contentType` (string, optional): Filter by content type (PRODUCT, REVIEW, COMMENT, MEDIA)
- `priority` (string, optional): Filter by priority (LOW, MEDIUM, HIGH, URGENT)
- `region` (string, optional): Filter by region
- `flagReason` (string, optional): Filter by flagging reason

**Response (200 OK):**
```json
{
  "content": [
    {
      "contentId": "CONTENT_98765",
      "contentType": "PRODUCT",
      "title": "Designer Handbag Collection",
      "vendorId": "VENDOR_12345",
      "region": "EU-WEST",
      "flaggedAt": "2024-01-15T09:15:00Z",
      "flagReason": "POTENTIAL_COUNTERFEIT",
      "priority": "HIGH",
      "aiConfidence": 0.85,
      "reviewDeadline": "2024-01-16T09:15:00Z"
    }
  ],
  "statistics": {
    "totalPending": 45,
    "highPriority": 12,
    "overdueReviews": 3,
    "averageReviewTime": "2.5 hours"
  }
}
```

#### POST /content/{contentId}/moderate

Moderate a specific content item.

**Path Parameters:**
- `contentId` (string, required): Content identifier

**Request Body:**
```json
{
  "decision": "APPROVE|REJECT|REQUIRE_CHANGES",
  "moderatorId": "moderator@example.com",
  "reviewNotes": "Content meets platform guidelines after vendor clarification",
  "tags": ["verified", "compliant"],
  "requiredChanges": [],
  "escalateToSupervisor": false
}
```

**Response (200 OK):**
```json
{
  "contentId": "CONTENT_98765",
  "decision": "APPROVE",
  "moderatedAt": "2024-01-15T10:30:00Z",
  "moderatorId": "moderator@example.com",
  "escalated": false,
  "notificationSent": true
}
```

## User Management API

### User Support Dashboard

#### GET /users/support-queue

Retrieve user support requests requiring admin attention.

**Required Role**: USER_SUPPORT, GLOBAL_ADMIN

**Query Parameters:**
- `priority` (string, optional): Filter by priority
- `category` (string, optional): Filter by issue category
- `status` (string, optional): Filter by status
- `assignedTo` (string, optional): Filter by assigned support agent

**Response (200 OK):**
```json
{
  "supportRequests": [
    {
      "ticketId": "TICKET_67890",
      "userId": "USER_54321",
      "category": "ACCOUNT_LOCKED",
      "priority": "HIGH",
      "status": "OPEN",
      "createdAt": "2024-01-15T08:00:00Z",
      "assignedTo": "support@example.com",
      "description": "User unable to access account after multiple failed login attempts"
    }
  ],
  "queueStatistics": {
    "totalOpen": 23,
    "averageResponseTime": "1.2 hours",
    "customerSatisfactionScore": 4.2
  }
}
```

#### POST /users/{userId}/actions

Perform administrative actions on user accounts.

**Path Parameters:**
- `userId` (string, required): User identifier

**Request Body:**
```json
{
  "action": "SUSPEND|UNSUSPEND|RESET_PASSWORD|UNLOCK_ACCOUNT|VERIFY_IDENTITY",
  "reason": "Security concern - suspicious activity detected",
  "duration": "7 days",
  "notifyUser": true,
  "adminId": "admin@example.com",
  "notes": "Account will be reviewed by security team"
}
```

## Vendor Management API

### Vendor Lifecycle Management

#### GET /vendors/{vendorId}/details

Get comprehensive vendor details for administrative review.

**Required Role**: VENDOR_MANAGER, GLOBAL_ADMIN

**Response (200 OK):**
```json
{
  "vendorId": "VENDOR_12345",
  "profile": {
    "businessName": "Fashion Plus Ltd",
    "registrationDate": "2024-01-10T09:00:00Z",
    "status": "ACTIVE",
    "complianceScore": 95,
    "performanceMetrics": {
      "monthlyRevenue": 25000.00,
      "orderFulfillmentRate": 98.5,
      "customerRating": 4.5,
      "disputeRate": 0.02
    }
  },
  "documentation": {
    "businessLicense": "VERIFIED",
    "taxCertificate": "VERIFIED",
    "insurancePolicy": "PENDING_RENEWAL",
    "lastAuditDate": "2023-11-15T00:00:00Z"
  },
  "alerts": [
    {
      "type": "WARNING",
      "message": "Insurance policy expires in 30 days",
      "actionRequired": true
    }
  ]
}
```

## Data Models

### AdminOverviewResponse
```json
{
  "regionCode": "string",
  "regionName": "string",
  "overview": {
    "totalVendors": "number",
    "activeUsers": "number",
    "pendingOrders": "number",
    "monthlyRevenue": "number",
    "growthRate": "number"
  },
  "alerts": [
    {
      "type": "INFO|WARNING|ERROR",
      "message": "string",
      "count": "number",
      "priority": "LOW|MEDIUM|HIGH"
    }
  ],
  "lastUpdated": "ISO 8601 datetime"
}
```

### VendorResponse
```json
{
  "vendorId": "string",
  "businessName": "string",
  "status": "enum (PENDING, ACTIVE, SUSPENDED, REJECTED)",
  "category": "string",
  "registrationDate": "ISO 8601 datetime",
  "monthlyRevenue": "number",
  "customerRating": "number",
  "complianceScore": "number",
  "lastActivity": "ISO 8601 datetime"
}
```

### ContentModerationRequest
```json
{
  "decision": "enum (APPROVE, REJECT, REQUIRE_CHANGES)",
  "moderatorId": "string (required)",
  "reviewNotes": "string (required, max 1000 chars)",
  "tags": "string[] (optional)",
  "requiredChanges": "string[] (optional)",
  "escalateToSupervisor": "boolean (default: false)"
}
```

### UserActionRequest
```json
{
  "action": "enum (SUSPEND, UNSUSPEND, RESET_PASSWORD, UNLOCK_ACCOUNT, VERIFY_IDENTITY)",
  "reason": "string (required, max 500 chars)",
  "duration": "string (optional, for temporary actions)",
  "notifyUser": "boolean (default: true)",
  "adminId": "string (required)",
  "notes": "string (optional, max 1000 chars)"
}
```

## Error Handling

### Standard HTTP Status Codes
- `200 OK`: Successful operation
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions for operation
- `404 Not Found`: Resource not found
- `422 Unprocessable Entity`: Validation errors
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

### Error Response Format
```json
{
  "timestamp": "2024-01-15T12:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions to access EU-WEST region data",
  "path": "/api/v1/admin-interfaces/regional/EU-WEST/overview",
  "requestId": "req_123456789",
  "adminId": "admin@example.com"
}
```

## Rate Limiting

Administrative APIs have generous rate limits to support dashboard functionality:
- **Global Admins**: 1000 requests per minute
- **Regional Admins**: 500 requests per minute
- **Specialized Roles**: 300 requests per minute

Rate limit headers:
- `X-RateLimit-Limit`: Requests allowed per minute
- `X-RateLimit-Remaining`: Requests remaining
- `X-RateLimit-Reset`: Reset time in Unix timestamp

## Security Considerations

### Audit Logging
All administrative actions are logged with:
- Admin user identification
- Timestamp and IP address
- Action performed and affected resources
- Request parameters and results

### Data Privacy
Administrative APIs handle sensitive data:
- Personal data access is logged and monitored
- Data access is limited by role and region
- Automatic data masking for sensitive fields
- GDPR compliance for EU operations

## OpenAPI Specification

Complete OpenAPI 3.0 specification available at:
- Swagger UI: `http://localhost:8082/swagger-ui.html`
- JSON format: `http://localhost:8082/v3/api-docs`
- YAML format: `http://localhost:8082/v3/api-docs.yaml`