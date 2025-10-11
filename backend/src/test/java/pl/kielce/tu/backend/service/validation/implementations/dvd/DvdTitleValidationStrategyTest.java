package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

class DvdTitleValidationStrategyTest {

    private UserContextLogger userContextLogger;
    private DvdTitleValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        userContextLogger = mock(UserContextLogger.class);
        strategy = new DvdTitleValidationStrategy(userContextLogger);
    }

    @Test
    void shouldValidateSuccessfully() {
        String title = "Valid Title";
        assertDoesNotThrow(() -> strategy.validate(title));
        verify(userContextLogger).logValidationOperation("DVD_TITLE", "STARTED", "11 chars");
        verify(userContextLogger).logValidationOperation("DVD_TITLE", "SUCCESS", "11 chars");
    }

    @Test
    void shouldThrowWhenTitleIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Title is required", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_TITLE", "STARTED", "null");
        verify(userContextLogger, never()).logValidationOperation("DVD_TITLE", "SUCCESS", "null");
    }

    @Test
    void shouldThrowWhenTitleIsBlank() {
        String title = "   ";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(title));
        assertEquals("Title cannot be blank", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_TITLE", "STARTED", "3 chars");
        verify(userContextLogger, never()).logValidationOperation("DVD_TITLE", "SUCCESS", "3 chars");
    }

    @Test
    void shouldThrowWhenTitleTooShort() {
        String title = "abcd";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(title));
        assertEquals("Title must be at least 5 characters long", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_TITLE", "STARTED", "4 chars");
        verify(userContextLogger, never()).logValidationOperation("DVD_TITLE", "SUCCESS", "4 chars");
    }

    @Test
    void shouldThrowWhenTitleTooLong() {
        StringBuilder sb = new StringBuilder(76);
        for (int i = 0; i < 76; i++)
            sb.append('a');
        String longTitle = sb.toString();
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(longTitle));
        assertEquals("Title cannot exceed 75 characters", ex.getMessage());
        verify(userContextLogger).logValidationOperation("DVD_TITLE", "STARTED", "76 chars");
        verify(userContextLogger, never()).logValidationOperation("DVD_TITLE", "SUCCESS", "76 chars");
    }

    @Test
    void shouldReturnCorrectStrategyType() {
        assertEquals(ValidationStrategyType.DVD_TITLE, strategy.getStrategyType());
    }
}
