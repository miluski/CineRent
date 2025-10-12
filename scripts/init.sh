#!/bin/bash
set -euo pipefail

detect_os() {
    case "$OSTYPE" in
        "darwin"*) echo "macos" ;;
        "linux-gnu"*|"linux"*) echo "linux" ;;
        "msys"|"win32") echo "windows" ;;
        *) echo "unknown" ;;
    esac
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

install_macos_deps() {
    if ! command_exists brew; then
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    if ! command_exists openssl; then
        brew install openssl
    fi
}

install_linux_deps() {
    if command_exists apt-get; then
        sudo apt-get update
        if ! command_exists openssl; then
            sudo apt-get install -y openssl
        fi
    elif command_exists yum; then
        if ! command_exists openssl; then
            sudo yum install -y openssl
        fi
    else
        echo "Linux detected, but package manager not supported."
        echo "Please install openssl manually."
        exit 1
    fi
}

check_windows_environment() {
    if [ "$OS" = "windows" ] && [ -z "${MSYSTEM:-}" ]; then
        echo "❌ Windows detected but not running in Git Bash!"
        echo ""
        echo "Please run this script in Git Bash, not Command Prompt or PowerShell."
        echo ""
        echo "To use Git Bash:"
        echo "1. Right-click in your project folder"
        echo "2. Select 'Git Bash Here'"
        echo "3. Run the script again: ./scripts/init.sh"
        echo ""
        exit 1
    fi

    echo "Windows (Git Bash) detected. Please ensure the following are installed:"
    echo "1. Docker Desktop - https://www.docker.com/products/docker-desktop/"
    echo "2. Git for Windows (includes Git Bash and OpenSSL)"
    echo ""

    if ! command_exists openssl; then
        echo "❌ OpenSSL not found. Please install Git for Windows which includes OpenSSL."
        echo "Download from: https://git-scm.com/download/win"
        exit 1
    fi

    if ! command_exists docker; then
        echo "❌ Docker not found. Please install Docker Desktop."
        echo "Download from: https://www.docker.com/products/docker-desktop/"
        exit 1
    fi

    echo "✅ Git Bash environment detected and ready."
}

normalize_path() {
    local path="$1"
    if [ "$OS" = "windows" ]; then
        echo "$path" | sed 's|\\|/|g'
    else
        echo "$path"
    fi
}

get_absolute_path() {
    local path="$1"
    if [ "$OS" = "windows" ]; then
        (cd "$path" 2>/dev/null && pwd -W 2>/dev/null || pwd) || echo "$path"
    else
        (cd "$path" && pwd) || echo "$path"
    fi
}

echo "🚀 Initializing DVD Rental Backend Application..."

OS=$(detect_os)

if [ "$OS" = "unknown" ]; then
    echo "❌ Unsupported OS: $OSTYPE"
    echo "This script supports macOS, Linux, and Windows (Git Bash)."
    exit 1
fi

if [ "$OS" = "windows" ]; then
    check_windows_environment
fi

MISSING_DEPS=()
if ! command_exists openssl; then MISSING_DEPS+=("openssl"); fi

