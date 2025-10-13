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
                        .title("CineRent API")
                        .version("1.0.0")
                        .description(
                                """
                                        ## CineRent - DVD Rental System API

                                        A comprehensive REST API for managing DVD rental operations including:

                                        ### Features
                                        - **User Authentication**: JWT-based authentication with access and refresh tokens
                                        - **DVD Management**: Complete CRUD operations for DVD catalog
                                        - **Reservation System**: Users can reserve DVDs for rental
                                        - **Rental Management**: Track active and historical rentals
                                        - **Recommendation Engine**: Personalized DVD recommendations based on user preferences and rental history
                                        - **Genre Management**: Organize DVDs by categories
                                        - **Resource Management**: Handle DVD poster images and other media

                                        ### Authentication
                                        Most endpoints require authentication using JWT tokens stored as HTTP-only cookies.
                                        Use the `/api/v1/auth/login` endpoint to authenticate and obtain tokens.

                                        ### Admin Features
                                        Some endpoints are restricted to admin users for managing system resources.
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
