# Historical Excel Upload Tracking API Documentation

## Overview

The Historical Tracking API provides comprehensive functionality for tracking Excel uploads and their results, enabling clients to maintain a complete audit trail of all file processing activities. This system automatically stores both original uploaded files and generated result files for historical reference.

## Key Features

- **Complete Audit Trail**: Track all Excel uploads with detailed metadata
- **File Storage**: Store both original and result files securely
- **Historical Access**: Download previous uploads and results
- **Statistics & Analytics**: Comprehensive upload statistics and performance metrics
- **Automatic Cleanup**: Scheduled cleanup of expired files
- **Search & Filter**: Advanced search capabilities for historical data
- **Security**: Client-isolated data access with proper authorization

## Base URL
```
/api/upload-history
```

## Authentication
All endpoints require authentication with CLIENT_ADMIN or SUPER_ADMIN role:
```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Get Upload History
**GET** `/api/upload-history`

Get all upload history for the current client.

**Response:**
```json
[
  {
    "id": "upload_123",
    "clientId": "client_456",
    "clientName": "TechStart Innovations",
    "originalFileName": "employees_2024.xlsx",
    "uploadedFileName": "upload_client_456_batch_123_20240115_143022.xlsx",
    "resultFileName": "result_client_456_batch_123_20240115_143022.xlsx",
    "fileSizeBytes": 245760,
    "contentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "batchId": "2024-01-15T14:30:22.123Z",
    "totalRows": 150,
    "processedRows": 150,
    "successRows": 145,
    "errorRows": 5,
    "processingTimeMs": 2500,
    "uploadFilePath": "./uploads/clients/client_456/upload_client_456_batch_123_20240115_143022.xlsx",
    "resultFilePath": "./uploads/clients/client_456/result_client_456_batch_123_20240115_143022.xlsx",
    "status": "COMPLETED",
    "errorMessage": null,
    "validationErrors": ["Row 5: Invalid salary format", "Row 12: Missing job title"],
    "uploadedBy": "admin_user",
    "uploadedByEmail": "admin@techstart.com",
    "createdAt": "2024-01-15T14:30:22.123Z",
    "updatedAt": "2024-01-15T14:30:25.623Z",
    "expiresAt": "2024-04-15T14:30:22.123Z",
    "filesDeleted": false,
    "description": null,
    "tags": "monthly,payroll"
  }
]
```

### 2. Get Paginated Upload History
**GET** `/api/upload-history/paginated?page=0&size=10&sort=createdAt,desc`

Get paginated upload history with sorting options.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 20 | Number of items per page |
| `sort` | string | "createdAt,desc" | Sort field and direction |

**Response:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false,
  "numberOfElements": 10
}
```

### 3. Get Upload by Batch ID
**GET** `/api/upload-history/batch/{batchId}`

Get specific upload history by batch ID.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `batchId` | string | Unique batch identifier |

### 4. Download Original File
**GET** `/api/upload-history/batch/{batchId}/download/original`

Download the original uploaded Excel file.

**Response:** Excel file download with original filename

### 5. Download Result File
**GET** `/api/upload-history/batch/{batchId}/download/result`

Download the generated result Excel file.

**Response:** Excel file download with result data

### 6. Get Upload Statistics
**GET** `/api/upload-history/statistics`

Get comprehensive upload statistics for the current client.

**Response:**
```json
{
  "totalUploads": 25,
  "successfulUploads": 22,
  "failedUploads": 3,
  "totalRows": 3750,
  "successRows": 3600,
  "errorRows": 150,
  "totalProcessingTimeMs": 125000,
  "averageProcessingTimeMs": 5000
}
```

### 7. Search Uploads
**GET** `/api/upload-history/search?filename=pattern`

Search uploads by filename pattern.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `filename` | string | Filename search pattern (case-insensitive) |

### 8. Get Recent Uploads
**GET** `/api/upload-history/recent?days=7`

Get uploads from the last N days.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `days` | int | 7 | Number of days to look back |

### 9. Cleanup Expired Files (Admin Only)
**POST** `/api/upload-history/cleanup`

Manually trigger cleanup of expired files.

**Response:**
```json
"Cleanup completed. Deleted 15 files."
```

## Data Models

### UploadHistory
| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique upload identifier |
| `clientId` | string | Client identifier |
| `clientName` | string | Client company name |
| `originalFileName` | string | Original uploaded filename |
| `uploadedFileName` | string | Stored filename |
| `resultFileName` | string | Generated result filename |
| `fileSizeBytes` | long | File size in bytes |
| `contentType` | string | MIME type |
| `batchId` | string | Processing batch identifier |
| `totalRows` | int | Total rows in file |
| `processedRows` | int | Rows processed |
| `successRows` | int | Successfully processed rows |
| `errorRows` | int | Rows with errors |
| `processingTimeMs` | long | Processing time in milliseconds |
| `uploadFilePath` | string | Path to stored upload file |
| `resultFilePath` | string | Path to result file |
| `status` | enum | Upload status (UPLOADING, PROCESSING, COMPLETED, FAILED, PARTIAL, CANCELLED) |
| `errorMessage` | string | Error message if failed |
| `validationErrors` | List<string> | List of validation errors |
| `uploadedBy` | string | User who uploaded |
| `uploadedByEmail` | string | Uploader's email |
| `createdAt` | string | Upload timestamp |
| `updatedAt` | string | Last update timestamp |
| `expiresAt` | string | File expiration timestamp |
| `filesDeleted` | boolean | Whether files have been cleaned up |
| `description` | string | Optional description |
| `tags` | string | Comma-separated tags |

