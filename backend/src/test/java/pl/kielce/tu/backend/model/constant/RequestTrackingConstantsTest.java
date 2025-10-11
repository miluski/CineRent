package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RequestTrackingConstantsTest {

    @Test
    void enumHasExpectedNumberOfConstants() {
        assertEquals(3, RequestTrackingConstants.values().length);
    }

    @Test
    void allConstantsHaveNonNullValues() {
        for (RequestTrackingConstants c : RequestTrackingConstants.values()) {
            assertNotNull(c.getValue(), "Value for " + c.name() + " should not be null");
        }
    }

    @Test
    void startTimeAttrHasExpectedStringValue() {
        Object v = RequestTrackingConstants.START_TIME_ATTR.getValue();
        assertTrue(v instanceof String, "START_TIME_ATTR value should be a String");
        assertEquals("startTime", v);
    }

    @Test
    void startedStatusHasExpectedStringValue() {
        Object v = RequestTrackingConstants.STARTED_STATUS.getValue();
        assertTrue(v instanceof String, "STARTED_STATUS value should be a String");
        assertEquals("STARTED", v);
    }

    @Test
    void slowRequestThresholdHasExpectedLongValue() {
        Object v = RequestTrackingConstants.SLOW_REQUEST_THRESHOLD_MS.getValue();
        assertTrue(v instanceof Long, "SLOW_REQUEST_THRESHOLD_MS value should be a Long");
        assertEquals(1000L, ((Long) v).longValue());
    }
}
