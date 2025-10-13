package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ReservationStatusTest {

    @Test
    void values_shouldContainThreeConstants() {
        ReservationStatus[] values = ReservationStatus.values();
        assertEquals(4, values.length, "There should be exactly three reservation statuses");
    }

    @Test
    void getValue_shouldReturnConfiguredString_forEachConstant() {
        assertEquals("PENDING", ReservationStatus.PENDING.getValue());
        assertEquals("CANCELLED", ReservationStatus.CANCELLED.getValue());
        assertEquals("ACCEPTED", ReservationStatus.ACCEPTED.getValue());
        assertEquals("REJECTED", ReservationStatus.REJECTED.getValue());
    }

    @Test
    void enumNames_shouldMatchExpected() {
        assertEquals("PENDING", ReservationStatus.PENDING.name());
        assertEquals("CANCELLED", ReservationStatus.CANCELLED.getValue());
        assertEquals("ACCEPTED", ReservationStatus.ACCEPTED.name());
        assertEquals("REJECTED", ReservationStatus.REJECTED.name());
    }

    @Test
    void values_shouldBeUnique() {
        String v1 = ReservationStatus.PENDING.getValue();
        String v2 = ReservationStatus.ACCEPTED.getValue();
        String v3 = ReservationStatus.REJECTED.getValue();
        String v4 = ReservationStatus.CANCELLED.getValue();

        assertNotEquals(v1, v2);
        assertNotEquals(v1, v3);
        assertNotEquals(v1, v4);
        assertNotEquals(v2, v3);
        assertNotEquals(v2, v4);
        assertNotEquals(v3, v4);
    }
}
