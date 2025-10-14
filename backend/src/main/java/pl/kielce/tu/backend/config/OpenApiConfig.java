package pl.kielce.tu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CineRent API - Complete DVD Rental Management System")
                        .version("1.0.0")
                        .description(
                                """
                                        # CineRent API Documentation

                                        **Professional DVD rental management system with complete business workflow automation**

                                        ## 🎯 System Overview
                                        CineRent provides a comprehensive REST API for managing all aspects of a DVD rental business, from user registration to automated invoice generation with professional PDF documents.

                                        ## 📋 Core Features

                                        ### 🔐 User Authentication & Authorization (`/api/v1/auth`)
                                        - **JWT Security**: Access (15min) + Refresh (7 days) tokens in HttpOnly cookies
                                        - **User Registration**: Account creation with preference selection
                                        - **Secure Login/Logout**: Credential validation and session management
                                        - **Role-Based Access**: USER and ADMIN roles with endpoint restrictions

                                        ### 👤 User Management (`/api/v1/user`)
                                        - **Profile Access**: Complete user information retrieval
                                        - **AI Recommendations**: Multi-factor recommendation engine based on:
                                          - Rental history analysis
                                          - Genre preferences matching
                                          - Age-appropriate content filtering
                                          - Real-time availability checking

                                        ### 💿 DVD Catalog Management (`/api/v1/dvd`)
                                        - **Advanced Browsing**: Search by title/description, filter by genres
                                        - **Complete CRUD**: Full administrative control over DVD inventory
                                        - **Rich Metadata**: Title, description, directors, genres, pricing, availability
                                        - **Poster Management**: Base64 image upload and secure serving
                                        - **Inventory Tracking**: Real-time copy availability and status

                                        ### 🎭 Genre Organization (`/api/v1/genres`)
                                        - **Category Management**: Complete genre CRUD operations
                                        - **Multi-Genre Support**: DVDs can belong to multiple categories
                                        - **Admin Controls**: Create/delete genres with dependency validation

                                        ### 📅 Advanced Reservation System (`/api/v1/reservations`)
                                        - **Date-Range Booking**: Users specify exact rental periods
                                        - **Admin Approval Workflow**: Reservation acceptance/rejection process
                                        - **Inventory Integration**: Automatic copy reservation and release
                                        - **Status Tracking**: PENDING → ACCEPTED/REJECTED → Rental creation
                                        - **Self-Service Cancellation**: Users can cancel pending reservations

                                        ### 🎬 Rental Lifecycle Management (`/api/v1/rentals`)
                                        - **Active Rental Tracking**: Monitor current user rentals
                                        - **Return Processing**: User-initiated return requests with admin approval
                                        - **Late Fee Calculation**: Automatic penalty computation for overdue returns
                                        - **Historical Records**: Complete rental history with filtering
                                        - **Admin Queue**: Centralized return request management

                                        ### 💰 Financial Transaction System (`/api/v1/transactions`)
                                        - **Automated Billing**: Transaction creation on rental completion
                                        - **Professional PDF Generation**: Polish-standard invoices and receipts using iText 5
                                        - **Document Types**:
                                          - Formal business invoices with complete details
                                          - Simple receipts for basic transactions
                                        - **Transaction History**: Complete financial records for users and admins
                                        - **Late Fee Integration**: Automatic penalty inclusion in final bills

                                        ### 📁 Resource Management (`/api/v1/resources`)
                                        - **Secure File Serving**: Protected poster image access
                                        - **Multiple Formats**: JPEG, PNG image support
                                        - **Performance Optimization**: Efficient caching and serving

                                        ## 🔒 Security Features
                                        - **HTTPS/SSL**: Encrypted communication with certificate management
                                        - **JWT Authentication**: Secure token-based authentication
                                        - **HttpOnly Cookies**: XSS protection for token storage
                                        - **Role-Based Authorization**: Granular access control
                                        - **CORS Configuration**: Proper cross-origin resource sharing

                                        ## 📊 HTTP Status Codes
                                        - **200**: Success - Resource retrieved/updated successfully
                                        - **201**: Created - New resource created successfully
                                        - **202**: Accepted - Request accepted for processing
                                        - **400**: Bad Request - Invalid input or business logic violation
                                        - **401**: Unauthorized - Authentication required or invalid token
                                        - **403**: Forbidden - Insufficient permissions for requested operation
                                        - **404**: Not Found - Requested resource does not exist
                                        - **500**: Internal Server Error - Unexpected server-side error

                                        ## 🚀 Getting Started
                                        1. **Register**: Create account at `/api/v1/auth/register`
                                        2. **Login**: Authenticate at `/api/v1/auth/login` to receive JWT cookies
                                        3. **Browse**: Explore DVD catalog with filtering and search
                                        4. **Reserve**: Book DVDs for specific date ranges
                                        5. **Rent**: Admin approves reservations creating active rentals
                                        6. **Return**: Request returns and receive professional PDF invoices

                                        ## 👑 Admin Features
                                        **Admin-only endpoints for system management:**
                                        - DVD creation and editing (`POST /api/v1/dvd/create`, `PATCH /api/v1/dvd/{id}/edit`)
                                        - Genre management (`POST /api/v1/genres/create`, `DELETE /api/v1/genres/{id}/delete`)
                                        - Reservation approval (`POST /api/v1/reservations/{id}/accept|decline`)
                                        - Return processing (`POST /api/v1/rentals/{id}/return-accept|decline`)
                                        - System-wide transaction overview (`GET /api/v1/transactions/all`)

                                        **Comprehensive business management with professional automation and Polish market compliance.**
                                        """))
                .components(new Components()
                        .addSecuritySchemes("accessToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("ACCESS_TOKEN")
                                .description(
                                        "JWT access token stored in HTTP-only, secure cookie. Required for authenticated endpoints."))
                        .addSecuritySchemes("refreshToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("REFRESH_TOKEN")
                                .description(
                                        "JWT refresh token stored in HTTP-only, secure cookie. Used for token refresh operations.")));
    }

}
