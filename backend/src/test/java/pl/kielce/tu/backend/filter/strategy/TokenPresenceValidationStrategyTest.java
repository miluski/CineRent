package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.service.auth.CookieService;

@ExtendWith(MockitoExtension.class)
class TokenPresenceValidationStrategyTest {

    @Mock
    private CookieService cookieService;

    @Mock
    private ResponseHelper responseHelper;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TokenPresenceValidationStrategy strategy;

    @Test
    void validate_shouldReturnInvalidAndSendUnauthorized_whenTokenMissing() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        TokenPresenceInput input = new TokenPresenceInput(req, CookieNames.ACCESS_TOKEN);
        when(cookieService.getTokenFromCookie(req, CookieNames.ACCESS_TOKEN)).thenReturn(null);

        ValidationResult result = strategy.validate(input, response, "/test/path");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getData());
        verify(responseHelper).sendUnauthorized(response, "Token not found");
    }

    @Test
    void validate_shouldReturnValidAndToken_whenTokenPresent() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        TokenPresenceInput input = new TokenPresenceInput(req, CookieNames.ACCESS_TOKEN);
        when(cookieService.getTokenFromCookie(req, CookieNames.ACCESS_TOKEN)).thenReturn("my-token");

        ValidationResult result = strategy.validate(input, response, "/other/path");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("my-token", result.getData());
        verify(responseHelper, never()).sendUnauthorized(any(), anyString());
    }

    @Test
    void getName_shouldReturnPresence() {
        assertEquals(TokenValidationNames.PRESENCE, strategy.getName());
    }
}
