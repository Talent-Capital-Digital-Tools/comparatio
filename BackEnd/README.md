# Comparatio - Compensation Ratio Calculation System

## Overview

Comparatio is a multi-tenant Spring Boot application for compensation ratio calculations. It provides a comprehensive platform for HR departments to manage compensation matrices, perform calculations, and track historical data with role-based access control.

## Key Features

- **Multi-Tenant Architecture**: Each client has isolated data and matrices
- **Role-Based Access Control**: SUPER_ADMIN and CLIENT_ADMIN roles
- **Compensation Calculations**: Individual and bulk Excel processing
- **Matrix Management**: Create, update, and validate compensation matrices
- **Profile Management**: User profiles with image uploads
- **Historical Tracking**: Complete audit trail of all operations
- **Dashboard Analytics**: Comprehensive statistics and monitoring

## Technology Stack

- **Backend**: Spring Boot 3.5.6, Java 17
- **Database**: MongoDB
- **Security**: JWT Authentication
- **File Processing**: Apache POI for Excel operations
- **Documentation**: OpenAPI/Swagger

## Architecture

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

## API Endpoints

### Authentication
- **POST** `/api/auth/login` - User login
- **POST** `/api/auth/register` - User registration
- **POST** `/api/auth/register-admin` - Admin registration

### Profile Management
- **GET** `/api/profile` - Get current user profile
- **PUT** `/api/profile` - Update current user profile
- **POST** `/api/profile/upload-image` - Upload profile image
- **GET** `/api/profile/{userId}` - Get user profile (Admin only)
- **PUT** `/api/profile/{userId}` - Update user profile (Admin only)
- **POST** `/api/profile/{userId}/upload-image` - Upload user image (Admin only)

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
- **GET** `/api/calc/results` - Get calculation results
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

## File Storage Structure

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

## Profile Image Naming Convention

Profile images are stored with unique names:
```
profile_image_{userId}_{timestamp}.{extension}
```

Example: `profile_image_user123_20250127_182345_123.jpg`

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB

### Running the Application

1. **Using Maven Wrapper (Windows)**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

2. **Using Maven Wrapper (Unix/Linux)**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Using Shell Script**:
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

### Configuration

The application uses `application.yml` for configuration. Key settings:
- Database connection
- JWT secret
- File upload settings
- CORS configuration

## API Documentation

Once the application is running, access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Security

- **JWT Authentication**: All endpoints require valid JWT tokens
- **Role-Based Access**: SUPER_ADMIN and CLIENT_ADMIN roles
- **Client Isolation**: Users can only access their own client's data
- **File Security**: Uploaded files are stored securely with proper access controls

## Error Handling

The application includes comprehensive error handling:
- **Global Exception Handler**: Centralized error processing
- **Validation Errors**: Detailed validation messages
- **Business Logic Errors**: Custom exceptions for business rules
- **Security Errors**: Proper authentication and authorization errors

## Development

### Project Structure
```
src/main/java/talentcapitalme/com/comparatio/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── enumeration/    # Enums
├── exception/      # Custom exceptions
├── repository/     # Data repositories
├── security/       # Security configuration
└── service/        # Business logic services
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

## License

This project is part of the Comparatio compensation calculation system.
