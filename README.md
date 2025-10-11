# DVD Rental

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue)
![React](https://img.shields.io/badge/React-18-61dafb)
![Docker](https://img.shields.io/badge/Docker-enabled-2496ed)

A comprehensive full-stack application for DVD rental management, providing secure and scalable services with SSL encryption, JWT authentication, and modern React frontend.

## 🏗️ Architecture

DVD Rental consists of:

- **🔐 Backend Service** (`443`) - REST API with authentication & business logic
- **⚛️ Frontend Service** (`5173`) - React + TypeScript + Vite
- **🗄️ PostgreSQL Database** - Production data storage
- **🧪 H2 Database** - In-memory testing database

### System Components

- **Backend**: Spring Boot 3.5.6 with SSL/HTTPS
- **Frontend**: React 18 with Vite dev server
- **Database**: PostgreSQL with connection pooling
- **Security**: JWT-based authentication with secure cookies
- **API Docs**: Swagger/OpenAPI at root path

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Docker Desktop** and **Docker Compose**
- **Java 21** or higher (for local development)
- **Maven 3.8+** (for local development)
- **Node.js 22** (for frontend development)
- **OpenSSL** (for certificate generation)

### Recommended Terminal/Shell:

- **Linux**: Terminal (bash/zsh)
- **macOS**: Terminal (bash/zsh)
- **Windows**: **Git Bash** (required for automation scripts)
  - Download Git Bash: [https://gitforwindows.org/](https://gitforwindows.org/)
  - Alternative: Use Windows Subsystem for Linux (WSL)

> **⚠️ Important for Windows Users**: You must use Git Bash to run the automation scripts. Command Prompt and PowerShell are not supported for the initialization scripts.

## 🚀 Quick Start

### 1. Initialize Project

The project includes automation scripts for easy setup. Run the initialization script from the project root:

**Linux/macOS/Git Bash:**

```bash
./scripts/init.sh
```

This script will:

- Check and install required dependencies (OpenSSL, Docker)
- Generate SSL certificates for backend (PKCS12 format)
- Create secure environment files (`.env`) with auto-generated:
  - SSL keystore password
  - PostgreSQL database credentials
  - 512-byte JWT secret
- Configure application properties for:
  - Backend service (`backend/src/main/resources/application.properties`)
  - Test environment (`backend/src/test/resources/application.properties`)
- Set up Docker volumes for node_modules
- Create necessary directories (certs, logs)

> **📋 Generated Files**: The script creates all required configuration files automatically. These files contain sensitive data and are gitignored.

### 2. Application Configuration

The initialization script automatically creates application.properties files with all required configurations.

#### 2.1 Automatic Configuration

The `init.sh` script generates complete application.properties including:

**Backend Service** (`backend/src/main/resources/application.properties`):

```properties
# Application Name & Profile
spring.application.name=backend
spring.profiles.active=dev

# SSL/TLS Configuration
server.port=443
server.ssl.enabled=true
server.ssl.key-store=/certs/backend.p12
server.ssl.key-store-password=[AUTO-GENERATED]
server.ssl.key-store-type=PKCS12

# Database Configuration
spring.datasource.url=jdbc:postgresql://cine-rent-db:5432/dvd_rental
spring.datasource.username=backend
spring.datasource.password=[AUTO-GENERATED]
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=20

# JPA/Hibernate Configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always

# JWT Configuration
jwt.secret=[AUTO-GENERATED-512-BYTES]
jwt.cookie.secure=true
jwt.expiration=300000
jwt.cookie.maxAge=360
jwt.refresh.expiration=86400000
jwt.refresh.cookie.maxAge=90000

# File Upload Configuration
spring.servlet.multipart.max-file-size=-1
spring.servlet.multipart.max-request-size=-1
spring.servlet.multipart.file-size-threshold=10MB
spring.servlet.multipart.location=/tmp

# Server Optimization
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/javascript,application/json
server.compression.min-response-size=1024
server.tomcat.threads.max=400
server.tomcat.threads.min-spare=50
server.tomcat.max-connections=10000
server.tomcat.accept-count=500
server.tomcat.connection-timeout=60000
server.tomcat.max-http-header-size=65536
server.tomcat.max-swallow-size=-1

# Error Handling
server.error.include-stacktrace=never
server.error.include-exception=false
server.error.include-message=always

# Logging Configuration
logging.file.name=logs/application.log
logging.level.pl.kielce.tu.backend=INFO
logging.level.root=INFO
logging.pattern.file=%d{yyyy-MM-dd} %d{HH:mm:ss} [%level] %logger{36} - %msg%n

# Swagger/OpenAPI Documentation
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/
```

**Test Environment** (`backend/src/test/resources/application.properties`):

```properties
# Test Profile
spring.profiles.active=test

# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Test Server Configuration
server.port=8080
server.ssl.enabled=false

# Test JWT Configuration
jwt.cookie.secure=false

# Test Logging
logging.file.name=logs/test-application.log
logging.level.pl.kielce.tu.backend=DEBUG

# Disable Swagger in Tests
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

#### 2.2 Configuration Notes

- **SSL/TLS**: External communication uses HTTPS with self-signed certificates
- **Database**: PostgreSQL for production, H2 for tests
- **JWT**: 512-byte secret with secure cookie configuration
- **API Documentation**: Swagger UI available at root path (`/`)
- **Logging**: Separate log files for application and tests

> **⚠️ Important**: Do not manually edit application.properties if you plan to re-run the init script, as it will overwrite your changes. The script regenerates all configuration files.

### 3. Development Setup (Docker Compose)

#### 3.1 Run Full Stack

From the project root directory:

```bash
docker compose up --build
```

This starts:

- **Backend**: Spring Boot application with PostgreSQL
- **Frontend**: React development server with hot reload
- **Database**: PostgreSQL with persistent volume

#### 3.2 Access Services

Once running, services are available at:

- **Backend API**: https://localhost:4443
- **Swagger UI**: https://localhost:4443/
- **Frontend**: http://localhost:5173
- **PostgreSQL**: localhost:5432 (for DB tools)

> **Note**: Accept SSL certificates in your browser for HTTPS endpoints (self-signed certificates for development)

#### 3.3 Stop Services

```bash
docker compose down
```

To remove volumes as well:

```bash
docker compose down -v
```

## 🧪 Testing

The backend includes comprehensive testing with H2 in-memory database.

### Run Tests

```bash
cd backend
./mvnw test
```

### Test Configuration

Tests automatically use:

- H2 in-memory database (no PostgreSQL required)
- Disabled SSL (HTTP only)
- Separate logging configuration
- In-memory JWT configuration

## 🛠️ Development

### Project Structure

```
dvd-rental/
├── backend/                            # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                   # Java source code
│   │   │   │   └── pl/kielce/tu/backend/
│   │   │   └── resources/
│   │   │       └── application.properties  # Main config (generated)
│   │   └── test/
│   │       ├── java/                   # Test source code
│   │       └── resources/
│   │           └── application.properties  # Test config (generated)
│   ├── Dockerfile                      # Production build
│   ├── Dockerfile.dev                  # Development build
│   └── pom.xml                         # Maven configuration
├── frontend/                           # React Frontend
│   ├── src/                            # React source code
│   │   ├── App.tsx                     # Main component
│   │   ├── main.tsx                    # Entry point
│   │   └── assets/                     # Static assets
│   ├── public/                         # Public assets
│   ├── Dockerfile                      # Production build (Nginx)
│   ├── Dockerfile.dev                  # Development build (Vite)
│   ├── package.json                    # Node dependencies
│   └── vite.config.ts                  # Vite configuration
├── scripts/                            # Automation scripts
│   ├── init.sh                         # Initialization script
│   └── cleanup.sh                      # Cleanup utility
├── certs/                              # SSL certificates (generated)
├── logs/                               # Application logs (generated)
├── .env                                # Environment variables (generated)
├── compose.yaml                        # Docker Compose configuration
└── README.md                           # This file
```

### Generated Files (Gitignored)

The initialization script creates:

- **SSL Certificates**: `certs/backend.p12` with auto-generated password
- **Environment File**: `.env` with database credentials and JWT secret
- **Application Properties**:
  - `backend/src/main/resources/application.properties` (production)
  - `backend/src/test/resources/application.properties` (testing)
- **Logs Directory**: `logs/` for application logs

### Docker Configuration

#### Backend Dockerfiles

**Production (`backend/Dockerfile`):**

- Multi-stage build with Eclipse Temurin JDK 21
- Distroless runtime for security
- SSL certificate generation via OpenSSL
- Optimized for production deployment

**Development (`backend/Dockerfile.dev`):**

- Eclipse Temurin JDK 21
- Maven wrapper for builds
- Hot reload support
- Debug-friendly configuration

#### Frontend Dockerfiles

**Production (`frontend/Dockerfile`):**

- Distroless Node.js 22 for building
- Unprivileged Nginx for serving
- Port 8080 (non-privileged)
- Optimized static assets

**Development (`frontend/Dockerfile.dev`):**

- Distroless Node.js 22
- Vite dev server with hot reload
- Port 5173
- Named volume for node_modules

## 🔒 Security

- **SSL/TLS**: All backend communications encrypted using HTTPS
- **JWT Authentication**: Secure token-based authentication with HttpOnly cookies
- **Database Security**: Encrypted connections with auto-generated passwords
- **Environment Variables**: Sensitive data stored in `.env` file (gitignored)
- **Distroless Images**: Minimal attack surface for production containers
- **Non-Root Users**: All containers run as non-privileged users
- **512-byte JWT Secret**: Cryptographically secure secret generation

## 📚 API Documentation

Swagger/OpenAPI documentation is available at:

- **Development**: https://localhost:4443/
- **API Docs JSON**: https://localhost:4443/api-docs

Features:

- Interactive API testing
- Request/response schemas
- Authentication support
- Try-it-out functionality

## 🔧 Maintenance

### Cleanup Development Environment

```bash
./scripts/cleanup.sh
```

This removes:

- Generated certificates and environment files
- Application properties files
- Build artifacts (`target/`)
- Docker volumes and containers
- IDE configuration files
- Node modules and frontend build artifacts
- Log files

### Regenerate Configuration

```bash
./scripts/cleanup.sh
./scripts/init.sh
docker compose up --build
```

## 📝 Development Guidelines

### Application Properties Management

When modifying application.properties files:

1. **For development**: Manually edit the files after running `init.sh`
2. **For permanent changes**: Modify the `init.sh` script templates directly
3. **Before commits**: Ensure configuration changes don't include secrets
4. **Team members**: Run `./scripts/init.sh` after pulling changes

### Environment Variables

The `.env` file contains:

```bash
POSTGRES_USER=backend
POSTGRES_PASSWORD=[AUTO-GENERATED]
POSTGRES_DB=dvd_rental
SSL_KEYSTORE_PASSWORD=[AUTO-GENERATED]
JWT_SECRET=[AUTO-GENERATED-512-BYTES]
```

### Frontend Development

The frontend uses:

- **React 18**: Modern React with hooks
- **TypeScript**: Type-safe development
- **Vite**: Fast HMR and building
- **Hot Reload**: Automatic browser refresh on changes

To develop frontend locally (without Docker):

```bash
cd frontend
npm install
npm run dev
```

### Backend Development

To run backend locally (without Docker):

```bash
cd backend
./mvnw spring-boot:run
```

Requirements:

- Java 21
- PostgreSQL running locally or via Docker
- SSL certificates generated via `init.sh`

## 🐛 Troubleshooting

### Common Issues

1. **SSL Certificate Errors**:

   - Accept self-signed certificates in browser
   - Click "Advanced" → "Proceed to localhost"

2. **Port Conflicts**:

   - Ensure ports 4443 (backend), 5173 (frontend), 5432 (postgres) are available
   - Check with: `lsof -i :4443` (macOS/Linux) or `netstat -ano | findstr :4443` (Windows)

3. **Docker Issues**:

   - Restart Docker Desktop
   - Clear volumes: `docker compose down -v`
   - Remove old images: `docker system prune -a`

4. **Permission Denied (Windows)**:

   - Use Git Bash, not Command Prompt/PowerShell
   - Run Git Bash as Administrator if needed

5. **Maven Build Failures**:

   - Ensure Java 21 is installed: `java -version`
   - Set JAVA_HOME environment variable
   - Clear Maven cache: `rm -rf ~/.m2/repository`

6. **Frontend Hot Reload Not Working**:

   - Ensure named volume is used in docker-compose
   - Check Vite configuration in `vite.config.ts`
   - Verify file watching is enabled: `docker compose logs frontend`

7. **Database Connection Failed**:

   - Verify PostgreSQL is running: `docker compose ps`
   - Check credentials in `.env` file
   - Ensure database initialization completed: `docker compose logs cine-rent-db`

8. **JWT Token Issues**:
   - Verify JWT secret length is 512 bytes in application.properties
   - Check cookie settings match SSL configuration
   - Clear browser cookies and try again

### Windows-Specific Issues

- **Scripts don't run**: Use Git Bash instead of Command Prompt/PowerShell
- **SSL generation fails**: Ensure OpenSSL is installed and in PATH
- **Docker connectivity**: Ensure Docker Desktop is running with WSL2 backend
- **File permissions**: Git Bash handles Unix permissions automatically

### Viewing Logs

```bash
# All services
docker compose logs

# Specific service
docker compose logs backend
docker compose logs frontend

# Follow logs
docker compose logs -f backend
```

### Database Access

Connect to PostgreSQL:

```bash
docker compose exec cine-rent-db psql -U backend -d dvd_rental
```

Or use a GUI tool:

- **Host**: localhost
- **Port**: 5432
- **Database**: dvd_rental
- **Username**: backend
- **Password**: (from `.env` file)

## 🚀 Production Deployment

### Building for Production

```bash
# Build all images
docker compose -f compose.yaml build

# Or build individually
docker build -t dvd-rental-backend:latest -f backend/Dockerfile backend/
docker build -t dvd-rental-frontend:latest -f frontend/Dockerfile frontend/
```

### Environment Variables for Production

Create a production `.env` file with:

- Strong database passwords (not auto-generated)
- Production JWT secret
- Valid SSL certificates (not self-signed)
- Production database URLs
