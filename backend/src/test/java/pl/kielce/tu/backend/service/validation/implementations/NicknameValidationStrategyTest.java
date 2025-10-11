package pl.kielce.tu.backend.service.validation.implementations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;

class NicknameValidationStrategyTest {

    private final NicknameValidationStrategy strategy = new NicknameValidationStrategy();

    @Test
    void getStrategyType_returnsNickname() {
        assertEquals(ValidationStrategyType.NICKNAME, strategy.getStrategyType());
    }

    @Test
    void validate_validNickname_doesNotThrow() {
        String valid = "user_123-name";
        assertDoesNotThrow(() -> strategy.validate(valid));
    }

    @Test
    void validate_nullNickname_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Nickname cannot be empty", ex.getMessage());
    }

    @Test
    void validate_blankNickname_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate("   "));
        assertEquals("Nickname cannot be empty", ex.getMessage());
    }

    @Test
    void validate_tooShortNickname_throwsValidationException() {
        int min = ValidationConstraints.MIN_NICKNAME_LENGTH.getValue();
        String tooShort = repeat('a', Math.max(0, min - 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(tooShort));
        assertEquals(String.format("Nickname must be at least %d characters long", min), ex.getMessage());
    }

    @Test
    void validate_tooLongNickname_throwsValidationException() {
        int max = ValidationConstraints.MAX_NICKNAME_LENGTH.getValue();
        String tooLong = repeat('b', max + 1);
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(tooLong));
        assertEquals(String.format("Nickname cannot be longer than %d characters", max), ex.getMessage());
    }

    @Test
    void validate_invalidPattern_throwsValidationException() {
        String invalid = "invalid name!";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(invalid));
        assertEquals("Nickname can only contain letters, numbers, underscores, and hyphens", ex.getMessage());
    }

    private static String repeat(char c, int times) {
        if (times <= 0)
            return "";
        StringBuilder sb = new StringBuilder(times);
        for (int i = 0; i < times; i++)
            sb.append(c);
        return sb.toString();
    }
}
