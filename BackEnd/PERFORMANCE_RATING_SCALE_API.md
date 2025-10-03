# Performance Rating Scale API Documentation

## 📋 Overview

The Performance Rating Scale API allows clients to configure their organization's performance rating system. Clients can choose between:
- **3-Point Scale**: Simple rating system (1=Low, 2=Medium, 3=High)
- **5-Point Scale**: More granular rating system (1-2=Low, 3=Medium, 4-5=High)

This configuration affects how performance ratings are interpreted across the entire compensation calculation system.

---

## 🔗 API Endpoints

### 1. Get Available Performance Rating Scales

Get all available performance rating scale options.

**Endpoint:** `GET /api/profile/performance-rating-scales`

**Authentication:** Required (Bearer Token)

**Response:**
```json
{
  "scales": {
    "THREE_POINT": {
      "maxRating": 3,
      "displayName": "3-Point Rating Scale"
    },
    "FIVE_POINT": {
      "maxRating": 5,
      "displayName": "5-Point Rating Scale"
    }
  },
  "default": "FIVE_POINT"
}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/profile/performance-rating-scales" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get Current Performance Rating Scale

Get the current user's configured performance rating scale.

**Endpoint:** `GET /api/profile/performance-rating-scale`

**Authentication:** Required (Bearer Token)

**Response:**
```json
{
  "performanceRatingScale": "FIVE_POINT",
  "maxRating": 5,
  "displayName": "5-Point Rating Scale",
  "description": "5-Point Scale: 1-2=Low Performance, 3=Medium Performance, 4-5=High Performance"
}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/profile/performance-rating-scale" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Update Performance Rating Scale

Update the current user's performance rating scale configuration.

**Endpoint:** `PATCH /api/profile/performance-rating-scale`

**Authentication:** Required (Bearer Token)

**Request Body:**
```json
{
  "performanceRatingScale": "THREE_POINT"
}
```

**Request Body Fields:**
| Field | Type | Required | Description | Valid Values |
|-------|------|----------|-------------|--------------|
| `performanceRatingScale` | String | Yes | The performance rating scale to use | `THREE_POINT`, `FIVE_POINT` |

**Response:**
```json
{
  "performanceRatingScale": "THREE_POINT",
  "maxRating": 3,
  "displayName": "3-Point Rating Scale",
  "description": "3-Point Scale: 1=Low Performance, 2=Medium Performance, 3=High Performance"
}
```

**Example Request:**
```bash
# Switch to 3-Point Scale
curl -X PATCH "http://localhost:8080/api/profile/performance-rating-scale" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "performanceRatingScale": "THREE_POINT"
  }'

# Switch to 5-Point Scale
curl -X PATCH "http://localhost:8080/api/profile/performance-rating-scale" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "performanceRatingScale": "FIVE_POINT"
  }'
```

**Error Responses:**

**400 Bad Request** - Invalid performance rating scale
```json
{
  "timestamp": "2025-01-27T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "performanceRatingScale must be THREE_POINT or FIVE_POINT"
}
```

**401 Unauthorized** - No valid authentication token
```json
{
  "timestamp": "2025-01-27T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**404 Not Found** - User not found
```json
{
  "timestamp": "2025-01-27T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found"
}
```

---

## 📊 How Performance Rating Scales Work

### 3-Point Scale
- **Rating 1**: Low Performance (Low Bucket)
- **Rating 2**: Medium Performance (Medium Bucket)
- **Rating 3**: High Performance (High Bucket)

### 5-Point Scale
- **Rating 1-2**: Low Performance (Low Bucket)
- **Rating 3**: Medium Performance (Medium Bucket)
- **Rating 4-5**: High Performance (High Bucket)

### Performance Buckets

Internally, all ratings are mapped to 3 performance buckets:
- **Bucket 1**: Low Performance
- **Bucket 2**: Medium Performance
- **Bucket 3**: High Performance

These buckets are used to determine the compensation adjustment matrix cell for salary calculations.

---

## 🎯 Use Cases

### Use Case 1: Client Switching from 5-Point to 3-Point Scale

**Scenario:** A client wants to simplify their performance rating system.

**Steps:**
1. Get current configuration
2. Update to 3-point scale
3. Verify the change

```bash
# Step 1: Get current scale
curl -X GET "http://localhost:8080/api/profile/performance-rating-scale" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Step 2: Update to 3-point scale
curl -X PATCH "http://localhost:8080/api/profile/performance-rating-scale" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"performanceRatingScale": "THREE_POINT"}'

# Step 3: Verify the change
curl -X GET "http://localhost:8080/api/profile/performance-rating-scale" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Use Case 2: Frontend Profile Settings Page

**JavaScript/TypeScript Example:**

