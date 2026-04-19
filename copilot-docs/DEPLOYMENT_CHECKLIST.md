# Deployment Checklist

## Pre-Deployment Setup

### 1. Database Setup
- [ ] PostgreSQL 12+ installed and running
- [ ] Database `pos_db` created
- [ ] Schema executed from `src/main/resources/schema.sql`
- [ ] Verify tables exist: restaurant, users, order_headers, order_payments

### 2. JWT Configuration
- [ ] Generate private key: `openssl genrsa -out privateKey.pem 2048`
- [ ] Generate public key: `openssl rsa -in privateKey.pem -pubout -out publicKey.pem`
- [ ] Place keys in `src/main/resources/META-INF/resources/`
- [ ] Alternative: Use existing keys (see JWT guide)

### 3. Admin Credentials Setup
- [ ] Generate BCrypt hash for admin password
- [ ] Use online tool or Java code:
  ```java
  // In Java: 
  String password = "your_admin_password";
  String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(10));
  System.out.println(hash);
  ```
- [ ] Store hash in `ADMIN_PASSWORD_HASH` environment variable

### 4. Environment Variables
Set these before running:
```bash
# Database
export DB_USER=postgres
export DB_PASSWORD=<your_db_password>
export DB_URL=jdbc:postgresql://localhost:5432/pos_db

# Admin Auth
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD_HASH=$2a$10$<your_bcrypt_hash>

# JWT
export JWT_ISSUER=pos-db-sync
export JWT_AUDIENCE=pos-mobile-app
```

### 5. Build Verification
- [ ] Run: `./mvnw clean compile`
- [ ] Verify no compilation errors
- [ ] Run: `./mvnw clean package`
- [ ] Verify JAR created in `target/quarkus-app/`

---

## Development Testing

### 1. Start Application
```bash
./mvnw quarkus:dev
```

### 2. Test Admin API
```bash
# Encode base64: echo -n "admin:your_admin_password" | base64
curl -X POST http://localhost:8080/api/v1/admin/restaurants \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic <base64_encoded>" \
  -d '{"name": "Test Restaurant"}'
```

### 3. Test POS Sync
```bash
# Extract apiKey from previous response
curl -X POST http://localhost:8080/api/v1/pos/orderheaders/sync \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: <api_key>" \
  -d '{"orderHeaders": [{"orderId": 1, "orderDateTime": "2026-02-25T10:00:00Z", "amountDue": 100}]}'
```

### 4. Verify Database
```bash
psql -U postgres -d pos_db
SELECT * FROM restaurant;
SELECT * FROM users;
SELECT * FROM order_headers;
```

---

## Production Deployment

### 1. Build Production JAR
```bash
./mvnw clean package -DskipTests
```

### 2. Create Startup Script (startup.sh)
```bash
#!/bin/bash
set -e

# Source environment
source /etc/pos-db-sync/env.conf

# Run application
cd /opt/pos-db-sync
java -Xmx2g \
  -Dquarkus.datasource.username=$DB_USER \
  -Dquarkus.datasource.password=$DB_PASSWORD \
  -Dquarkus.datasource.jdbc.url=$DB_URL \
  -Dadmin.username=$ADMIN_USERNAME \
  -Dadmin.password.hash=$ADMIN_PASSWORD_HASH \
  -jar target/quarkus-app/quarkus-run.jar
```

### 3. Systemd Service File (/etc/systemd/system/pos-db-sync.service)
```ini
[Unit]
Description=POS Database Sync Service
After=network.target
Wants=postgresql.service

[Service]
Type=simple
User=posapp
WorkingDirectory=/opt/pos-db-sync
ExecStart=/opt/pos-db-sync/startup.sh
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=pos-db-sync
Environment="JAVA_OPTS=-Xmx2g -Xms1g"

[Install]
WantedBy=multi-user.target
```

### 4. Enable & Start Service
```bash
sudo systemctl daemon-reload
sudo systemctl enable pos-db-sync
sudo systemctl start pos-db-sync
sudo systemctl status pos-db-sync
```

### 5. Verify Logs
```bash
sudo journalctl -u pos-db-sync -f
```

---

## Monitoring & Maintenance

### Health Check
```bash
curl -s http://localhost:8080/q/health | jq .
```

### Check Running Processes
```bash
ps aux | grep quarkus
```

### Database Maintenance
```bash
# Daily backup
pg_dump -U postgres pos_db > /backups/pos_db_$(date +%Y%m%d).sql

# Check index usage
psql -U postgres -d pos_db
SELECT * FROM pg_stat_user_indexes WHERE idx_scan = 0;
```

### Application Logs
```bash
# Recent logs
tail -100 /var/log/pos-db-sync.log

# Search for errors
grep ERROR /var/log/pos-db-sync.log

# Real-time monitoring
tail -f /var/log/pos-db-sync.log
```

---

## Security Checklist

