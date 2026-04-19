# POS Database Sync - API Reference

## Base URL
```
http://localhost:8080
```

---

## Admin APIs (Basic Auth Required)

### 1. Create Restaurant
**Endpoint:** `POST /api/v1/admin/restaurants`

**Authentication:** Basic Auth (username:password)

**Request Body:**
```json
{
  "name": "Restaurant Name"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Restaurant Name",
  "apiKey": "aBcDeFgHiJkLmNoPqRsTuVwXyZ123456"
}
```

**Error Responses:**
- `400 Bad Request`: Missing name
- `401 Unauthorized`: Invalid credentials
- `500 Internal Server Error`: Database error

---

### 2. Create Owner/Manager
**Endpoint:** `POST /api/v1/admin/restaurants/{restaurantId}/owners`

**Authentication:** Basic Auth (username:password)

**Path Parameters:**
- `restaurantId` (UUID): ID of the restaurant

**Request Body:**
```json
{
  "email": "owner@restaurant.com",
  "role": "OWNER"
}
```

**Response (201 Created):**
```json
{
  "email": "owner@restaurant.com",
  "password": "Xy7zQ2pL",
  "role": "OWNER",
  "mustChangePassword": true
}
```

**Error Responses:**
- `400 Bad Request`: Missing email, invalid restaurant ID, or duplicate email
- `401 Unauthorized`: Invalid credentials
- `404 Not Found`: Restaurant not found
- `500 Internal Server Error`: Database error

---

## Authentication APIs

### 3. Login
**Endpoint:** `POST /api/v1/auth/login`

**Authentication:** None

**Request Body:**
```json
{
  "email": "owner@restaurant.com",
  "password": "Xy7zQ2pL"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "mustChangePassword": true
}
```

**Error Responses:**
- `400 Bad Request`: Missing email or password
- `401 Unauthorized`: Invalid credentials
- `500 Internal Server Error`: Server error

**Token Expiry:** 24 hours

---

## Owner APIs (JWT Required)

### 4. Change Password
**Endpoint:** `POST /api/v1/owner/change-password`

