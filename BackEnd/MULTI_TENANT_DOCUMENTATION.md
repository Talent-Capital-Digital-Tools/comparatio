# Multi-Tenant Compensation Calculation System

## Overview

This Spring Boot application now supports a **multi-tenant architecture** where each client (e.g., HR Department, Finance Department) has their own separate set of compensation matrices and isolated calculations. The SUPER_ADMIN manages all clients and their matrices, while CLIENT_ADMINs can only access their own client's data.

## Architecture Components

### 1. **User Roles Hierarchy**
```
SUPER_ADMIN    -> Manages all clients, matrices, and users
├── ADMIN      -> Can register users and manage system settings  
├── CLIENT_ADMIN -> Manages users within their client organization
└── USER       -> Performs calculations within their client scope
```

### 2. **Multi-Tenant Data Model**

#### Client Entity
- **Purpose**: Represents a tenant organization (e.g., HR Dept, Finance Dept)
- **Key Fields**: 
  - `id` (String): Unique client identifier
  - `name` (String): Client organization name
  - `active` (Boolean): Whether client is currently active

#### AdjustmentMatrix Entity (Client-Specific)
- **Purpose**: Compensation adjustment rules specific to each client
- **Key Fields**:
  - `clientId` (String): **Links matrix to specific client**
  - `perfBucket` (Integer): Performance rating (1-3)
  - `compaFrom/compaTo` (BigDecimal): Salary comparison ratio range
  - `adjFrom/adjTo` (Integer): Adjustment percentage range

### 3. **Security & Access Control**

#### Authentication Flow
1. **Login**: POST `/api/auth/login` with username/password
2. **JWT Token**: Contains user role and client ID
3. **Authorization**: Role-based access to endpoints

#### Endpoint Security Matrix
| Endpoint | SUPER_ADMIN | ADMIN | CLIENT_ADMIN | USER |
|----------|-------------|-------|--------------|------|
| `/api/auth/register` | ✅ | ✅ | ❌ | ❌ |
| `/api/clients/**` | ✅ | ❌ | ❌ | ❌ |
| `/api/matrix/**` | ✅ | ❌ | ❌ | ❌ |
| `/api/calc/**` | ✅ | ✅ | ✅ | ✅ |
| `/api/users/**` | ✅ | ✅ | ❌ | ❌ |

## API Endpoints

### Client Management (SUPER_ADMIN Only)

#### Create Client
```http
POST /api/clients
Content-Type: application/json
Authorization: Bearer <super_admin_jwt>

{
  "name": "HR Department",
  "active": true
}
```

#### Get All Clients
```http
GET /api/clients
Authorization: Bearer <super_admin_jwt>
```

#### Update Client
```http
PUT /api/clients/{clientId}
Content-Type: application/json
Authorization: Bearer <super_admin_jwt>

{
  "name": "Updated HR Department",
  "active": false
}
```

#### Delete Client
```http
DELETE /api/clients/{clientId}
Authorization: Bearer <super_admin_jwt>
```

### Matrix Management (SUPER_ADMIN Only)

#### Create Client Matrix
```http
POST /api/matrix
Content-Type: application/json
Authorization: Bearer <super_admin_jwt>

{
  "clientId": "client123",
  "perfBucket": 3,
  "compaFrom": 0.80,
  "compaTo": 1.00,
  "adjFrom": 12,
  "adjTo": 17,
  "active": true
}
```

#### Get Client Matrices
```http
GET /api/matrix/client/{clientId}
Authorization: Bearer <super_admin_jwt>
```

#### Seed Default Matrices for Client
```http
POST /api/matrix/seed/{clientId}
Authorization: Bearer <super_admin_jwt>
```

### Calculation (All Authenticated Users)

#### Calculate Compensation
```http
POST /api/calc/calculate
Content-Type: application/json
Authorization: Bearer <user_jwt>

{
  "currentSalary": 50000,
  "performanceRating": 3,
  "jobGradeId": "grade123"
}
```

**Note**: Calculations automatically use the authenticated user's client matrices.

## Client Isolation Implementation

