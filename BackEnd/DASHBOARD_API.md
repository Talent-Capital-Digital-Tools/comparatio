# Super Admin Dashboard API Documentation

## Overview

The Super Admin Dashboard API provides comprehensive client account management functionality for super administrators. This API allows super admins to view, manage, and monitor all client accounts in the system.

## Base URL
```
/api/admin/dashboard
```

## Authentication
All endpoints require SUPER_ADMIN role and valid JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Get Dashboard Data (Paginated)
**GET** `/api/admin/dashboard`

Get paginated dashboard data with client accounts and statistics.

#### Query Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Number of items per page |
| `sortBy` | string | "companyName" | Field to sort by |
| `sortDir` | string | "asc" | Sort direction (asc/desc) |

#### Example Request
```http
GET /api/admin/dashboard?page=0&size=10&sortBy=companyName&sortDir=asc
Authorization: Bearer <jwt_token>
```

#### Response
```json
{
  "stats": {
    "totalClients": 3,
    "activeClients": 2,
    "inactiveClients": 1,
    "totalEmployees": 150,
    "totalCalculations": 500,
    "totalMatrices": 18,
    "averageRating": 4.5,
    "lastUpdated": "2024-01-15T10:30:00Z"
  },
  "clientAccounts": [
    {
      "id": "client1",
      "companyName": "TechStart Innovations",
      "contactPerson": "alice_cooper",
      "email": "demo1@techstart.com",
      "industry": "Technology",
      "ratingScale": "5/5",
      "active": true,
      "createdAt": "2024-01-01T00:00:00Z",
      "lastLoginAt": "2024-01-15T09:00:00Z",
      "totalEmployees": 50,
      "totalCalculations": 200,
      "status": "Active"
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 3,
  "hasNext": false,
  "hasPrevious": false
}
```

### 2. Get All Client Accounts
**GET** `/api/admin/dashboard/clients`

Get all client accounts without pagination.

#### Example Request
```http
GET /api/admin/dashboard/clients
Authorization: Bearer <jwt_token>
```

#### Response
```json
[
  {
    "id": "client1",
    "companyName": "TechStart Innovations",
    "contactPerson": "alice_cooper",
    "email": "demo1@techstart.com",
    "industry": "Technology",
    "ratingScale": "5/5",
    "active": true,
    "createdAt": "2024-01-01T00:00:00Z",
    "lastLoginAt": "2024-01-15T09:00:00Z",
    "totalEmployees": 50,
    "totalCalculations": 200,
    "status": "Active"
  }
]
```

### 3. Get Client Account by ID
**GET** `/api/admin/dashboard/clients/{clientId}`

Get specific client account details.

#### Path Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

#### Example Request
```http
GET /api/admin/dashboard/clients/client1
Authorization: Bearer <jwt_token>
```

#### Response
```json
{
  "id": "client1",
  "companyName": "TechStart Innovations",
  "contactPerson": "alice_cooper",
  "email": "demo1@techstart.com",
  "industry": "Technology",
  "ratingScale": "5/5",
  "active": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLoginAt": "2024-01-15T09:00:00Z",
  "totalEmployees": 50,
  "totalCalculations": 200,
  "status": "Active"
}
```

### 4. Toggle Client Status
**PUT** `/api/admin/dashboard/clients/{clientId}/toggle-status`

Toggle client account status between active and inactive.

#### Path Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

#### Example Request
```http
PUT /api/admin/dashboard/clients/client1/toggle-status
Authorization: Bearer <jwt_token>
```

#### Response
```json
{
  "id": "client1",
  "companyName": "TechStart Innovations",
  "contactPerson": "alice_cooper",
  "email": "demo1@techstart.com",
  "industry": "Technology",
  "ratingScale": "5/5",
  "active": false,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLoginAt": "2024-01-15T09:00:00Z",
  "totalEmployees": 50,
  "totalCalculations": 200,
  "status": "Inactive"
}
```

### 5. Activate Client
**PUT** `/api/admin/dashboard/clients/{clientId}/activate`

Activate a client account.

#### Path Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

#### Example Request
```http
PUT /api/admin/dashboard/clients/client1/activate
Authorization: Bearer <jwt_token>
```

