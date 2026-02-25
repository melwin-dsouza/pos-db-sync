# Multi-Tenant POS Database Sync - Implementation Summary

## Overview
A complete multi-tenant POS database synchronization system built with Quarkus, featuring API Key authentication for POS agents and JWT for mobile app owners.

## Project Structure

### Entities (com.posdb.sync.entity)
- **Restaurant.java** - Restaurant master data with unique API key
- **User.java** - Restaurant owners/managers with JWT role-based access
- **OrderHeader.java** - Order header records with multi-field support
- **OrderPayment.java** - Payment records linked to orders

### DTOs (com.posdb.sync.dto)
- **RestaurantRequest/Response** - Restaurant onboarding
- **OwnerRequest/Response** - Owner creation with auto-generated passwords
- **LoginRequest/Response** - Authentication with JWT token
- **ChangePasswordRequest/Response** - Password management
- **OrderHeaderData/SyncRequest** - Order header bulk sync
- **OrderPaymentData/SyncRequest** - Payment bulk sync
- **SyncResponse** - Standardized sync operation response
- **DailyOrderResponse** - Dashboard reporting
- **ErrorResponse** - Standardized error format

### Services (com.posdb.sync.service)
- **RandomKeyGenerator** - Generates 32-char API keys and 8-char passwords
- **PasswordUtil** - BCrypt password hashing/verification
- **JwtProvider** - JWT token generation with 24-hour expiry
- **ApiKeyValidator** - API key extraction and validation

### Resources (REST Endpoints)
- **AdminResource** (`/api/v1/admin`)
  - `POST /restaurants` - Create restaurant (generates API key)
  - `POST /restaurants/{id}/owners` - Create owner (generates password)

- **AuthResource** (`/api/v1/auth`)
  - `POST /login` - Owner login with JWT token generation

- **OwnerResource** (`/api/v1/owner`)
  - `POST /change-password` - Password change with current password verification

- **OrderSyncResource** (`/api/v1/pos`)
  - `POST /orderheaders/sync` - Bulk sync order headers (max 500/batch)
  - `POST /orderpayments/sync` - Bulk sync payments (max 500/batch)

- **DashboardResource** (`/api/v1/dashboard`)
  - `GET /daily?from=&to=` - Daily order counts by type
  - `GET /orders?from=&to=&limit=&offset=` - Order list with pagination

### Filters
- **BasicAuthFilter** - HTTP Basic Auth for admin endpoints (hashed credentials)

## Configuration (application.properties)

### Database
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/pos_db}
quarkus.hibernate-orm.database.generation=validate
```

### Authentication
```properties
admin.username=${ADMIN_USERNAME:admin}
admin.password.hash=${ADMIN_PASSWORD_HASH:<bcrypt_hash>}

quarkus.smallrye-jwt.verify.issuer=${JWT_ISSUER:pos-db-sync}
quarkus.smallrye-jwt.audience=${JWT_AUDIENCE:pos-mobile-app}
```

### Logging
```properties
quarkus.log.level=INFO
quarkus.log.category."com.posdb.sync".level=DEBUG
```

## Database Schema (schema.sql)

### Tables
1. **restaurant** - 1 row per restaurant
   - Unique plaintext API key
   - Status tracking (ACTIVE/INACTIVE)

2. **users** - Restaurant staff
   - Unique email per restaurant
   - BCrypt password hash
   - `must_change_password` flag for first login

3. **order_headers** - Order master
   - Multi-field support (table, driver, discounts, etc.)
   - TIMESTAMP WITH TIME ZONE for all dates
   - Unique constraint on (restaurant_id, order_id)

4. **order_payments** - Payment records
   - Linked to orders
   - Multiple payment methods
   - Unique constraint on (restaurant_id, order_payment_id)

### Indexes
- All tables indexed on restaurant_id for multi-tenant isolation
- Date indexes for time-range queries
- Email indexes for user lookups

## Authentication Flows

### Admin Endpoints (Basic Auth)
```
1. Admin provides Base64(username:password) in Authorization header
2. Server decodes and verifies against BCrypt hash in config
3. Allows restaurant/owner creation
```

### POS Sync (API Key)
```
1. POS agent sends X-API-KEY header
2. ApiKeyValidator extracts and verifies key exists and is ACTIVE
3. Extracts restaurant_id and adds to request context
4. Persists data with automatic restaurant_id isolation
```

### Mobile Owner (JWT)
```
1. Owner sends email + password to /api/v1/auth/login
2. Server validates credentials against database
3. Generates JWT containing: user_id, restaurant_id, role
4. Token expiry: 24 hours
5. Mobile app stores token securely and sends in Authorization header
6. Backend extracts restaurant_id from token for data filtering
```

### First Login Password Change
```
1. When owner is created, must_change_password = true
2. Login returns flag to mobile app
3. App prompts user to change password
4. POST /api/v1/owner/change-password requires current password
5. Server clears must_change_password flag
```

## Key Features

### Multi-Tenancy
- Single database, single schema
- All tables have restaurant_id column
- All queries filtered by restaurant_id
- No database isolation overhead

### Security
- Plaintext API keys (as requested) - store in secure vault in production
- BCrypt password hashing (strength: 10 rounds)
- JWT token-based mobile auth with 24h expiry
- Basic auth for admin endpoints
- Role-based access control (OWNER/MANAGER)

### Bulk Operations
- Order header sync: max 500 records per batch
- Payment sync: max 500 records per batch
- Per-record error handling - partial success allowed
- Detailed response: total, success, failed counts

### Logging
- SLF4J throughout
- INFO level for business events
- DEBUG level for detailed flow
- WARN level for validation failures
- ERROR level for exceptions

### Error Handling
- Standardized JSON error format
- HTTP status codes: 400 (bad request), 401 (auth), 404 (not found), 500 (error)
- Error codes: INVALID_INPUT, UNAUTHORIZED, NOT_FOUND, BATCH_SIZE_EXCEEDED, etc.

## Deployment Instructions

### Prerequisites
1. PostgreSQL 12+ database running
2. Java 17+ JDK
3. Maven 3.8+

### Step 1: Create Database
```bash
createdb pos_db
```

### Step 2: Execute Schema
```bash
psql -U postgres -d pos_db -f src/main/resources/schema.sql
```

### Step 3: Generate Admin Password Hash
```bash
# Use any bcrypt tool to hash your admin password, e.g.:
# Hash "admin123" with BCrypt -> $2a$10$R9h7cIPz0OV8jGskHyQLaOYVYXiWXoNFWwZvgW.MqQwWEKNAqEQJW
```

### Step 4: Set Environment Variables
```bash
export DB_USER=postgres
export DB_PASSWORD=your_password
export DB_URL=jdbc:postgresql://localhost:5432/pos_db
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD_HASH=$2a$10$R9h7cIPz0OV8jGskHyQLaOYVYXiWXoNFWwZvgW.MqQwWEKNAqEQJW
export JWT_ISSUER=pos-db-sync
export JWT_AUDIENCE=pos-mobile-app
```

### Step 5: Build & Run
```bash
# Development mode (live reload)
./mvnw quarkus:dev

