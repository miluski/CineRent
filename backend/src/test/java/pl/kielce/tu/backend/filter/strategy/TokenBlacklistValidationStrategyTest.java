package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.service.auth.TokenService;
import pl.kielce.tu.backend.util.UserContextLogger;

class TokenBlacklistValidationStrategyTest {

    @Test
    void isTokenBlacklisted_returnsFalseAndSendsUnauthorized_whenTokenIsBlacklisted() throws Exception {
        TokenService tokenService = Mockito.mock(TokenService.class);
        ResponseHelper responseHelper = Mockito.mock(ResponseHelper.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = "blacklisted-token";
        when(tokenService.isTokenBlacklisted(token)).thenReturn(true);
        doNothing().when(responseHelper).sendUnauthorized(response, "Token is blacklisted");

        UserContextLogger userContextLogger = mock(UserContextLogger.class);
        TokenBlacklistValidationStrategy strategy = new TokenBlacklistValidationStrategy(responseHelper, tokenService,
                userContextLogger);

        ValidationResult result = strategy.validate(token, response, "/some/path");

        assertFalse(result.isSuccess(), "Expected validation to be false for blacklisted token");
        assertNull(result.getData(), "Expected value to be null for blacklisted token");

        verify(responseHelper, times(1)).sendUnauthorized(response, "Token is blacklisted");
    }

    @Test
    void validate_shouldReturnValidAndPreserveToken_whenTokenIsNotBlacklisted() throws Exception {
        TokenService tokenService = Mockito.mock(TokenService.class);
        ResponseHelper responseHelper = Mockito.mock(ResponseHelper.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = "valid-token";
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);

        UserContextLogger userContextLogger = mock(UserContextLogger.class);
        TokenBlacklistValidationStrategy strategy = new TokenBlacklistValidationStrategy(responseHelper, tokenService,
                userContextLogger);

        ValidationResult result = strategy.validate(token, response, "/another/path");
        assertTrue(result.isSuccess(), "Expected validation to be true for non-blacklisted token");
        assertEquals(token, result.getData(), "Expected returned value to be the original token");

        verify(responseHelper, never()).sendUnauthorized(Mockito.any(), Mockito.anyString());
    }

    @Test
    void getName_shouldReturnBlacklist() {
        TokenService tokenService = Mockito.mock(TokenService.class);
        ResponseHelper responseHelper = Mockito.mock(ResponseHelper.class);

        UserContextLogger userContextLogger = mock(UserContextLogger.class);
        TokenBlacklistValidationStrategy strategy = new TokenBlacklistValidationStrategy(responseHelper, tokenService,
                userContextLogger);

        TokenValidationNames name = strategy.getName();
        assertEquals(TokenValidationNames.BLACKLIST, name);
    }
}
