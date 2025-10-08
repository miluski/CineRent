package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BlacklistedTokenTest {

    @Test
    void noArgsConstructorAndSetters_shouldSetAndGetValues() {
        BlacklistedToken token = new BlacklistedToken();
        assertNull(token.getId());
        assertNull(token.getTokenValue());

        token.setId(10L);
        token.setTokenValue("abc123");

        assertEquals(10L, token.getId());
        assertEquals("abc123", token.getTokenValue());
    }

    @Test
    void allArgsConstructor_shouldInitializeFields() {
        BlacklistedToken token = new BlacklistedToken(1L, "value1");

        assertEquals(1L, token.getId());
        assertEquals("value1", token.getTokenValue());
    }

    @Test
    void builder_shouldBuildObjectCorrectly() {
        BlacklistedToken token = BlacklistedToken.builder()
                .id(2L)
                .tokenValue("built-token")
                .build();

        assertEquals(2L, token.getId());
        assertEquals("built-token", token.getTokenValue());
    }

    @Test
    void equalsAndHashCode_shouldConsiderAllFields() {
        BlacklistedToken a = new BlacklistedToken(5L, "tkn");
        BlacklistedToken b = new BlacklistedToken(5L, "tkn");
        BlacklistedToken c = new BlacklistedToken(6L, "tkn");
        BlacklistedToken d = new BlacklistedToken(5L, "different");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());

        assertNotEquals(a, d);
        assertNotEquals(a.hashCode(), d.hashCode());
    }

    @Test
    void toString_shouldContainFieldValues() {
        BlacklistedToken token = new BlacklistedToken(7L, "xyz");
        String s = token.toString();
        assertTrue(s.contains("7"));
        assertTrue(s.contains("xyz"));
    }
}
