# Email Configuration Guide

This guide explains how to configure Gmail SMTP for sending verification emails in CineRent.

## Quick Setup

Run the interactive configuration script:

```bash
./scripts/configure-email.sh
```

The script will guide you through the setup process.

## Manual Setup Instructions

### Step 1: Enable 2-Factor Authentication

1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Under "How you sign in to Google", click on "2-Step Verification"
3. Follow the instructions to enable 2FA

### Step 2: Generate App Password

1. Go to [App Passwords](https://myaccount.google.com/apppasswords)
2. Select app: **Mail**
3. Select device: **Other (Custom name)** → enter "CineRent"
4. Click **Generate**
5. Copy the 16-character password (you won't be able to see it again)

### Step 3: Configure Email Settings

#### Option A: Using the Configuration Script (Recommended)

```bash
./scripts/configure-email.sh
```

The script will prompt you for:

- Your Gmail address
- Your App Password (16 characters)
- Optional sender name

#### Option B: Manual Configuration

Edit the following files:

**1. `.env` file (for Docker Compose):**

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-character-app-password
MAIL_FROM=your-email@gmail.com
```

**2. `backend/src/main/resources/application.properties`:**

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
mail.from=your-email@gmail.com
```

### Step 4: Restart the Application

If your application is already running:

```bash
docker compose restart backend
```

Or restart everything:

```bash
docker compose down
docker compose up -d
```

## Testing

1. Register a new user account through the application
2. Check if the verification email arrives
3. The email should be sent from your configured Gmail address
4. Check `logs/application.log` for any email-related errors

## Troubleshooting

### "Invalid credentials" or "Authentication failed"

- Make sure you're using an **App Password**, not your regular Gmail password
- Verify that 2-Factor Authentication is enabled
- Check that the App Password has no spaces
- Try generating a new App Password

### "Connection timed out"

- Check your firewall settings
- Verify that port 587 is not blocked
- Try using port 465 with SSL instead (requires changing configuration)

### Emails not being received

- Check your spam folder
- Verify the sender email address is correct
- Check application logs: `docker compose logs backend`
- Look for errors in `logs/application.log`

### Gmail Security Block

If Gmail blocks the login attempt:

1. Check your email for a "Critical security alert" from Google
2. Click "Yes, it was me" in the security alert email
3. Try sending email again

## Gmail SMTP Settings Reference

| Setting        | Value                     |
| -------------- | ------------------------- |
| Host           | smtp.gmail.com            |
| Port           | 587 (TLS) or 465 (SSL)    |
| Security       | STARTTLS (for port 587)   |
| Authentication | Required                  |
| Username       | Your full Gmail address   |
| Password       | 16-character App Password |

## Alternative Email Providers

The configuration script is designed for Gmail, but you can manually configure other providers:

### Outlook/Hotmail

```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
```

### Yahoo Mail

```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
```

### Custom SMTP Server

Edit the configuration files with your SMTP server details.

## Security Notes

- **Never commit** your App Password to version control
- The `.env` file and `application.properties` are in `.gitignore`
- Use environment variables in production
- Rotate App Passwords periodically
- Revoke App Passwords when no longer needed

## Support

For more information:

- [Gmail SMTP Documentation](https://support.google.com/mail/answer/7126229)
- [Google App Passwords](https://support.google.com/accounts/answer/185833)
