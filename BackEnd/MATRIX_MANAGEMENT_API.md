# Professional Matrix Management & Dashboard API Documentation

## Overview
The Matrix Management & Dashboard API provides comprehensive functionality for SUPER_ADMIN users to manage compensation matrices for CLIENT_ADMIN users and monitor client accounts through a professional dashboard. This system includes advanced validation, bulk operations, client management, and professional error handling.

## Base URLs
```
/api/admin/matrix    - Matrix Management
/api/admin/dashboard - Client Dashboard Management
```

## Authentication
All endpoints require SUPER_ADMIN role authentication with valid JWT token:
```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Get Client Matrices
**GET** `/client/{clientId}`

Retrieves all matrices for a specific client with detailed information.

**Response:**
```json
[
  {
    "id": "client123_m_3_0.71_0.85",
    "clientId": "client123",
    "clientName": "Acme Corporation",
    "perfBucket": 3,
    "compaFrom": 0.71,
    "compaTo": 0.85,
    "pctLt5Years": 17.0,
    "pctGte5Years": 21.0,
    "effectiveFrom": "2025-01-01",
    "effectiveTo": null,
    "active": true,
    "compaRangeLabel": "71%–85%"
  }
]
```

### 2. Get Grouped Matrices
**GET** `/client/{clientId}/grouped`

Retrieves matrices grouped by performance bucket for better organization.

**Response:**
```json
{
  "clientId": "client123",
  "clientName": "Acme Corporation",
  "totalMatrices": 18,
  "performanceBucket1": [...],
  "performanceBucket2": [...],
  "performanceBucket3": [...]
}
```

### 3. Get Single Matrix
**GET** `/{matrixId}/client/{clientId}`

Retrieves a specific matrix by ID.

### 4. Create Matrix
**POST** `/client/{clientId}`

Creates a new matrix for a client.

**Request Body:**
```json
{
  "perfBucket": 3,
  "compaFrom": 0.71,
  "compaTo": 0.85,
  "pctLt5Years": 17.0,
  "pctGte5Years": 21.0,
  "effectiveFrom": "2025-01-01",
  "effectiveTo": null,
  "active": true
}
```

**Validation Rules:**
- `perfBucket`: Must be between 1-3
- `compaFrom`: Must be >= 0
- `compaTo`: Must be > compaFrom
- `pctLt5Years`: Must be between 0-100
- `pctGte5Years`: Must be between 0-100
- `effectiveFrom`: Must be before `effectiveTo` if both provided

### 5. Update Matrix
**PUT** `/{matrixId}/client/{clientId}`

Updates an existing matrix.

### 6. Delete Matrix
**DELETE** `/{matrixId}/client/{clientId}`

Deletes a matrix. Prevents deletion if it's the last matrix for a performance bucket.

### 7. Bulk Update Matrices
**PUT** `/client/{clientId}/bulk`

Updates all matrices for a client in a single operation.

**Request Body:**
```json
[
  {
    "perfBucket": 1,
    "compaFrom": 0.0,
    "compaTo": 0.70,
    "pctLt5Years": 8.0,
    "pctGte5Years": 12.0,
    "active": true
  }
]
```

### 8. Reset to Default Matrices
**POST** `/client/{clientId}/reset`

Resets all matrices to default configuration (6 matrices per performance bucket).

### 9. Get Matrix Statistics
**GET** `/client/{clientId}/stats`

Retrieves comprehensive statistics about a client's matrices.

**Response:**
```json
{
  "clientId": "client123",
  "clientName": "Acme Corporation",
  "totalMatrices": 18,
  "performanceBucket1Count": 6,
  "performanceBucket2Count": 6,
  "performanceBucket3Count": 6,
  "hasActiveMatrices": true,
  "lastUpdated": "2025-01-15T10:30:00Z"
}
```

### 10. Validate Current Matrices
**GET** `/client/{clientId}/validate`

Validates the current matrix configuration for a client.

**Response:**
```json
{
  "isValid": true,
  "errors": [],
  "warnings": [
    "Performance bucket 1: Gap between 0.50-0.70 and 0.71-0.85"
  ],
  "summary": "✓ Matrix configuration is valid (18 matrices)",
  "totalMatrices": 18
}
```

### 11. Validate Matrix Configuration
**POST** `/client/{clientId}/validate`

Validates a matrix configuration before saving.

---

## Dashboard Management API

### 1. Get Dashboard Data (Paginated)
**GET** `/api/admin/dashboard`

Get paginated dashboard data with client accounts and statistics.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Number of items per page |
| `sortBy` | string | "companyName" | Field to sort by |
| `sortDir` | string | "asc" | Sort direction (asc/desc) |

**Response:**
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

**Response:**
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

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

### 4. Toggle Client Status
**PUT** `/api/admin/dashboard/clients/{clientId}/toggle-status`

Toggle client account status between active and inactive.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

### 5. Activate Client
**PUT** `/api/admin/dashboard/clients/{clientId}/activate`

Activate a client account.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

### 6. Deactivate Client
**PUT** `/api/admin/dashboard/clients/{clientId}/deactivate`

Deactivate a client account.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `clientId` | string | Client account ID |

### 7. Get Dashboard Statistics
**GET** `/api/admin/dashboard/stats`

Get dashboard statistics only.

**Response:**
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

---

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

---

## Professional Features

### 1. Advanced Validation
- **Range Overlap Detection**: Prevents overlapping compa ratio ranges
- **Gap Detection**: Warns about gaps in compa ratio coverage
- **Business Logic Validation**: Ensures higher performance buckets have appropriate percentages
- **Data Integrity**: Validates all required fields and constraints

### 2. Comprehensive Error Handling
- **Detailed Error Messages**: Clear, actionable error descriptions
- **Warning System**: Non-blocking warnings for potential issues
- **Validation Summary**: Quick overview of configuration status

### 3. Bulk Operations
- **Atomic Updates**: All-or-nothing bulk operations
- **Performance Optimized**: Efficient database operations
- **Validation**: Pre-validates entire configuration before saving

### 4. Dashboard Management
- **Client Overview**: Comprehensive client account monitoring
- **Status Management**: Toggle, activate, and deactivate client accounts
- **Real-time Statistics**: Live system metrics and performance data
- **Pagination Support**: Efficient data loading for large datasets
- **Sorting & Filtering**: Flexible data organization and search

### 5. Professional Logging
- **Operation Tracking**: Detailed logs for all matrix and dashboard operations
- **Performance Metrics**: Processing time and statistics
- **Audit Trail**: Complete history of changes and client management actions

### 6. Security
- **Role-Based Access**: Only SUPER_ADMIN can manage matrices and dashboard
- **Client Isolation**: Matrices and data are properly isolated by client
- **Input Validation**: Comprehensive validation of all inputs
- **JWT Authentication**: Secure token-based authentication for all endpoints

## Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input data |
| 401 | Unauthorized - Invalid or missing authentication |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Client or matrix not found |
| 409 | Conflict - Overlapping ranges or validation errors |
| 500 | Internal Server Error - System error |

## Best Practices

1. **Always validate** matrix configurations before saving
2. **Use bulk operations** for multiple changes
3. **Check statistics** to ensure proper matrix coverage
4. **Monitor warnings** for potential configuration issues
5. **Test with small datasets** before bulk operations

## Performance Considerations

- **Bulk operations** are optimized for large datasets
- **Validation** is performed in-memory for speed
- **Database operations** use batch processing
- **Logging** is asynchronous to avoid performance impact

## Example Usage

### Matrix Management Workflow
```bash
# 1. Validate current configuration
GET /api/admin/matrix/client/{clientId}/validate

