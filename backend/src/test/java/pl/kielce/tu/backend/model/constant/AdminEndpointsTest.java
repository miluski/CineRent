package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AdminEndpointsTest {

    @Test
    void getAllPatterns_shouldReturnAllDefinedPatterns() {
        String[] patterns = AdminEndpoints.getAllPatterns();
        assertNotNull(patterns, "getAllPatterns should not return null");
        assertEquals(10, patterns.length, "Expected 10 patterns for defined endpoints");
        assertEquals("/api/v1/dvd/create", patterns[0]);
        assertEquals("/api/v1/dvd/*/edit", patterns[1]);
        assertEquals("/api/v1/genres/create", patterns[2]);
        assertEquals("/api/v1/genres/*/delete", patterns[3]);
        assertEquals("/api/v1/reservations/*/accept", patterns[4]);
        assertEquals("/api/v1/reservations/*/decline", patterns[5]);
        assertEquals("/api/v1/reservations/all", patterns[6]);
        assertEquals("/api/v1/rentals/*/return-accept", patterns[7]);
        assertEquals("/api/v1/rentals/*/return-decline", patterns[8]);
        assertEquals("/api/v1/rentals/return-requests", patterns[9]);
    }

    @Test
    void isMember_shouldReturnTrue_forDvdCreateEndpoint() {
        assertTrue(AdminEndpoints.isMember("/api/v1/dvd/create"),
                "DVD create endpoint should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forDvdEditEndpoints() {
        assertTrue(AdminEndpoints.isMember("/api/v1/dvd/123/edit"),
                "DVD edit with numeric ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/dvd/456/edit"),
                "DVD edit with different ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/dvd/abc123/edit"),
                "DVD edit with alphanumeric ID should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forGenreCreateEndpoint() {
        assertTrue(AdminEndpoints.isMember("/api/v1/genres/create"),
                "Genre create endpoint should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forGenreDeleteEndpoints() {
        assertTrue(AdminEndpoints.isMember("/api/v1/genres/1/delete"),
                "Genre delete with numeric ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/genres/456/delete"),
                "Genre delete with different ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/genres/abc123/delete"),
                "Genre delete with alphanumeric ID should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forReservationAcceptEndpoints() {
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/1/accept"),
                "Reservation accept with numeric ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/456/accept"),
                "Reservation accept with different ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/abc123/accept"),
                "Reservation accept with alphanumeric ID should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forReservationDeclineEndpoints() {
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/1/decline"),
                "Reservation decline with numeric ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/456/decline"),
                "Reservation decline with different ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/abc123/decline"),
                "Reservation decline with alphanumeric ID should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forRentalReturnAcceptEndpoints() {
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/1/return-accept"),
                "Rental return accept with numeric ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/456/return-accept"),
                "Rental return accept with different ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/abc123/return-accept"),
                "Rental return accept with alphanumeric ID should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forRentalReturnDeclineEndpoints() {
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/1/return-decline"),
                "Rental return decline with numeric ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/456/return-decline"),
                "Rental return decline with different ID should be recognized as admin endpoint");
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/abc123/return-decline"),
                "Rental return decline with alphanumeric ID should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forAdminReturnRequestsEndpoint() {
        assertTrue(AdminEndpoints.isMember("/api/v1/rentals/return-requests"),
                "Admin return requests endpoint should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnTrue_forAdminAllReservationsEndpoint() {
        assertTrue(AdminEndpoints.isMember("/api/v1/reservations/all"),
                "Admin all reservations endpoint should be recognized as admin endpoint");
    }

    @Test
    void isMember_shouldReturnFalse_forNonAdminEndpoints() {
        assertFalse(AdminEndpoints.isMember("/api/v1/dvd"),
                "DVD list endpoint should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/dvd/123"),
                "DVD get by ID should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/user/edit"),
                "User edit endpoint should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/auth/login"),
                "Auth login should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/genres"),
                "Genre list endpoint should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/genres/123"),
                "Genre get by ID should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/reservations"),
                "Reservation list endpoint should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/reservations/123"),
                "Reservation get by ID should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/rentals"),
                "Rental list endpoint should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/api/v1/rentals/123"),
                "Rental get by ID should not be admin endpoint");
    }

    @Test
    void isMember_shouldReturnFalse_forInvalidPaths() {
        assertFalse(AdminEndpoints.isMember("/api/v1/dvd/create/extra"),
                "DVD create with extra path should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/dvd/edit"),
                "DVD edit without ID should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/dvd/123/update"),
                "DVD update (not edit) should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/genres/create/extra"),
                "Genre create with extra path should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/genres/delete"),
                "Genre delete without ID should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/genres/123/remove"),
                "Genre remove (not delete) should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/reservations/accept"),
                "Reservation accept without ID should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/reservations/decline"),
                "Reservation decline without ID should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/reservations/123/approve"),
                "Reservation approve (not accept) should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/reservations/123/reject"),
                "Reservation reject (not decline) should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/rentals/return-accept"),
                "Rental return accept without ID should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/rentals/return-decline"),
                "Rental return decline without ID should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/rentals/123/accept-return"),
                "Rental accept-return (different format) should not match");
        assertFalse(AdminEndpoints.isMember("/api/v1/rentals/123/decline-return"),
                "Rental decline-return (different format) should not match");
    }

    @Test
    void isMember_shouldHandleEdgeCases() {
        assertFalse(AdminEndpoints.isMember(""),
                "Empty string should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember(null),
                "Null should not be admin endpoint");
        assertFalse(AdminEndpoints.isMember("/"),
                "Root path should not be admin endpoint");
    }

    @Test
    void enumValues_shouldHaveCorrectPatterns() {
        assertEquals("/api/v1/dvd/create", AdminEndpoints.DVD_CREATE.getPattern());
        assertEquals("/api/v1/dvd/*/edit", AdminEndpoints.DVD_PATCH.getPattern());
        assertEquals("/api/v1/genres/create", AdminEndpoints.GENRE_CREATE.getPattern());
        assertEquals("/api/v1/genres/*/delete", AdminEndpoints.GENRE_DELETE.getPattern());
        assertEquals("/api/v1/reservations/*/accept", AdminEndpoints.RESERVATIONS_ACCEPT.getPattern());
        assertEquals("/api/v1/reservations/*/decline", AdminEndpoints.RESERVATIONS_DECLINE.getPattern());
        assertEquals("/api/v1/rentals/*/return-accept", AdminEndpoints.RETURN_ACCEPT.getPattern());
        assertEquals("/api/v1/rentals/*/return-decline", AdminEndpoints.RETURN_DECLINE.getPattern());
    }
}
