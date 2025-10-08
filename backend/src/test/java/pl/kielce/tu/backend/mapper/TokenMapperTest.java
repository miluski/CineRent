package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.entity.BlacklistedToken;

class TokenMapperTest {

    private final TokenMapper mapper = new TokenMapper();

    @Test
    void toBlacklistedToken_shouldMapTokenValue() {
        String token = "sample-token-123";
        BlacklistedToken result = mapper.toBlacklistedToken(token);

        assertNotNull(result, "Expected a BlacklistedToken instance");
        assertEquals(token, result.getTokenValue(), "Token value should be mapped");
    }

    @Test
    void toBlacklistedToken_withNull_shouldReturnInstanceWithNullValue() {
        BlacklistedToken result = mapper.toBlacklistedToken(null);

        assertNotNull(result, "Mapper should return an instance even if input is null");
        assertNull(result.getTokenValue(), "Token value should be null when input is null");
    }

    @Test
    void toBlacklistedToken_shouldReturnDistinctInstances() {
        BlacklistedToken first = mapper.toBlacklistedToken("t1");
        BlacklistedToken second = mapper.toBlacklistedToken("t1");

        assertNotSame(first, second, "Each mapping call should produce a new instance");
        assertEquals(first.getTokenValue(), second.getTokenValue(), "Both instances should have equal token values");
    }
}
