# Pageable Calculation Results Endpoints

## 🎯 What You Have Now

I've created **2 new pageable endpoints** that fetch your calculation results efficiently from MongoDB:

---

## 📍 Endpoint 1: Get ALL Calculation Results

**URL:** `GET /api/calc/results`

**Description:** Get all calculation results for your client (filtered by `clientId`) with pagination and sorting.

### Query Parameters:

- `page` (default: 0) - Page number (0-based)
- `size` (default: 20) - Items per page
- `sortBy` (default: "createdAt") - Field to sort by
- `sortDirection` (default: "DESC") - ASC or DESC

### Example Requests:

```bash
# Get first page (20 items)
GET http://localhost:8080/api/calc/results

# Get second page with 50 items
GET http://localhost:8080/api/calc/results?page=1&size=50

# Sort by new salary (highest first)
GET http://localhost:8080/api/calc/results?sortBy=newSalary&sortDirection=DESC

# Sort by employee code (alphabetical)
GET http://localhost:8080/api/calc/results?sortBy=employeeCode&sortDirection=ASC
```

---

## 📍 Endpoint 2: Get Results by Batch ID

**URL:** `GET /api/calc/results/batch/{batchId}`

**Description:** Get calculation results for a specific batch AND client (both filters applied).

### Path Parameters:

- `batchId` - The batch ID (e.g., "2025-09-30T08:19:43.220131800Z")

### Query Parameters:

Same as endpoint 1

### Example Requests:

```bash
# Get first page for specific batch
GET http://localhost:8080/api/calc/results/batch/2025-09-30T08:19:43.220131800Z

# Get all results for a batch (page 0, size 100)
GET http://localhost:8080/api/calc/results/batch/2025-09-30T08:19:43.220131800Z?page=0&size=100

# Sort by years of experience
GET http://localhost:8080/api/calc/results/batch/2025-09-30T08:19:43.220131800Z?sortBy=yearsExperience&sortDirection=DESC
```

---

## 📦 Response Format

```json
{
  "batchId": "2025-09-30T08:19:43.220131800Z",
  "totalRows": 21,
  "successCount": 20,
  "errorCount": 0,
  "rows": [
    {
      "employeeCode": "EMP001",
      "employeeName": "N/A",
      "jobTitle": "HR Manager",
      "yearsExperience": 10,
      "performanceRating5": 3,
      "currentSalary": 12000.0,
      "midOfScale": 20000.0,
      "compaRatio": 60,
      "compaLabel": "> 115%",
      "increasePct": 17.0,
      "newSalary": 14040.0,
      "increaseAmount": 2040.0,
      "error": null
    }
    // ... more rows
  ],

  // NEW: Pagination metadata
  "pageNumber": 0,
  "pageSize": 20,
  "totalPages": 2,
  "totalElements": 21,
  "first": true,
  "last": false
}
```

### Response Fields Explained:

- `pageNumber`: Current page (0 = first page, 1 = second page, etc.)
- `pageSize`: Items per page
- `totalPages`: Total number of pages
- `totalElements`: Total number of items across all pages
- `first`: Is this the first page? (boolean)
- `last`: Is this the last page? (boolean)

---

## 🔍 Sortable Fields

You can sort by any of these fields:

| Field             | Description              | Example Values       |
| ----------------- | ------------------------ | -------------------- |
| `createdAt`       | When record was created  | 2025-09-30T08:19:43Z |
| `updatedAt`       | When record was updated  | 2025-09-30T08:19:43Z |
| `employeeCode`    | Employee code            | "EMP001"             |
| `jobTitle`        | Job title                | "HR Manager"         |
| `yearsExperience` | Years of experience      | 10                   |
| `perfBucket`      | Performance bucket (1-3) | 2                    |
| `currentSalary`   | Current salary           | 12000.0              |
| `midOfScale`      | Mid of scale             | 20000.0              |
| `compaRatio`      | Compa ratio              | 60                   |
| `increasePct`     | Increase percentage      | 17.0                 |
| `newSalary`       | New salary               | 14040.0              |

---

## 🚀 Why These Endpoints Are Better

### Old Endpoint (`/bulk/{batchId}/table`):

❌ Loads ALL 21 results into memory  
❌ Then manually slices to get page  
❌ Inefficient for large datasets  
❌ No sorting

### New Endpoints (`/results` and `/results/batch/{batchId}`):

✅ Database loads ONLY the requested page (e.g., 20 items)  
✅ MongoDB does the filtering, sorting, and counting  
✅ Efficient even with 10,000+ results  
✅ Full sorting support

---

## 💻 Testing with cURL

```bash
# Test getting all results (first page)
curl -X GET "http://localhost:8080/api/calc/results?page=0&size=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test getting batch results
curl -X GET "http://localhost:8080/api/calc/results/batch/2025-09-30T08:19:43.220131800Z?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 What Was Changed

### 1. Repository (`CalculationResultRepository.java`)

Added Spring Data pageable methods:

```java
Page<CalculationResult> findByClientId(String clientId, Pageable pageable);
Page<CalculationResult> findByClientIdAndBatchId(String clientId, String batchId, Pageable pageable);
```

### 2. DTO (`BulkResponse.java`)

Added pagination metadata fields:

- `pageNumber`, `pageSize`, `totalPages`, `totalElements`, `first`, `last`

### 3. Controller (`CalcController.java`)

Added 2 new endpoints:

- `GET /api/calc/results`
- `GET /api/calc/results/batch/{batchId}`

Plus a helper method `convertToRowResult()` to convert entities to DTOs.

---

## ✅ Ready to Use!

Your endpoints are ready to use. Start your Spring Boot application and test them with:

- Postman
- cURL
- Swagger UI: http://localhost:8080/swagger-ui.html

The endpoints automatically filter by your `clientId` (from JWT token), so each client only sees their own calculation results! 🔒
