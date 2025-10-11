package pl.kielce.tu.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RequestInfoTest {

    @Test
    void accessorsReturnConstructorValues() {
        RequestInfo info = new RequestInfo("GET", "/api/items", "200 OK", 123L);

        assertEquals("GET", info.method());
        assertEquals("/api/items", info.endpoint());
        assertEquals("200 OK", info.statusInfo());
        assertEquals(123L, info.duration());
    }

    @Test
    void equalsAndHashCodeForSameValues() {
        RequestInfo a = new RequestInfo("POST", "/submit", "201 Created", 50L);
        RequestInfo b = new RequestInfo("POST", "/submit", "201 Created", 50L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqualsWhenAnyFieldDiffers() {
        RequestInfo base = new RequestInfo("PUT", "/resource", "204 No Content", 10L);

        RequestInfo differentMethod = new RequestInfo("PATCH", "/resource", "204 No Content", 10L);
        RequestInfo differentEndpoint = new RequestInfo("PUT", "/other", "204 No Content", 10L);
        RequestInfo differentStatus = new RequestInfo("PUT", "/resource", "500 Error", 10L);
        RequestInfo differentDuration = new RequestInfo("PUT", "/resource", "204 No Content", 11L);

        assertNotEquals(base, differentMethod);
        assertNotEquals(base, differentEndpoint);
        assertNotEquals(base, differentStatus);
        assertNotEquals(base, differentDuration);
    }

    @Test
    void toStringContainsFieldValues() {
        RequestInfo info = new RequestInfo("DELETE", "/items/1", "404 Not Found", 0L);
        String s = info.toString();

        assertTrue(s.contains("DELETE"));
        assertTrue(s.contains("/items/1"));
        assertTrue(s.contains("404 Not Found"));
        assertTrue(s.contains("0"));
    }
}