### UploadStatistics
| Field | Type | Description |
|-------|------|-------------|
| `totalUploads` | int | Total number of uploads |
| `successfulUploads` | int | Number of successful uploads |
| `failedUploads` | int | Number of failed uploads |
| `totalRows` | int | Total rows processed |
| `successRows` | int | Successfully processed rows |
| `errorRows` | int | Rows with errors |
| `totalProcessingTimeMs` | long | Total processing time |
| `averageProcessingTimeMs` | long | Average processing time per upload |

## File Storage Structure

```
uploads/
├── clients/
│   ├── client_123/
│   │   ├── upload_client_123_batch_456_20240115_143022.xlsx
│   │   ├── result_client_123_batch_456_20240115_143022.xlsx
│   │   └── ...
│   └── client_456/
│       ├── upload_client_456_batch_789_20240115_150000.xlsx
│       └── result_client_456_batch_789_20240115_150000.xlsx
```

## Configuration

### Application Properties
```yaml
app:
  file-storage:
    base-path: ${FILE_STORAGE_PATH:./uploads}
    retention-days: ${FILE_RETENTION_DAYS:90}
```

### Environment Variables
- `FILE_STORAGE_PATH`: Base directory for file storage (default: ./uploads)
- `FILE_RETENTION_DAYS`: Days to retain files (default: 90)

## Automatic Cleanup

### Scheduled Tasks
- **Daily Cleanup**: Runs at 2:00 AM daily to clean expired files
- **Old Files Cleanup**: Runs every 6 hours to clean files older than retention period

### Cleanup Criteria
- Files older than `retention-days` are automatically deleted
- Upload history records are marked as `filesDeleted: true`
- Orphaned files are cleaned up during scheduled maintenance

## Security Features

### Access Control
- **Client Isolation**: Each client can only access their own upload history
- **Role-based Access**: CLIENT_ADMIN and SUPER_ADMIN roles supported
- **File Security**: Files are stored in client-specific directories

### Data Protection
- **Secure File Storage**: Files stored outside web root
- **Access Validation**: All file access is validated against client permissions
- **Audit Logging**: All file operations are logged

## Best Practices

### 1. File Management
- **Regular Cleanup**: Monitor disk usage and adjust retention periods
- **Backup Strategy**: Implement regular backups of important files
- **Access Patterns**: Use pagination for large datasets

### 2. Performance
- **Pagination**: Always use pagination for large result sets
- **Search Optimization**: Use specific search terms for better performance
- **File Size Limits**: Monitor and limit file sizes to prevent storage issues

### 3. Monitoring
- **Storage Usage**: Monitor disk usage regularly
- **Error Tracking**: Monitor failed uploads and processing errors
- **Performance Metrics**: Track processing times and success rates

## Error Handling

### Common Error Responses

#### 404 Not Found
```json
{
  "timestamp": "2024-01-15T14:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Upload history not found"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T14:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied to this upload history"
}
```

#### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T14:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "File storage error"
}
```

## Usage Examples

### Frontend Integration
```javascript
// Get upload history
const getUploadHistory = async (page = 0, size = 10) => {
  const response = await fetch(`/api/upload-history/paginated?page=${page}&size=${size}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};

// Download result file
const downloadResult = async (batchId) => {
  const response = await fetch(`/api/upload-history/batch/${batchId}/download/result`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.ok) {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `result_${batchId}.xlsx`;
    a.click();
  }
};

// Get statistics
const getStatistics = async () => {
  const response = await fetch('/api/upload-history/statistics', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};
```

### Backend Integration
```java
// Get upload history for client
List<UploadHistory> history = uploadHistoryService.getUploadHistoryByClient(clientId);

// Download file
Resource file = fileStorageService.loadFileAsResource(filePath);

// Get statistics
UploadStatistics stats = uploadHistoryService.getUploadStatistics(clientId);
```

## Monitoring and Maintenance

### Health Checks
- **File System**: Monitor available disk space
- **Database**: Check upload history collection size
- **Performance**: Monitor processing times and success rates

### Maintenance Tasks
- **Regular Cleanup**: Ensure scheduled cleanup is running
- **Storage Monitoring**: Monitor disk usage and file counts
- **Error Analysis**: Review failed uploads and processing errors
- **Performance Optimization**: Optimize queries and file operations

This historical tracking system provides enterprise-grade functionality for maintaining complete audit trails of Excel uploads and processing results, ensuring data integrity and providing comprehensive historical access for all clients.
