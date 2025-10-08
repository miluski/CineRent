package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.constant.CookieNames;

class TokenPresenceInputTest {

    @Test
    void shouldReturnRequestAndTokenType() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        CookieNames[] names = CookieNames.values();
        Assumptions.assumeTrue(names.length >= 1, "CookieNames must contain at least one constant for the test");
        CookieNames tokenType = names[0];

        TokenPresenceInput input = new TokenPresenceInput(request, tokenType);

        assertSame(request, input.request());
        assertSame(tokenType, input.tokenType());
    }

    @Test
    void equalsAndHashCode_sameValuesAreEqual() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        CookieNames[] names = CookieNames.values();
        Assumptions.assumeTrue(names.length >= 1, "CookieNames must contain at least one constant for the test");
        CookieNames tokenType = names[0];

        TokenPresenceInput a = new TokenPresenceInput(request, tokenType);
        TokenPresenceInput b = new TokenPresenceInput(request, tokenType);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqualsWhenRequestOrTokenTypeDiffer() {
        HttpServletRequest request1 = mock(HttpServletRequest.class);
        HttpServletRequest request2 = mock(HttpServletRequest.class);
        CookieNames[] names = CookieNames.values();
        Assumptions.assumeTrue(names.length >= 1, "CookieNames must contain at least one constant for the test");
        CookieNames tokenType1 = names[0];

        TokenPresenceInput base = new TokenPresenceInput(request1, tokenType1);
        TokenPresenceInput differentRequest = new TokenPresenceInput(request2, tokenType1);

        assertNotEquals(base, differentRequest);

        if (names.length >= 2) {
            CookieNames tokenType2 = names[1];
            TokenPresenceInput differentToken = new TokenPresenceInput(request1, tokenType2);
            assertNotEquals(base, differentToken);
        }
    }

    @Test
    void toStringContainsTokenTypeName() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        CookieNames[] names = CookieNames.values();
        Assumptions.assumeTrue(names.length >= 1, "CookieNames must contain at least one constant for the test");
        CookieNames tokenType = names[0];

        TokenPresenceInput input = new TokenPresenceInput(request, tokenType);

        String s = input.toString();
        assertNotNull(s);
        assertTrue(s.contains(tokenType.toString()),
                () -> "toString should contain tokenType representation, was: " + s);
    }
}
