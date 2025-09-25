# Professional Matrix Management API Documentation

## Overview
The Matrix Management API provides comprehensive functionality for SUPER_ADMIN users to manage compensation matrices for CLIENT_ADMIN users. This system includes advanced validation, bulk operations, and professional error handling.

## Base URL
```
/api/admin/matrix
```

## Authentication
All endpoints require SUPER_ADMIN role authentication.

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

### 4. Professional Logging
- **Operation Tracking**: Detailed logs for all matrix operations
- **Performance Metrics**: Processing time and statistics
- **Audit Trail**: Complete history of changes

### 5. Security
- **Role-Based Access**: Only SUPER_ADMIN can manage matrices
- **Client Isolation**: Matrices are properly isolated by client
- **Input Validation**: Comprehensive validation of all inputs

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

### Creating a Complete Matrix Set
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

This professional matrix management system provides enterprise-grade functionality for managing compensation matrices with comprehensive validation, error handling, and performance optimization.