### 1. **Data Isolation**
- Each `AdjustmentMatrix` record includes a `clientId` field
- Repository queries filter by `clientId` automatically
- No cross-client data access possible

### 2. **Authentication Context**
- JWT tokens contain user's `clientId`
- `Authz.getCurrentUserClientId()` extracts client context
- All calculations use client-specific matrices

### 3. **Default Matrix Seeding**
When a new client is created:
1. `ClientService.createClient()` saves the client
2. `SeedService.seedMatricesForClient()` creates default compensation matrices
3. 5 performance buckets × multiple salary ranges = complete matrix set

## Key Services

### ClientService
- **Purpose**: CRUD operations for client management
- **Key Methods**:
  - `createClient()`: Creates client + seeds default matrices
  - `getAllClients()`: Lists all clients (SUPER_ADMIN only)
  - `deleteClient()`: Removes client + all associated matrices

### CompensationService  
- **Purpose**: Client-aware salary calculations
- **Key Methods**:
  - `calculate()`: Uses `getCurrentUserClientId()` for client-specific matrices
  - Automatically isolates calculations per client

### Authz (Authorization Utility)
- **Purpose**: Centralized security and client context
- **Key Methods**:
  - `getCurrentUserClientId()`: Extracts client ID from JWT
  - `getCurrentUserRole()`: Gets user role for authorization
  - `requireClientScope()`: Validates client access permissions

## Database Schema

### Collections Structure
```
clients: [
  { _id: "client1", name: "HR Department", active: true },
  { _id: "client2", name: "Finance Dept", active: true }
]

adjustmentMatrices: [
  { _id: "...", clientId: "client1", perfBucket: 3, compaFrom: 0.8, ... },
  { _id: "...", clientId: "client1", perfBucket: 2, compaFrom: 0.8, ... },
  { _id: "...", clientId: "client2", perfBucket: 3, compaFrom: 0.8, ... }
]

users: [
  { _id: "...", username: "admin", role: "SUPER_ADMIN", clientId: null },
  { _id: "...", username: "hr_admin", role: "CLIENT_ADMIN", clientId: "client1" },
  { _id: "...", username: "fin_user", role: "USER", clientId: "client2" }
]
```

## Setup Instructions

### 1. **Initial System Setup**
1. Start the application
2. Default SUPER_ADMIN user is created automatically
3. Login as SUPER_ADMIN to create clients

### 2. **Create First Client**
```bash
# Login as SUPER_ADMIN
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Create client (using returned JWT)
curl -X POST http://localhost:8080/api/clients \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"HR Department","active":true}'
```

### 3. **Register Client Users**
```bash
# Register CLIENT_ADMIN for HR Department
curl -X POST http://localhost:8080/api/auth/register \
  -H "Authorization: Bearer <super_admin_jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "hr_admin",
    "password": "password123",
    "role": "CLIENT_ADMIN",
    "clientId": "client_id_from_step2"
  }'
```

## Testing Multi-Tenant Functionality

### Test Controller (Development Only)
```http
# Create sample clients with matrices
POST /api/test/setup-clients
Authorization: Bearer <super_admin_jwt>

# List all clients
GET /api/test/clients
Authorization: Bearer <super_admin_jwt>
```

### Verification Steps
1. Create multiple clients using `/api/clients`
2. Each client gets separate matrix sets automatically
3. Login as different users with different `clientId`s
4. Perform calculations - results will use client-specific matrices
5. Verify no cross-client data access

## Security Features

- **JWT Authentication**: Stateless token-based auth
- **Role-Based Access Control**: Hierarchical permissions
- **Client Data Isolation**: No cross-tenant data access
- **Admin-Controlled Registration**: Only admins can create users
- **Automatic Matrix Seeding**: New clients get default compensation rules

## Production Considerations

1. **Remove TestController**: Delete test endpoints before production
2. **Environment Variables**: Configure JWT secrets, DB connections
3. **Logging**: Add audit trails for matrix changes
4. **Backup Strategy**: Client-specific data backup procedures
5. **Performance**: Index `clientId` fields for faster queries

This multi-tenant architecture ensures complete data isolation between client organizations while maintaining a single application instance for efficiency.