#!/bin/bash
# -*- coding: utf-8 -*-
set -euo pipefail

export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$BASE_DIR/.env"
APP_PROPERTIES_FILE="$BASE_DIR/backend/src/main/resources/application.properties"

echo "📧 Gmail SMTP Configuration"
echo "================================"
echo ""
echo "Before starting, make sure you have:"
echo "1. Enabled 2-Factor Authentication on your Google Account"
echo "   https://myaccount.google.com/security"
echo "2. Generated an App Password for CineRent"
echo "   https://myaccount.google.com/apppasswords"
echo ""
read -p "Press Enter to continue..."
echo ""

read -p "Enter your Gmail address (e.g., your-email@gmail.com): " GMAIL_ADDRESS
while [[ ! "$GMAIL_ADDRESS" =~ ^[a-zA-Z0-9._%+-]+@gmail\.com$ ]]; do
    echo "❌ Invalid Gmail address. Please use format: name@gmail.com"
    read -p "Enter your Gmail address: " GMAIL_ADDRESS
done

echo ""
echo "Enter your Gmail App Password (16 characters, no spaces)"
echo "Note: This is NOT your regular Gmail password!"
read -s -p "App Password: " GMAIL_APP_PASSWORD
echo ""

while [[ ${#GMAIL_APP_PASSWORD} -lt 16 ]]; do
    echo "❌ App password must be at least 16 characters"
    read -s -p "App Password: " GMAIL_APP_PASSWORD
    echo ""
done

GMAIL_APP_PASSWORD=$(echo "$GMAIL_APP_PASSWORD" | tr -d ' ')

echo ""
read -p "Enter sender name (optional, press Enter to use email): " SENDER_NAME
if [ -z "$SENDER_NAME" ]; then
    MAIL_FROM="$GMAIL_ADDRESS"
else
    MAIL_FROM="$SENDER_NAME <$GMAIL_ADDRESS>"
fi

echo ""
echo "📝 Updating configuration files..."

if [ -f "$ENV_FILE" ]; then
    if grep -q "MAIL_HOST=" "$ENV_FILE"; then
        sed -i.bak "s|^MAIL_HOST=.*|MAIL_HOST=smtp.gmail.com|g" "$ENV_FILE"
        sed -i.bak "s|^MAIL_PORT=.*|MAIL_PORT=587|g" "$ENV_FILE"
        sed -i.bak "s|^MAIL_USERNAME=.*|MAIL_USERNAME=$GMAIL_ADDRESS|g" "$ENV_FILE"
        sed -i.bak "s|^MAIL_PASSWORD=.*|MAIL_PASSWORD=$GMAIL_APP_PASSWORD|g" "$ENV_FILE"
        sed -i.bak "s|^MAIL_FROM=.*|MAIL_FROM=$MAIL_FROM|g" "$ENV_FILE"
        rm -f "$ENV_FILE.bak"
        echo "   ✅ Updated $ENV_FILE"
    else
        echo "" >> "$ENV_FILE"
        echo "MAIL_HOST=smtp.gmail.com" >> "$ENV_FILE"
        echo "MAIL_PORT=587" >> "$ENV_FILE"
        echo "MAIL_USERNAME=$GMAIL_ADDRESS" >> "$ENV_FILE"
        echo "MAIL_PASSWORD=$GMAIL_APP_PASSWORD" >> "$ENV_FILE"
        echo "MAIL_FROM=$MAIL_FROM" >> "$ENV_FILE"
        echo "   ✅ Added mail settings to $ENV_FILE"
    fi
else
    echo "   ⚠️  .env file not found. Run ./scripts/init.sh first."
fi

if [ -f "$APP_PROPERTIES_FILE" ]; then
    if grep -q "spring.mail.host=" "$APP_PROPERTIES_FILE"; then
        sed -i.bak "s|^spring.mail.host=.*|spring.mail.host=smtp.gmail.com|g" "$APP_PROPERTIES_FILE"
        sed -i.bak "s|^spring.mail.port=.*|spring.mail.port=587|g" "$APP_PROPERTIES_FILE"
        sed -i.bak "s|^spring.mail.username=.*|spring.mail.username=$GMAIL_ADDRESS|g" "$APP_PROPERTIES_FILE"
        sed -i.bak "s|^spring.mail.password=.*|spring.mail.password=$GMAIL_APP_PASSWORD|g" "$APP_PROPERTIES_FILE"
        sed -i.bak "s|^spring.mail.properties.mail.smtp.ssl.trust=.*|spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com|g" "$APP_PROPERTIES_FILE"
        sed -i.bak "s|^mail.from=.*|mail.from=$MAIL_FROM|g" "$APP_PROPERTIES_FILE"
        rm -f "$APP_PROPERTIES_FILE.bak"
        echo "   ✅ Updated $APP_PROPERTIES_FILE"
    else
        echo "   ⚠️  Mail properties not found in application.properties"
    fi
else
    echo "   ⚠️  application.properties not found. Run ./scripts/init.sh first."
fi

echo ""
echo "✅ Gmail SMTP configuration completed successfully!"
echo ""
echo "📋 Configuration Summary:"
echo "   • SMTP Host: smtp.gmail.com"
echo "   • SMTP Port: 587"
echo "   • Email: $GMAIL_ADDRESS"
echo "   • Sender: $MAIL_FROM"
echo ""
echo "🚀 Next steps:"
echo "   1. If your application is running, restart it:"
echo "      docker compose restart backend"
echo "   2. Test the email functionality by registering a new user"
echo ""
