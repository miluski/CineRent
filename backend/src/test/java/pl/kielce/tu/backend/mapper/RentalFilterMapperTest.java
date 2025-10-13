package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.FilterConstants;
import pl.kielce.tu.backend.model.constant.RentalStatus;

class RentalFilterMapperTest {

    private RentalFilterMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RentalFilterMapper();
    }

    @Test
    void mapFilterToStatus_nullFilter_returnsNull() {
        assertNull(mapper.mapFilterToStatus(null));
    }

    @Test
    void mapFilterToStatus_historicalFilter_returnsInactive() {
        String historicalValue = FilterConstants.HISTORICAL.getValue();
        assertEquals(RentalStatus.INACTIVE, mapper.mapFilterToStatus(historicalValue));
        assertEquals(RentalStatus.INACTIVE, mapper.mapFilterToStatus(historicalValue.toUpperCase()));
        assertEquals(RentalStatus.INACTIVE, mapper.mapFilterToStatus(historicalValue.toLowerCase()));
    }

    @Test
    void mapFilterToStatus_validEnumName_returnsCorrespondingStatus() {
        assertEquals(RentalStatus.INACTIVE, mapper.mapFilterToStatus("INACTIVE"));
        assertEquals(RentalStatus.INACTIVE, mapper.mapFilterToStatus("inactive"));
    }

    @Test
    void mapFilterToStatus_invalidEnumName_returnsNull() {
        assertNull(mapper.mapFilterToStatus("unknown_status"));
    }

    @Test
    void parseRentalId_validNumericString_returnsLong() {
        Long expected = 123L;
        assertEquals(expected, mapper.parseRentalId("123"));
    }

    @Test
    void parseRentalId_null_throwsIllegalArgumentExceptionWithMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.parseRentalId(null));
        assertEquals("Rental ID cannot be null or empty", ex.getMessage());
    }

    @Test
    void parseRentalId_empty_throwsIllegalArgumentExceptionWithMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.parseRentalId("   "));
        assertEquals("Rental ID cannot be null or empty", ex.getMessage());
    }

    @Test
    void parseRentalId_nonNumeric_throwsIllegalArgumentExceptionWithMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mapper.parseRentalId("abc123"));
        assertEquals("Invalid rental ID format", ex.getMessage());
    }
}