```typescript
// Fetch available scales
const getAvailableScales = async () => {
  const response = await fetch('/api/profile/performance-rating-scales', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
};

// Get current scale
const getCurrentScale = async () => {
  const response = await fetch('/api/profile/performance-rating-scale', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
};

// Update scale
const updateScale = async (scale) => {
  const response = await fetch('/api/profile/performance-rating-scale', {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      performanceRatingScale: scale
    })
  });
  
  if (!response.ok) {
    throw new Error('Failed to update performance rating scale');
  }
  
  return await response.json();
};

// Usage in React component
const ProfileSettings = () => {
  const [currentScale, setCurrentScale] = useState(null);
  
  useEffect(() => {
    loadCurrentScale();
  }, []);
  
  const loadCurrentScale = async () => {
    const scale = await getCurrentScale();
    setCurrentScale(scale);
  };
  
  const handleScaleChange = async (newScale) => {
    try {
      const updated = await updateScale(newScale);
      setCurrentScale(updated);
      toast.success('Performance rating scale updated successfully!');
    } catch (error) {
      toast.error('Failed to update performance rating scale');
    }
  };
  
  return (
    <div>
      <h3>Performance Rating Scale</h3>
      <p>Current: {currentScale?.displayName}</p>
      <select onChange={(e) => handleScaleChange(e.target.value)}>
        <option value="THREE_POINT">3-Point Scale</option>
        <option value="FIVE_POINT">5-Point Scale</option>
      </select>
      <p className="description">{currentScale?.description}</p>
    </div>
  );
};
```

---

## 🔄 Impact on Calculations

When you change the performance rating scale, it affects:

### ✅ Individual Calculations
- New calculations will use the updated scale
- Performance ratings must be within the new scale's range
- Example: If you switch to 3-point, ratings must be 1-3 (not 1-5)

### ✅ Bulk Excel Uploads
- Excel files must use the current scale
- Performance rating column should have values within the scale's range
- System validates ratings against the current scale

### ⚠️ Important Notes
- **Existing calculation results are NOT automatically updated**
- Only new calculations use the new scale
- Consider the timing when changing scales (e.g., before annual review cycle)

---

## 🧪 Testing

### Postman Collection

**1. Get Available Scales**
```
GET http://localhost:8080/api/profile/performance-rating-scales
Headers:
  Authorization: Bearer {{token}}
```

**2. Get Current Scale**
```
GET http://localhost:8080/api/profile/performance-rating-scale
Headers:
  Authorization: Bearer {{token}}
```

**3. Update to 3-Point Scale**
```
PATCH http://localhost:8080/api/profile/performance-rating-scale
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json
Body:
{
  "performanceRatingScale": "THREE_POINT"
}
```

**4. Update to 5-Point Scale**
```
PATCH http://localhost:8080/api/profile/performance-rating-scale
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json
Body:
{
  "performanceRatingScale": "FIVE_POINT"
}
```

### Test Scenarios

**Scenario 1: Valid Update**
1. Update scale to THREE_POINT
2. Verify response shows maxRating = 3
3. Get current scale
4. Confirm scale is THREE_POINT

**Scenario 2: Invalid Scale**
1. Try to update with invalid scale value
2. Should return 400 Bad Request

**Scenario 3: Missing Token**
1. Call API without Authorization header
2. Should return 401 Unauthorized

---

## 📝 Database Schema

The performance rating scale is stored in the `users` collection:

```json
{
  "_id": "user123",
  "username": "client@example.com",
  "email": "client@example.com",
  "role": "CLIENT_ADMIN",
  "performanceRatingScale": "FIVE_POINT",  // <-- Stored here
  "currency": "USD",
  "active": true,
  // ... other fields
}
```

**Field Details:**
- **Field Name**: `performanceRatingScale`
- **Type**: Enum (PerformanceRatingScale)
- **Default**: `FIVE_POINT`
- **Valid Values**: `THREE_POINT`, `FIVE_POINT`

---

## 🔒 Security

- **Authentication Required**: All endpoints require valid JWT token
- **Authorization**: Users can only update their own performance rating scale
- **Validation**: Request body is validated using Jakarta Validation
- **Audit Trail**: Changes are logged with timestamps in audit fields

---

## 📚 Related APIs

### Update Full Profile
You can also update the performance rating scale as part of a full profile update:

**Endpoint:** `PUT /api/profile`

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "companyName": "Acme Corp",
  "industry": "Technology",
  "performanceRatingScale": "THREE_POINT",
  "currency": "USD"
}
```

---

## 🆘 Troubleshooting

### Issue: "User not found" error
**Solution:** Ensure you're using a valid JWT token for an existing user.

### Issue: "performanceRatingScale is required" error
**Solution:** Include the `performanceRatingScale` field in your request body.

### Issue: Invalid enum value error
**Solution:** Use only `THREE_POINT` or `FIVE_POINT` (case-sensitive).

### Issue: Changes not reflected in calculations
**Solution:** 
- Clear any cached user data
- Ensure new calculations are being made (not using old cached results)
- Verify the user profile shows the updated scale

---

## 📖 Summary

| Endpoint | Method | Purpose | Authentication |
|----------|--------|---------|----------------|
| `/api/profile/performance-rating-scales` | GET | Get all available scales | Required |
| `/api/profile/performance-rating-scale` | GET | Get current user's scale | Required |
| `/api/profile/performance-rating-scale` | PATCH | Update user's scale | Required |

**Key Points:**
✅ Simple API to switch between 3-point and 5-point scales
✅ Affects all future calculations automatically
✅ User-friendly responses with descriptions
✅ Full validation and error handling
✅ Production-ready with comprehensive logging

---

## 🚀 Next Steps

1. **Test the API** using Postman or curl
2. **Integrate with Frontend** using the provided JavaScript examples
3. **Add UI Controls** in your profile settings page
4. **Monitor Logs** to track scale changes
5. **Educate Users** about the impact of changing scales

**Your Performance Rating Scale API is ready to use!** 🎉