**Authentication:** Bearer Token (JWT)

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body:**
```json
{
  "currentPassword": "Xy7zQ2pL",
  "newPassword": "NewPassword123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**Error Responses:**
- `400 Bad Request`: Missing passwords
- `401 Unauthorized`: Invalid current password or missing token
- `404 Not Found`: User not found
- `500 Internal Server Error`: Server error

---

## POS Sync APIs (API Key Required)

### 5. Sync Order Headers
**Endpoint:** `POST /api/v1/pos/orderheaders/sync`

**Authentication:** X-API-KEY Header

**Request Headers:**
```
X-API-KEY: aBcDeFgHiJkLmNoPqRsTuVwXyZ123456
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderHeaders": [
    {
      "orderId": 1001,
      "orderDateTime": "2026-02-25T10:30:00+05:30",
      "employeeId": 5,
      "stationId": 2,
      "orderType": "DINE_IN",
      "dineInTableId": 3,
      "driverEmployeeId": null,
      "discountId": null,
      "discountAmount": null,
      "amountDue": 250.50,
      "cashDiscountAmount": null,
      "cashDiscountApprovalEmpId": null,
      "subTotal": 250.50,
      "guestNumber": 2,
      "editTimestamp": null,
      "rowGuid": "550e8400-e29b-41d4-a716-446655440001"
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "totalRecords": 1,
  "successRecords": 1,
  "failedRecords": 0,
  "message": "Order headers synced successfully"
}
```

**Error Responses:**
- `400 Bad Request`: Missing/empty list or batch size > 500
- `401 Unauthorized`: Missing or invalid API key
- `500 Internal Server Error`: Server error

**Constraints:**
- Maximum 500 records per batch
- Unique constraint on (restaurant_id, order_id)
- Duplicate records are silently skipped

---

### 6. Sync Order Payments
**Endpoint:** `POST /api/v1/pos/orderpayments/sync`

**Authentication:** X-API-KEY Header

**Request Headers:**
```
X-API-KEY: aBcDeFgHiJkLmNoPqRsTuVwXyZ123456
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderPayments": [
    {
      "orderPaymentId": 2001,
      "paymentDateTime": "2026-02-25T10:35:00+05:30",
      "cashierId": 1,
      "nonCashierEmployeeId": null,
      "orderId": 1001,
      "paymentMethod": "CASH",
      "amountTendered": 300.00,
      "amountPaid": 250.50,
      "employeeComp": null,
      "rowGuid": "550e8400-e29b-41d4-a716-446655440002"
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "totalRecords": 1,
  "successRecords": 1,
  "failedRecords": 0,
  "message": "Order payments synced successfully"
}
```

**Error Responses:**
- `400 Bad Request`: Missing/empty list or batch size > 500
- `401 Unauthorized`: Missing or invalid API key
- `500 Internal Server Error`: Server error

**Constraints:**
- Maximum 500 records per batch
- Unique constraint on (restaurant_id, order_payment_id)

---

## Dashboard APIs (JWT Required)

### 7. Get Daily Orders Report
**Endpoint:** `GET /api/v1/dashboard/daily`

**Authentication:** Bearer Token (JWT)

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Query Parameters:**
- `from` (required): ISO 8601 DateTime (e.g., 2026-02-01T00:00:00Z)
- `to` (required): ISO 8601 DateTime (e.g., 2026-02-28T23:59:59Z)

**Example Request:**
```
GET /api/v1/dashboard/daily?from=2026-02-01T00:00:00Z&to=2026-02-28T23:59:59Z
```

**Response (200 OK):**
```json
[
  {
    "date": "2026-02-25",
    "totalOrders": 5,
    "ordersByType": {
      "DINE_IN": 3,
      "TAKEAWAY": 2
    }
  },
  {
    "date": "2026-02-24",
    "totalOrders": 8,
    "ordersByType": {
      "DINE_IN": 5,
      "DELIVERY": 3
    }
  }
]
```

**Error Responses:**
- `400 Bad Request`: Missing date parameters or invalid date format
- `401 Unauthorized`: Missing/invalid JWT token
- `500 Internal Server Error`: Server error

---

### 8. Get Orders List
**Endpoint:** `GET /api/v1/dashboard/orders`

**Authentication:** Bearer Token (JWT)

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Query Parameters:**
- `from` (required): ISO 8601 DateTime
- `to` (required): ISO 8601 DateTime
- `limit` (optional): Number of records, default 100
- `offset` (optional): Pagination offset, default 0

**Example Request:**
```
GET /api/v1/dashboard/orders?from=2026-02-01T00:00:00Z&to=2026-02-28T23:59:59Z&limit=20&offset=0
```

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "orderId": 1001,
    "orderDateTime": "2026-02-25T10:30:00+05:30",
    "orderType": "DINE_IN",
    "amountDue": 250.50,
    "subTotal": 250.50
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "orderId": 1002,
    "orderDateTime": "2026-02-25T11:00:00+05:30",
    "orderType": "TAKEAWAY",
    "amountDue": 175.00,
    "subTotal": 175.00
  }
]
```

**Error Responses:**
- `400 Bad Request`: Missing date parameters
- `401 Unauthorized`: Missing/invalid JWT token
- `500 Internal Server Error`: Server error

---

## Error Response Format

All error responses follow this format:

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2026-02-25T10:30:00.123456+05:30"
}
```

**Common Error Codes:**
- `INVALID_INPUT` - Request validation failed
- `UNAUTHORIZED` - Authentication failed
- `NOT_FOUND` - Resource not found
- `BATCH_SIZE_EXCEEDED` - Too many records in batch
- `DUPLICATE_EMAIL` - Email already exists
- `INVALID_PASSWORD` - Password validation failed
- `INTERNAL_ERROR` - Server error

---

## Authentication Details

### Basic Auth (Admin APIs)
- Format: Base64 encoded `username:password`
- Header: `Authorization: Basic <base64_encoded_credentials>`
- Example: `Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=`

### API Key (POS Sync APIs)
- Header: `X-API-KEY: <plaintext_api_key>`
- 32-character random string
- Tied to specific restaurant
- Example: `X-API-KEY: aBcDeFgHiJkLmNoPqRsTuVwXyZ123456`

### JWT Token (Owner APIs)
- Format: Bearer token (RFC 7519)
- Header: `Authorization: Bearer <jwt_token>`
- Expiry: 24 hours from generation
- Contains: user_id, restaurant_id, role
- Example: `Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...`

---

## Rate Limiting
Currently no rate limiting implemented. Consider adding for production.

## Pagination
- `limit`: Maximum 1000 records per request
- `offset`: Starting position (0-based)
- Default limit: 100

## Timestamps
All timestamps use ISO 8601 format with timezone:
- Format: `YYYY-MM-DDTHH:mm:ss±HH:MM`
- Example: `2026-02-25T10:30:00+05:30`
- Database: Stored as TIMESTAMP WITH TIME ZONE

