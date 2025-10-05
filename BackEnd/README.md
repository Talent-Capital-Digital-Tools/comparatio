# Comparatio - Compensation Ratio Calculation System
## By Talent Capital

## 📋 Overview

Comparatio is a multi-tenant Spring Boot application for compensation ratio calculations. It provides a comprehensive platform for HR departments to manage compensation matrices, perform calculations, and track historical data with role-based access control.

## ✨ Key Features

- **Multi-Tenant Architecture**: Each client has isolated data and matrices
- **Role-Based Access Control**: SUPER_ADMIN and CLIENT_ADMIN roles
- **Compensation Calculations**: Individual and bulk Excel processing
- **Matrix Management**: Create, update, and validate compensation matrices
- **Profile Management**: User profiles with image uploads
- **Historical Tracking**: Complete audit trail of all operations
- **Dashboard Analytics**: Comprehensive statistics and monitoring
- **Performance Monitoring**: Micrometer metrics and health checks
- **Caching**: Redis-style caching for performance optimization
- **API Documentation**: Complete Swagger/OpenAPI documentation

## 🏗️ Technology Stack

- **Backend**: Spring Boot 3.5.6, Java 21
- **Database**: MongoDB
- **Security**: JWT Authentication with AOP
- **File Processing**: Apache POI for Excel operations
- **Documentation**: OpenAPI/Swagger
- **Monitoring**: Micrometer, Prometheus metrics
- **Caching**: Spring Cache
- **AOP**: AspectJ for cross-cutting concerns

## 🎯 Architecture

### User Roles
```
SUPER_ADMIN    -> Manages all clients, matrices, and users
├── CLIENT_ADMIN -> Manages users within their organization
└── USER       -> Performs calculations within their client scope
```

### Multi-Tenant Data Model
- **Client Isolation**: Each client has separate matrices and calculations
- **User Management**: Role-based access with client-specific permissions
- **File Storage**: Organized by client with unique naming conventions

### Enterprise Features
- **Metrics Collection**: Automatic performance monitoring
- **Health Checks**: Database and file system health monitoring
- **Request/Response Logging**: Comprehensive audit trail
- **Custom Validation**: Business rule enforcement
- **Configuration Management**: Type-safe application properties

## 📚 API Documentation

### Authentication
- **POST** `/api/auth/login` - User login
- **POST** `/api/auth/register` - User registration (handles both initial admin and regular users)

### Profile Management
- **GET** `/api/profile` - Get current user profile
- **PUT** `/api/profile` - Update current user profile
- **POST** `/api/profile/upload-image` - Upload profile image
- **GET** `/api/profile/{userId}` - Get user profile (Admin only)
- **PUT** `/api/profile/{userId}` - Update user profile (Admin only)
- **POST** `/api/profile/{userId}/upload-image` - Upload user image (Admin only)

#### Performance Rating Scale API
- **GET** `/api/profile/performance-rating-scales` - Get available rating scales
- **GET** `/api/profile/performance-rating-scale` - Get current user's rating scale
- **PATCH** `/api/profile/performance-rating-scale` - Update rating scale

**Supported Scales:**
- **3-Point Scale**: Simple rating (1=Low, 2=Medium, 3=High)
- **5-Point Scale**: Granular rating (1-2=Low, 3=Medium, 4-5=High)

### Dashboard (Super Admin)
- **GET** `/api/admin/dashboard` - Get dashboard data with pagination
- **GET** `/api/admin/dashboard/client-accounts` - Get all client accounts
- **GET** `/api/admin/dashboard/client-accounts/{clientId}` - Get specific client
- **PUT** `/api/admin/dashboard/client-accounts/{clientId}/toggle` - Toggle client status

### Matrix Management
- **GET** `/api/admin/matrix/client/{clientId}` - Get client matrices
- **GET** `/api/admin/matrix/{matrixId}` - Get specific matrix
- **POST** `/api/admin/matrix/client/{clientId}` - Create matrix
- **PUT** `/api/admin/matrix/{matrixId}` - Update matrix
- **DELETE** `/api/admin/matrix/{matrixId}` - Delete matrix
- **POST** `/api/admin/matrix/client/{clientId}/bulk` - Bulk update matrices
- **POST** `/api/admin/matrix/client/{clientId}/reset` - Reset to default matrices

