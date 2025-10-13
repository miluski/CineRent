package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ValidationStrategyTypeTest {

    @Test
    void values_shouldContainAllConstantsInDeclaredOrder() {
        ValidationStrategyType[] expected = {
                ValidationStrategyType.NICKNAME,
                ValidationStrategyType.PASSWORD,
                ValidationStrategyType.AGE,
                ValidationStrategyType.GENRE,
                ValidationStrategyType.GENRE_NAME,
                ValidationStrategyType.GENRE_DELETION,
                ValidationStrategyType.DVD_TITLE,
                ValidationStrategyType.DVD_DIRECTORS,
                ValidationStrategyType.DVD_DESCRIPTION,
                ValidationStrategyType.DVD_YEAR,
                ValidationStrategyType.DVD_DURATION,
                ValidationStrategyType.DVD_COPIES,
                ValidationStrategyType.DVD_PRICE,
                ValidationStrategyType.RESERVATION_DVD_AVAILABILITY,
                ValidationStrategyType.RESERVATION_COPY_COUNT,
                ValidationStrategyType.RESERVATION_CANCELLATION
        };
        ValidationStrategyType[] actual = ValidationStrategyType.values();
        assertArrayEquals(expected, actual, "Enum values should match declared order and contents");
        assertEquals(16, actual.length, "There should be exactly 16 enum constants");
    }

    @Test
    void valueOf_shouldReturnCorrectEnumForEachName() {
        for (ValidationStrategyType type : ValidationStrategyType.values()) {
            ValidationStrategyType fromName = ValidationStrategyType.valueOf(type.name());
            assertSame(type, fromName, "valueOf should return the same enum constant for name: " + type.name());
        }
    }

    @Test
    void names_shouldMatchExpectedStrings() {
        String[] expectedNames = {
                "NICKNAME", "PASSWORD", "AGE", "GENRE", "GENRE_NAME", "GENRE_DELETION",
                "DVD_TITLE", "DVD_DIRECTORS", "DVD_DESCRIPTION",
                "DVD_YEAR", "DVD_DURATION", "DVD_COPIES", "DVD_PRICE",
                "RESERVATION_DVD_AVAILABILITY", "RESERVATION_COPY_COUNT", "RESERVATION_CANCELLATION"
        };
        String[] actualNames = new String[ValidationStrategyType.values().length];
        for (int i = 0; i < ValidationStrategyType.values().length; i++) {
            actualNames[i] = ValidationStrategyType.values()[i].name();
        }
        assertArrayEquals(expectedNames, actualNames, "Enum names should match the expected identifiers");
    }
}
