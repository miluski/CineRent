package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Year;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdYearValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private DvdYearValidationStrategy strategy;

    @Test
    void shouldValidateValidYearAndLogOperations() throws ValidationException {
        int validYear = 2000;

        strategy.validate(validYear);

        verify(userContextLogger, times(1)).logValidationOperation("DVD_YEAR", "STARTED", Integer.toString(validYear));
        verify(userContextLogger, times(1)).logValidationOperation("DVD_YEAR", "SUCCESS", Integer.toString(validYear));
        assertEquals(ValidationStrategyType.DVD_YEAR, strategy.getStrategyType());
    }

    @Test
    void shouldThrowWhenYearIsNullAndOnlyLogStarted() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Release year is required", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_YEAR", "STARTED", "null");
        verify(userContextLogger, never()).logValidationOperation("DVD_YEAR", "SUCCESS", "null");
    }

    @Test
    void shouldThrowWhenYearIsLessThan1000AndOnlyLogStarted() {
        int badYear = 999;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(badYear));
        assertEquals("Release year must be greater than 1000", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_YEAR", "STARTED", Integer.toString(badYear));
        verify(userContextLogger, never()).logValidationOperation("DVD_YEAR", "SUCCESS", Integer.toString(badYear));
    }

    @Test
    void shouldThrowWhenYearIsInFutureAndOnlyLogStarted() {
        int futureYear = Year.now().plusYears(1).getValue();
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(futureYear));
        assertEquals("Release year cannot be in the future", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_YEAR", "STARTED", Integer.toString(futureYear));
        verify(userContextLogger, never()).logValidationOperation("DVD_YEAR", "SUCCESS", Integer.toString(futureYear));
    }
}
