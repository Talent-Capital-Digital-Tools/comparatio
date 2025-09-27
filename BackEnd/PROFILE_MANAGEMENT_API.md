# Profile Management API Documentation

This document describes the profile management endpoints for the Comparatio application.

## Overview

The profile management system allows users to:
- View and update their own profile information
- Upload profile images (logos)
- Super admins can view and edit any user's profile

## Profile Fields

- **id**: User ID
- **username**: Username
- **email**: Email address
- **fullName**: Full name of the user
- **companyName**: Company name (for CLIENT_ADMIN users)
- **industry**: Industry sector
- **avatarUrl**: URL/path to profile image/logo
- **role**: User role (SUPER_ADMIN or CLIENT_ADMIN)
- **active**: Account status (for CLIENT_ADMIN users)

## Endpoints

### User Profile Management

#### Get Current User Profile
- **GET** `/api/profile`
- **Description**: Retrieve current authenticated user's profile
- **Authentication**: Required
- **Response**: `ProfileResponse`

#### Update Current User Profile
- **PUT** `/api/profile`
- **Description**: Update current authenticated user's profile
- **Authentication**: Required
- **Request Body**: `ProfileUpdateRequest`
- **Response**: `ProfileResponse`

#### Upload Profile Image
- **POST** `/api/profile/upload-image`
- **Description**: Upload profile image for current user
- **Authentication**: Required
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `file`: Image file (required)
- **Response**: JSON with message and avatarUrl

### Admin Profile Management

#### Get User Profile by ID
- **GET** `/api/profile/{userId}`
- **Description**: Retrieve user profile by ID (admin only)
- **Authentication**: Required (Admin)
- **Parameters**:
  - `userId`: User ID (path parameter)
- **Response**: `ProfileResponse`

#### Update User Profile by ID
- **PUT** `/api/profile/{userId}`
- **Description**: Update user profile by ID (admin only)
- **Authentication**: Required (Admin)
- **Parameters**:
  - `userId`: User ID (path parameter)
- **Request Body**: `ProfileUpdateRequest`
- **Response**: `ProfileResponse`

#### Upload Profile Image for User
- **POST** `/api/profile/{userId}/upload-image`
- **Description**: Upload profile image for specific user (admin only)
- **Authentication**: Required (Admin)
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `userId`: User ID (path parameter)
  - `file`: Image file (required)
- **Response**: JSON with message and avatarUrl

#### Get Profile Directory Info
- **GET** `/api/profile/directory-info`
- **Description**: Get information about profile images directory structure
- **Authentication**: Required
- **Response**: JSON with directory information and examples

## Request/Response Models

### ProfileResponse
```json
{
  "id": "string",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "companyName": "string",
  "industry": "string",
  "avatarUrl": "string",
  "role": "string",
  "active": boolean
}
```

### ProfileUpdateRequest
```json
{
  "fullName": "string",
  "email": "string",
  "companyName": "string",
  "industry": "string",
  "avatarUrl": "string"
}
```

## File Storage

Profile images are stored in the `uploads/profiles/{userId}/` directory with unique filenames following the pattern:
`profile_image_{userId}_{timestamp}.{extension}`

### Directory Structure
```
uploads/
├── clients/           # Client-specific uploads (existing)
└── profiles/          # Profile images directory
    ├── user123/       # User-specific profile directory
    │   └── profile_image_user123_20250127_182345_123.jpg
    ├── user456/       # Another user's profile directory
    │   └── profile_image_user456_20250127_183000_456.png
    └── ...
```

### Unique Avatar URL Format
- **Pattern**: `uploads/profiles/{userId}/profile_image_{userId}_{timestamp}.{extension}`
- **Example**: `uploads/profiles/user123/profile_image_user123_20250127_182345_123.jpg`
- **Timestamp Format**: `yyyyMMdd_HHmmss_SSS` (year, month, day, hour, minute, second, millisecond)

### Supported Image Formats
All standard image formats (JPEG, PNG, GIF, WebP, etc.)

### Uniqueness Guarantee
Each avatar URL is guaranteed to be unique because:
1. **User ID**: Each user has a unique identifier
2. **Timestamp**: Includes date, time, and milliseconds
3. **File Replacement**: New uploads replace existing files in the same user directory

## Error Responses

- **400 Bad Request**: Invalid request data or file format
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: User not found
- **500 Internal Server Error**: Server error during file upload or processing

## Usage Examples

### Update Profile
```bash
curl -X PUT "http://localhost:8080/api/profile" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john.doe@company.com",
    "companyName": "Acme Corp",
    "industry": "Technology",
    "avatarUrl": "/uploads/profiles/user123/profile_image.jpg"
  }'
```

### Upload Profile Image
```bash
curl -X POST "http://localhost:8080/api/profile/upload-image" \
  -H "Authorization: Bearer <token>" \
  -F "file=@profile_image.jpg"
```

### Get User Profile (Admin)
```bash
curl -X GET "http://localhost:8080/api/profile/user123" \
  -H "Authorization: Bearer <admin_token>"
```

### Get Profile Directory Info
```bash
curl -X GET "http://localhost:8080/api/profile/directory-info" \
  -H "Authorization: Bearer <token>"
```

**Response:**
```json
{
  "baseDirectory": "./uploads/profiles",
  "directoryStructure": "uploads/profiles/{userId}/profile_image_{userId}_{timestamp}.{extension}",
  "example": "uploads/profiles/user123/profile_image_user123_20250127_182345_123.jpg"
}
```
