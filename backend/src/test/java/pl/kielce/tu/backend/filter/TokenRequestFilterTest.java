package pl.kielce.tu.backend.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.factory.TokenValidationStrategyFactory;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.PublicEndpoints;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

public class TokenRequestFilterTest {

    @Mock
    private ResponseHelper responseHelper;

    @Mock
    private TokenValidationStrategyFactory validationFactory;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserContextLogger userContextLogger;

    private AutoCloseable mocks;
    private TokenRequestFilter filter;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        filter = new TokenRequestFilter(responseHelper, userContextLogger, validationFactory);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void publicEndpoint_shouldInvokeFilterChain() throws ServletException, IOException {
        try (MockedStatic<PublicEndpoints> publicMock = mockStatic(PublicEndpoints.class)) {
            when(request.getRequestURI()).thenReturn("/public/test");
            publicMock.when(() -> PublicEndpoints.isMember("/public/test")).thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void authenticateUser_shouldSetSecurityContext()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        User user = new User();
        user.setId(42L);

        Method authenticateUser = TokenRequestFilter.class.getDeclaredMethod("authenticateUser", User.class);
        authenticateUser.setAccessible(true);

        authenticateUser.invoke(filter, user);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        assert Long.valueOf(42L).equals(auth.getPrincipal());
    }

    @Test
    void handleExpiredToken_shouldSendUnauthorized() throws Exception {
        Method handler = TokenRequestFilter.class.getDeclaredMethod("handleExpiredToken", HttpServletResponse.class,
                String.class);
        handler.setAccessible(true);

        handler.invoke(filter, response, "/protected/resource");

        verify(responseHelper).sendUnauthorized(eq(response), eq("Token expired"));
    }

    @Test
    void handleInvalidToken_shouldSendUnauthorizedWithMessage() throws Exception {
        Method handler = TokenRequestFilter.class.getDeclaredMethod("handleInvalidToken", HttpServletResponse.class,
                String.class, JwtException.class);
        handler.setAccessible(true);

        JwtException ex = new JwtException("bad token");
        handler.invoke(filter, response, "/protected/resource", ex);

        verify(responseHelper).sendUnauthorized(eq(response), eq("Invalid token: " + ex.getMessage()));
    }

    @Test
    void handleInvalidTokenFormat_shouldSendUnauthorized() throws Exception {
        Method handler = TokenRequestFilter.class.getDeclaredMethod("handleInvalidTokenFormat",
                HttpServletResponse.class, String.class);
        handler.setAccessible(true);

        handler.invoke(filter, response, "/protected/resource");

        verify(responseHelper).sendUnauthorized(eq(response), eq("Invalid token format"));
    }

    @Test
    void handleUnexpectedError_shouldSendUnauthorized() throws Exception {
        Method handler = TokenRequestFilter.class.getDeclaredMethod("handleUnexpectedError", HttpServletResponse.class,
                String.class, Exception.class);
        handler.setAccessible(true);

        handler.invoke(filter, response, "/protected/resource", new RuntimeException("boom"));

        verify(responseHelper).sendUnauthorized(eq(response), eq("Authentication failed"));
    }

    @Test
    void processAuthentication_whenValidationThrowsExpiredJwtException_shouldSendExpired() throws Exception {
        try (MockedStatic<PublicEndpoints> publicMock = mockStatic(PublicEndpoints.class)) {
            when(request.getRequestURI()).thenReturn("/api/secure");
            publicMock.when(() -> PublicEndpoints.isMember("/api/secure")).thenReturn(false);

            when(validationFactory.createValidationChain(any(HttpServletRequest.class),
                    any(HttpServletResponse.class), eq("/api/secure"), any()))
                    .thenThrow(new ExpiredJwtException(null, null, "expired"));

            filter.doFilterInternal(request, response, filterChain);

            verify(responseHelper).sendUnauthorized(eq(response), eq("Token expired"));
        }
    }

    @Test
    void processAuthentication_whenValidationThrowsJwtException_shouldSendInvalid() throws Exception {
        try (MockedStatic<PublicEndpoints> publicMock = mockStatic(PublicEndpoints.class)) {
            when(request.getRequestURI()).thenReturn("/api/secure2");
            publicMock.when(() -> PublicEndpoints.isMember("/api/secure2")).thenReturn(false);

            when(validationFactory.createValidationChain(any(HttpServletRequest.class),
                    any(HttpServletResponse.class), eq("/api/secure2"), any()))
                    .thenThrow(new JwtException("invalid"));

            filter.doFilterInternal(request, response, filterChain);

            verify(responseHelper).sendUnauthorized(eq(response), eq("Invalid token: invalid"));
        }
    }

    @Test
    void processAuthentication_whenValidationThrowsNumberFormatException_shouldSendInvalidFormat() throws Exception {
        try (MockedStatic<PublicEndpoints> publicMock = mockStatic(PublicEndpoints.class)) {
            when(request.getRequestURI()).thenReturn("/api/num");
            publicMock.when(() -> PublicEndpoints.isMember("/api/num")).thenReturn(false);

            when(validationFactory.createValidationChain(any(HttpServletRequest.class),
                    any(HttpServletResponse.class), eq("/api/num"), any()))
                    .thenThrow(new NumberFormatException("bad number"));

            filter.doFilterInternal(request, response, filterChain);

            verify(responseHelper).sendUnauthorized(eq(response), eq("Invalid token format"));
        }
    }

    @Test
    void processAuthentication_whenValidationThrowsGenericException_shouldSendAuthFailed() throws Exception {
        try (MockedStatic<PublicEndpoints> publicMock = mockStatic(PublicEndpoints.class)) {
            when(request.getRequestURI()).thenReturn("/api/err");
            publicMock.when(() -> PublicEndpoints.isMember("/api/err")).thenReturn(false);

            when(validationFactory.createValidationChain(any(HttpServletRequest.class),
                    any(HttpServletResponse.class), eq("/api/err"), any()))
                    .thenThrow(new RuntimeException("uh oh"));

            filter.doFilterInternal(request, response, filterChain);

            verify(responseHelper).sendUnauthorized(eq(response), eq("Authentication failed"));
        }
    }
}
