package pl.kielce.tu.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public class OpenApiConfigTest {

    @Test
    void customOpenAPI_shouldContainCorrectInfo() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI.getInfo(), "Info should not be null");
        assertEquals("CineRent API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals("Backend API for DVD rental application", openAPI.getInfo().getDescription());
    }

    @Test
    void customOpenAPI_shouldContainSecuritySchemesForTokens() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI.getComponents(), "Components should not be null");

        SecurityScheme access = openAPI.getComponents().getSecuritySchemes().get("accessToken");
        assertNotNull(access, "accessToken scheme should be present");
        assertEquals(SecurityScheme.Type.APIKEY, access.getType());
        assertEquals(SecurityScheme.In.COOKIE, access.getIn());
        assertEquals("ACCESS_TOKEN", access.getName());
        assertEquals("JWT access token stored in HTTP-only, secure cookie", access.getDescription());

        SecurityScheme refresh = openAPI.getComponents().getSecuritySchemes().get("refreshToken");
        assertNotNull(refresh, "refreshToken scheme should be present");
        assertEquals(SecurityScheme.Type.APIKEY, refresh.getType());
        assertEquals(SecurityScheme.In.COOKIE, refresh.getIn());
        assertEquals("REFRESH_TOKEN", refresh.getName());
        assertEquals("JWT refresh token stored in HTTP-only, secure cookie", refresh.getDescription());
    }
}
