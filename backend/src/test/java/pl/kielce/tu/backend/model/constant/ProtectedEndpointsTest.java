package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ProtectedEndpointsTest {

    @Test
    void getPattern_returnsExpectedPattern() {
        assertEquals("/api/v1/auth/refresh-tokens", ProtectedEndpoints.REFRESH_TOKENS.getPattern());
    }

    @Test
    void matches_returnsTrue_forExactPath() {
        assertTrue(ProtectedEndpoints.REFRESH_TOKENS.matches("/api/v1/auth/refresh-tokens"));
    }

    @Test
    void matches_returnsFalse_forDifferentPath() {
        assertFalse(ProtectedEndpoints.REFRESH_TOKENS.matches("/api/v1/auth/other"));
    }

    @Test
    void matches_returnsFalse_forNullPath() {
        assertFalse(ProtectedEndpoints.REFRESH_TOKENS.matches(null));
    }
}
