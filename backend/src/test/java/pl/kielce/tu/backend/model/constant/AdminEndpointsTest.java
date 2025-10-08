package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AdminEndpointsTest {

    @Test
    void getAllPatterns_shouldReturnEmptyArray_whenNoEnumsDefined() {
        String[] patterns = AdminEndpoints.getAllPatterns();
        assertNotNull(patterns, "getAllPatterns should not return null");
        assertEquals(0, patterns.length, "Expected no patterns when enum has no constants");
    }

    @Test
    void isMember_shouldAlwaysReturnFalse_whenNoPatternsDefined() {
        assertFalse(AdminEndpoints.isMember("/admin"), "isMember should be false for '/admin'");
        assertFalse(AdminEndpoints.isMember("/some/path"), "isMember should be false for '/some/path'");
        assertFalse(AdminEndpoints.isMember(""), "isMember should be false for empty string");
        assertFalse(AdminEndpoints.isMember(null), "isMember should be false for null");
    }
}