### Calculations
- **POST** `/api/calc/individual` - Individual calculation
- **POST** `/api/calc/bulk` - Bulk Excel processing
- **GET** `/api/calc/results` - Get all calculation results (paginated)
- **GET** `/api/calc/results/batch/{batchId}` - Get results by batch (paginated)
- **GET** `/api/calc/results/{resultId}/download` - Download results

### Templates
- **GET** `/api/template/bulk-upload` - Download Excel template

### Upload History
- **GET** `/api/upload-history` - Get upload history
- **GET** `/api/upload-history/{uploadId}` - Get specific upload
- **GET** `/api/upload-history/{uploadId}/download` - Download original file
- **GET** `/api/upload-history/{uploadId}/results` - Download results
- **GET** `/api/upload-history/statistics` - Get upload statistics

### Client Management (Super Admin)
- **GET** `/api/clients` - Get all client admins
- **GET** `/api/clients/{id}` - Get specific client admin
- **POST** `/api/clients` - Create client admin
- **PUT** `/api/clients/{id}` - Update client admin
- **DELETE** `/api/clients/{id}` - Delete client admin
- **POST** `/api/clients/{id}/activate` - Activate client admin
- **POST** `/api/clients/{id}/deactivate` - Deactivate client admin

## 📄 Pageable Endpoints

### Get ALL Calculation Results
**URL:** `GET /api/calc/results`

**Query Parameters:**
- `page` (default: 0) - Page number (0-based)
- `size` (default: 20) - Items per page
- `sortBy` (default: "createdAt") - Field to sort by
- `sortDirection` (default: "DESC") - ASC or DESC

**Example:**
```bash
GET http://localhost:8080/api/calc/results?page=0&size=50&sortBy=newSalary&sortDirection=DESC
```

### Get Results by Batch ID
**URL:** `GET /api/calc/results/batch/{batchId}`

**Example:**
```bash
GET http://localhost:8080/api/calc/results/batch/2025-09-30T08:19:43.220131800Z?page=0&size=100
```

**Sortable Fields:** `createdAt`, `employeeCode`, `jobTitle`, `yearsExperience`, `currentSalary`, `newSalary`, `compaRatio`, `increasePct`

## 📁 File Storage Structure

```
uploads/
├── clients/
│   └── {clientId}/
│       ├── original/
│       └── results/
└── profiles/
    └── {userId}/
        └── profile_image_{userId}_{timestamp}.{extension}
```

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+
- MongoDB
- AspectJ (included via spring-boot-starter-aop)

### Running the Application

1. **Using Maven Wrapper (Windows)**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

2. **Using Maven Wrapper (Unix/Linux)**:
   ```bash
   ./mvnw spring-boot:run
   ```

### Configuration

The application uses `application.yml` for configuration. Key settings:
- Database connection
- JWT secret
- File upload settings
- CORS configuration
- Cache configuration
- AOP settings
- Metrics and monitoring

### Default Admin User

The application automatically creates a default admin user on startup:
- **Email:** admin@talentcapital.com
- **Username:** admin
- **Password:** admin
- **Company:** Talent Capital
- **Role:** SUPER_ADMIN

**Important:** Change the default password in production!

## 📊 API Documentation

Once the application is running, access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🔒 Security

- **JWT Authentication**: All endpoints require valid JWT tokens
- **Role-Based Access**: SUPER_ADMIN and CLIENT_ADMIN roles
- **Client Isolation**: Users can only access their own client's data
- **File Security**: Uploaded files are stored securely with proper access controls
- **Request Logging**: Comprehensive audit trail of all API interactions

## 📈 Monitoring & Metrics

### Health Checks
- **Database Health**: MongoDB connectivity monitoring
- **File System Health**: Upload directory accessibility
- **Application Health**: Spring Boot Actuator endpoints

### Metrics Collection
- **Method Execution Time**: `@Timed` annotations
- **Method Invocation Count**: `@Counted` annotations
- **Custom Metrics**: Business-specific measurements
- **Prometheus Integration**: Metrics export for monitoring systems

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics format

## ⚡ Performance Features

