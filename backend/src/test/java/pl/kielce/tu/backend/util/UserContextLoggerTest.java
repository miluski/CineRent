package pl.kielce.tu.backend.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import pl.kielce.tu.backend.model.constant.LoggingConstants;

public class UserContextLoggerTest {

    private UserContextLogger userContextLogger;

    @BeforeEach
    void setUp() {
        userContextLogger = new UserContextLogger();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_whenPrincipalIsLong_returnsOptionalWithId() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(123L);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        assertTrue(userContextLogger.getCurrentUserId().isPresent());
        assertEquals(Long.valueOf(123L), userContextLogger.getCurrentUserId().get());
    }

    @Test
    void getCurrentUserId_whenPrincipalIsNotLong_returnsEmptyOptional() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-a-long");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        assertFalse(userContextLogger.getCurrentUserId().isPresent());
    }

    @Test
    void getUserIdentifierForLogging_withUser_returnsPrefixedId() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(42L);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String expected = LoggingConstants.USER_PREFIX.getValue() + "42";
        assertEquals(expected, userContextLogger.getUserIdentifierForLogging());
    }

    @Test
    void getUserIdentifierForLogging_anonymousWhenNoUser_returnsAnonymousConstant() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        assertEquals(LoggingConstants.ANONYMOUS_USER.getValue(), userContextLogger.getUserIdentifierForLogging());
    }

    @Test
    void loggingMethods_doNotThrow_whenUserPresent() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(7L);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        assertDoesNotThrow(() -> userContextLogger.logUserOperation("CREATE", "Created resource"));
        assertDoesNotThrow(() -> userContextLogger.logValidationOperation("INPUT", "OK", "All good"));
        assertDoesNotThrow(() -> userContextLogger.logEndpointAccess("GET", "/api/test", "200"));
    }

    @Test
    void loggingMethods_doNotThrow_whenAnonymous() {
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx);

        assertDoesNotThrow(() -> userContextLogger.logUserOperation("DELETE", "Deleted resource"));
        assertDoesNotThrow(() -> userContextLogger.logValidationOperation("SCHEMA", "FAIL", "Missing field"));
        assertDoesNotThrow(() -> userContextLogger.logEndpointAccess("POST", "/api/anon", "401"));
    }
}
