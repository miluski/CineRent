package pl.kielce.tu.backend.service.validation.implementations.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

class AgeValidationStrategyTest {

    private AgeValidationStrategy strategy;
    private UserContextLogger userContextLogger;

    @BeforeEach
    void setUp() {
        userContextLogger = mock(UserContextLogger.class);
        strategy = new AgeValidationStrategy(userContextLogger);
    }

    @Test
    void validate_nullAge_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Age cannot be null", ex.getMessage());
    }

    @Test
    void validate_ageBelowMin_throwsValidationExceptionWithRangeMessage() {
        int min = ValidationConstraints.MIN_AGE.getValue();
        int max = ValidationConstraints.MAX_AGE.getValue();
        int belowMin = min - 1;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(belowMin));
        assertEquals(String.format("Age must be between %d and %d", min, max), ex.getMessage());
    }

    @Test
    void validate_ageAboveMax_throwsValidationExceptionWithRangeMessage() {
        int min = ValidationConstraints.MIN_AGE.getValue();
        int max = ValidationConstraints.MAX_AGE.getValue();
        int aboveMax = max + 1;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(aboveMax));
        assertEquals(String.format("Age must be between %d and %d", min, max), ex.getMessage());
    }

    @Test
    void validate_ageAtMin_doesNotThrow() {
        int min = ValidationConstraints.MIN_AGE.getValue();
        assertDoesNotThrow(() -> strategy.validate(min));
    }

    @Test
    void validate_ageAtMax_doesNotThrow() {
        int max = ValidationConstraints.MAX_AGE.getValue();
        assertDoesNotThrow(() -> strategy.validate(max));
    }

    @Test
    void validate_ageWithinRange_doesNotThrow() {
        int min = ValidationConstraints.MIN_AGE.getValue();
        int max = ValidationConstraints.MAX_AGE.getValue();
        int mid = min + (max - min) / 2;
        assertDoesNotThrow(() -> strategy.validate(mid));
    }

    @Test
    void getStrategyType_returnsAge() {
        assertEquals(ValidationStrategyType.AGE, strategy.getStrategyType());
    }
}
