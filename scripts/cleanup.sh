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

check_windows_environment() {
    OS=$(detect_os)
    if [ "$OS" = "windows" ] && [ -z "${MSYSTEM:-}" ]; then
        echo "❌ Windows detected but not running in Git Bash!"
        echo ""
        echo "Please run this script in Git Bash, not Command Prompt or PowerShell."
        echo ""
        echo "To use Git Bash:"
        echo "1. Right-click in your project folder"
        echo "2. Select 'Git Bash Here'"
        echo "3. Run the script again: ./scripts/cleanup.sh"
        echo ""
        exit 1
    fi
}

OS=$(detect_os)
if [ "$OS" = "windows" ]; then
    check_windows_environment
fi

echo "🧹 Starting cleanup of DVD Rental Backend Application..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$BASE_DIR/backend"
CERTS_DIR="$BASE_DIR/certs"
LOGS_DIR="$BASE_DIR/logs"

echo "🐳 Stopping and removing Docker containers and volumes..."
docker compose down -v 2>/dev/null || true

echo "🗑️ Removing Docker volumes..."
docker volume rm \
    "$(basename "$BASE_DIR")_postgres-data" \
    "$(basename "$BASE_DIR")_backend-logs" \
    "$(basename "$BASE_DIR")_frontend_node_modules" \
    2>/dev/null || echo "   Some volumes may not exist (this is normal)"

echo "🧽 Cleaning up unused Docker resources..."
docker system prune -f 2>/dev/null || true

echo "🔐 Removing SSL certificates..."
if [ -d "$CERTS_DIR" ]; then
    rm -rf "$CERTS_DIR"
    echo "   ✅ Certificates directory removed: $CERTS_DIR"
else
    echo "   ℹ️ Certificates directory not found (already clean)"
fi

echo "📝 Removing environment configuration..."
ENV_FILE="$BASE_DIR/.env"
if [ -f "$ENV_FILE" ]; then
    rm -f "$ENV_FILE"
    echo "   ✅ Environment file removed: $ENV_FILE"
else
    echo "   ℹ️ Environment file not found (already clean)"
fi

echo "⚙️ Removing application properties..."
RESOURCES_DIR="$BACKEND_DIR/src/main/resources"
TEST_RESOURCES_DIR="$BACKEND_DIR/src/test/resources"
APP_PROPERTIES_FILE="$RESOURCES_DIR/application.properties"
APP_TEST_PROPERTIES_FILE="$TEST_RESOURCES_DIR/application.properties"

if [ -f "$APP_PROPERTIES_FILE" ]; then
    rm -f "$APP_PROPERTIES_FILE"
    echo "   ✅ Application properties removed: $APP_PROPERTIES_FILE"
else
    echo "   ℹ️ Application properties file not found"
fi

if [ -f "$APP_TEST_PROPERTIES_FILE" ]; then
    rm -f "$APP_TEST_PROPERTIES_FILE"
    echo "   ✅ Test properties removed: $APP_TEST_PROPERTIES_FILE"
else
    echo "   ℹ️ Test properties file not found"
fi

echo "📊 Removing logs directory..."
if [ -d "$LOGS_DIR" ]; then
    rm -rf "$LOGS_DIR"
    echo "   ✅ Logs directory removed: $LOGS_DIR"
else
    echo "   ℹ️ Logs directory not found (already clean)"
fi

echo "🗂️ Removing build artifacts and temporary files..."
rm -rf /tmp/dvdrental-* 2>/dev/null || true
rm -rf "$BASE_DIR"/target 2>/dev/null || true
rm -rf "$BACKEND_DIR"/target 2>/dev/null || true
rm -rf "$BASE_DIR/frontend/node_modules" 2>/dev/null || true
rm -rf "$BASE_DIR/frontend/dist" 2>/dev/null || true

echo "🗃️ Removing backup files..."
find "$BASE_DIR" -name "*.bak" -type f -delete 2>/dev/null || true
find "$BASE_DIR" -name "*~" -type f -delete 2>/dev/null || true

echo ""
echo "✨ Cleanup completed successfully!"
echo ""
echo "🧹 Summary of cleaned items:"
echo "   • Docker containers and volumes"
echo "   • SSL certificates and keystore"
echo "   • Environment configuration files"
echo "   • Application logs"
echo "   • Temporary and backup files"
echo "   • Application properties reset to default"
echo ""
echo "🔄 To reinitialize the application, run:"
echo "   ./scripts/init.sh"
echo ""
