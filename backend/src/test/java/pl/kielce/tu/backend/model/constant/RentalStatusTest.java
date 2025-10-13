package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class RentalStatusTest {

    @Test
    void shouldHaveThreeValuesInOrder() {
        RentalStatus[] values = RentalStatus.values();
        assertEquals(3, values.length, "There should be exactly three RentalStatus values");
        assertEquals(RentalStatus.ACTIVE, values[0], "First value should be ACTIVE");
        assertEquals(RentalStatus.RETURN_REQUESTED, values[1], "Second value should be RETURN_REQUESTED");
        assertEquals(RentalStatus.INACTIVE, values[2], "Third value should be INACTIVE");
    }

    @Test
    void getValueReturnsConfiguredString() {
        assertEquals("ACTIVE", RentalStatus.ACTIVE.getValue(), "ACTIVE.getValue() should return \"ACTIVE\"");
        assertEquals("RETURN_REQUESTED", RentalStatus.RETURN_REQUESTED.getValue(),
                "RETURN_REQUESTED.getValue() should return \"RETURN_REQUESTED\"");
        assertEquals("INACTIVE", RentalStatus.INACTIVE.getValue(), "INACTIVE.getValue() should return \"INACTIVE\"");
    }

    @Test
    void valueOfReturnsEnumByName() {
        assertSame(RentalStatus.ACTIVE, RentalStatus.valueOf("ACTIVE"));
        assertSame(RentalStatus.RETURN_REQUESTED, RentalStatus.valueOf("RETURN_REQUESTED"));
        assertSame(RentalStatus.INACTIVE, RentalStatus.valueOf("INACTIVE"));
    }
}