# Build JAR
./mvnw clean package

# Run JAR
java -jar target/quarkus-app/quarkus-run.jar
```

## API Testing Examples

### 1. Create Restaurant (requires Basic Auth)
```bash
curl -X POST http://localhost:8080/api/v1/admin/restaurants \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"name": "Pizza Palace"}'

# Response: {
#   "id": "550e8400-e29b-41d4-a716-446655440000",
#   "name": "Pizza Palace",
#   "apiKey": "aBcDeFgHiJkLmNoPqRsTuVwXyZ123456"
# }
```

### 2. Create Owner
```bash
curl -X POST http://localhost:8080/api/v1/admin/restaurants/550e8400-e29b-41d4-a716-446655440000/owners \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"email": "owner@pizzapalace.com", "role": "OWNER"}'

# Response: {
#   "email": "owner@pizzapalace.com",
#   "password": "Xy7zQ2pL",
#   "role": "OWNER",
#   "mustChangePassword": true
# }
```

### 3. Login (Mobile Owner)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "owner@pizzapalace.com", "password": "Xy7zQ2pL"}'

# Response: {
#   "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "mustChangePassword": true
# }
```

### 4. Sync Order Headers (POS Agent)
```bash
curl -X POST http://localhost:8080/api/v1/pos/orderheaders/sync \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: aBcDeFgHiJkLmNoPqRsTuVwXyZ123456" \
  -d '{
    "orderHeaders": [
      {
        "orderId": 1001,
        "orderDateTime": "2026-02-25T10:30:00+05:30",
        "employeeId": 5,
        "stationId": 2,
        "orderType": "DINE_IN",
        "dineInTableId": 3,
        "amountDue": 250.50,
        "subTotal": 250.50,
        "guestNumber": 2,
        "rowGuid": "550e8400-e29b-41d4-a716-446655440001"
      }
    ]
  }'

# Response: {
#   "totalRecords": 1,
#   "successRecords": 1,
#   "failedRecords": 0,
#   "message": "Order headers synced successfully"
# }
```

### 5. Get Daily Orders (JWT Protected)
```bash
curl -X GET "http://localhost:8080/api/v1/dashboard/daily?from=2026-02-01T00:00:00Z&to=2026-02-28T23:59:59Z" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Response: [
#   {
#     "date": "2026-02-25",
#     "totalOrders": 5,
#     "ordersByType": {
#       "DINE_IN": 3,
#       "TAKEAWAY": 2
#     }
#   }
# ]
```

## Known Limitations & Future Enhancements

1. **No refresh tokens** - JWT expires in 24 hours (as requested)
2. **Plaintext API keys** - Secure with vault in production
3. **Admin credentials in config** - Use secure config service in production
4. **No rate limiting** - Consider adding for sync endpoints
5. **No audit logging** - Consider implementing for compliance
6. **Single JWT issuer** - Consider multi-tenant JWT setup if needed
7. **No data retention policies** - Implement purge strategies for old orders

## Troubleshooting

### Connection Error
```
Error: could not find a public key in <path>
Solution: Generate JWT keys - see Quarkus JWT guide
```

### Schema Validation Error
```
Error: "tables not found"
Solution: Execute schema.sql manually in PostgreSQL
```

### API Key Invalid
```
401: Invalid API key
Solution: Check API key in restaurant table, ensure status=ACTIVE
```

### JWT Token Invalid
```
401: Invalid token
Solution: Check token expiry (24h), verify signature, check issuer/audience
```

## Support & Maintenance
- Requires PostgreSQL 12+
- Quarkus version: 3.31.4
- Java version: 17+
- Framework: Quarkus with REST, JPA/Hibernate, SmallRye JWT
- ORM: Hibernat ORM Panache (Quarkus simplified JPA)

