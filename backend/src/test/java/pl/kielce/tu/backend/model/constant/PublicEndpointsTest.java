package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class PublicEndpointsTest {

    @Test
    void getAllPatterns_containsAllEnumPatterns() {
        String[] patterns = PublicEndpoints.getAllPatterns();
        assertEquals(PublicEndpoints.values().length, patterns.length, "Number of patterns should match enum values");

        Arrays.stream(PublicEndpoints.values())
                .map(PublicEndpoints::getPattern)
                .forEach(p -> assertTrue(Arrays.asList(patterns).contains(p), "Pattern list should contain: " + p));
    }

    @Test
    void isMember_exactAndRootMatches() {
        assertTrue(PublicEndpoints.isMember("/api/v1/auth/login"));
        assertTrue(PublicEndpoints.isMember("/api/v1/auth/register"));
        assertTrue(PublicEndpoints.isMember("/api/v1/auth/logout"));
        assertTrue(PublicEndpoints.isMember("/swagger-ui.html"));
        assertTrue(PublicEndpoints.isMember("/"));
    }

    @Test
    void isMember_wildcardMatches() {
        assertTrue(PublicEndpoints.isMember("/v3/api-docs"));
        assertTrue(PublicEndpoints.isMember("/v3/api-docs/some/path"));
        assertTrue(PublicEndpoints.isMember("/api-docs/some/legacy"));
        assertTrue(PublicEndpoints.isMember("/swagger-ui/index.html"));
        assertTrue(PublicEndpoints.isMember("/swagger-resources/configuration/ui"));
    }

    @Test
    void isMember_nonMembersReturnFalse() {
        assertFalse(PublicEndpoints.isMember("/api/v1/users"));
        assertFalse(PublicEndpoints.isMember("/api/v1/auth/logins")); // similar but not exact
        assertFalse(PublicEndpoints.isMember("/private/resource"));
        assertFalse(PublicEndpoints.isMember("/random/swagger-uix"));
    }
}
