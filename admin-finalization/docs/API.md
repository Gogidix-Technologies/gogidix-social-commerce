# Admin Finalization Service API Documentation

## Overview

The Admin Finalization Service provides REST API endpoints for managing administrative workflows. All endpoints support JSON request/response format and include comprehensive error handling and validation.

## Base URL

```
http://localhost:8081/api/v1/admin-finalization
```

## Authentication

All endpoints require JWT authentication via Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Core API Endpoints

### Health and Monitoring

#### GET /actuator/health
Returns service health status.

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

#### GET /actuator/info
Returns service information and version details.

## Workflow Management API

### Create Workflow

#### POST /workflows

Creates a new administrative workflow.

**Request Body:**
```json
{
  "title": "string",
  "description": "string",
  "requestData": {
    "key": "value"
  },
  "priority": "LOW|MEDIUM|HIGH|URGENT"
}
```

**Response (201 Created):**
```json
{
  "id": "uuid",
  "title": "string",
  "description": "string",
  "status": "PENDING",
  "priority": "HIGH",
  "requestData": {},
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "admin@example.com",
  "updatedAt": "2024-01-15T10:30:00Z",
  "updatedBy": "admin@example.com"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Missing or invalid authentication
- `422 Unprocessable Entity`: Validation errors

### Get Workflow by ID

#### GET /workflows/{id}

Retrieves a specific workflow by its unique identifier.

**Path Parameters:**
- `id` (UUID, required): Workflow identifier

**Response (200 OK):**
```json
{
  "id": "uuid",
  "title": "string",
  "description": "string",
  "status": "PENDING|IN_PROGRESS|APPROVED|REJECTED|COMPLETED|CANCELLED",
  "priority": "LOW|MEDIUM|HIGH|URGENT",
  "requestData": {},
  "responseData": {},
  "approvalReason": "string",
  "rejectionReason": "string",
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "admin@example.com",
  "updatedAt": "2024-01-15T10:30:00Z",
  "updatedBy": "admin@example.com"
}
```

**Error Responses:**
- `404 Not Found`: Workflow not found
- `401 Unauthorized`: Missing or invalid authentication

### Get All Workflows

#### GET /workflows

Retrieves a paginated list of workflows with optional filtering.

**Query Parameters:**
- `page` (int, optional, default: 0): Page number
- `size` (int, optional, default: 20): Page size
- `status` (string, optional): Filter by workflow status
- `priority` (string, optional): Filter by priority level
- `createdBy` (string, optional): Filter by creator

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "string",
      "status": "PENDING",
      "priority": "HIGH",
      "createdAt": "2024-01-15T10:30:00Z",
      "createdBy": "admin@example.com"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### Update Workflow

#### PUT /workflows/{id}

Updates an existing workflow. Only workflows in PENDING or IN_PROGRESS status can be updated.

**Path Parameters:**
- `id` (UUID, required): Workflow identifier

**Request Body:**
```json
{
  "title": "string",
  "description": "string",
  "requestData": {},
  "priority": "LOW|MEDIUM|HIGH|URGENT"
}
```

**Response (200 OK):**
```json
{
  "id": "uuid",
  "title": "Updated title",
  "description": "Updated description",
  "status": "PENDING",
  "priority": "HIGH",
  "updatedAt": "2024-01-15T11:30:00Z",
  "updatedBy": "admin@example.com"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid workflow status for update
- `404 Not Found`: Workflow not found
- `422 Unprocessable Entity`: Validation errors

### Delete Workflow

#### DELETE /workflows/{id}

Deletes a workflow. Only workflows in PENDING status can be deleted.

**Path Parameters:**
- `id` (UUID, required): Workflow identifier

**Response (204 No Content)**

**Error Responses:**
- `400 Bad Request`: Workflow cannot be deleted in current status
- `404 Not Found`: Workflow not found

### Approve Workflow

#### POST /workflows/{id}/approve

Approves a workflow and moves it to APPROVED status.

**Path Parameters:**
- `id` (UUID, required): Workflow identifier

**Request Body:**
```json
{
  "approvalReason": "string",
  "responseData": {
    "key": "value"
  },
  "additionalNotes": "string"
}
```

**Response (200 OK):**
```json
{
  "id": "uuid",
  "status": "APPROVED",
  "approvalReason": "Meets all requirements",
  "responseData": {},
  "updatedAt": "2024-01-15T12:30:00Z",
  "updatedBy": "supervisor@example.com"
}
```

**Error Responses:**
- `400 Bad Request`: Workflow not in approvable status
- `404 Not Found`: Workflow not found

### Reject Workflow

#### POST /workflows/{id}/reject

Rejects a workflow and moves it to REJECTED status.

**Path Parameters:**
- `id` (UUID, required): Workflow identifier

**Request Body:**
```json
{
  "rejectionReason": "string",
  "responseData": {
    "key": "value"
  },
  "additionalNotes": "string"
}
```

**Response (200 OK):**
```json
{
  "id": "uuid",
  "status": "REJECTED",
  "rejectionReason": "Missing required documentation",
  "responseData": {},
  "updatedAt": "2024-01-15T12:30:00Z",
  "updatedBy": "supervisor@example.com"
}
```

## Data Models

### WorkflowRequest

```json
{
  "title": "string (required, max 255 chars)",
  "description": "string (optional, max 1000 chars)",
  "requestData": "object (optional)",
  "priority": "enum (LOW, MEDIUM, HIGH, URGENT)"
}
```

### WorkflowResponse

```json
{
  "id": "UUID",
  "title": "string",
  "description": "string",
  "status": "enum (PENDING, IN_PROGRESS, APPROVED, REJECTED, COMPLETED, CANCELLED)",
  "priority": "enum (LOW, MEDIUM, HIGH, URGENT)",
  "requestData": "object",
  "responseData": "object",
  "approvalReason": "string",
  "rejectionReason": "string",
  "createdAt": "ISO 8601 datetime",
  "createdBy": "string",
  "updatedAt": "ISO 8601 datetime",
  "updatedBy": "string"
}
```

### ApprovalRequest

```json
{
  "approvalReason": "string (required, max 500 chars)",
  "responseData": "object (optional)",
  "additionalNotes": "string (optional, max 1000 chars)"
}
```

### RejectionRequest

```json
{
  "rejectionReason": "string (required, max 500 chars)",
  "responseData": "object (optional)",
  "additionalNotes": "string (optional, max 1000 chars)"
}
```

## Error Handling

### Standard HTTP Status Codes

- `200 OK`: Successful operation
- `201 Created`: Resource created successfully
- `204 No Content`: Successful operation with no response body
- `400 Bad Request`: Invalid request parameters or business logic error
- `401 Unauthorized`: Authentication required or invalid
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `422 Unprocessable Entity`: Validation errors
- `500 Internal Server Error`: Server error

### Error Response Format

```json
{
  "timestamp": "2024-01-15T12:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/v1/admin-finalization/workflows/123",
  "validationErrors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

## Rate Limiting

- **Default Limit**: 100 requests per minute per user
- **Burst Limit**: 20 requests per 10 seconds
- **Headers**: Rate limit information included in response headers
  - `X-RateLimit-Limit`: Total requests allowed
  - `X-RateLimit-Remaining`: Requests remaining
  - `X-RateLimit-Reset`: Time when limit resets

## Pagination

For endpoints returning lists, pagination follows the standard Spring Data format:

```json
{
  "content": [...],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20
}
```

## OpenAPI Specification

Complete OpenAPI 3.0 specification is available at:
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- JSON format: `http://localhost:8081/v3/api-docs`
- YAML format: `http://localhost:8081/v3/api-docs.yaml`