if [ ${#MISSING_DEPS[@]} -gt 0 ]; then
    echo "❌ Missing dependencies: ${MISSING_DEPS[*]}"
    if [ "$OS" = "macos" ]; then
        install_macos_deps
    elif [ "$OS" = "linux" ]; then
        install_linux_deps
    fi
else
    echo "✅ All required dependencies are installed."
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ "$OS" = "windows" ]; then
    SCRIPT_DIR=$(normalize_path "$SCRIPT_DIR")
    BASE_DIR=$(normalize_path "$BASE_DIR")
fi

BACKEND_DIR="$BASE_DIR/backend"
CERTS_DIR="$BASE_DIR/certs"
LOGS_DIR="$BASE_DIR/logs"
UPLOADS_DIR="$BASE_DIR/uploads"
POSTERS_DIR="$UPLOADS_DIR/posters"

echo "📁 Creating necessary directories..."
mkdir -p "$CERTS_DIR" "$LOGS_DIR" "$UPLOADS_DIR" "$POSTERS_DIR"

echo "🧹 Cleaning up previous Docker containers and volumes..."
docker compose down -v 2>/dev/null || true
docker volume rm \
    "$(basename "$BASE_DIR")_postgres-data" \
    "$(basename "$BASE_DIR")_backend-logs" \
    2>/dev/null || true

echo "🔐 Generating SSL certificates..."
SSL_PASSWORD=$(openssl rand -base64 18 | tr -d '/+=' | tr -cd '[:alnum:]' | head -c 16)
CERT_FILE="$CERTS_DIR/backend.p12"
KEY_FILE="$CERTS_DIR/backend.key"
CRT_FILE="$CERTS_DIR/backend.crt"

rm -f "$CERT_FILE" "$KEY_FILE" "$CRT_FILE"

if [ "$OS" = "windows" ]; then
    WIN_KEY_FILE=$(cygpath -w "$KEY_FILE" 2>/dev/null || echo "$KEY_FILE" | sed 's|^/\([a-z]\)/|\1:/|')
    MSYS_NO_PATHCONV=1 openssl genrsa -out "$WIN_KEY_FILE" 2048
else
    openssl genrsa -out "$KEY_FILE" 2048
fi

if [ "$OS" = "windows" ]; then
    WIN_KEY_FILE=$(cygpath -w "$KEY_FILE" 2>/dev/null || echo "$KEY_FILE" | sed 's|^/\([a-z]\)/|\1:/|')
    WIN_CRT_FILE=$(cygpath -w "$CRT_FILE" 2>/dev/null || echo "$CRT_FILE" | sed 's|^/\([a-z]\)/|\1:/|')
    MSYS_NO_PATHCONV=1 openssl req -new -x509 -key "$WIN_KEY_FILE" -out "$WIN_CRT_FILE" -days 365 \
        -subj "//C=PL/ST=Swietokrzyskie/L=Kielce/O=DVD Rental/OU=Development/CN=backend"
else
    openssl req -new -x509 -key "$KEY_FILE" -out "$CRT_FILE" -days 365 \
        -subj "/C=PL/ST=Swietokrzyskie/L=Kielce/O=DVD Rental/OU=Development/CN=backend"
fi

if [ "$OS" = "windows" ]; then
    WIN_KEY_FILE=$(cygpath -w "$KEY_FILE" 2>/dev/null || echo "$KEY_FILE" | sed 's|^/\([a-z]\)/|\1:/|')
    WIN_CRT_FILE=$(cygpath -w "$CRT_FILE" 2>/dev/null || echo "$CRT_FILE" | sed 's|^/\([a-z]\)/|\1:/|')
    WIN_CERT_FILE=$(cygpath -w "$CERT_FILE" 2>/dev/null || echo "$CERT_FILE" | sed 's|^/\([a-z]\)/|\1:/|')
    MSYS_NO_PATHCONV=1 openssl pkcs12 -export -in "$WIN_CRT_FILE" -inkey "$WIN_KEY_FILE" \
        -out "$WIN_CERT_FILE" -name backend -password pass:"$SSL_PASSWORD"
else
    openssl pkcs12 -export -in "$CRT_FILE" -inkey "$KEY_FILE" \
        -out "$CERT_FILE" -name backend -password pass:"$SSL_PASSWORD"
fi

rm -f "$KEY_FILE" "$CRT_FILE"

echo "✅ SSL certificate generated: $CERT_FILE"

echo "🗄️ Generating database credentials..."
DB_USER="backend"
DB_PASSWORD=$(openssl rand -base64 18 | tr -d '/+=' | tr -cd '[:alnum:]' | head -c 16)
DB_NAME="dvd_rental"

echo "🔑 Generating JWT secrets..."
JWT_SECRET=$(openssl rand -base64 684 | tr -d '/+=' | tr -cd '[:alnum:]' | head -c 512)

echo "📁 Configuring media settings..."
MEDIA_POSTER_DIR="${MEDIA_POSTER_DIR:-/app/uploads/posters}"
MEDIA_POSTER_BASE_URL="${MEDIA_POSTER_BASE_URL:-/api/v1/resources/posters}"
MEDIA_POSTER_MAX_SIZE="${MEDIA_POSTER_MAX_SIZE:-5242880}"
MEDIA_POSTER_CACHE_CONTROL="${MEDIA_POSTER_CACHE_CONTROL:-public, max-age=31536000}"
MEDIA_POSTER_DEFAULT_CONTENT_TYPE="${MEDIA_POSTER_DEFAULT_CONTENT_TYPE:-application/octet-stream}"

echo "📝 Creating environment configuration..."
ENV_FILE="$BASE_DIR/.env"
cat > "$ENV_FILE" <<EOF
POSTGRES_USER=$DB_USER
POSTGRES_PASSWORD=$DB_PASSWORD
POSTGRES_DB=$DB_NAME
SSL_KEYSTORE_PASSWORD=$SSL_PASSWORD
JWT_SECRET=$JWT_SECRET
EOF

echo "✅ Environment file created: $ENV_FILE"

echo "⚙️ Configuring application properties..."
RESOURCES_DIR="$BACKEND_DIR/src/main/resources"
mkdir -p "$RESOURCES_DIR"

APP_PROPERTIES_FILE="$RESOURCES_DIR/application.properties"
cat > "$APP_PROPERTIES_FILE" <<EOF
spring.profiles.active=dev
spring.application.name=backend
spring.datasource.url=jdbc:postgresql://cine-rent-db:5432/$DB_NAME
spring.datasource.username=$DB_USER
spring.datasource.password=$DB_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=20
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
spring.servlet.multipart.max-file-size=-1
spring.servlet.multipart.max-request-size=-1
spring.servlet.multipart.file-size-threshold=10MB
spring.servlet.multipart.location=/tmp
server.port=443
server.ssl.enabled=true
server.ssl.key-store=/certs/backend.p12
server.ssl.key-store-password=$SSL_PASSWORD
server.ssl.key-store-type=PKCS12
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/javascript,application/json
server.compression.min-response-size=1024
server.error.include-stacktrace=never
server.error.include-exception=false
server.error.include-message=always
server.tomcat.threads.max=400
server.tomcat.threads.min-spare=50
server.tomcat.max-connections=10000
server.tomcat.accept-count=500
server.tomcat.connection-timeout=60000
server.tomcat.max-http-header-size=65536
server.tomcat.max-swallow-size=-1
jwt.secret=$JWT_SECRET
jwt.cookie.secure=true
jwt.expiration=300000
jwt.cookie.maxAge=360
jwt.refresh.expiration=86400000
jwt.refresh.cookie.maxAge=90000
logging.file.name=logs/application.log
logging.level.pl.kielce.tu.backend=INFO
logging.level.root=INFO
logging.pattern.file=%d{yyyy-MM-dd} %d{HH:mm:ss} [%level] %logger{36} - %msg%n
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.endpoint.metrics.enabled=true
media.poster.dir=$MEDIA_POSTER_DIR
media.poster.base-url=$MEDIA_POSTER_BASE_URL
media.poster.max-size=$MEDIA_POSTER_MAX_SIZE
media.poster.cache-control=$MEDIA_POSTER_CACHE_CONTROL
media.poster.default-content-type=$MEDIA_POSTER_DEFAULT_CONTENT_TYPE
EOF

echo "✅ Application properties configured: $APP_PROPERTIES_FILE"

echo "⚙️ Configuring test properties..."
TEST_RESOURCES_DIR="$BACKEND_DIR/src/test/resources"
mkdir -p "$TEST_RESOURCES_DIR"
APP_TEST_PROPERTIES_FILE="$TEST_RESOURCES_DIR/application.properties"
cat > "$APP_TEST_PROPERTIES_FILE" <<EOF
spring.profiles.active=test
spring.application.name=backend
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.h2.console.enabled=false
spring.sql.init.mode=never
jwt.secret=$JWT_SECRET
jwt.expiration=300000
jwt.refresh.expiration=86400000
jwt.cookie.name=authToken
jwt.cookie.maxAge=86400
jwt.cookie.httpOnly=true
jwt.cookie.secure=false
jwt.cookie.sameSite=Lax
logging.level.pl.kielce.tu.backend=WARN
logging.level.root=WARN
logging.level.org.springframework.web=WARN
logging.level.org.hibernate=WARN
EOF

echo "✅ Test properties configured: $APP_TEST_PROPERTIES_FILE"

echo "📊 Setting up logging directory..."
mkdir -p "$LOGS_DIR"
chmod 755 "$LOGS_DIR"

echo "⚛️ Setting up frontend environment..."
FRONTEND_DIR="$BASE_DIR/frontend"
if [ ! -f "$FRONTEND_DIR/package.json" ]; then
    echo "   ⚠️ Frontend package.json not found, skipping frontend setup"
else
    echo "   ✅ Frontend configuration found"
fi

echo ""
echo "🎉 DVD Rental Application initialization completed successfully!"
echo ""
echo "📋 Summary of generated files:"
echo "   • SSL Certificate: $CERT_FILE"
echo "   • Environment file: $ENV_FILE"
echo "   • Application properties: $APP_PROPERTIES_FILE"
echo "   • Test properties: $APP_TEST_PROPERTIES_FILE"
echo "   • Logs directory: $LOGS_DIR"
echo "   • Uploads directory: $UPLOADS_DIR"
echo "   • Posters directory: $POSTERS_DIR"
echo "   • Frontend Dockerfiles: $FRONTEND_DIR/Dockerfile, $FRONTEND_DIR/Dockerfile.dev"
echo ""
echo "🔐 Generated credentials:"
echo "   • SSL Keystore password: $SSL_PASSWORD"
echo "   • Database user: $DB_USER"
echo "   • Database password: $DB_PASSWORD"
echo "   • Database name: $DB_NAME"
echo "   • JWT Secret length: 512 bytes"
echo ""
echo "📁 Media configuration:"
echo "   • Poster directory: $MEDIA_POSTER_DIR"
echo "   • Poster base URL: $MEDIA_POSTER_BASE_URL"
echo "   • Max poster size: $MEDIA_POSTER_MAX_SIZE bytes ($(($MEDIA_POSTER_MAX_SIZE / 1024 / 1024))MB)"
echo "   • Cache control: $MEDIA_POSTER_CACHE_CONTROL"
echo ""
echo "🚀 Next steps:"
echo "   1. Run 'docker compose up -d' to start all services"
echo "   2. Access the backend at https://localhost:4443"
echo "   3. Access the frontend at http://localhost:5173"
echo "   4. View API documentation at https://localhost:4443/"
echo ""
echo "⚠️  Note: You may need to accept the self-signed certificate in your browser for the backend."