#### Response
```json
{
  "id": "client1",
  "companyName": "TechStart Innovations",
  "contactPerson": "alice_cooper",
  "email": "demo1@techstart.com",
  "industry": "Technology",
  "ratingScale": "5/5",
  "active": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLoginAt": "2024-01-15T09:00:00Z",
  "totalEmployees": 50,
  "totalCalculations": 200,
  "status": "Active"
}
```

### 6. Deactivate Client
**PUT** `/api/admin/dashboard/clients/{clientId}/deactivate`

Deactivate a client account.

#### Path Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

#### Example Request
```http
PUT /api/admin/dashboard/clients/client1/deactivate
Authorization: Bearer <jwt_token>
```

#### Response
```json
{
  "id": "client1",
  "companyName": "TechStart Innovations",
  "contactPerson": "alice_cooper",
  "email": "demo1@techstart.com",
  "industry": "Technology",
  "ratingScale": "5/5",
  "active": false,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLoginAt": "2024-01-15T09:00:00Z",
  "totalEmployees": 50,
  "totalCalculations": 200,
  "status": "Inactive"
}
```

### 7. Get Dashboard Statistics
**GET** `/api/admin/dashboard/stats`

Get dashboard statistics only.

#### Example Request
```http
GET /api/admin/dashboard/stats
Authorization: Bearer <jwt_token>
```

#### Response
```json
{
  "stats": {
    "totalClients": 3,
    "activeClients": 2,
    "inactiveClients": 1,
    "totalEmployees": 150,
    "totalCalculations": 500,
    "totalMatrices": 18,
    "averageRating": 4.5,
    "lastUpdated": "2024-01-15T10:30:00Z"
  },
  "clientAccounts": [],
  "currentPage": 0,
  "totalPages": 0,
  "totalElements": 0,
  "hasNext": false,
  "hasPrevious": false
}
```

## Data Models

### ClientAccountSummary
| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Client account ID |
| `companyName` | string | Company name |
| `contactPerson` | string | Contact person username |
| `email` | string | Contact email |
| `industry` | string | Industry category |
| `ratingScale` | string | Performance rating (e.g., "5/5") |
| `active` | boolean | Account active status |
| `createdAt` | string | Account creation timestamp |
| `lastLoginAt` | string | Last login timestamp |
| `totalEmployees` | int | Number of employees |
| `totalCalculations` | int | Number of calculations performed |
| `status` | string | Human-readable status ("Active", "Inactive") |

### DashboardStats
| Field | Type | Description |
|-------|------|-------------|
| `totalClients` | int | Total number of clients |
| `activeClients` | int | Number of active clients |
| `inactiveClients` | int | Number of inactive clients |
| `totalEmployees` | int | Total employees across all clients |
| `totalCalculations` | int | Total calculations performed |
| `totalMatrices` | int | Total adjustment matrices |
| `averageRating` | double | Average client rating |
| `lastUpdated` | string | Last update timestamp |

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Access denied. SUPER_ADMIN role required."
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Client not found"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Usage Examples

### Frontend Integration
```javascript
// Get dashboard data
const getDashboard = async (page = 0, size = 10) => {
  const response = await fetch(`/api/admin/dashboard?page=${page}&size=${size}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};

// Toggle client status
const toggleClientStatus = async (clientId) => {
  const response = await fetch(`/api/admin/dashboard/clients/${clientId}/toggle-status`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};
```

## Security Notes

1. **Role-based Access**: All endpoints require SUPER_ADMIN role
2. **JWT Authentication**: Valid JWT token required in Authorization header
3. **Input Validation**: All input parameters are validated
4. **Error Handling**: Comprehensive error handling with appropriate HTTP status codes
5. **Logging**: All operations are logged for audit purposes

## Performance Considerations

1. **Pagination**: Use pagination for large datasets
2. **Caching**: Consider implementing caching for frequently accessed data
3. **Database Indexing**: Ensure proper database indexing for optimal performance
4. **Rate Limiting**: Implement rate limiting for production use

## Monitoring and Analytics

The dashboard provides comprehensive monitoring capabilities:
- Client account status tracking
- Usage statistics
- Performance metrics
- Activity monitoring
- Error tracking

This API is designed to provide a complete client management solution for super administrators with enterprise-grade security and performance.