# 2. Reset to defaults if needed
POST /api/admin/matrix/client/{clientId}/reset

# 3. Get current matrices
GET /api/admin/matrix/client/{clientId}

# 4. Update specific matrices
PUT /api/admin/matrix/{matrixId}/client/{clientId}

# 5. Validate final configuration
GET /api/admin/matrix/client/{clientId}/validate
```

### Dashboard Management Workflow
```bash
# 1. Get dashboard overview
GET /api/admin/dashboard?page=0&size=10&sortBy=companyName&sortDir=asc

# 2. Get specific client details
GET /api/admin/dashboard/clients/{clientId}

# 3. Toggle client status
PUT /api/admin/dashboard/clients/{clientId}/toggle-status

# 4. Get system statistics
GET /api/admin/dashboard/stats

# 5. Manage client accounts
PUT /api/admin/dashboard/clients/{clientId}/activate
PUT /api/admin/dashboard/clients/{clientId}/deactivate
```

### Frontend Integration Example
```javascript
// Dashboard Management
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

// Matrix management
const getClientMatrices = async (clientId) => {
  const response = await fetch(`/api/admin/matrix/client/${clientId}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};
```

This professional matrix management and dashboard system provides enterprise-grade functionality for managing compensation matrices and client accounts with comprehensive validation, error handling, and performance optimization.