### Caching
- **Matrix Data**: Frequently accessed compensation matrices
- **User Data**: User profile and authentication data
- **Performance Scales**: Rating scale configurations
- **Client Data**: Client-specific configurations

### Optimization
- **Database Pagination**: Efficient large dataset handling
- **File Processing**: Optimized Excel processing
- **Memory Management**: Efficient bulk data handling
- **Connection Pooling**: Database connection optimization

## 🛠️ Development

### Project Structure
```
src/main/java/talentcapitalme/com/comparatio/
├── config/              # Configuration classes
│   ├── health/          # Health indicators
│   └── logging/         # Request/response logging
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── enumeration/         # Enums
├── exception/           # Custom exceptions
├── repository/          # Data repositories
├── security/            # Security configuration
├── service/             # Business logic services
├── util/                # Utility classes
└── validation/          # Custom validators
```

### Interface Layer
All controllers depend on service interfaces rather than concrete implementations:
- `IUserService` → `UserService`
- `IAuthService` → `AuthService`
- `IDashboardService` → `DashboardService`
- `ICompensationService` → `CompensationService`
- `IExcelProcessingService` → `ExcelProcessingService`
- `ITemplateService` → `TemplateService`
- `IMatrixManagementService` → `MatrixManagementService`
- `IMatrixValidationService` → `MatrixValidationService`
- `IMatrixSeederService` → `MatrixSeederService`
- `IUserManagementService` → `UserManagementService`
- `IFileStorageService` → `FileStorageService`
- `IUploadHistoryService` → `UploadHistoryService`

### Enterprise Features
- **Utility Classes**: Centralized helper methods
- **Configuration Properties**: Type-safe configuration management
- **Custom Validation**: Business rule enforcement
- **Request Logging**: Comprehensive API audit trail
- **Metrics Collection**: Performance monitoring
- **Health Monitoring**: System health indicators

## 🧪 Testing

### API Testing
Use the provided endpoints with tools like:
- **Postman**: Import the API collection
- **cURL**: Command-line testing
- **Swagger UI**: Interactive API testing

### Performance Testing
Monitor application performance using:
- **Actuator Metrics**: Built-in performance data
- **Custom Timers**: Business logic execution times
- **Health Checks**: System component status

## 🚨 Error Handling

The application includes comprehensive error handling:
- **Global Exception Handler**: Centralized error processing
- **Validation Errors**: Detailed validation messages
- **Business Logic Errors**: Custom exceptions for business rules
- **Security Errors**: Proper authentication and authorization errors
- **Request Logging**: Detailed error tracking and debugging

## 📝 Database Schema

### Key Collections
- **users**: User profiles with performance rating scales
- **adjustmentMatrices**: Compensation matrices per client
- **calculationResults**: Individual and bulk calculation results
- **uploadHistory**: File upload tracking and audit
- **audit**: System audit trail

### Performance Rating Scale Storage
```json
{
  "_id": "user123",
  "username": "client@example.com",
  "performanceRatingScale": "FIVE_POINT",
  "currency": "USD",
  "active": true
}
```

## 🔄 Data Cleanup

The application includes comprehensive data cleanup mechanisms:
- **Bulk Upload Cleanup**: Previous data deletion before new uploads
- **Client Isolation**: Automatic client-specific data filtering
- **Audit Trail**: Complete operation history tracking
- **File Management**: Automatic cleanup of temporary files

## 📋 Troubleshooting

### Common Issues
- **Authentication Errors**: Verify JWT token validity
- **File Upload Issues**: Check file permissions and size limits
- **Database Connection**: Verify MongoDB connectivity
- **Performance Issues**: Monitor metrics and health endpoints

### Debug Information
- **Request Logging**: Detailed API interaction logs
- **Health Checks**: System component status
- **Metrics**: Performance and usage statistics
- **Audit Trail**: Complete operation history

## 📄 License

This project is part of the Comparatio compensation calculation system by Talent Capital.

---

## 🎉 Ready to Use!

Your enterprise-grade compensation calculation system is ready with:
- ✅ **Multi-tenant architecture**
- ✅ **Role-based security**
- ✅ **Performance monitoring**
- ✅ **Comprehensive API documentation**
- ✅ **Enterprise features**
- ✅ **Production-ready configuration**

Start your application and explore the API at `http://localhost:8080/swagger-ui.html`