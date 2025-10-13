package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.ReservationStatus;

class ReservationFilterMapperTest {

    private final ReservationFilterMapper mapper = new ReservationFilterMapper();

    @Test
    void mapFilterToStatus_returnsNullWhenFilterIsNull() {
        assertNull(mapper.mapFilterToStatus(null));
    }

    @Test
    void mapFilterToStatus_parsesValidStatusIgnoringCase() {
        ReservationStatus anyStatus = ReservationStatus.values()[0];
        String lower = anyStatus.name().toLowerCase();
        String mixed = Character.toLowerCase(anyStatus.name().charAt(0)) + anyStatus.name().substring(1).toLowerCase();

        assertEquals(anyStatus, mapper.mapFilterToStatus(lower));
        assertEquals(anyStatus, mapper.mapFilterToStatus(mixed));
        assertEquals(anyStatus, mapper.mapFilterToStatus(anyStatus.name()));
    }

    @Test
    void mapFilterToStatus_returnsNullForUnknownStatus() {
        assertNull(mapper.mapFilterToStatus("non_existing_status"));
    }

    @Test
    void parseReservationId_parsesValidLong() {
        Long result = mapper.parseReservationId("12345");
        assertEquals(12345L, result);
    }

    @Test
    void parseReservationId_throwsForNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> mapper.parseReservationId(null));
        assertThrows(IllegalArgumentException.class, () -> mapper.parseReservationId(""));
        assertThrows(IllegalArgumentException.class, () -> mapper.parseReservationId("   "));
    }

    @Test
    void parseReservationId_throwsForNonNumeric() {
        assertThrows(IllegalArgumentException.class, () -> mapper.parseReservationId("12a34"));
        assertThrows(IllegalArgumentException.class, () -> mapper.parseReservationId("abc"));
    }
}
