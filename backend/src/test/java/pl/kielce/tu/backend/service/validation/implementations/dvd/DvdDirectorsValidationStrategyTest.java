package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdDirectorsValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    private DvdDirectorsValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DvdDirectorsValidationStrategy(userContextLogger);
    }

    @Test
    void getStrategyType_shouldReturnDvdDirectors() {
        assertEquals(ValidationStrategyType.DVD_DIRECTORS, strategy.getStrategyType());
    }

    @Test
    void validate_validDirectors_shouldSucceedAndLog() throws ValidationException {
        var directors = Arrays.asList("Christopher Nolan");
        strategy.validate(directors);

        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "1 directors");
        inOrder.verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "SUCCESS", "1 directors");
    }

    @Test
    void validate_nullList_shouldThrowValidationExceptionAndLogStartedWithNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Directors list is required", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "null");
    }

    @Test
    void validate_emptyList_shouldThrowValidationExceptionAndLogStartedWithZero() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> strategy.validate(Collections.emptyList()));
        assertEquals("At least one director is required", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "0 directors");
    }

    @Test
    void validate_elementNull_shouldThrowValidationExceptionAndLogStarted() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> strategy.validate(Arrays.asList((String) null)));
        assertEquals("Director name cannot be null", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "1 directors");
    }

    @Test
    void validate_elementBlank_shouldThrowValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(Arrays.asList("   ")));
        assertEquals("Director name cannot be blank", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "1 directors");
    }

    @Test
    void validate_elementTooShort_shouldThrowValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> strategy.validate(Arrays.asList("TooShort")));
        assertEquals("Director name must be at least 10 characters long", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "1 directors");
    }

    @Test
    void validate_elementTooLong_shouldThrowValidationException() {
        String longName = "A".repeat(51);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> strategy.validate(Arrays.asList(longName)));
        assertEquals("Director name cannot exceed 50 characters", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_DIRECTORS", "STARTED", "1 directors");
    }
}
