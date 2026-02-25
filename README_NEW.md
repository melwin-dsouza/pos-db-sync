# pos-db-sync - Multi-Tenant POS Database Synchronization

A production-ready multi-tenant POS order synchronization system built with Quarkus, featuring API Key authentication for POS agents and JWT-based mobile app access.

## 🚀 Features

- **Multi-Tenancy**: Single database with restaurant-level isolation via `restaurant_id`
- **Bulk Sync**: Order headers and payments sync (max 500 records/batch)
- **Security**: Basic Auth for admin, API Keys for POS agents, JWT for mobile owners
- **Reporting**: Daily order counts, order listings with time-range filters
- **Logging**: Comprehensive SLF4J logging throughout
- **Error Handling**: Standardized JSON error responses
- **Password Management**: Auto-generated passwords with first-login change requirement

## 📋 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 12+

### Setup (5 minutes)

1. **Create database**
   ```bash
   psql -U postgres -c "CREATE DATABASE pos_db;"
   ```

2. **Execute schema**
   ```bash
   psql -U postgres -d pos_db -f src/main/resources/schema.sql
   ```

3. **Set environment variables**
   ```bash
   export DB_USER=postgres
   export DB_PASSWORD=postgres
   export DB_URL=jdbc:postgresql://localhost:5432/pos_db
   export ADMIN_USERNAME=admin
   export ADMIN_PASSWORD_HASH='$2a$10$R9h7cIPz0OV8jGskHyQLaOYVYXiWXoNFWwZvgW.MqQwWEKNAqEQJW'
   export JWT_ISSUER=pos-db-sync
   export JWT_AUDIENCE=pos-mobile-app
   ```

4. **Start application**
   ```bash
   ./mvnw quarkus:dev
   ```

5. **Test API**
   ```bash
   curl -X POST http://localhost:8080/api/v1/admin/restaurants \
     -H "Content-Type: application/json" \
     -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
     -d '{"name": "Test Restaurant"}'
   ```

For detailed setup, see [QUICKSTART.md](QUICKSTART.md)

## 📚 Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute setup guide
- **[API_REFERENCE.md](API_REFERENCE.md)** - All 8 endpoints with examples
- **[IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)** - Architecture & design details
- **[DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)** - Production deployment guide

## 🔌 API Endpoints

### Admin APIs (Basic Auth)
- `POST /api/v1/admin/restaurants` - Create restaurant with auto-generated API key
- `POST /api/v1/admin/restaurants/{id}/owners` - Create owner with auto-generated password

### Auth APIs
- `POST /api/v1/auth/login` - Login and get JWT token

### Owner APIs (JWT)
- `POST /api/v1/owner/change-password` - Change password

### POS Sync APIs (API Key)
- `POST /api/v1/pos/orderheaders/sync` - Bulk sync order headers
- `POST /api/v1/pos/orderpayments/sync` - Bulk sync payments

### Dashboard APIs (JWT)
- `GET /api/v1/dashboard/daily?from=&to=` - Daily order counts by type
- `GET /api/v1/dashboard/orders?from=&to=` - Orders list with pagination

## 🏗️ Architecture

### Database Schema
- **restaurant** - Restaurant master data with plaintext API keys
- **users** - Restaurant owners/managers with role-based access
- **order_headers** - Order records with 15+ fields
- **order_payments** - Payment details linked to orders

All tables use UUID primary keys and include `restaurant_id` for multi-tenant isolation.

### Authentication Flows
1. **Admin**: HTTP Basic Auth with BCrypt password hash
2. **POS Agent**: X-API-KEY header (plaintext, stored in DB)
3. **Mobile Owner**: JWT token (24-hour expiry) containing user_id, restaurant_id, role

### Security
- BCrypt password hashing (10 rounds)
- API key validation against active restaurants
- JWT token with standard claims
- Role-based access control (OWNER, MANAGER)

## 🛠️ Development

### Running in dev mode (hot reload)
```bash
./mvnw quarkus:dev
```
Available at: http://localhost:8080

### Building for production
```bash
./mvnw clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

### Running tests
```bash
./mvnw test
```

## 📊 Database

### Setup
```bash
# Create database
createdb pos_db

# Execute schema
psql -U postgres -d pos_db -f src/main/resources/schema.sql

# Verify tables
psql -U postgres -d pos_db -c "\dt"
```

### Configuration
```properties
# src/main/resources/application.properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/pos_db}
quarkus.hibernate-orm.database.generation=validate
```

## 🔐 Security Configuration

### Admin Credentials (Change Before Production)
- Username: `admin`
- Password: `admin123`
- Hash: `$2a$10$R9h7cIPz0OV8jGskHyQLaOYVYXiWXoNFWwZvgW.MqQwWEKNAqEQJW`

### JWT Configuration
```properties
quarkus.smallrye-jwt.verify.issuer=${JWT_ISSUER:pos-db-sync}
quarkus.smallrye-jwt.audience=${JWT_AUDIENCE:pos-mobile-app}
```

## 📈 Performance

### Bulk Operations
- Order sync: Max 500 records per batch
- Partial success handling with detailed response
- Per-record error logging

### Query Performance
- Indexed on `restaurant_id` for multi-tenant isolation
- Indexed on `(restaurant_id, order_id)` for uniqueness
- Date indexes for time-range queries

### Storage Estimates (50 restaurants)
- ~50-100 MB database size
- ~10-15 MB growth per year
- Scales well for 365,000+ annual orders

## 🐳 Docker Support

Pre-configured Dockerfiles in `src/main/docker/`:
- `Dockerfile.jvm` - Regular JVM container
- `Dockerfile.native` - Native image (GraalVM)

## 📝 Logging

SLF4J logging throughout:
- **INFO**: Business operations (login, sync)
- **DEBUG**: Detailed flow and data
- **WARN**: Validation failures
- **ERROR**: Exceptions with stack traces

Configure in `application.properties`:
```properties
quarkus.log.level=INFO
quarkus.log.category."com.posdb.sync".level=DEBUG
```

## 🚢 Production Deployment

See [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) for:
- Systemd service configuration
- Database backup/recovery
- Monitoring and health checks
- Security hardening
- Upgrade procedures
- Troubleshooting guide

## 🆘 Support

### Common Issues
| Issue | Solution |
|-------|----------|
| Connection refused | PostgreSQL not running |
| Invalid API key | Check key in restaurant table |
| JWT token expired | Tokens expire in 24 hours |
| 401 unauthorized | Check credentials/headers |

See [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md#troubleshooting) for detailed troubleshooting.

## 📦 Technology Stack

- **Framework**: Quarkus 3.31.4
- **Database**: PostgreSQL 12+
- **ORM**: Hibernate ORM + Panache
- **Auth**: JWT (SmallRye), Basic Auth
- **Hashing**: BCrypt
- **Logging**: SLF4J with Logmanager
- **Build**: Maven 3.8+
- **Java**: 17+

## 📄 License

This project is part of the Aldelo POS system.

## 🔗 Related Documentation

- [Quarkus Guide](https://quarkus.io/guides/)
- [SmallRye JWT](https://smallrye.io/smallrye-jwt/)
- [Hibernate ORM](https://hibernate.org/orm/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)

## 📞 Support & Maintenance

For issues or questions:
1. Check [QUICKSTART.md](QUICKSTART.md) for setup help
2. See [API_REFERENCE.md](API_REFERENCE.md) for endpoint details
3. Review [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) for architecture
4. Consult [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) for ops

---

**Last Updated**: February 25, 2026  
**Version**: 1.0.0

