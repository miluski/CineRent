package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CalculationConstantsTest {

    @Test
    void shouldReturnCorrectLateFeeMultiplier() {
        BigDecimal expected = BigDecimal.TEN;
        BigDecimal actual = CalculationConstants.LATE_FEE_MULTIPLIER.getValue();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnCorrectRentalPeriodDays() {
        BigDecimal expected = new BigDecimal("7");
        BigDecimal actual = CalculationConstants.RENTAL_PERIOD_DAYS.getValue();

        assertEquals(expected, actual);
    }

    @Test
    void shouldHaveConsistentEnumValues() {
        assertEquals(2, CalculationConstants.values().length);
    }
}