### Before Production
- [ ] Change default admin username
- [ ] Use strong admin password (>16 chars, mixed case, numbers, symbols)
- [ ] Generate new JWT keys (don't use development keys)
- [ ] Set database password (don't use default)
- [ ] Enable PostgreSQL SSL connections
- [ ] Use HTTPS for API endpoints
- [ ] Configure firewall rules
- [ ] Set up database backups
- [ ] Enable application logging
- [ ] Rotate JWT keys periodically
- [ ] Review and adjust API rate limits

### Secrets Management
- [ ] Use environment variables (not in code)
- [ ] Store secrets in secure vault (AWS Secrets Manager, HashiCorp Vault, etc.)
- [ ] Never commit `.env` files or private keys to git
- [ ] Use different credentials for dev/staging/prod
- [ ] Rotate credentials every 90 days

### API Security
- [ ] Use HTTPS/TLS (not HTTP)
- [ ] Implement rate limiting
- [ ] Add request validation
- [ ] Monitor for suspicious activity
- [ ] Log all authentication attempts
- [ ] Set up alerts for failed login attempts

---

## Troubleshooting

### Application Won't Start
```
Error: Failed to create connection pool
Solution: Check database credentials and URL
- Verify PostgreSQL is running
- Check connection string
- Test: psql -U postgres -h localhost -d pos_db
```

### API Key Invalid
```
Error: 401 Invalid API key
Solution:
- Verify API key in database: SELECT * FROM restaurant WHERE api_key = '...';
- Check key format (should be 32 chars)
- Ensure restaurant status = 'ACTIVE'
```

### JWT Token Invalid
```
Error: 401 Invalid token
Solution:
- Check token expiry (24 hours from generation)
- Verify private/public key pair
- Check issuer and audience
- Ensure Authorization header format: Bearer <token>
```

### Database Connection Pooling
```
Error: HikariPool - Connection is not available
Solution:
- Check max connections: show max_connections;
- Increase pool size in application.properties
- Restart application
```

### High Database Load
```
Monitor:
- SELECT * FROM pg_stat_statements;
- EXPLAIN ANALYZE for slow queries
- Check index usage: SELECT * FROM pg_stat_user_indexes;
Optimize:
- Add missing indexes
- Optimize queries
- Increase server resources
```

---

## Upgrade Procedure

### 1. Backup Database
```bash
pg_dump -U postgres pos_db > /backups/pos_db_backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. Build New Version
```bash
git pull
./mvnw clean package -DskipTests
```

### 3. Stop Application
```bash
sudo systemctl stop pos-db-sync
```

### 4. Backup Current JAR
```bash
cp /opt/pos-db-sync/target/quarkus-app/quarkus-run.jar \
   /backups/quarkus-run_backup_$(date +%Y%m%d_%H%M%S).jar
```

### 5. Deploy New Version
```bash
cp target/quarkus-app/quarkus-run.jar /opt/pos-db-sync/target/quarkus-app/
```

### 6. Start Application
```bash
sudo systemctl start pos-db-sync
```

### 7. Verify
```bash
sudo systemctl status pos-db-sync
curl -s http://localhost:8080/q/health
```

### 8. Rollback (if needed)
```bash
sudo systemctl stop pos-db-sync
cp /backups/quarkus-run_backup_*.jar /opt/pos-db-sync/target/quarkus-app/quarkus-run.jar
sudo systemctl start pos-db-sync
```

---

## Performance Tuning

### JVM Settings
```bash
# In startup script:
JAVA_OPTS="-Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Database Tuning
```sql
-- Increase shared_buffers for large datasets
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET work_mem = '16MB';
SELECT pg_reload_conf();
```

### Connection Pool
```properties
quarkus.datasource.jdbc.max-size=20
quarkus.datasource.jdbc.min-size=5
```

---

## Backup & Recovery

### Automated Backups
```bash
# Create backup script: /usr/local/bin/backup-pos-db.sh
#!/bin/bash
BACKUP_DIR="/backups/pos_db"
RETENTION_DAYS=30

mkdir -p $BACKUP_DIR
pg_dump -U postgres pos_db | gzip > $BACKUP_DIR/pos_db_$(date +%Y%m%d_%H%M%S).sql.gz

# Clean old backups
find $BACKUP_DIR -mtime +$RETENTION_DAYS -delete

# Crontab entry (daily 2 AM):
# 0 2 * * * /usr/local/bin/backup-pos-db.sh
```

### Recovery
```bash
# List available backups
ls -lh /backups/pos_db/

# Restore from backup
gunzip < /backups/pos_db/pos_db_20260225_020000.sql.gz | psql -U postgres pos_db

# Verify restoration
psql -U postgres -d pos_db -c "SELECT COUNT(*) FROM restaurant;"
```

---

## Support Contacts

- **Database Issues**: DBA Team
- **Application Issues**: Development Team
- **Infrastructure**: DevOps Team
- **Security Issues**: Security Team (security@company.com)

For urgent issues, contact on-call engineer.

