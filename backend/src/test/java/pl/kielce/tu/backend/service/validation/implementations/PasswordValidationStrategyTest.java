package pl.kielce.tu.backend.service.validation.implementations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;

class PasswordValidationStrategyTest {

    private final PasswordValidationStrategy strategy = new PasswordValidationStrategy();

    @Test
    void getStrategyType_shouldReturnPassword() {
        assertEquals(ValidationStrategyType.PASSWORD, strategy.getStrategyType());
    }

    @Test
    void validate_nullPassword_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> strategy.validate(null));
    }

    @Test
    void validate_emptyOrWhitespacePassword_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> strategy.validate(""));
        assertThrows(ValidationException.class, () -> strategy.validate("   "));
    }

    @Test
    void validate_tooShortPassword_shouldThrowValidationException() {
        int min = ValidationConstraints.MIN_PASSWORD_LENGTH.getValue();
        String tooShort = buildString(Math.max(0, min - 1));
        assertThrows(ValidationException.class, () -> strategy.validate(tooShort));
    }

    @Test
    void validate_tooLongPassword_shouldThrowValidationException() {
        int max = ValidationConstraints.MAX_PASSWORD_LENGTH.getValue();
        String tooLong = buildString(max + 1);
        assertThrows(ValidationException.class, () -> strategy.validate(tooLong));
    }

    @Test
    void validate_minLengthPassword_shouldNotThrow() {
        int min = ValidationConstraints.MIN_PASSWORD_LENGTH.getValue();
        String minPwd = buildString(min);
        assertDoesNotThrow(() -> strategy.validate(minPwd));
    }

    @Test
    void validate_maxLengthPassword_shouldNotThrow() {
        int max = ValidationConstraints.MAX_PASSWORD_LENGTH.getValue();
        String maxPwd = buildString(max);
        assertDoesNotThrow(() -> strategy.validate(maxPwd));
    }

    private static String buildString(int length) {
        if (length <= 0) {
            return "";
        }
        char[] chars = new char[length];
        Arrays.fill(chars, 'a');
        return new String(chars);
    }
}
