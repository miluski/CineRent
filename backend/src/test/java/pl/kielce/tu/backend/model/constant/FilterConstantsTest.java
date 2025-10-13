package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class FilterConstantsTest {

    @Test
    void shouldHaveHistoricalConstant() {
        FilterConstants constant = FilterConstants.HISTORICAL;
        assertNotNull(constant);
        assertEquals("HISTORICAL", constant.getValue());
    }

    @Test
    void valueOfShouldReturnEnum() {
        assertEquals(FilterConstants.HISTORICAL, FilterConstants.valueOf("HISTORICAL"));
    }

    @Test
    void valuesShouldContainHistorical() {
        FilterConstants[] values = FilterConstants.values();
        assertTrue(Arrays.stream(values).anyMatch(v -> v == FilterConstants.HISTORICAL));
    }

    @Test
    void toStringShouldContainValue() {
        String s = FilterConstants.HISTORICAL.toString();
        assertTrue(s.contains("HISTORICAL"));
    }
}
